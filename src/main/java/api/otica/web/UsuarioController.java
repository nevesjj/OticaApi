package api.otica.web;

import api.otica.dto.UsuarioDto;
import api.otica.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Acesso restrito ao perfil ADMIN (ver SecurityConfig). */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    public List<UsuarioDto.Response> listar() {
        return service.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioDto.Response criar(@RequestBody @Valid UsuarioDto.Request req) {
        return service.criar(req);
    }

    @PutMapping("/{id}")
    public UsuarioDto.Response atualizar(@PathVariable Long id, @RequestBody @Valid UsuarioDto.Request req) {
        return service.atualizar(id, req);
    }
}
