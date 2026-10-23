package api.otica.web;

import api.otica.dto.AuthDto;
import api.otica.dto.UsuarioDto;
import api.otica.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public AuthDto.TokenResponse login(@RequestBody @Valid AuthDto.LoginRequest req) {
        return service.login(req);
    }

    @GetMapping("/me")
    public UsuarioDto.Response me() {
        return service.me();
    }

    @PutMapping("/senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void alterarSenha(@RequestBody @Valid AuthDto.AlterarSenhaRequest req) {
        service.alterarSenha(req);
    }
}
