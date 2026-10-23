package api.otica.repository;

import api.otica.domain.Fornecedor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FornecedorRepository extends JpaRepository<Fornecedor, Long> {

    /** {@code termo} deve vir no formato de LIKE em minúsculas, ex.: "%lentes%". */
    @Query("""
            select f from Fornecedor f
            where f.ativo = true
              and (lower(f.nome) like :termo or f.cnpj like :termo or lower(f.contato) like :termo)
            """)
    Page<Fornecedor> buscar(@Param("termo") String termo, Pageable pageable);

    boolean existsByCnpj(String cnpj);

    boolean existsByCnpjAndIdNot(String cnpj, Long id);
}
