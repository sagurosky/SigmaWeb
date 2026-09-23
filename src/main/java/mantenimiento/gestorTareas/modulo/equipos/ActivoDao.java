package mantenimiento.gestorTareas.modulo.equipos;
import mantenimiento.gestorTareas.infraestructura.multitenant.TenantContext;
import mantenimiento.gestorTareas.modulo.tareas.Tarea;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    @Query("SELECT t FROM Activo t WHERE t.nombreCamelCase = ?1 AND t.tenant.id = ?2")
    Activo findByNameAndTenantId(String nombre, Long tenantId);

    @Query("SELECT t FROM Activo t WHERE t.estado = ?1 AND t.tenant.id = ?2")
    List<Activo> findByStatusAndTenantId(String estado, Long tenantId);

    // 🚀 Dejá que Spring Data genere el query automáticamente
    Optional<Activo> findByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT a FROM Activo a WHERE a.disponibilidadDesde IS NOT NULL AND a.disponibilidadDesde <= ?1 AND (a.disponibilidadHasta IS NULL OR a.disponibilidadHasta > ?1) AND a.estado != 'disponible'")
    List<Activo> findActivosPendientesDisponibilidad(LocalDateTime ahora);

    @Query("SELECT a FROM Activo a WHERE a.disponibilidadHasta IS NOT NULL AND a.disponibilidadHasta <= ?1 AND a.estado = 'disponible'")
    List<Activo> findActivosVencidosDisponibilidad(LocalDateTime ahora);

    @Query("SELECT a FROM Activo a WHERE a.disponibilidadHasta IS NOT NULL AND a.disponibilidadHasta <= ?1 AND a.estado != 'disponible'")
    List<Activo> findActivosProgramacionExpirada(LocalDateTime ahora);
}
