package api.otica.service;

import api.otica.domain.Fornecedor;
import api.otica.dto.FornecedorDto;
import api.otica.dto.PageResponse;
import api.otica.exception.NaoEncontradoException;
import api.otica.exception.NegocioException;
import api.otica.repository.FornecedorRepository;
import api.otica.repository.LancamentoRepository;
import api.otica.repository.ProdutoRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FornecedorService {

    private final FornecedorRepository fornecedores;
    private final ProdutoRepository produtos;
    private final LancamentoRepository lancamentos;

    public FornecedorService(FornecedorRepository fornecedores, ProdutoRepository produtos,
                             LancamentoRepository lancamentos) {
        this.fornecedores = fornecedores;
        this.produtos = produtos;
        this.lancamentos = lancamentos;
    }

    public PageResponse<FornecedorDto.Response> buscar(String q, Pageable pageable) {
        return PageResponse.of(fornecedores.buscar(Textos.termoBusca(q), pageable), FornecedorDto.Response::of);
    }

    public FornecedorDto.Response obter(Long id) {
        return FornecedorDto.Response.of(entidade(id));
    }

    Fornecedor entidade(Long id) {
        return fornecedores.findById(id).filter(Fornecedor::isAtivo)
                .orElseThrow(() -> new NaoEncontradoException("Fornecedor", id));
    }

    @Transactional
    public FornecedorDto.Response criar(FornecedorDto.Request req) {
        Fornecedor f = new Fornecedor();
        preencher(f, req);
        if (f.getCnpj() != null && fornecedores.existsByCnpj(f.getCnpj())) {
            throw new NegocioException("Já existe um fornecedor com o CNPJ " + f.getCnpj());
        }
        return FornecedorDto.Response.of(fornecedores.save(f));
    }

    @Transactional
    public FornecedorDto.Response atualizar(Long id, FornecedorDto.Request req) {
        Fornecedor f = entidade(id);
        preencher(f, req);
        if (f.getCnpj() != null && fornecedores.existsByCnpjAndIdNot(f.getCnpj(), id)) {
            throw new NegocioException("Já existe um fornecedor com o CNPJ " + f.getCnpj());
        }
        return FornecedorDto.Response.of(f);
    }

    @Transactional
    public void excluir(Long id) {
        Fornecedor f = entidade(id);
        Specification<api.otica.domain.Produto> doFornecedor = (root, query, cb) -> cb.equal(root.get("fornecedor").get("id"), id);
        if (produtos.exists(doFornecedor) || lancamentos.existsByFornecedorId(id)) {
            f.setAtivo(false);
            f.setCnpj(null);
        } else {
            fornecedores.delete(f);
        }
    }

    private void preencher(Fornecedor f, FornecedorDto.Request req) {
        f.setNome(req.nome().trim());
        f.setCnpj(Textos.limpar(req.cnpj()));
        f.setTelefone(Textos.limpar(req.telefone()));
        f.setEmail(Textos.limpar(req.email()));
        f.setContato(Textos.limpar(req.contato()));
        f.setObservacoes(Textos.limpar(req.observacoes()));
    }
}
