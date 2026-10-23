package api.otica.web;

import api.otica.domain.TipoLancamento;
import api.otica.dto.LancamentoDto;
import api.otica.dto.PageResponse;
import api.otica.service.FinanceiroService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/financeiro")
public class FinanceiroController {

    private final FinanceiroService service;

    public FinanceiroController(FinanceiroService service) {
        this.service = service;
    }

    @GetMapping("/lancamentos")
    public PageResponse<LancamentoDto.Response> buscar(
            @RequestParam(required = false) TipoLancamento tipo,
            @RequestParam(required = false) String situacao,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String categoria,
            @PageableDefault(size = 20, sort = "dataVencimento") Pageable pageable) {
        return service.buscar(tipo, situacao, inicio, fim, q, categoria, pageable);
    }

    @GetMapping("/lancamentos/{id}")
    public LancamentoDto.Response obter(@PathVariable Long id) {
        return service.obter(id);
    }

    @PostMapping("/lancamentos")
    @ResponseStatus(HttpStatus.CREATED)
    public List<LancamentoDto.Response> criar(@RequestBody @Valid LancamentoDto.Request req) {
        return service.criar(req);
    }

    @PutMapping("/lancamentos/{id}")
    public LancamentoDto.Response atualizar(@PathVariable Long id, @RequestBody @Valid LancamentoDto.Request req) {
        return service.atualizar(id, req);
    }

    @PostMapping("/lancamentos/{id}/baixar")
    public LancamentoDto.Response baixar(@PathVariable Long id, @RequestBody(required = false) LancamentoDto.BaixaRequest req) {
        return service.baixar(id, req);
    }

    @PostMapping("/lancamentos/{id}/estornar")
    public LancamentoDto.Response estornar(@PathVariable Long id) {
        return service.estornarBaixa(id);
    }

    @PostMapping("/lancamentos/{id}/cancelar")
    public LancamentoDto.Response cancelar(@PathVariable Long id) {
        return service.cancelar(id);
    }

    @DeleteMapping("/lancamentos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }

    @GetMapping("/categorias")
    public List<String> categorias() {
        return service.categorias();
    }

    /** Sem datas, considera o mês corrente. */
    @GetMapping("/resumo")
    public LancamentoDto.Resumo resumo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        YearMonth mes = YearMonth.now();
        return service.resumo(inicio != null ? inicio : mes.atDay(1), fim != null ? fim : mes.atEndOfMonth());
    }
}
