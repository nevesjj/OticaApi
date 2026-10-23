package api.otica.repository;

import api.otica.domain.Produto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Long>, JpaSpecificationExecutor<Produto> {

    @Query("""
            select p from Produto p
            where p.ativo = true
              and p.categoria <> api.otica.domain.CategoriaProduto.SERVICO
              and p.quantidade <= p.estoqueMinimo
            order by p.quantidade, p.nome
            """)
    List<Produto> estoqueBaixo();

    @Query("""
            select coalesce(sum(p.precoCusto * p.quantidade), 0) from Produto p
            where p.ativo = true and p.categoria <> api.otica.domain.CategoriaProduto.SERVICO
            """)
    BigDecimal valorEstoqueCusto();

    @Query("""
            select coalesce(sum(p.precoVenda * p.quantidade), 0) from Produto p
            where p.ativo = true and p.categoria <> api.otica.domain.CategoriaProduto.SERVICO
            """)
    BigDecimal valorEstoqueVenda();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Produto p where p.id = :id")
    Optional<Produto> findByIdParaAtualizar(@Param("id") Long id);

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, Long id);
}
