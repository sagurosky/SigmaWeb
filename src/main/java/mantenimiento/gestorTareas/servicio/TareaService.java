package mantenimiento.gestorTareas.servicio;

import java.time.LocalDateTime;
import java.util.List;
import mantenimiento.gestorTareas.dominio.Activo;
import mantenimiento.gestorTareas.dominio.Tarea;
import mantenimiento.gestorTareas.dominio.Tecnico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TareaService extends JpaRepository<Tarea,Long> {
    
 
    
    @Query("SELECT t FROM Tarea t  WHERE "
        + "t.estado !='cerrada' ")
    public List<Tarea> traerNoCerradas( );
    
    @Query("SELECT t FROM Tarea t  WHERE "
        + "t.estado ='cerrada' ")
    public List<Tarea> traerCerradas( );
    
    @Query("SELECT t FROM Tarea t  WHERE "
        + "t.estado ='cerrada' and "
            + "t.activo=?1")
    public List<Tarea> traerCerradasPorActivo(Activo activo );
    
    
    @Query("SELECT t FROM Tarea t  WHERE "
        + "t.estado !='cerrada' and "
            + "t.activo=?1")
    public List<Tarea> traerNoCerradaPorActivo(Activo activo );
    
    
    @Query("SELECT t FROM Tarea t  WHERE "
            + "t.activo.nombre LIKE CONCAT('%', ?1, '%') AND "
//            + "t.activo.momentoDetencion >= ?2 and "
//             + "t.activo.momentoDetencion <= ?3")
//    public List<Tarea> traerPorLineaEnRangoDeFecha(String linea, LocalDateTime  fechainicio, LocalDateTime  fechaFin );
   + "t.momentoDetencion >= STR_TO_DATE(?2, '%Y-%m-%dT%H:%i:%s') AND "
        + "t.momentoDetencion <= STR_TO_DATE(?3, '%Y-%m-%dT%H:%i:%s')")
public List<Tarea> traerPorLineaEnRangoDeFecha(String linea, String fechaInicio, String fechaFin);
    
      @Query("SELECT t FROM Tarea t JOIN t.asignaciones a WHERE a.tecnico = ?1 AND t.estado = 'cerrada'")
    public List<Tarea> traerPorTecnico(Tecnico tecnico );
     
    @Query("SELECT t FROM Tarea t JOIN t.asignaciones a WHERE a.tecnico = ?1 AND t.estado = 'cerrada' and  t.informe.estadoInforme= ?2")
    public List<Tarea> traerPorTecnicoYEstadoInforme(Tecnico tecnico, String estado );
  
    @Query("SELECT t FROM Tarea t  WHERE  t.estado = 'cerrada' and  t.informe.estadoInforme=?1")
    public List<Tarea> traerPorEstadoInforme(String estado );
   
    
    
}
