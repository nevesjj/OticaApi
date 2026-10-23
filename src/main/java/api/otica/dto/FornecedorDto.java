package api.otica.dto;

import api.otica.domain.Fornecedor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class FornecedorDto {

    private FornecedorDto() {
    }

    public record Request(
            @NotBlank(message = "Informe o nome") @Size(max = 150) String nome,
            @Size(max = 18) String cnpj,
            @Size(max = 20) String telefone,
            @Email(message = "E-mail inválido") @Size(max = 120) String email,
            @Size(max = 120) String contato,
            @Size(max = 1000) String observacoes) {
    }

    public record Response(Long id, String nome, String cnpj, String telefone, String email, String contato,
                           String observacoes, LocalDateTime criadoEm) {
        public static Response of(Fornecedor f) {
            return new Response(f.getId(), f.getNome(), f.getCnpj(), f.getTelefone(), f.getEmail(), f.getContato(),
                    f.getObservacoes(), f.getCriadoEm());
        }
    }
}
