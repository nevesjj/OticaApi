package api.otica.repository;

import api.otica.domain.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /** {@code termo} deve vir no formato de LIKE em minúsculas, ex.: "%maria%". */
    @Query("""
            select c from Cliente c
            where c.ativo = true
              and (lower(c.nome) like :termo or c.cpf like :termo or c.telefone like :termo or lower(c.email) like :termo)
            """)
    Page<Cliente> buscar(@Param("termo") String termo, Pageable pageable);

    @Query("""
            select c from Cliente c
            where c.ativo = true and extract(month from c.dataNascimento) = :mes
            order by extract(day from c.dataNascimento)
            """)
    List<Cliente> aniversariantesDoMes(@Param("mes") int mes);

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, Long id);

    long countByAtivoTrue();
}
