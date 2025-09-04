package mantenimiento.gestorTareas.datos;

import mantenimiento.gestorTareas.dominio.Asignacion;
import mantenimiento.gestorTareas.dominio.AsignacionPreventivo;
import mantenimiento.gestorTareas.dominio.TenantContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AsignacionPreventivoDao extends JpaRepository<AsignacionPreventivo, Long> {
    default List<AsignacionPreventivo> findAllByTenant() {
        Long tenantId = TenantContext.getTenantId();
        return findByTenantId(tenantId);
    }

    List<AsignacionPreventivo> findByTenantId(Long tenantId);

    List<AsignacionPreventivo> findByPreventivoIdAndTenantId(Long preventivoId, Long tenantId);

    void deleteByPreventivoIdAndTenantId(Long preventivoId, Long tenantId);
}

