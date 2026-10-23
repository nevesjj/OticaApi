package api.otica.dto;

import api.otica.domain.CategoriaProduto;
import api.otica.domain.Produto;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class ProdutoDto {

    private ProdutoDto() {
    }

    /** {@code quantidadeInicial} só é usada na criação; depois o saldo muda apenas por movimentações. */
    public record Request(
            @NotBlank(message = "Informe o código") @Size(max = 40) String codigo,
            @NotBlank(message = "Informe o nome") @Size(max = 150) String nome,
            @NotNull(message = "Informe a categoria") CategoriaProduto categoria,
            @Size(max = 80) String marca,
            @Size(max = 80) String modelo,
            @Size(max = 40) String cor,
            @Size(max = 1000) String descricao,
            @NotNull(message = "Informe o preço de custo") @PositiveOrZero BigDecimal precoCusto,
            @NotNull(message = "Informe o preço de venda") @PositiveOrZero BigDecimal precoVenda,
            @PositiveOrZero(message = "Quantidade não pode ser negativa") Integer quantidadeInicial,
            @PositiveOrZero(message = "Estoque mínimo não pode ser negativo") Integer estoqueMinimo,
            Long fornecedorId) {
    }

    public record Response(Long id, String codigo, String nome, CategoriaProduto categoria, String marca, String modelo,
                           String cor, String descricao, BigDecimal precoCusto, BigDecimal precoVenda,
                           BigDecimal margem, int quantidade, int estoqueMinimo, boolean estoqueBaixo,
                           boolean controlaEstoque, Long fornecedorId, String fornecedorNome) {
        public static Response of(Produto p) {
            BigDecimal margem = null;
            if (p.getPrecoVenda().signum() > 0) {
                margem = p.getPrecoVenda().subtract(p.getPrecoCusto())
                        .multiply(BigDecimal.valueOf(100))
                        .divide(p.getPrecoVenda(), 1, RoundingMode.HALF_UP);
            }
            boolean baixo = p.controlaEstoque() && p.getQuantidade() <= p.getEstoqueMinimo();
            return new Response(p.getId(), p.getCodigo(), p.getNome(), p.getCategoria(), p.getMarca(), p.getModelo(),
                    p.getCor(), p.getDescricao(), p.getPrecoCusto(), p.getPrecoVenda(), margem, p.getQuantidade(),
                    p.getEstoqueMinimo(), baixo, p.controlaEstoque(),
                    p.getFornecedor() != null ? p.getFornecedor().getId() : null,
                    p.getFornecedor() != null ? p.getFornecedor().getNome() : null);
        }
    }
}
