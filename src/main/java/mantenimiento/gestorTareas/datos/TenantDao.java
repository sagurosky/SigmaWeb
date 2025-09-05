package mantenimiento.gestorTareas.datos;

import mantenimiento.gestorTareas.dominio.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantDao extends JpaRepository<Tenant, Long> {
}