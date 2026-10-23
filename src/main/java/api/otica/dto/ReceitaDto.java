package api.otica.dto;

import api.otica.domain.Receita;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class ReceitaDto {

    private ReceitaDto() {
    }

    public record Request(
            @NotNull(message = "Informe o cliente") Long clienteId,
            @NotNull(message = "Informe a data do exame") @PastOrPresent(message = "Data do exame não pode ser futura") LocalDate dataExame,
            LocalDate validade,
            @Size(max = 120) String medico,
            @Size(max = 20) String crm,
            @DecimalMin("-30") @DecimalMax("30") BigDecimal odEsferico,
            @DecimalMin("-15") @DecimalMax("15") BigDecimal odCilindrico,
            @Min(value = 0, message = "Eixo entre 0 e 180") @Max(value = 180, message = "Eixo entre 0 e 180") Integer odEixo,
            @DecimalMin("0") @DecimalMax("5") BigDecimal odAdicao,
            @DecimalMin("0") @DecimalMax("50") BigDecimal odDnp,
            @DecimalMin("0") @DecimalMax("50") BigDecimal odAltura,
            @DecimalMin("-30") @DecimalMax("30") BigDecimal oeEsferico,
            @DecimalMin("-15") @DecimalMax("15") BigDecimal oeCilindrico,
            @Min(value = 0, message = "Eixo entre 0 e 180") @Max(value = 180, message = "Eixo entre 0 e 180") Integer oeEixo,
            @DecimalMin("0") @DecimalMax("5") BigDecimal oeAdicao,
            @DecimalMin("0") @DecimalMax("50") BigDecimal oeDnp,
            @DecimalMin("0") @DecimalMax("50") BigDecimal oeAltura,
            @Size(max = 1000) String observacoes) {
    }

    public record Response(Long id, Long clienteId, String clienteNome, LocalDate dataExame, LocalDate validade,
                           boolean vencida, String medico, String crm,
                           BigDecimal odEsferico, BigDecimal odCilindrico, Integer odEixo, BigDecimal odAdicao,
                           BigDecimal odDnp, BigDecimal odAltura,
                           BigDecimal oeEsferico, BigDecimal oeCilindrico, Integer oeEixo, BigDecimal oeAdicao,
                           BigDecimal oeDnp, BigDecimal oeAltura,
                           String observacoes, LocalDateTime criadoEm) {
        public static Response of(Receita r) {
            boolean vencida = r.getValidade() != null && r.getValidade().isBefore(LocalDate.now());
            return new Response(r.getId(), r.getCliente().getId(), r.getCliente().getNome(), r.getDataExame(),
                    r.getValidade(), vencida, r.getMedico(), r.getCrm(),
                    r.getOdEsferico(), r.getOdCilindrico(), r.getOdEixo(), r.getOdAdicao(), r.getOdDnp(), r.getOdAltura(),
                    r.getOeEsferico(), r.getOeCilindrico(), r.getOeEixo(), r.getOeAdicao(), r.getOeDnp(), r.getOeAltura(),
                    r.getObservacoes(), r.getCriadoEm());
        }
    }
}
