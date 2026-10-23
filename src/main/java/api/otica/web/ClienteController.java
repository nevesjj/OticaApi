package api.otica.web;

import api.otica.dto.ClienteDto;
import api.otica.dto.PageResponse;
import api.otica.dto.ReceitaDto;
import api.otica.dto.VendaDto;
import api.otica.service.ClienteService;
import api.otica.service.ReceitaService;
import api.otica.service.VendaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService service;
    private final ReceitaService receitaService;
    private final VendaService vendaService;

    public ClienteController(ClienteService service, ReceitaService receitaService, VendaService vendaService) {
        this.service = service;
        this.receitaService = receitaService;
        this.vendaService = vendaService;
    }

    @GetMapping
    public PageResponse<ClienteDto.Response> buscar(@RequestParam(required = false) String q,
                                                    @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return service.buscar(q, pageable);
    }

    @GetMapping("/{id}")
    public ClienteDto.Response obter(@PathVariable Long id) {
        return service.obter(id);
    }

    @GetMapping("/{id}/receitas")
    public List<ReceitaDto.Response> receitas(@PathVariable Long id) {
        return receitaService.doCliente(id);
    }

    @GetMapping("/{id}/vendas")
    public List<VendaDto.Resumo> vendas(@PathVariable Long id) {
        return vendaService.doCliente(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteDto.Response criar(@RequestBody @Valid ClienteDto.Request req) {
        return service.criar(req);
    }

    @PutMapping("/{id}")
    public ClienteDto.Response atualizar(@PathVariable Long id, @RequestBody @Valid ClienteDto.Request req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
