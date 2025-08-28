package mantenimiento.gestorTareas.servicio;

import java.util.List;
import mantenimiento.gestorTareas.dominio.Evaluacion;
import mantenimiento.gestorTareas.dominio.Tarea;
import mantenimiento.gestorTareas.dominio.Tecnico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EvaluacionService extends JpaRepository<Evaluacion,Long> {
    List<Evaluacion> findByTenantId(Long tenantId);
    
}
