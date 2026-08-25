package mantenimiento.gestorTareas.modulo.produccion;
import mantenimiento.gestorTareas.infraestructura.multitenant.TenantContext;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductoService extends JpaRepository<Producto,Long> {
    default List<Producto> findAllByTenant() {
        Long tenantId = TenantContext.getTenantId();
        return findByTenantId(tenantId);
    }
    List<Producto> findByTenantId(Long tenantId);
}
