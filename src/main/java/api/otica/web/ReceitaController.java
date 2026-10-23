package api.otica.web;

import api.otica.dto.ReceitaDto;
import api.otica.service.ReceitaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/receitas")
public class ReceitaController {

    private final ReceitaService service;

    public ReceitaController(ReceitaService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ReceitaDto.Response obter(@PathVariable Long id) {
        return service.obter(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReceitaDto.Response criar(@RequestBody @Valid ReceitaDto.Request req) {
        return service.criar(req);
    }

    @PutMapping("/{id}")
    public ReceitaDto.Response atualizar(@PathVariable Long id, @RequestBody @Valid ReceitaDto.Request req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
