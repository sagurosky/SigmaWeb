package mantenimiento.gestorTareas.datos;

import mantenimiento.gestorTareas.dominio.Asignacion;
import mantenimiento.gestorTareas.dominio.TenantContext;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AsignacionDao extends JpaRepository<Asignacion,Long>{
    default List<Asignacion> findAllByTenant() {
        Long tenantId = TenantContext.getTenantId();
        return findByTenantId(tenantId);
    }

    List<Asignacion> findByTenantId(Long tenantId);
    List<Asignacion> findByTareaIdAndTenantId(Long tareaId, Long tenantId);
    List<Asignacion> findByTecnicoIdAndTenantId(Long tecnicoId, Long tenantId);
}
