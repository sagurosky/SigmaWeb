package mantenimiento.gestorTareas.servicio;

import java.util.List;
import mantenimiento.gestorTareas.dominio.Activo;
import mantenimiento.gestorTareas.dominio.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ActivoService extends JpaRepository<Activo,Long> {
    
//    public List<Activo> listar();
//    public List<Tarea> filtrar(String palabraClave);
//    public Tarea encontrar(Tarea tarea);
//    public Usuario encontrarUsuario(Usuario usuario);
//    public void guardar(Activo activo);
//    public void eliminar(Activo activo);
//    @Query("SELECT t FROM Tarea t JOIN t.activo a WHERE "
//        + "t.descripcion LIKE %?1% "
//        + "OR t.solicita LIKE %?1% "
//        + "OR a.nombre LIKE %?1% "
//        + "OR a.codigo LIKE %?1%")
//    public List<Tarea> filtrar(String palabraClave);
    
       @Query("SELECT t FROM Activo t  WHERE "
        + "t.nombreCamelCase LIKE %?1% ")
    public Activo findByName(String nombre);
    
    @Query("SELECT t FROM Activo t  WHERE "
        + "t.estado LIKE %?1% ")
    public List<Activo> findByStatus(String estado);
}
