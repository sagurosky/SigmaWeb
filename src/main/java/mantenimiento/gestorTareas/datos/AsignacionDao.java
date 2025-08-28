package mantenimiento.gestorTareas.datos;

import mantenimiento.gestorTareas.dominio.Asignacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AsignacionDao extends JpaRepository<Asignacion,Long>{
    List<Asignacion> findByTenantId(Long tenantId);
    List<Asignacion> findByTareaIdAndTenantId(Long tareaId, Long tenantId);
    List<Asignacion> findByTecnicoIdAndTenantId(Long tecnicoId, Long tenantId);
}
