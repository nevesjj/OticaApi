package api.otica.service;

import api.otica.domain.Usuario;
import api.otica.dto.AuthDto;
import api.otica.dto.UsuarioDto;
import api.otica.exception.NegocioException;
import api.otica.repository.UsuarioRepository;
import api.otica.security.TokenService;
import api.otica.security.UsuarioLogado;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public AuthDto.TokenResponse login(AuthDto.LoginRequest req) {
        Usuario usuario = usuarios.findByLogin(req.login().trim())
                .filter(Usuario::isAtivo)
                .filter(u -> passwordEncoder.matches(req.senha(), u.getSenhaHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login ou senha inválidos"));
        return new AuthDto.TokenResponse(tokenService.gerar(usuario), UsuarioDto.Response.of(usuario));
    }

    @Transactional(readOnly = true)
    public UsuarioDto.Response me() {
        return UsuarioDto.Response.of(usuarioAtual());
    }

    @Transactional
    public void alterarSenha(AuthDto.AlterarSenhaRequest req) {
        Usuario usuario = usuarioAtual();
        if (!passwordEncoder.matches(req.senhaAtual(), usuario.getSenhaHash())) {
            throw new NegocioException("Senha atual incorreta");
        }
        usuario.setSenhaHash(passwordEncoder.encode(req.novaSenha()));
    }

    private Usuario usuarioAtual() {
        return usuarios.findByLogin(UsuarioLogado.login())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessão inválida"));
    }
}
