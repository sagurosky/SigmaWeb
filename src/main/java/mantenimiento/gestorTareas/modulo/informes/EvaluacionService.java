package mantenimiento.gestorTareas.modulo.informes;
import mantenimiento.gestorTareas.infraestructura.multitenant.TenantContext;
import mantenimiento.gestorTareas.modulo.tareas.Tarea;
import mantenimiento.gestorTareas.modulo.tecnicos.Tecnico;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EvaluacionService extends JpaRepository<Evaluacion,Long> {
    default List<Evaluacion> findAllByTenant() {
        Long tenantId = TenantContext.getTenantId();
        return findByTenantId(tenantId);
    }
    List<Evaluacion> findByTenantId(Long tenantId);
    
}
