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

    List<Informe> findByTenantId(Long tenantId);

     @Query("SELECT i FROM Informe i JOIN i.asignaciones a WHERE a.tecnico = ?1 and i.tenant.id = :tenantId and a.tenant.id = :tenantId")
    public List<Informe> traerPorTecnico(Tecnico tecnico, @Param("tenantId") Long tenantId );
    
    
    @Query("SELECT i FROM Informe i JOIN i.asignaciones a WHERE a.tecnico = ?1 AND "
       + "i.fechaDeCreacion >= STR_TO_DATE(?2, '%Y-%m-%dT%H:%i:%s') AND "
        + "i.fechaDeCreacion <= STR_TO_DATE(?3, '%Y-%m-%dT%H:%i:%s') and i.tenant.id = :tenantId and a.tenant.id = :tenantId")
public List<Informe> traerPorTecnicoEnRangoDeFecha(Tecnico tecnico, String fechaInicio, String fechaFin, @Param("tenantId") Long tenantId);



 @Query("SELECT t FROM Informe t  WHERE "
            + " t.estadoInforme=?1 and t.tenant.id = :tenantId")
    public List<Informe> traerInformesPorEstado(String estado, @Param("tenantId") Long tenantId );

    //chequear bien si se usa, porque en el servicio de tareas tengo uno que hace lo mismo
    
 @Query("SELECT i FROM Informe i JOIN i.asignaciones a WHERE a.tecnico = ?1 and i.estadoInforme=?2 and i.tenant.id = :tenantId and a.tenant.id = :tenantId")
    public List<Informe> traerInformesPorEstadoPorTecnico(Tecnico tecnico, String estado, @Param("tenantId") Long tenantId  );
    
    
    
    

    





}
