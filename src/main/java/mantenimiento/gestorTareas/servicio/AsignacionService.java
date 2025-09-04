package mantenimiento.gestorTareas.servicio;

import java.util.List;
import mantenimiento.gestorTareas.dominio.Asignacion;
import mantenimiento.gestorTareas.dominio.Tarea;
import mantenimiento.gestorTareas.dominio.Tecnico;
import mantenimiento.gestorTareas.dominio.TenantContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AsignacionService extends JpaRepository<Asignacion,Long> {
    default List<Asignacion> findAllByTenant() {
        Long tenantId = TenantContext.getTenantId();
        return findByTenantId(tenantId);
    }

    List<Asignacion> findByTenantId(Long tenantId);

    @Query("SELECT a FROM Asignacion a " +
            "WHERE a.tarea = :tarea " +
            "AND a.tenant.id = :tenantId")
    public List<Asignacion> traerPorTarea(@Param("tarea") Tarea tarea,
                                          @Param("tenantId") Long tenantId);

    @Query("SELECT a FROM Asignacion a " +
            "WHERE a.tecnico = :tecnico " +
            "AND a.tenant.id = :tenantId")
    public List<Asignacion> traerPorTecnico(@Param("tecnico") Tecnico tecnico,
                                            @Param("tenantId") Long tenantId);

    
    
    
    
}
