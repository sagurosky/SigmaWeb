package mantenimiento.gestorTareas.datos;

import mantenimiento.gestorTareas.dominio.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
//para poder hacer extends de una interface, la clase debe ser una interfase tambien
public interface UsuarioDao extends JpaRepository<Usuario,Long>{
    //podria haber extendido de CrudRepository, pero JpaRepository tiene otras funciones
Usuario findByUsername(String username);
        //este metodo tiene que estar escrito tal cual porque es parte de la configuracion de SpringSecurity
//Spring lohace todo en automatico
}

