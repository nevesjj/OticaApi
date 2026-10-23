package api.otica.service;

import api.otica.domain.*;
import api.otica.dto.LancamentoDto;
import api.otica.dto.PageResponse;
import api.otica.exception.NaoEncontradoException;
import api.otica.exception.NegocioException;
import api.otica.repository.LancamentoRepository;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

@Service
@Transactional(readOnly = true)
public class FinanceiroService {

    /** Categorias sugeridas no cadastro (somadas às já usadas nos lançamentos). */
    private static final List<String> CATEGORIAS_PADRAO = List.of(
            "Venda", "Exame de vista", "Outras receitas",
            "Compra de mercadoria", "Laboratório", "Aluguel", "Salários", "Energia", "Água", "Internet/Telefone",
            "Impostos", "Marketing", "Manutenção", "Taxas de cartão", "Estorno de venda", "Outras despesas");

    private final LancamentoRepository lancamentos;
    private final ClienteService clienteService;
    private final FornecedorService fornecedorService;

    public FinanceiroService(LancamentoRepository lancamentos, ClienteService clienteService,
                             FornecedorService fornecedorService) {
        this.lancamentos = lancamentos;
        this.clienteService = clienteService;
        this.fornecedorService = fornecedorService;
    }

    /**
     * @param situacao PENDENTE, PAGO, CANCELADO ou VENCIDO (pendente com vencimento passado)
     * @param inicio   filtra por vencimento (ou data de pagamento quando situacao = PAGO)
     */
    public PageResponse<LancamentoDto.Response> buscar(TipoLancamento tipo, String situacao, LocalDate inicio,
                                                       LocalDate fim, String q, String categoria, Pageable pageable) {
        String termo = Textos.termoBusca(q);
        LocalDate hoje = LocalDate.now();
        Specification<Lancamento> spec = (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            String campoData = "PAGO".equalsIgnoreCase(situacao) ? "dataPagamento" : "dataVencimento";
            if (tipo != null) ps.add(cb.equal(root.get("tipo"), tipo));
            if (situacao != null && !situacao.isBlank()) {
                if ("VENCIDO".equalsIgnoreCase(situacao)) {
                    ps.add(cb.equal(root.get("status"), StatusLancamento.PENDENTE));
                    ps.add(cb.lessThan(root.get("dataVencimento"), hoje));
                } else {
                    ps.add(cb.equal(root.get("status"), StatusLancamento.valueOf(situacao.toUpperCase())));
                }
            }
            if (inicio != null) ps.add(cb.greaterThanOrEqualTo(root.get(campoData), inicio));
            if (fim != null) ps.add(cb.lessThanOrEqualTo(root.get(campoData), fim));
            if (categoria != null && !categoria.isBlank()) ps.add(cb.equal(root.get("categoria"), categoria));
            if (!"%".equals(termo)) {
                var cliente = root.join("cliente", JoinType.LEFT);
                var fornecedor = root.join("fornecedor", JoinType.LEFT);
                ps.add(cb.or(
                        cb.like(cb.lower(root.get("descricao")), termo),
                        cb.like(cb.lower(cliente.get("nome")), termo),
                        cb.like(cb.lower(fornecedor.get("nome")), termo)));
            }
            return cb.and(ps.toArray(Predicate[]::new));
        };
        return PageResponse.of(lancamentos.findAll(spec, pageable), LancamentoDto.Response::of);
    }

    public LancamentoDto.Response obter(Long id) {
        return LancamentoDto.Response.of(entidade(id));
    }

    public List<LancamentoDto.Response> daVenda(Long vendaId) {
        return lancamentos.findByVendaIdOrderByDataVencimento(vendaId).stream().map(LancamentoDto.Response::of).toList();
    }

    public List<String> categorias() {
        TreeSet<String> todas = new TreeSet<>(CATEGORIAS_PADRAO);
        todas.addAll(lancamentos.categorias());
        return List.copyOf(todas);
    }

    private Lancamento entidade(Long id) {
        return lancamentos.findById(id).orElseThrow(() -> new NaoEncontradoException("Lançamento", id));
    }

