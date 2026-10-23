package api.otica;

import api.otica.domain.*;
import api.otica.dto.*;
import api.otica.exception.NegocioException;
import api.otica.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:otica-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "app.jwt.secret=segredo-de-teste-com-mais-de-32-caracteres"})
class OticaApplicationTests {

    @Autowired ClienteService clienteService;
    @Autowired ProdutoService produtoService;
    @Autowired VendaService vendaService;
    @Autowired FinanceiroService financeiroService;
    @Autowired DashboardService dashboardService;
    @Autowired EstoqueService estoqueService;

    private ProdutoDto.Response produto(String codigo, CategoriaProduto categoria, String preco, int qtd) {
        return produtoService.criar(new ProdutoDto.Request(codigo, "Produto " + codigo, categoria, "Marca", null, null,
                null, new BigDecimal("50"), new BigDecimal(preco), qtd, 2, null));
    }

    @Test
    void fluxoCompletoDeVendaParcelada() {
        var cliente = clienteService.criar(new ClienteDto.Request("Maria Silva", "123.456.789-00", "(11) 99999-0000",
                "maria@email.com", LocalDate.of(1990, LocalDate.now().getMonthValue(), 1), null, null, "São Paulo", "sp", null));
        var armacao = produto("ARM-1", CategoriaProduto.ARMACAO, "300.00", 5);
        var servico = produto("SRV-1", CategoriaProduto.SERVICO, "50.00", 0);

        var venda = vendaService.criar(new VendaDto.Request(cliente.id(), null,
                List.of(new VendaDto.ItemRequest(armacao.id(), 2, null), new VendaDto.ItemRequest(servico.id(), 1, null)),
                new BigDecimal("50.00"), FormaPagamento.CREDIARIO, 3, new BigDecimal("100.00"), null, null, null, true));

        assertEquals(StatusVenda.ABERTA, venda.status());
        assertEquals(0, new BigDecimal("600.00").compareTo(venda.total()));
        assertEquals(3, produtoService.obter(armacao.id()).quantidade());
        // entrada paga + 3 parcelas pendentes que somam o restante
        assertEquals(4, venda.lancamentos().size());
        BigDecimal pendente = venda.lancamentos().stream().filter(l -> l.status() == StatusLancamento.PENDENTE)
                .map(LancamentoDto.Response::valor).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, new BigDecimal("500.00").compareTo(pendente));

        var dash = dashboardService.carregar();
        assertEquals(0, new BigDecimal("600.00").compareTo(dash.vendidoMes()));
        assertEquals(1, dash.aniversariantes().size());

        var cancelada = vendaService.cancelar(venda.id());
        assertEquals(StatusVenda.CANCELADA, cancelada.status());
        assertEquals(5, produtoService.obter(armacao.id()).quantidade());
        assertTrue(cancelada.lancamentos().stream().anyMatch(l -> l.tipo() == TipoLancamento.DESPESA
                && l.valor().compareTo(new BigDecimal("100.00")) == 0));
    }

    @Test
    void naoPermiteVenderSemEstoque() {
        var lente = produto("LEN-1", CategoriaProduto.LENTE, "200.00", 1);
        assertThrows(NegocioException.class, () -> vendaService.criar(new VendaDto.Request(null, null,
                List.of(new VendaDto.ItemRequest(lente.id(), 2, null)), null, FormaPagamento.PIX, 1, null, null,
                null, null, true)));
        assertEquals(1, produtoService.obter(lente.id()).quantidade());
    }

    @Test
    void ajusteDeEstoqueEBuscaPorEstoqueBaixo() {
        var oculos = produto("SOL-1", CategoriaProduto.OCULOS_SOL, "400.00", 10);
        estoqueService.registrar(new EstoqueDto.MovimentacaoRequest(oculos.id(), TipoMovimentacao.AJUSTE, 1,
                "Inventário", null, null, null));
        assertTrue(produtoService.obter(oculos.id()).estoqueBaixo());
        var baixos = produtoService.buscar("sol", null, true, Pageable.unpaged());
        assertEquals(1, baixos.totalElements());
    }

    @Test
    void lancamentoParceladoDivideValor() {
        var criados = financeiroService.criar(new LancamentoDto.Request(TipoLancamento.DESPESA, "Equipamento",
                "Manutenção", new BigDecimal("100.00"), LocalDate.now(), null, null, null, null, false, null, 3));
        assertEquals(3, criados.size());
        assertEquals(0, new BigDecimal("33.34").compareTo(criados.get(2).valor()));
        var baixado = financeiroService.baixar(criados.get(0).id(), null);
        assertEquals(StatusLancamento.PAGO, baixado.status());
    }
}
