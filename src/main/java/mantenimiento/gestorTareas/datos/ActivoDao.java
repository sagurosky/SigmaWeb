package mantenimiento.gestorTareas.datos;

import java.util.List;
import mantenimiento.gestorTareas.dominio.Activo;
import mantenimiento.gestorTareas.dominio.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ActivoDao extends JpaRepository<Activo,Long> {
    List<Activo> findByTenantId(Long tenantId);

    @Query("SELECT t FROM Activo t WHERE t.nombreCamelCase LIKE %?1% AND t.tenant.id = ?2")
    Activo findByNameAndTenantId(String nombre, Long tenantId);

    @Query("SELECT t FROM Activo t WHERE t.estado LIKE %?1% AND t.tenant.id = ?2")
    List<Activo> findByStatusAndTenantId(String estado, Long tenantId);
}
