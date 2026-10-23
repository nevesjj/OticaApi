package api.otica.service;

import api.otica.domain.*;
import api.otica.dto.PageResponse;
import api.otica.dto.VendaDto;
import api.otica.exception.NaoEncontradoException;
import api.otica.exception.NegocioException;
import api.otica.repository.ProdutoRepository;
import api.otica.repository.VendaRepository;
import api.otica.security.UsuarioLogado;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class VendaService {

    private final VendaRepository vendas;
    private final ProdutoRepository produtos;
    private final ClienteService clienteService;
    private final ReceitaService receitaService;
    private final EstoqueService estoqueService;
    private final FinanceiroService financeiroService;

    public VendaService(VendaRepository vendas, ProdutoRepository produtos, ClienteService clienteService,
                        ReceitaService receitaService, EstoqueService estoqueService,
                        FinanceiroService financeiroService) {
        this.vendas = vendas;
        this.produtos = produtos;
        this.clienteService = clienteService;
        this.receitaService = receitaService;
        this.estoqueService = estoqueService;
        this.financeiroService = financeiroService;
    }

    public PageResponse<VendaDto.Resumo> buscar(String q, StatusVenda status, Long clienteId, LocalDate inicio,
                                                LocalDate fim, Pageable pageable) {
        String termo = Textos.termoBusca(q);
        Specification<Venda> spec = (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (status != null) ps.add(cb.equal(root.get("status"), status));
            if (clienteId != null) ps.add(cb.equal(root.get("cliente").get("id"), clienteId));
            if (inicio != null) ps.add(cb.greaterThanOrEqualTo(root.get("criadoEm"), inicio.atStartOfDay()));
            if (fim != null) ps.add(cb.lessThan(root.get("criadoEm"), fim.plusDays(1).atStartOfDay()));
            if (!"%".equals(termo)) {
                var cliente = root.join("cliente", JoinType.LEFT);
                List<Predicate> ou = new ArrayList<>();
                ou.add(cb.like(cb.lower(cliente.get("nome")), termo));
                ou.add(cb.like(cliente.get("cpf"), termo));
                String digitos = q.trim().replace("#", "");
                if (digitos.matches("\\d{1,18}")) ou.add(cb.equal(root.get("id"), Long.valueOf(digitos)));
                ps.add(cb.or(ou.toArray(Predicate[]::new)));
            }
            return cb.and(ps.toArray(Predicate[]::new));
        };
        return PageResponse.of(vendas.findAll(spec, pageable), VendaDto.Resumo::of);
    }

    public VendaDto.Response obter(Long id) {
        return resposta(entidade(id));
    }

    public List<VendaDto.Resumo> doCliente(Long clienteId) {
        return vendas.findByClienteIdOrderByCriadoEmDesc(clienteId).stream().map(VendaDto.Resumo::of).toList();
    }

    private Venda entidade(Long id) {
        return vendas.findById(id).orElseThrow(() -> new NaoEncontradoException("Venda", id));
    }

    private VendaDto.Response resposta(Venda v) {
        return VendaDto.Response.of(v, financeiroService.daVenda(v.getId()));
    }

    @Transactional
    public VendaDto.Response criar(VendaDto.Request req) {
        Venda v = new Venda();
        v.setVendedor(UsuarioLogado.nome());
        preencher(v, req);
        vendas.save(v);
        if (Boolean.TRUE.equals(req.confirmar())) {
            confirmar(v);
        }
        return resposta(v);
    }

    @Transactional
    public VendaDto.Response atualizar(Long id, VendaDto.Request req) {
        Venda v = entidade(id);
        if (v.getStatus() != StatusVenda.ORCAMENTO) {
            throw new NegocioException("Apenas orçamentos podem ser editados. Cancele a venda para refazê-la.");
        }
        v.getItens().clear();
        vendas.flush();
        preencher(v, req);
        if (Boolean.TRUE.equals(req.confirmar())) {
            confirmar(v);
        }
        return resposta(v);
    }

    @Transactional
    public VendaDto.Response confirmar(Long id) {
        Venda v = entidade(id);
        confirmar(v);
        return resposta(v);
    }

    private void confirmar(Venda v) {
        if (v.getStatus() != StatusVenda.ORCAMENTO) {
            throw new NegocioException("Somente orçamentos podem ser confirmados");
        }
        if (v.getFormaPagamento() == null) {
            throw new NegocioException("Informe a forma de pagamento para confirmar a venda");
        }
        if (!v.getFormaPagamento().isAVista() && v.getFormaPagamento() != FormaPagamento.CARTAO_CREDITO
                && v.getCliente() == null) {
            throw new NegocioException("Vendas no crediário/boleto exigem um cliente cadastrado");
        }
        for (ItemVenda item : v.getItens()) {
            Produto produto = produtos.findByIdParaAtualizar(item.getProduto().getId()).orElseThrow();
            if (produto.controlaEstoque()) {
                estoqueService.movimentar(produto, TipoMovimentacao.SAIDA, item.getQuantidade(),
                        "Venda #" + v.getId(), v.getId());
            }
        }
        v.setStatus(StatusVenda.ABERTA);
        v.setDataConfirmacao(LocalDateTime.now());
        financeiroService.gerarParaVenda(v);
    }

    @Transactional
    public VendaDto.Response alterarStatus(Long id, StatusVenda novo) {
        Venda v = entidade(id);
        StatusVenda atual = v.getStatus();
        boolean permitido = switch (novo) {
            case PRONTA -> atual == StatusVenda.ABERTA;
            case ENTREGUE -> atual == StatusVenda.ABERTA || atual == StatusVenda.PRONTA;
            case ABERTA -> atual == StatusVenda.PRONTA;
            default -> false;
        };
        if (!permitido) {
            throw new NegocioException("Não é possível mudar a venda de " + atual + " para " + novo
                    + (novo == StatusVenda.ABERTA && atual == StatusVenda.ORCAMENTO ? " (use confirmar)" : "")
                    + (novo == StatusVenda.CANCELADA ? " (use cancelar)" : ""));
        }
        v.setStatus(novo);
        v.setDataEntrega(novo == StatusVenda.ENTREGUE ? LocalDateTime.now() : null);
        return resposta(v);
    }

    @Transactional
    public VendaDto.Response cancelar(Long id) {
        Venda v = entidade(id);
        StatusVenda atual = v.getStatus();
        if (atual == StatusVenda.CANCELADA) {
            throw new NegocioException("Venda já está cancelada");
        }
        if (atual == StatusVenda.ENTREGUE) {
            throw new NegocioException("Venda já entregue não pode ser cancelada");
        }
        if (atual != StatusVenda.ORCAMENTO) {
            for (ItemVenda item : v.getItens()) {
                Produto produto = produtos.findByIdParaAtualizar(item.getProduto().getId()).orElseThrow();
                if (produto.controlaEstoque()) {
                    estoqueService.movimentar(produto, TipoMovimentacao.ENTRADA, item.getQuantidade(),
                            "Cancelamento da venda #" + v.getId(), v.getId());
                }
            }
            financeiroService.estornarVenda(v);
        }
        v.setStatus(StatusVenda.CANCELADA);
        return resposta(v);
    }

    @Transactional
    public void excluir(Long id) {
        Venda v = entidade(id);
        if (v.getStatus() != StatusVenda.ORCAMENTO) {
            throw new NegocioException("Apenas orçamentos podem ser excluídos; vendas confirmadas devem ser canceladas");
        }
        vendas.delete(v);
    }

    private void preencher(Venda v, VendaDto.Request req) {
        Cliente cliente = req.clienteId() != null ? clienteService.entidade(req.clienteId()) : null;
        Receita receita = null;
        if (req.receitaId() != null) {
            receita = receitaService.entidade(req.receitaId());
            if (cliente == null || !receita.getCliente().getId().equals(cliente.getId())) {
                throw new NegocioException("A receita selecionada não pertence ao cliente da venda");
            }
        }
        v.setCliente(cliente);
        v.setReceita(receita);

        BigDecimal subtotal = BigDecimal.ZERO;
        for (VendaDto.ItemRequest ir : req.itens()) {
            Produto produto = produtos.findById(ir.produtoId()).filter(Produto::isAtivo)
                    .orElseThrow(() -> new NaoEncontradoException("Produto", ir.produtoId()));
            ItemVenda item = new ItemVenda();
            item.setProduto(produto);
            item.setQuantidade(ir.quantidade());
            item.setPrecoUnitario(ir.precoUnitario() != null ? ir.precoUnitario() : produto.getPrecoVenda());
            item.setTotal(item.getPrecoUnitario().multiply(BigDecimal.valueOf(ir.quantidade())));
            v.adicionarItem(item);
            subtotal = subtotal.add(item.getTotal());
        }
        BigDecimal desconto = req.desconto() != null ? req.desconto() : BigDecimal.ZERO;
        if (desconto.compareTo(subtotal) > 0) {
            throw new NegocioException("O desconto não pode ser maior que o subtotal");
        }
        v.setSubtotal(subtotal);
        v.setDesconto(desconto);
        v.setTotal(subtotal.subtract(desconto));

        FormaPagamento forma = req.formaPagamento();
        int parcelas = req.parcelas() != null ? req.parcelas() : 1;
        BigDecimal entrada = req.valorEntrada() != null ? req.valorEntrada() : BigDecimal.ZERO;
        if (forma != null && forma.isAVista()) {
            parcelas = 1;
            entrada = BigDecimal.ZERO;
        }
        if (entrada.compareTo(v.getTotal()) > 0) {
            throw new NegocioException("A entrada não pode ser maior que o total da venda");
        }
        v.setFormaPagamento(forma);
        v.setParcelas(parcelas);
        v.setValorEntrada(entrada);
        v.setPrimeiroVencimento(req.primeiroVencimento());
        v.setPrevisaoEntrega(req.previsaoEntrega());
        v.setObservacoes(Textos.limpar(req.observacoes()));
    }
}
