package api.otica.dto;

import api.otica.domain.MovimentacaoEstoque;
import api.otica.domain.TipoMovimentacao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class EstoqueDto {

    private EstoqueDto() {
    }

    /**
     * ENTRADA/SAIDA: {@code quantidade} é quanto entra/sai. AJUSTE: {@code quantidade} é o novo saldo (inventário).
     * Em ENTRADA, se {@code valorCompra} for informado, gera uma conta a pagar com vencimento {@code vencimentoCompra}.
     */
    public record MovimentacaoRequest(
            @NotNull(message = "Informe o produto") Long produtoId,
            @NotNull(message = "Informe o tipo") TipoMovimentacao tipo,
            @NotNull(message = "Informe a quantidade") @PositiveOrZero(message = "Quantidade não pode ser negativa") Integer quantidade,
            @Size(max = 255) String motivo,
            @PositiveOrZero BigDecimal valorCompra,
            LocalDate vencimentoCompra,
            Long fornecedorId) {
    }

    public record MovimentacaoResponse(Long id, Long produtoId, String produtoCodigo, String produtoNome,
                                       TipoMovimentacao tipo, int quantidade, int saldoAnterior, int saldoAtual,
                                       String motivo, Long vendaId, String usuario, LocalDateTime data) {
        public static MovimentacaoResponse of(MovimentacaoEstoque m) {
            return new MovimentacaoResponse(m.getId(), m.getProduto().getId(), m.getProduto().getCodigo(),
                    m.getProduto().getNome(), m.getTipo(), m.getQuantidade(), m.getSaldoAnterior(), m.getSaldoAtual(),
                    m.getMotivo(), m.getVendaId(), m.getUsuario(), m.getData());
        }
    }

    public record Resumo(long totalProdutos, long produtosEstoqueBaixo, BigDecimal valorCusto, BigDecimal valorVenda) {
    }
}
