package mantenimiento.gestorTareas.servicio;

import java.time.LocalDateTime;
import java.util.List;
import mantenimiento.gestorTareas.dominio.Produccion;
import mantenimiento.gestorTareas.dominio.TenantContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProduccionService extends JpaRepository<Produccion,Long> {
    default List<Produccion> findAllByTenant() {
        Long tenantId = TenantContext.getTenantId();
        return findByTenantId(tenantId);
    }
    List<Produccion> findByTenantId(Long tenantId);
     @Query("SELECT p FROM Produccion p WHERE p.estado='abierta' and p.tenant.id=:tenantId")
    List<Produccion> traerAbiertas(@Param("tenantId") Long tenantId);
     @Query("SELECT p FROM Produccion p WHERE p.estado='cerrada' and p.tenant.id=:tenantId")
    List<Produccion> traerCerradas(@Param("tenantId") Long tenantId);


}
