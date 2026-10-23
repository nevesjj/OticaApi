package api.otica.service;

import api.otica.domain.Perfil;
import api.otica.domain.Usuario;
import api.otica.dto.UsuarioDto;
import api.otica.exception.NaoEncontradoException;
import api.otica.exception.NegocioException;
import api.otica.repository.UsuarioRepository;
import api.otica.security.UsuarioLogado;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UsuarioDto.Response> listar() {
        return usuarios.findAll(Sort.by("nome")).stream().map(UsuarioDto.Response::of).toList();
    }

    @Transactional
    public UsuarioDto.Response criar(UsuarioDto.Request req) {
        String login = req.login().trim().toLowerCase();
        if (usuarios.existsByLogin(login)) {
            throw new NegocioException("Já existe um usuário com o login " + login);
        }
        if (req.senha() == null || req.senha().isBlank()) {
            throw new NegocioException("Informe a senha do novo usuário");
        }
        Usuario u = new Usuario();
        u.setLogin(login);
        u.setNome(req.nome().trim());
        u.setPerfil(req.perfil());
        u.setSenhaHash(passwordEncoder.encode(req.senha()));
        u.setAtivo(req.ativo() == null || req.ativo());
        return UsuarioDto.Response.of(usuarios.save(u));
    }

    @Transactional
    public UsuarioDto.Response atualizar(Long id, UsuarioDto.Request req) {
        Usuario u = usuarios.findById(id).orElseThrow(() -> new NaoEncontradoException("Usuário", id));
        String login = req.login().trim().toLowerCase();
        usuarios.findByLogin(login).filter(outro -> !outro.getId().equals(id)).ifPresent(outro -> {
            throw new NegocioException("Já existe um usuário com o login " + login);
        });
        boolean ativo = req.ativo() == null || req.ativo();
        boolean ehVoceMesmo = u.getLogin().equals(UsuarioLogado.login());
        if (ehVoceMesmo && (!ativo || req.perfil() != Perfil.ADMIN)) {
            throw new NegocioException("Você não pode desativar nem rebaixar o seu próprio usuário");
        }
        u.setLogin(login);
        u.setNome(req.nome().trim());
        u.setPerfil(req.perfil());
        u.setAtivo(ativo);
        if (req.senha() != null && !req.senha().isBlank()) {
            u.setSenhaHash(passwordEncoder.encode(req.senha()));
        }
        return UsuarioDto.Response.of(u);
    }
}
