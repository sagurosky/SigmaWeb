package mantenimiento.gestorTareas.modulo.usuarios;
import mantenimiento.gestorTareas.infraestructura.multitenant.TenantContext;
import mantenimiento.gestorTareas.modulo.tareas.Tarea;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
public interface RolDao extends JpaRepository<Rol,Long>{
    default List<Rol> findAllByTenant() {
        Long tenantId = TenantContext.getTenantId();
        return findByTenantId(tenantId);
    }
    List<Rol> findByTenantId(Long tenantId);

    List<Rol> findByUsuario(Usuario usuario);
}

