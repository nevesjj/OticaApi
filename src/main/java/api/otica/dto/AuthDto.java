package api.otica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDto {

    private AuthDto() {
    }

    public record LoginRequest(
            @NotBlank(message = "Informe o login") String login,
            @NotBlank(message = "Informe a senha") String senha) {
    }

    public record TokenResponse(String token, UsuarioDto.Response usuario) {
    }

    public record AlterarSenhaRequest(
            @NotBlank(message = "Informe a senha atual") String senhaAtual,
            @NotBlank @Size(min = 6, message = "A nova senha deve ter pelo menos 6 caracteres") String novaSenha) {
    }
}
