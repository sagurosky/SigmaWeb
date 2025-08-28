package mantenimiento.gestorTareas.servicio;

import java.util.List;
import mantenimiento.gestorTareas.dominio.Asignacion;
import mantenimiento.gestorTareas.dominio.Tarea;
import mantenimiento.gestorTareas.dominio.Tecnico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AsignacionService extends JpaRepository<Asignacion,Long> {

    List<Asignacion> findByTenantId(Long tenantId);
    
    @Query("SELECT a FROM Asignacion a  WHERE "
        + "a.tarea =?1 and a.tenant.id = :tenantId")
    public List<Asignacion> traerPorTarea(Tarea tarea, @Param("tenantId") Long tenantId );
   
    @Query("SELECT a FROM Asignacion a  WHERE "
        + "a.tecnico =?1 and a.tenant.id = :tenantId")
    public List<Asignacion> traerPorTecnico(Tecnico tecnico,@Param("tenantId") Long tenantId );

    
    
    
    
}