    @Transactional
    public List<LancamentoDto.Response> criar(LancamentoDto.Request req) {
        int n = req.parcelas() != null ? req.parcelas() : 1;
        List<BigDecimal> valores = dividir(req.valor(), n);
        List<LancamentoDto.Response> criados = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Lancamento l = new Lancamento();
            preencher(l, req);
            l.setValor(valores.get(i));
            l.setDataVencimento(req.dataVencimento().plusMonths(i));
            if (n > 1) {
                l.setParcela((i + 1) + "/" + n);
                l.setDescricao(req.descricao().trim() + " (" + (i + 1) + "/" + n + ")");
            }
            // Só a primeira parcela pode nascer paga.
            if (Boolean.TRUE.equals(req.pago()) && i == 0) {
                l.setStatus(StatusLancamento.PAGO);
                l.setDataPagamento(req.dataPagamento() != null ? req.dataPagamento() : LocalDate.now());
            }
            criados.add(LancamentoDto.Response.of(lancamentos.save(l)));
        }
        return criados;
    }

    @Transactional
    public LancamentoDto.Response atualizar(Long id, LancamentoDto.Request req) {
        Lancamento l = entidade(id);
        if (l.getStatus() == StatusLancamento.CANCELADO) {
            throw new NegocioException("Lançamento cancelado não pode ser alterado");
        }
        if (l.getVenda() != null && (l.getValor().compareTo(req.valor()) != 0 || l.getTipo() != req.tipo())) {
            throw new NegocioException("Valor e tipo de lançamentos gerados por venda não podem ser alterados");
        }
        preencher(l, req);
        l.setValor(req.valor());
        l.setDataVencimento(req.dataVencimento());
        if (Boolean.TRUE.equals(req.pago()) && l.getStatus() == StatusLancamento.PENDENTE) {
            l.setStatus(StatusLancamento.PAGO);
            l.setDataPagamento(req.dataPagamento() != null ? req.dataPagamento() : LocalDate.now());
        } else if (l.getStatus() == StatusLancamento.PAGO && req.dataPagamento() != null) {
            l.setDataPagamento(req.dataPagamento());
        }
        return LancamentoDto.Response.of(l);
    }

    @Transactional
    public LancamentoDto.Response baixar(Long id, LancamentoDto.BaixaRequest req) {
        Lancamento l = entidade(id);
        if (l.getStatus() != StatusLancamento.PENDENTE) {
            throw new NegocioException("Somente lançamentos pendentes podem ser baixados");
        }
        l.setStatus(StatusLancamento.PAGO);
        l.setDataPagamento(req != null && req.dataPagamento() != null ? req.dataPagamento() : LocalDate.now());
        if (req != null && req.formaPagamento() != null) {
            l.setFormaPagamento(req.formaPagamento());
        }
        return LancamentoDto.Response.of(l);
    }

    @Transactional
    public LancamentoDto.Response estornarBaixa(Long id) {
        Lancamento l = entidade(id);
        if (l.getStatus() != StatusLancamento.PAGO) {
            throw new NegocioException("Somente lançamentos pagos podem ter a baixa estornada");
        }
        l.setStatus(StatusLancamento.PENDENTE);
        l.setDataPagamento(null);
        return LancamentoDto.Response.of(l);
    }

    @Transactional
    public LancamentoDto.Response cancelar(Long id) {
        Lancamento l = entidade(id);
        if (l.getStatus() != StatusLancamento.PENDENTE) {
            throw new NegocioException("Somente lançamentos pendentes podem ser cancelados");
        }
        l.setStatus(StatusLancamento.CANCELADO);
        return LancamentoDto.Response.of(l);
    }

    @Transactional
    public void excluir(Long id) {
        Lancamento l = entidade(id);
        if (l.getVenda() != null) {
            throw new NegocioException("Lançamentos gerados por venda não podem ser excluídos; cancele a venda");
        }
        lancamentos.delete(l);
    }

    public LancamentoDto.Resumo resumo(LocalDate inicio, LocalDate fim) {
        LocalDate hoje = LocalDate.now();
        BigDecimal recebido = lancamentos.totalPago(TipoLancamento.RECEITA, inicio, fim);
        BigDecimal pago = lancamentos.totalPago(TipoLancamento.DESPESA, inicio, fim);
        return new LancamentoDto.Resumo(inicio, fim, recebido, pago, recebido.subtract(pago),
                lancamentos.totalPendente(TipoLancamento.RECEITA, inicio, fim),
                lancamentos.totalPendente(TipoLancamento.DESPESA, inicio, fim),
                lancamentos.totalVencido(TipoLancamento.RECEITA, hoje),
                lancamentos.totalVencido(TipoLancamento.DESPESA, hoje),
                porCategoria(TipoLancamento.RECEITA, inicio, fim),
                porCategoria(TipoLancamento.DESPESA, inicio, fim));
    }

    private List<LancamentoDto.CategoriaValor> porCategoria(TipoLancamento tipo, LocalDate inicio, LocalDate fim) {
        return lancamentos.totalPorCategoria(tipo, inicio, fim).stream()
                .map(r -> new LancamentoDto.CategoriaValor((String) r[0], (BigDecimal) r[1]))
                .toList();
    }

    // ---------------------------------------------------------------- integração com vendas

    /** Gera as contas a receber de uma venda confirmada. */
    @Transactional
    public void gerarParaVenda(Venda venda) {
        if (venda.getTotal().signum() == 0) {
            return;
        }
        String descricao = "Venda #" + venda.getId() + (venda.getCliente() != null ? " - " + venda.getCliente().getNome() : "");
        FormaPagamento forma = venda.getFormaPagamento();
        LocalDate hoje = LocalDate.now();

        if (forma.isAVista()) {
            salvarDaVenda(venda, descricao, venda.getTotal(), hoje, true, forma, null);
            return;
        }
        BigDecimal entrada = venda.getValorEntrada() != null ? venda.getValorEntrada() : BigDecimal.ZERO;
        if (entrada.signum() > 0) {
            salvarDaVenda(venda, descricao + " (entrada)", entrada, hoje, true, null, "Entrada");
        }
        BigDecimal restante = venda.getTotal().subtract(entrada);
        if (restante.signum() <= 0) {
            return;
        }
        int n = venda.getParcelas();
        LocalDate primeiro = venda.getPrimeiroVencimento() != null ? venda.getPrimeiroVencimento() : hoje.plusMonths(1);
        List<BigDecimal> valores = dividir(restante, n);
        for (int i = 0; i < n; i++) {
            String parcela = (i + 1) + "/" + n;
            salvarDaVenda(venda, descricao + " (" + parcela + ")", valores.get(i), primeiro.plusMonths(i), false, forma, parcela);
        }
    }

    /** Ao cancelar uma venda: cancela o que está pendente e registra o estorno do que já foi recebido. */
    @Transactional
    public void estornarVenda(Venda venda) {
        BigDecimal recebido = BigDecimal.ZERO;
        for (Lancamento l : lancamentos.findByVendaIdOrderByDataVencimento(venda.getId())) {
            if (l.getTipo() != TipoLancamento.RECEITA) continue;
            if (l.getStatus() == StatusLancamento.PENDENTE) {
                l.setStatus(StatusLancamento.CANCELADO);
            } else if (l.getStatus() == StatusLancamento.PAGO) {
                recebido = recebido.add(l.getValor());
            }
        }
        if (recebido.signum() > 0) {
            Lancamento estorno = new Lancamento();
            estorno.setTipo(TipoLancamento.DESPESA);
            estorno.setCategoria("Estorno de venda");
            estorno.setDescricao("Estorno da venda #" + venda.getId());
            estorno.setValor(recebido);
            estorno.setDataVencimento(LocalDate.now());
            estorno.setDataPagamento(LocalDate.now());
            estorno.setStatus(StatusLancamento.PAGO);
            estorno.setVenda(venda);
            estorno.setCliente(venda.getCliente());
            lancamentos.save(estorno);
        }
    }

    private void salvarDaVenda(Venda venda, String descricao, BigDecimal valor, LocalDate vencimento, boolean pago,
                               FormaPagamento forma, String parcela) {
        Lancamento l = new Lancamento();
        l.setTipo(TipoLancamento.RECEITA);
        l.setCategoria("Venda");
        l.setDescricao(descricao);
        l.setValor(valor);
        l.setDataVencimento(vencimento);
        l.setFormaPagamento(forma);
        l.setParcela(parcela);
        l.setVenda(venda);
        l.setCliente(venda.getCliente());
        if (pago) {
            l.setStatus(StatusLancamento.PAGO);
            l.setDataPagamento(LocalDate.now());
        }
        lancamentos.save(l);
    }

    /** Divide o valor em n parcelas; a diferença de arredondamento fica na última. */
    static List<BigDecimal> dividir(BigDecimal valor, int n) {
        BigDecimal parcela = valor.divide(BigDecimal.valueOf(n), 2, RoundingMode.DOWN);
        List<BigDecimal> valores = new ArrayList<>();
        for (int i = 0; i < n - 1; i++) valores.add(parcela);
        valores.add(valor.subtract(parcela.multiply(BigDecimal.valueOf(n - 1))));
        return valores;
    }

    private void preencher(Lancamento l, LancamentoDto.Request req) {
        l.setTipo(req.tipo());
        l.setDescricao(req.descricao().trim());
        l.setCategoria(req.categoria().trim());
        l.setFormaPagamento(req.formaPagamento());
        l.setObservacoes(Textos.limpar(req.observacoes()));
        l.setCliente(req.clienteId() != null ? clienteService.entidade(req.clienteId()) : null);
        l.setFornecedor(req.fornecedorId() != null ? fornecedorService.entidade(req.fornecedorId()) : null);
    }
}
