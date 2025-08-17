package mantenimiento.gestorTareas.datos;

import mantenimiento.gestorTareas.dominio.Asignacion;
import mantenimiento.gestorTareas.dominio.AsignacionPreventivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AsignacionPreventivoDao extends JpaRepository<AsignacionPreventivo, Long> {

    List<AsignacionPreventivo> findByPreventivoId(Long preventivoId);

    void deleteByPreventivoId(Long preventivoId); // opcional si querés eliminar directamente
}

