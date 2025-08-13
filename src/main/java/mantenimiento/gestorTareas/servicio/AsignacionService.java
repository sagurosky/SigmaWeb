package mantenimiento.gestorTareas.servicio;

import java.util.List;
import mantenimiento.gestorTareas.dominio.Asignacion;
import mantenimiento.gestorTareas.dominio.Tarea;
import mantenimiento.gestorTareas.dominio.Tecnico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AsignacionService extends JpaRepository<Asignacion,Long> {
    
 
    
    @Query("SELECT a FROM Asignacion a  WHERE "
        + "a.tarea =?1 ")
    public List<Asignacion> traerPorTarea(Tarea tarea );
   
    @Query("SELECT a FROM Asignacion a  WHERE "
        + "a.tecnico =?1 ")
    public List<Asignacion> traerPorTecnico(Tecnico tecnico );

    
    
    
    
}
