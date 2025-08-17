package mantenimiento.gestorTareas.servicio;

import java.util.List;
import mantenimiento.gestorTareas.dominio.Activo;
import mantenimiento.gestorTareas.dominio.Preventivo;
import mantenimiento.gestorTareas.dominio.Tarea;
import mantenimiento.gestorTareas.dominio.Tecnico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PreventivoService extends JpaRepository<Preventivo,Long> {
    
    
    @Query("SELECT t FROM Preventivo t  WHERE "
            + "t.activo=?1 ")
    public List<Preventivo> traerPorActivo(Activo activo );
    
    @Query("SELECT t FROM Preventivo t  WHERE "
            + "t.activo.nombreCamelCase=?1 and t.estado='validado'")
    public List<Preventivo> traerPreventivosValidadosPorNombreActivo(String activo );
    
    @Query("SELECT t FROM Preventivo t  WHERE "
            + " t.estado='pendiente'")
    public List<Preventivo> traerPreventivosNoValidados( );

    @Query("SELECT t FROM Preventivo t  WHERE "
            + " t.estado='cerrado'"
            + " and t.frecuencia <> 'una vez'")
    public List<Preventivo> traerPreventivosCerradosPeriodicos( );

     @Query("SELECT p FROM Preventivo p JOIN p.asignaciones a WHERE a.tecnico = ?1 ")
    public List<Preventivo> traerPorTecnico(Tecnico tecnico );
    
    
    //pendiente
    @Query("SELECT p FROM Preventivo p JOIN p.asignaciones a WHERE a.tecnico = ?1 AND "
       + "p.fechaRealizado >= STR_TO_DATE(?2, '%Y-%m-%dT%H:%i:%s') AND "
        + "p.fechaRealizado <= STR_TO_DATE(?3, '%Y-%m-%dT%H:%i:%s')")
public List<Preventivo> traerPorTecnicoEnRangoDeFecha(Tecnico tecnico, String fechaInicio, String fechaFin);
    



}
