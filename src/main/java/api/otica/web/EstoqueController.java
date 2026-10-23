package api.otica.web;

import api.otica.dto.EstoqueDto;
import api.otica.dto.PageResponse;
import api.otica.service.EstoqueService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/estoque")
public class EstoqueController {

    private final EstoqueService service;

    public EstoqueController(EstoqueService service) {
        this.service = service;
    }

    @GetMapping("/resumo")
    public EstoqueDto.Resumo resumo() {
        return service.resumo();
    }

    @GetMapping("/movimentacoes")
    public PageResponse<EstoqueDto.MovimentacaoResponse> historico(
            @RequestParam(required = false) Long produtoId,
            @PageableDefault(size = 20, sort = "data", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.historico(produtoId, pageable);
    }

    @PostMapping("/movimentacoes")
    @ResponseStatus(HttpStatus.CREATED)
    public EstoqueDto.MovimentacaoResponse registrar(@RequestBody @Valid EstoqueDto.MovimentacaoRequest req) {
        return service.registrar(req);
    }
}
