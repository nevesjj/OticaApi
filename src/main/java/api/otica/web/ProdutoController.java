package api.otica.web;

import api.otica.domain.CategoriaProduto;
import api.otica.dto.PageResponse;
import api.otica.dto.ProdutoDto;
import api.otica.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoService service;

    public ProdutoController(ProdutoService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<ProdutoDto.Response> buscar(@RequestParam(required = false) String q,
                                                    @RequestParam(required = false) CategoriaProduto categoria,
                                                    @RequestParam(defaultValue = "false") boolean estoqueBaixo,
                                                    @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return service.buscar(q, categoria, estoqueBaixo, pageable);
    }

    @GetMapping("/{id}")
    public ProdutoDto.Response obter(@PathVariable Long id) {
        return service.obter(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProdutoDto.Response criar(@RequestBody @Valid ProdutoDto.Request req) {
        return service.criar(req);
    }

    @PutMapping("/{id}")
    public ProdutoDto.Response atualizar(@PathVariable Long id, @RequestBody @Valid ProdutoDto.Request req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
