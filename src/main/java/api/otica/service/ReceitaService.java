package api.otica.service;

import api.otica.domain.Receita;
import api.otica.dto.ReceitaDto;
import api.otica.exception.NaoEncontradoException;
import api.otica.exception.NegocioException;
import api.otica.repository.ReceitaRepository;
import api.otica.repository.VendaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReceitaService {

    private final ReceitaRepository receitas;
    private final ClienteService clienteService;
    private final VendaRepository vendas;

    public ReceitaService(ReceitaRepository receitas, ClienteService clienteService, VendaRepository vendas) {
        this.receitas = receitas;
        this.clienteService = clienteService;
        this.vendas = vendas;
    }

    public List<ReceitaDto.Response> doCliente(Long clienteId) {
        return receitas.findByClienteIdOrderByDataExameDesc(clienteId).stream().map(ReceitaDto.Response::of).toList();
    }

    public ReceitaDto.Response obter(Long id) {
        return ReceitaDto.Response.of(entidade(id));
    }

    Receita entidade(Long id) {
        return receitas.findById(id).orElseThrow(() -> new NaoEncontradoException("Receita", id));
    }

    @Transactional
    public ReceitaDto.Response criar(ReceitaDto.Request req) {
        Receita r = new Receita();
        preencher(r, req);
        return ReceitaDto.Response.of(receitas.save(r));
    }

    @Transactional
    public ReceitaDto.Response atualizar(Long id, ReceitaDto.Request req) {
        Receita r = entidade(id);
        preencher(r, req);
        return ReceitaDto.Response.of(r);
    }

    @Transactional
    public void excluir(Long id) {
        if (vendas.existsByReceitaId(id)) {
            throw new NegocioException("Receita vinculada a uma venda não pode ser excluída");
        }
        receitas.delete(entidade(id));
    }

    private void preencher(Receita r, ReceitaDto.Request req) {
        if (req.validade() != null && req.validade().isBefore(req.dataExame())) {
            throw new NegocioException("A validade não pode ser anterior à data do exame");
        }
        r.setCliente(clienteService.entidade(req.clienteId()));
        r.setDataExame(req.dataExame());
        r.setValidade(req.validade() != null ? req.validade() : req.dataExame().plusYears(1));
        r.setMedico(Textos.limpar(req.medico()));
        r.setCrm(Textos.limpar(req.crm()));
        r.setOdEsferico(req.odEsferico());
        r.setOdCilindrico(req.odCilindrico());
        r.setOdEixo(req.odEixo());
        r.setOdAdicao(req.odAdicao());
        r.setOdDnp(req.odDnp());
        r.setOdAltura(req.odAltura());
        r.setOeEsferico(req.oeEsferico());
        r.setOeCilindrico(req.oeCilindrico());
        r.setOeEixo(req.oeEixo());
        r.setOeAdicao(req.oeAdicao());
        r.setOeDnp(req.oeDnp());
        r.setOeAltura(req.oeAltura());
        r.setObservacoes(Textos.limpar(req.observacoes()));
    }
}
