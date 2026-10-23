package api.otica.repository;

import api.otica.domain.Receita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReceitaRepository extends JpaRepository<Receita, Long> {

    List<Receita> findByClienteIdOrderByDataExameDesc(Long clienteId);
}
