package mantenimiento.gestorTareas.datos;

import mantenimiento.gestorTareas.dominio.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
//para poder hacer extends de una interface, la clase debe ser una interfase tambien

public interface UsuarioDao extends JpaRepository<Usuario, Long> {

    // Método necesario para Spring Security
    Usuario findByUsername(String username);

    // Nuevo método para listar usuarios por tenant
    List<Usuario> findByTenantId(Long tenantId);
}

