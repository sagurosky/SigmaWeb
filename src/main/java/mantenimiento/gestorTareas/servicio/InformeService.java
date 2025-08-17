package mantenimiento.gestorTareas.servicio;

import java.time.LocalDateTime;
import java.util.List;
import mantenimiento.gestorTareas.dominio.Informe;
import mantenimiento.gestorTareas.dominio.Produccion;
import mantenimiento.gestorTareas.dominio.Tecnico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InformeService extends JpaRepository<Informe,Long> {
    
     @Query("SELECT i FROM Informe i JOIN i.asignaciones a WHERE a.tecnico = ?1 ")
    public List<Informe> traerPorTecnico(Tecnico tecnico );
    
    
    @Query("SELECT i FROM Informe i JOIN i.asignaciones a WHERE a.tecnico = ?1 AND "
       + "i.fechaDeCreacion >= STR_TO_DATE(?2, '%Y-%m-%dT%H:%i:%s') AND "
        + "i.fechaDeCreacion <= STR_TO_DATE(?3, '%Y-%m-%dT%H:%i:%s')")
public List<Informe> traerPorTecnicoEnRangoDeFecha(Tecnico tecnico, String fechaInicio, String fechaFin);



 @Query("SELECT t FROM Informe t  WHERE "
            + " t.estadoInforme=?1 ")
    public List<Informe> traerInformesPorEstado(String estado );

    //chequear bien si se usa, porque en el servicio de tareas tengo uno que hace lo mismo
    
 @Query("SELECT i FROM Informe i JOIN i.asignaciones a WHERE a.tecnico = ?1 and i.estadoInforme=?2")
    public List<Informe> traerInformesPorEstadoPorTecnico(Tecnico tecnico, String estado  );
    
    
    
    

    





}
