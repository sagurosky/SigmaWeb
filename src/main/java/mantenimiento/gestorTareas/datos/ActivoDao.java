package mantenimiento.gestorTareas.datos;

import java.util.List;
import java.util.Optional;

import mantenimiento.gestorTareas.dominio.Activo;
import mantenimiento.gestorTareas.dominio.Tarea;
import mantenimiento.gestorTareas.dominio.TenantContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivoDao extends JpaRepository<Activo, Long> {

    default List<Activo> findAllByTenant() {
        Long tenantId = TenantContext.getTenantId();
        return findByTenantId(tenantId);
    }

    List<Activo> findByTenantId(Long tenantId);

    @Query("SELECT t FROM Activo t WHERE t.nombreCamelCase LIKE %?1% AND t.tenant.id = ?2")
    Activo findByNameAndTenantId(String nombre, Long tenantId);

    @Query("SELECT t FROM Activo t WHERE t.estado LIKE %?1% AND t.tenant.id = ?2")
    List<Activo> findByStatusAndTenantId(String estado, Long tenantId);

    // 🚀 Dejá que Spring Data genere el query automáticamente
    Optional<Activo> findByIdAndTenantId(Long id, Long tenantId);
}