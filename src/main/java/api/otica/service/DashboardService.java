package api.otica.service;

import api.otica.domain.StatusVenda;
import api.otica.domain.TipoLancamento;
import api.otica.dto.ClienteDto;
import api.otica.dto.DashboardDto;
import api.otica.dto.ProdutoDto;
import api.otica.dto.VendaDto;
import api.otica.repository.ClienteRepository;
import api.otica.repository.LancamentoRepository;
import api.otica.repository.ProdutoRepository;
import api.otica.repository.VendaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final VendaRepository vendas;
    private final LancamentoRepository lancamentos;
    private final ProdutoRepository produtos;
    private final ClienteRepository clientes;

    public DashboardService(VendaRepository vendas, LancamentoRepository lancamentos, ProdutoRepository produtos,
                            ClienteRepository clientes) {
        this.vendas = vendas;
        this.lancamentos = lancamentos;
        this.produtos = produtos;
        this.clientes = clientes;
    }

    public DashboardDto carregar() {
        LocalDate hoje = LocalDate.now();
        YearMonth mes = YearMonth.from(hoje);
        LocalDate inicioMes = mes.atDay(1);
        LocalDate fimMes = mes.atEndOfMonth();

        BigDecimal vendido = vendas.totalVendido(inicioMes.atStartOfDay(), fimMes.plusDays(1).atStartOfDay());
        long qtdVendas = vendas.quantidadeVendida(inicioMes.atStartOfDay(), fimMes.plusDays(1).atStartOfDay());
        BigDecimal ticket = qtdVendas > 0 ? vendido.divide(BigDecimal.valueOf(qtdVendas), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal recebido = lancamentos.totalPago(TipoLancamento.RECEITA, inicioMes, fimMes);
        BigDecimal pago = lancamentos.totalPago(TipoLancamento.DESPESA, inicioMes, fimMes);

        List<DashboardDto.Mes> serie = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth m = mes.minusMonths(i);
            LocalDate ini = m.atDay(1);
            LocalDate fim = m.atEndOfMonth();
            serie.add(new DashboardDto.Mes(m.toString(),
                    vendas.totalVendido(ini.atStartOfDay(), fim.plusDays(1).atStartOfDay()),
                    lancamentos.totalPago(TipoLancamento.RECEITA, ini, fim),
                    lancamentos.totalPago(TipoLancamento.DESPESA, ini, fim)));
        }

        return new DashboardDto(
                vendido, qtdVendas, ticket, recebido, pago, recebido.subtract(pago),
                lancamentos.totalVencido(TipoLancamento.RECEITA, hoje),
                lancamentos.totalVencido(TipoLancamento.DESPESA, hoje),
                lancamentos.totalPendente(TipoLancamento.RECEITA, hoje, hoje.plusDays(7)),
                lancamentos.totalPendente(TipoLancamento.DESPESA, hoje, hoje.plusDays(7)),
                clientes.countByAtivoTrue(),
                vendas.countByStatus(StatusVenda.ORCAMENTO),
                vendas.countByStatus(StatusVenda.ABERTA),
                vendas.countByStatus(StatusVenda.PRONTA),
                produtos.valorEstoqueCusto(),
                produtos.estoqueBaixo().stream().limit(10).map(ProdutoDto.Response::of).toList(),
                clientes.aniversariantesDoMes(hoje.getMonthValue()).stream().map(ClienteDto.Aniversariante::of).toList(),
                vendas.findTop8ByStatusInOrderByPrevisaoEntregaAsc(List.of(StatusVenda.ABERTA, StatusVenda.PRONTA))
                        .stream().map(VendaDto.Resumo::of).toList(),
                serie);
    }
}
