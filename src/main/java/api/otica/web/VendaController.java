package api.otica.web;

import api.otica.domain.StatusVenda;
import api.otica.dto.PageResponse;
import api.otica.dto.VendaDto;
import api.otica.service.VendaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/vendas")
public class VendaController {

    private final VendaService service;

    public VendaController(VendaService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<VendaDto.Resumo> buscar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) StatusVenda status,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @PageableDefault(size = 20, sort = "criadoEm", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.buscar(q, status, clienteId, inicio, fim, pageable);
    }

    @GetMapping("/{id}")
    public VendaDto.Response obter(@PathVariable Long id) {
        return service.obter(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VendaDto.Response criar(@RequestBody @Valid VendaDto.Request req) {
        return service.criar(req);
    }

    @PutMapping("/{id}")
    public VendaDto.Response atualizar(@PathVariable Long id, @RequestBody @Valid VendaDto.Request req) {
        return service.atualizar(id, req);
    }

    @PostMapping("/{id}/confirmar")
    public VendaDto.Response confirmar(@PathVariable Long id) {
        return service.confirmar(id);
    }

    @PatchMapping("/{id}/status")
    public VendaDto.Response alterarStatus(@PathVariable Long id, @RequestBody @Valid VendaDto.StatusRequest req) {
        return service.alterarStatus(id, req.status());
    }

    @PostMapping("/{id}/cancelar")
    public VendaDto.Response cancelar(@PathVariable Long id) {
        return service.cancelar(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
