package api.otica.service;

import api.otica.domain.CategoriaProduto;
import api.otica.domain.Produto;
import api.otica.domain.TipoMovimentacao;
import api.otica.dto.PageResponse;
import api.otica.dto.ProdutoDto;
import api.otica.exception.NaoEncontradoException;
import api.otica.exception.NegocioException;
import api.otica.repository.MovimentacaoEstoqueRepository;
import api.otica.repository.ProdutoRepository;
import api.otica.repository.VendaRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProdutoService {

    private final ProdutoRepository produtos;
    private final FornecedorService fornecedorService;
    private final EstoqueService estoqueService;
    private final VendaRepository vendas;
    private final MovimentacaoEstoqueRepository movimentacoes;

    public ProdutoService(ProdutoRepository produtos, FornecedorService fornecedorService,
                          EstoqueService estoqueService, VendaRepository vendas,
                          MovimentacaoEstoqueRepository movimentacoes) {
        this.produtos = produtos;
        this.fornecedorService = fornecedorService;
        this.estoqueService = estoqueService;
        this.vendas = vendas;
        this.movimentacoes = movimentacoes;
    }

    public PageResponse<ProdutoDto.Response> buscar(String q, CategoriaProduto categoria, boolean somenteEstoqueBaixo,
                                                    Pageable pageable) {
        String termo = Textos.termoBusca(q);
        Specification<Produto> spec = (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            ps.add(cb.isTrue(root.get("ativo")));
            if (!"%".equals(termo)) {
                ps.add(cb.or(
                        cb.like(cb.lower(root.get("nome")), termo),
                        cb.like(cb.lower(root.get("codigo")), termo),
                        cb.like(cb.lower(root.get("marca")), termo),
                        cb.like(cb.lower(root.get("modelo")), termo)));
            }
            if (categoria != null) {
                ps.add(cb.equal(root.get("categoria"), categoria));
            }
            if (somenteEstoqueBaixo) {
                ps.add(cb.notEqual(root.get("categoria"), CategoriaProduto.SERVICO));
                ps.add(cb.le(root.get("quantidade"), root.get("estoqueMinimo")));
            }
            return cb.and(ps.toArray(Predicate[]::new));
        };
        return PageResponse.of(produtos.findAll(spec, pageable), ProdutoDto.Response::of);
    }

    public ProdutoDto.Response obter(Long id) {
        return ProdutoDto.Response.of(entidade(id));
    }

    Produto entidade(Long id) {
        return produtos.findById(id).filter(Produto::isAtivo).orElseThrow(() -> new NaoEncontradoException("Produto", id));
    }

    @Transactional
    public ProdutoDto.Response criar(ProdutoDto.Request req) {
        String codigo = req.codigo().trim().toUpperCase();
        if (produtos.existsByCodigo(codigo)) {
            throw new NegocioException("Já existe um produto com o código " + codigo);
        }
        Produto p = new Produto();
        preencher(p, req);
        p.setCodigo(codigo);
        produtos.save(p);
        int inicial = req.quantidadeInicial() != null ? req.quantidadeInicial() : 0;
        if (inicial > 0 && p.controlaEstoque()) {
            estoqueService.movimentar(p, TipoMovimentacao.ENTRADA, inicial, "Estoque inicial", null);
        }
        return ProdutoDto.Response.of(p);
    }

    @Transactional
    public ProdutoDto.Response atualizar(Long id, ProdutoDto.Request req) {
        Produto p = entidade(id);
        String codigo = req.codigo().trim().toUpperCase();
        if (produtos.existsByCodigoAndIdNot(codigo, id)) {
            throw new NegocioException("Já existe um produto com o código " + codigo);
        }
        if (p.controlaEstoque() && req.categoria() == CategoriaProduto.SERVICO && p.getQuantidade() != 0) {
            throw new NegocioException("Zere o estoque antes de transformar o produto em serviço");
        }
        preencher(p, req);
        p.setCodigo(codigo);
        return ProdutoDto.Response.of(p);
    }

    @Transactional
    public void excluir(Long id) {
        Produto p = entidade(id);
        if (vendas.produtoPossuiVendas(id) || movimentacoes.existsByProdutoId(id)) {
            p.setAtivo(false);
            p.setCodigo(p.getCodigo() + "#" + p.getId());
        } else {
            produtos.delete(p);
        }
    }

    private void preencher(Produto p, ProdutoDto.Request req) {
        p.setNome(req.nome().trim());
        p.setCategoria(req.categoria());
        p.setMarca(Textos.limpar(req.marca()));
        p.setModelo(Textos.limpar(req.modelo()));
        p.setCor(Textos.limpar(req.cor()));
        p.setDescricao(Textos.limpar(req.descricao()));
        p.setPrecoCusto(req.precoCusto());
        p.setPrecoVenda(req.precoVenda());
        p.setEstoqueMinimo(req.estoqueMinimo() != null ? req.estoqueMinimo() : 0);
        p.setFornecedor(req.fornecedorId() != null ? fornecedorService.entidade(req.fornecedorId()) : null);
    }
}
