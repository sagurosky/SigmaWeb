package mantenimiento.gestorTareas.datos;

import mantenimiento.gestorTareas.dominio.TenantContext;
import mantenimiento.gestorTareas.dominio.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
//para poder hacer extends de una interface, la clase debe ser una interfase tambien
public interface UsuarioDao extends JpaRepository<Usuario, Long> {

    default List<Usuario> findAllByTenant() {
        Long tenantId = TenantContext.getTenantId();
        return findByTenantId(tenantId);
    }

        boolean existsByUsername(String username);

    // Método necesario para Spring Security
    Usuario findByUsername(String username);

    // Nuevo método para listar usuarios por tenant
    List<Usuario> findByTenantId(Long tenantId);


}

