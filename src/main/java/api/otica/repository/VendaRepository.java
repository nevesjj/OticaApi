package api.otica.repository;

import api.otica.domain.StatusVenda;
import api.otica.domain.Venda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface VendaRepository extends JpaRepository<Venda, Long>, JpaSpecificationExecutor<Venda> {

    @Query("""
            select coalesce(sum(v.total), 0) from Venda v
            where v.status in (api.otica.domain.StatusVenda.ABERTA, api.otica.domain.StatusVenda.PRONTA, api.otica.domain.StatusVenda.ENTREGUE)
              and v.dataConfirmacao >= :inicio and v.dataConfirmacao < :fim
            """)
    BigDecimal totalVendido(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("""
            select count(v) from Venda v
            where v.status in (api.otica.domain.StatusVenda.ABERTA, api.otica.domain.StatusVenda.PRONTA, api.otica.domain.StatusVenda.ENTREGUE)
              and v.dataConfirmacao >= :inicio and v.dataConfirmacao < :fim
            """)
    long quantidadeVendida(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    long countByStatus(StatusVenda status);

    List<Venda> findByClienteIdOrderByCriadoEmDesc(Long clienteId);

    List<Venda> findTop8ByStatusInOrderByPrevisaoEntregaAsc(List<StatusVenda> status);

    boolean existsByClienteId(Long clienteId);

    boolean existsByReceitaId(Long receitaId);

    @Query("select count(i) > 0 from ItemVenda i where i.produto.id = :produtoId")
    boolean produtoPossuiVendas(@Param("produtoId") Long produtoId);
}
