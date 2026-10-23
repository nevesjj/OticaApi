package api.otica.dto;

import api.otica.domain.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class VendaDto {

    private VendaDto() {
    }

    /** {@code precoUnitario} opcional: se ausente, usa o preço de venda do produto. */
    public record ItemRequest(
            @NotNull(message = "Informe o produto") Long produtoId,
            @NotNull @Min(value = 1, message = "Quantidade mínima é 1") Integer quantidade,
            @PositiveOrZero BigDecimal precoUnitario) {
    }

    /** Com {@code confirmar = true} a venda é criada já confirmada (baixa estoque e gera financeiro). */
    public record Request(
            Long clienteId,
            Long receitaId,
            @NotEmpty(message = "Adicione pelo menos um item") List<@Valid ItemRequest> itens,
            @PositiveOrZero(message = "Desconto não pode ser negativo") BigDecimal desconto,
            FormaPagamento formaPagamento,
            @Min(value = 1, message = "Mínimo 1 parcela") @Max(value = 24, message = "Máximo 24 parcelas") Integer parcelas,
            @PositiveOrZero BigDecimal valorEntrada,
            LocalDate primeiroVencimento,
            LocalDate previsaoEntrega,
            @Size(max = 1000) String observacoes,
            Boolean confirmar) {
    }

    public record StatusRequest(@NotNull(message = "Informe o status") StatusVenda status) {
    }

    public record ItemResponse(Long id, Long produtoId, String produtoCodigo, String produtoNome,
                               CategoriaProduto categoria, int quantidade, BigDecimal precoUnitario, BigDecimal total) {
        public static ItemResponse of(ItemVenda i) {
            Produto p = i.getProduto();
            return new ItemResponse(i.getId(), p.getId(), p.getCodigo(), p.getNome(), p.getCategoria(),
                    i.getQuantidade(), i.getPrecoUnitario(), i.getTotal());
        }
    }

    public record Resumo(Long id, Long clienteId, String clienteNome, String clienteTelefone, StatusVenda status,
                         BigDecimal total, FormaPagamento formaPagamento, int parcelas, LocalDate previsaoEntrega,
                         String vendedor, LocalDateTime criadoEm, LocalDateTime dataConfirmacao) {
        public static Resumo of(Venda v) {
            Cliente c = v.getCliente();
            return new Resumo(v.getId(), c != null ? c.getId() : null, c != null ? c.getNome() : null,
                    c != null ? c.getTelefone() : null, v.getStatus(), v.getTotal(), v.getFormaPagamento(),
                    v.getParcelas(), v.getPrevisaoEntrega(), v.getVendedor(), v.getCriadoEm(), v.getDataConfirmacao());
        }
    }

    public record Response(Long id, Long clienteId, String clienteNome, String clienteTelefone, Long receitaId,
                           StatusVenda status, List<ItemResponse> itens, BigDecimal subtotal, BigDecimal desconto,
                           BigDecimal total, FormaPagamento formaPagamento, int parcelas, BigDecimal valorEntrada,
                           LocalDate primeiroVencimento, LocalDate previsaoEntrega, LocalDateTime dataConfirmacao,
                           LocalDateTime dataEntrega, String vendedor, String observacoes, LocalDateTime criadoEm,
                           ReceitaDto.Response receita, List<LancamentoDto.Response> lancamentos) {
        public static Response of(Venda v, List<LancamentoDto.Response> lancamentos) {
            Cliente c = v.getCliente();
            return new Response(v.getId(), c != null ? c.getId() : null, c != null ? c.getNome() : null,
                    c != null ? c.getTelefone() : null, v.getReceita() != null ? v.getReceita().getId() : null,
                    v.getStatus(), v.getItens().stream().map(ItemResponse::of).toList(), v.getSubtotal(),
                    v.getDesconto(), v.getTotal(), v.getFormaPagamento(), v.getParcelas(), v.getValorEntrada(),
                    v.getPrimeiroVencimento(), v.getPrevisaoEntrega(), v.getDataConfirmacao(), v.getDataEntrega(),
                    v.getVendedor(), v.getObservacoes(), v.getCriadoEm(),
                    v.getReceita() != null ? ReceitaDto.Response.of(v.getReceita()) : null, lancamentos);
        }
    }
}
