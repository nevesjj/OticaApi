package api.otica.dto;

import api.otica.domain.Cliente;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class ClienteDto {

    private ClienteDto() {
    }

    public record Request(
            @NotBlank(message = "Informe o nome") @Size(max = 150) String nome,
            @Size(max = 14) String cpf,
            @Size(max = 20) String telefone,
            @Email(message = "E-mail inválido") @Size(max = 120) String email,
            @Past(message = "Data de nascimento deve estar no passado") LocalDate dataNascimento,
            @Size(max = 9) String cep,
            @Size(max = 200) String endereco,
            @Size(max = 80) String cidade,
            @Size(max = 2) String uf,
            @Size(max = 1000) String observacoes) {
    }

    public record Response(Long id, String nome, String cpf, String telefone, String email, LocalDate dataNascimento,
                           String cep, String endereco, String cidade, String uf, String observacoes,
                           LocalDateTime criadoEm) {
        public static Response of(Cliente c) {
            return new Response(c.getId(), c.getNome(), c.getCpf(), c.getTelefone(), c.getEmail(), c.getDataNascimento(),
                    c.getCep(), c.getEndereco(), c.getCidade(), c.getUf(), c.getObservacoes(), c.getCriadoEm());
        }
    }

    public record Aniversariante(Long id, String nome, String telefone, LocalDate dataNascimento) {
        public static Aniversariante of(Cliente c) {
            return new Aniversariante(c.getId(), c.getNome(), c.getTelefone(), c.getDataNascimento());
        }
    }
}
