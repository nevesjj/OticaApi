package api.otica.service;

import api.otica.domain.Cliente;
import api.otica.dto.ClienteDto;
import api.otica.dto.PageResponse;
import api.otica.exception.NaoEncontradoException;
import api.otica.exception.NegocioException;
import api.otica.repository.ClienteRepository;
import api.otica.repository.LancamentoRepository;
import api.otica.repository.ReceitaRepository;
import api.otica.repository.VendaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ClienteService {

    private final ClienteRepository clientes;
    private final VendaRepository vendas;
    private final LancamentoRepository lancamentos;
    private final ReceitaRepository receitas;

    public ClienteService(ClienteRepository clientes, VendaRepository vendas, LancamentoRepository lancamentos,
                          ReceitaRepository receitas) {
        this.clientes = clientes;
        this.vendas = vendas;
        this.lancamentos = lancamentos;
        this.receitas = receitas;
    }

    public PageResponse<ClienteDto.Response> buscar(String q, Pageable pageable) {
        return PageResponse.of(clientes.buscar(Textos.termoBusca(q), pageable), ClienteDto.Response::of);
    }

    public ClienteDto.Response obter(Long id) {
        return ClienteDto.Response.of(entidade(id));
    }

    Cliente entidade(Long id) {
        return clientes.findById(id).filter(Cliente::isAtivo).orElseThrow(() -> new NaoEncontradoException("Cliente", id));
    }

    @Transactional
    public ClienteDto.Response criar(ClienteDto.Request req) {
        Cliente c = new Cliente();
        preencher(c, req);
        if (c.getCpf() != null && clientes.existsByCpf(c.getCpf())) {
            throw new NegocioException("Já existe um cliente com o CPF " + c.getCpf());
        }
        return ClienteDto.Response.of(clientes.save(c));
    }

    @Transactional
    public ClienteDto.Response atualizar(Long id, ClienteDto.Request req) {
        Cliente c = entidade(id);
        preencher(c, req);
        if (c.getCpf() != null && clientes.existsByCpfAndIdNot(c.getCpf(), id)) {
            throw new NegocioException("Já existe um cliente com o CPF " + c.getCpf());
        }
        return ClienteDto.Response.of(c);
    }

    /** Exclui de vez se não houver histórico; caso contrário apenas inativa (preserva vendas e financeiro). */
    @Transactional
    public void excluir(Long id) {
        Cliente c = entidade(id);
        if (vendas.existsByClienteId(id) || lancamentos.existsByClienteId(id)) {
            c.setAtivo(false);
            c.setCpf(null);
        } else {
            receitas.deleteAll(receitas.findByClienteIdOrderByDataExameDesc(id));
            clientes.delete(c);
        }
    }

    private void preencher(Cliente c, ClienteDto.Request req) {
        c.setNome(req.nome().trim());
        c.setCpf(Textos.limpar(req.cpf()));
        c.setTelefone(Textos.limpar(req.telefone()));
        c.setEmail(Textos.limpar(req.email()));
        c.setDataNascimento(req.dataNascimento());
        c.setCep(Textos.limpar(req.cep()));
        c.setEndereco(Textos.limpar(req.endereco()));
        c.setCidade(Textos.limpar(req.cidade()));
        c.setUf(req.uf() != null ? Textos.limpar(req.uf().toUpperCase()) : null);
        c.setObservacoes(Textos.limpar(req.observacoes()));
    }
}
