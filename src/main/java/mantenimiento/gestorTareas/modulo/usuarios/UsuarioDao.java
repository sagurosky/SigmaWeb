package mantenimiento.gestorTareas.modulo.usuarios;
import mantenimiento.gestorTareas.infraestructura.multitenant.TenantContext;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    @Query("SELECT DISTINCT u FROM Usuario u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Usuario findByUsername(@Param("username") String username);

    // Método para listar usuarios por tenant evitando N+1 con roles
    @Query("SELECT DISTINCT u FROM Usuario u LEFT JOIN FETCH u.roles WHERE u.tenant.id = :tenantId")
    List<Usuario> findByTenantId(@Param("tenantId") Long tenantId);

    @Query("SELECT DISTINCT u FROM Usuario u LEFT JOIN FETCH u.roles")
    List<Usuario> findAllWithRoles();

    Usuario findByPreapprovalId(String preapprovalId);
}

