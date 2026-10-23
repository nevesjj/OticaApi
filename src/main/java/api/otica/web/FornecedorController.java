package api.otica.web;

import api.otica.dto.FornecedorDto;
import api.otica.dto.PageResponse;
import api.otica.service.FornecedorService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fornecedores")
public class FornecedorController {

    private final FornecedorService service;

    public FornecedorController(FornecedorService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<FornecedorDto.Response> buscar(@RequestParam(required = false) String q,
                                                       @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return service.buscar(q, pageable);
    }

    @GetMapping("/{id}")
    public FornecedorDto.Response obter(@PathVariable Long id) {
        return service.obter(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FornecedorDto.Response criar(@RequestBody @Valid FornecedorDto.Request req) {
        return service.criar(req);
    }

    @PutMapping("/{id}")
    public FornecedorDto.Response atualizar(@PathVariable Long id, @RequestBody @Valid FornecedorDto.Request req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
