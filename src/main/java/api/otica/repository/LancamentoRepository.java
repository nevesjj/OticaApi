package api.otica.repository;

import api.otica.domain.Lancamento;
import api.otica.domain.TipoLancamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long>, JpaSpecificationExecutor<Lancamento> {

    List<Lancamento> findByVendaIdOrderByDataVencimento(Long vendaId);

    @Query("""
            select coalesce(sum(l.valor), 0) from Lancamento l
            where l.tipo = :tipo and l.status = api.otica.domain.StatusLancamento.PAGO
              and l.dataPagamento between :inicio and :fim
            """)
    BigDecimal totalPago(@Param("tipo") TipoLancamento tipo, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("""
            select coalesce(sum(l.valor), 0) from Lancamento l
            where l.tipo = :tipo and l.status = api.otica.domain.StatusLancamento.PENDENTE
              and l.dataVencimento between :inicio and :fim
            """)
    BigDecimal totalPendente(@Param("tipo") TipoLancamento tipo, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("""
            select coalesce(sum(l.valor), 0) from Lancamento l
            where l.tipo = :tipo and l.status = api.otica.domain.StatusLancamento.PENDENTE
              and l.dataVencimento < :hoje
            """)
    BigDecimal totalVencido(@Param("tipo") TipoLancamento tipo, @Param("hoje") LocalDate hoje);

    @Query("""
            select l.categoria, sum(l.valor) from Lancamento l
            where l.tipo = :tipo and l.status = api.otica.domain.StatusLancamento.PAGO
              and l.dataPagamento between :inicio and :fim
            group by l.categoria
            order by sum(l.valor) desc
            """)
    List<Object[]> totalPorCategoria(@Param("tipo") TipoLancamento tipo, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("select distinct l.categoria from Lancamento l order by l.categoria")
    List<String> categorias();

    boolean existsByClienteId(Long clienteId);

    boolean existsByFornecedorId(Long fornecedorId);
}
