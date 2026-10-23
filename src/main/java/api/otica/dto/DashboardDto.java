package api.otica.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardDto(
        BigDecimal vendidoMes,
        long quantidadeVendasMes,
        BigDecimal ticketMedio,
        BigDecimal recebidoMes,
        BigDecimal pagoMes,
        BigDecimal saldoMes,
        BigDecimal receberVencido,
        BigDecimal pagarVencido,
        BigDecimal receberProximos7Dias,
        BigDecimal pagarProximos7Dias,
        long totalClientes,
        long orcamentos,
        long ordensAbertas,
        long ordensProntas,
        BigDecimal valorEstoque,
        List<ProdutoDto.Response> estoqueBaixo,
        List<ClienteDto.Aniversariante> aniversariantes,
        List<VendaDto.Resumo> proximasEntregas,
        List<Mes> serieMensal) {

    public record Mes(String mes, BigDecimal vendido, BigDecimal recebido, BigDecimal pago) {
    }
}
