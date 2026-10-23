package api.otica.dto;

import api.otica.domain.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class LancamentoDto {

    private LancamentoDto() {
    }

    /**
     * Com {@code parcelas > 1} o valor é dividido em parcelas mensais a partir de {@code dataVencimento}
     * (na edição, {@code parcelas} é ignorado). {@code pago = true} já registra o pagamento.
     */
    public record Request(
            @NotNull(message = "Informe o tipo") TipoLancamento tipo,
            @NotBlank(message = "Informe a descrição") @Size(max = 200) String descricao,
            @NotBlank(message = "Informe a categoria") @Size(max = 60) String categoria,
            @NotNull(message = "Informe o valor") @Positive(message = "Valor deve ser maior que zero") BigDecimal valor,
            @NotNull(message = "Informe o vencimento") LocalDate dataVencimento,
            FormaPagamento formaPagamento,
            Long clienteId,
            Long fornecedorId,
            @Size(max = 1000) String observacoes,
            Boolean pago,
            LocalDate dataPagamento,
            @Min(1) @Max(60) Integer parcelas) {
    }

    public record BaixaRequest(LocalDate dataPagamento, FormaPagamento formaPagamento) {
    }

    public record Response(Long id, TipoLancamento tipo, String descricao, String categoria, BigDecimal valor,
                           LocalDate dataVencimento, LocalDate dataPagamento, StatusLancamento status, boolean vencido,
                           FormaPagamento formaPagamento, String parcela, Long vendaId, Long clienteId,
                           String clienteNome, Long fornecedorId, String fornecedorNome, String observacoes) {
        public static Response of(Lancamento l) {
            return new Response(l.getId(), l.getTipo(), l.getDescricao(), l.getCategoria(), l.getValor(),
                    l.getDataVencimento(), l.getDataPagamento(), l.getStatus(), l.isVencido(), l.getFormaPagamento(),
                    l.getParcela(), l.getVenda() != null ? l.getVenda().getId() : null,
                    l.getCliente() != null ? l.getCliente().getId() : null,
                    l.getCliente() != null ? l.getCliente().getNome() : null,
                    l.getFornecedor() != null ? l.getFornecedor().getId() : null,
                    l.getFornecedor() != null ? l.getFornecedor().getNome() : null,
                    l.getObservacoes());
        }
    }

    public record CategoriaValor(String categoria, BigDecimal valor) {
    }

    public record Resumo(LocalDate inicio, LocalDate fim, BigDecimal recebido, BigDecimal pago, BigDecimal saldo,
                         BigDecimal aReceber, BigDecimal aPagar, BigDecimal receberVencido, BigDecimal pagarVencido,
                         List<CategoriaValor> receitasPorCategoria, List<CategoriaValor> despesasPorCategoria) {
    }
}
