package mantenimiento.gestorTareas.servicio;

import java.util.List;
import mantenimiento.gestorTareas.dominio.Activo;
import mantenimiento.gestorTareas.dominio.Preventivo;
import mantenimiento.gestorTareas.dominio.Tarea;
import mantenimiento.gestorTareas.dominio.Tecnico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PreventivoService extends JpaRepository<Preventivo,Long> {

    List<Preventivo> findByTenantId(Long tenantId);

    @Query("SELECT t FROM Preventivo t  WHERE "
            + "t.activo=?1 and t.tenant.id=:tenantId")
    public List<Preventivo> traerPorActivo(Activo activo, @Param("tenantId") Long tenantId );
    
    @Query("SELECT t FROM Preventivo t  WHERE "
            + "t.activo.nombreCamelCase=?1 and t.estado='validado' and t.tenant.id=:tenantId")
    public List<Preventivo> traerPreventivosValidadosPorNombreActivo(String activo,@Param("tenantId") Long tenantId );
    
    @Query("SELECT t FROM Preventivo t  WHERE "
            + " t.estado='pendiente' and t.tenant.id=:tenantId")
    public List<Preventivo> traerPreventivosNoValidados( @Param("tenantId") Long tenantId);

    @Query("SELECT t FROM Preventivo t  WHERE "
            + " t.estado='cerrado'"
            + " and t.frecuencia <> 'una vez' and t.tenant.id=:tenantId")
    public List<Preventivo> traerPreventivosCerradosPeriodicos( @Param("tenantId") Long tenantId);

     @Query("SELECT p FROM Preventivo p JOIN p.asignaciones a WHERE a.tecnico = ?1 and p.tenant.id=:tenantId and a.tenant.id=:tenantId")
    public List<Preventivo> traerPorTecnico(Tecnico tecnico,@Param("tenantId") Long tenantId );
    
    
    //pendiente
    @Query("SELECT p FROM Preventivo p JOIN p.asignaciones a WHERE a.tecnico = ?1 AND "
       + "p.fechaRealizado >= STR_TO_DATE(?2, '%Y-%m-%dT%H:%i:%s') AND "
        + "p.fechaRealizado <= STR_TO_DATE(?3, '%Y-%m-%dT%H:%i:%s') and p.tenant.id=:tenantId and a.tenant.id=:tenantId")
public List<Preventivo> traerPorTecnicoEnRangoDeFecha(Tecnico tecnico, String fechaInicio, String fechaFin,@Param("tenantId") Long tenantId);
    



}
