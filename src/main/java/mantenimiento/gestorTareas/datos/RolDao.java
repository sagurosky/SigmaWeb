package mantenimiento.gestorTareas.datos;

import java.util.List;
import mantenimiento.gestorTareas.dominio.Rol;
import mantenimiento.gestorTareas.dominio.Tarea;
import mantenimiento.gestorTareas.dominio.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
public interface RolDao extends JpaRepository<Rol,Long>{

    
    
}

