package api.otica.dto;

import api.otica.domain.Perfil;
import api.otica.domain.Usuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class UsuarioDto {

    private UsuarioDto() {
    }

    /** {@code senha} é obrigatória na criação; na edição, se vazia, mantém a atual. */
    public record Request(
            @NotBlank(message = "Informe o nome") @Size(max = 120) String nome,
            @NotBlank(message = "Informe o login") @Size(max = 60) String login,
            @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres") String senha,
            @NotNull(message = "Informe o perfil") Perfil perfil,
            Boolean ativo) {
    }

    public record Response(Long id, String nome, String login, Perfil perfil, boolean ativo, LocalDateTime criadoEm) {
        public static Response of(Usuario u) {
            return new Response(u.getId(), u.getNome(), u.getLogin(), u.getPerfil(), u.isAtivo(), u.getCriadoEm());
        }
    }
}
