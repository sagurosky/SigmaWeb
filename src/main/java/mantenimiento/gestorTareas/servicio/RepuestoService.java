package mantenimiento.gestorTareas.servicio;

import java.util.List;

import mantenimiento.gestorTareas.dominio.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface RepuestoService extends JpaRepository<Repuesto,Long> {
    default List<Repuesto> findAllByTenant() {
        Long tenantId = TenantContext.getTenantId();
        return findByTenantId(tenantId);
    }
    List<Repuesto> findByTenantId(Long tenantId);
}
