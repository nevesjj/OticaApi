package api.otica.service;

import api.otica.domain.*;
import api.otica.dto.EstoqueDto;
import api.otica.dto.PageResponse;
import api.otica.exception.NaoEncontradoException;
import api.otica.exception.NegocioException;
import api.otica.repository.FornecedorRepository;
import api.otica.repository.LancamentoRepository;
import api.otica.repository.MovimentacaoEstoqueRepository;
import api.otica.repository.ProdutoRepository;
import api.otica.security.UsuarioLogado;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class EstoqueService {

    private final ProdutoRepository produtos;
    private final MovimentacaoEstoqueRepository movimentacoes;
    private final LancamentoRepository lancamentos;
    private final FornecedorRepository fornecedores;

    public EstoqueService(ProdutoRepository produtos, MovimentacaoEstoqueRepository movimentacoes,
                          LancamentoRepository lancamentos, FornecedorRepository fornecedores) {
        this.produtos = produtos;
        this.movimentacoes = movimentacoes;
        this.lancamentos = lancamentos;
        this.fornecedores = fornecedores;
    }

    public PageResponse<EstoqueDto.MovimentacaoResponse> historico(Long produtoId, Pageable pageable) {
        var page = produtoId != null ? movimentacoes.findByProdutoId(produtoId, pageable) : movimentacoes.findAll(pageable);
        return PageResponse.of(page, EstoqueDto.MovimentacaoResponse::of);
    }

    public EstoqueDto.Resumo resumo() {
        Specification<Produto> ativos = (root, query, cb) -> cb.isTrue(root.get("ativo"));
        return new EstoqueDto.Resumo(produtos.count(ativos), produtos.estoqueBaixo().size(),
                produtos.valorEstoqueCusto(), produtos.valorEstoqueVenda());
    }

    /** Movimentação manual feita na tela de estoque. */
    @Transactional
    public EstoqueDto.MovimentacaoResponse registrar(EstoqueDto.MovimentacaoRequest req) {
        Produto produto = produtos.findByIdParaAtualizar(req.produtoId())
                .filter(Produto::isAtivo)
                .orElseThrow(() -> new NaoEncontradoException("Produto", req.produtoId()));
        if (!produto.controlaEstoque()) {
            throw new NegocioException("Serviços não possuem controle de estoque");
        }
        if (req.tipo() != TipoMovimentacao.AJUSTE && req.quantidade() == 0) {
            throw new NegocioException("A quantidade deve ser maior que zero");
        }
        String motivo = Textos.limpar(req.motivo());
        MovimentacaoEstoque mov;
        if (req.tipo() == TipoMovimentacao.AJUSTE) {
            mov = ajustar(produto, req.quantidade(), motivo != null ? motivo : "Ajuste de inventário");
        } else {
            mov = movimentar(produto, req.tipo(), req.quantidade(),
                    motivo != null ? motivo : (req.tipo() == TipoMovimentacao.ENTRADA ? "Entrada de mercadoria" : "Saída manual"),
                    null);
        }
        if (req.tipo() == TipoMovimentacao.ENTRADA && req.valorCompra() != null && req.valorCompra().signum() > 0) {
            gerarContaPagarCompra(produto, req);
        }
        return EstoqueDto.MovimentacaoResponse.of(mov);
    }

    /** Entrada/saída usada pelas vendas e pela tela de estoque. Quantidade sempre positiva. */
    @Transactional
    public MovimentacaoEstoque movimentar(Produto produto, TipoMovimentacao tipo, int quantidade, String motivo, Long vendaId) {
        int anterior = produto.getQuantidade();
        int atual = tipo == TipoMovimentacao.ENTRADA ? anterior + quantidade : anterior - quantidade;
        if (atual < 0) {
            throw new NegocioException("Estoque insuficiente para " + produto.getNome() + " (" + produto.getCodigo()
                    + "): disponível " + anterior + ", solicitado " + quantidade);
        }
        return salvar(produto, tipo, quantidade, anterior, atual, motivo, vendaId);
    }

    private MovimentacaoEstoque ajustar(Produto produto, int novoSaldo, String motivo) {
        int anterior = produto.getQuantidade();
        return salvar(produto, TipoMovimentacao.AJUSTE, novoSaldo - anterior, anterior, novoSaldo, motivo, null);
    }

    private MovimentacaoEstoque salvar(Produto produto, TipoMovimentacao tipo, int quantidade, int anterior, int atual,
                                       String motivo, Long vendaId) {
        produto.setQuantidade(atual);
        MovimentacaoEstoque mov = new MovimentacaoEstoque();
        mov.setProduto(produto);
        mov.setTipo(tipo);
        mov.setQuantidade(quantidade);
        mov.setSaldoAnterior(anterior);
        mov.setSaldoAtual(atual);
        mov.setMotivo(motivo);
        mov.setVendaId(vendaId);
        mov.setUsuario(UsuarioLogado.nome());
        return movimentacoes.save(mov);
    }

    private void gerarContaPagarCompra(Produto produto, EstoqueDto.MovimentacaoRequest req) {
        Lancamento l = new Lancamento();
        l.setTipo(TipoLancamento.DESPESA);
        l.setCategoria("Compra de mercadoria");
        l.setDescricao("Compra: " + req.quantidade() + "x " + produto.getNome() + " (" + produto.getCodigo() + ")");
        l.setValor(req.valorCompra());
        l.setDataVencimento(req.vencimentoCompra() != null ? req.vencimentoCompra() : LocalDate.now());
        Long fornecedorId = req.fornecedorId() != null ? req.fornecedorId()
                : produto.getFornecedor() != null ? produto.getFornecedor().getId() : null;
        if (fornecedorId != null) {
            l.setFornecedor(fornecedores.findById(fornecedorId)
                    .orElseThrow(() -> new NaoEncontradoException("Fornecedor", fornecedorId)));
        }
        lancamentos.save(l);
    }
}
