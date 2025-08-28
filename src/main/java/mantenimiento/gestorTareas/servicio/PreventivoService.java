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
    @Query("SELECT t FROM Preventivo t " +
            "WHERE t.activo = :activo " +
            "AND t.tenant.id = :tenantId")
    public List<Preventivo> traerPorActivo(@Param("activo") Activo activo,
                                           @Param("tenantId") Long tenantId);

    @Query("SELECT t FROM Preventivo t " +
            "WHERE t.activo.nombreCamelCase = :activoNombre " +
            "AND t.estado = 'validado' " +
            "AND t.tenant.id = :tenantId")
    public List<Preventivo> traerPreventivosValidadosPorNombreActivo(@Param("activoNombre") String activo,
                                                                     @Param("tenantId") Long tenantId);

    @Query("SELECT t FROM Preventivo t " +
            "WHERE t.estado = 'pendiente' " +
            "AND t.tenant.id = :tenantId")
    public List<Preventivo> traerPreventivosNoValidados(@Param("tenantId") Long tenantId);

    @Query("SELECT t FROM Preventivo t " +
            "WHERE t.estado = 'cerrado' " +
            "AND t.frecuencia <> 'una vez' " +
            "AND t.tenant.id = :tenantId")
    public List<Preventivo> traerPreventivosCerradosPeriodicos(@Param("tenantId") Long tenantId);

    @Query("SELECT p FROM Preventivo p " +
            "JOIN p.asignaciones a " +
            "WHERE a.tecnico = :tecnico " +
            "AND p.tenant.id = :tenantId " +
            "AND a.tenant.id = :tenantId")
    public List<Preventivo> traerPorTecnico(@Param("tecnico") Tecnico tecnico,
                                            @Param("tenantId") Long tenantId);

    @Query("SELECT p FROM Preventivo p " +
            "JOIN p.asignaciones a " +
            "WHERE a.tecnico = :tecnico " +
            "AND p.fechaRealizado >= STR_TO_DATE(:fechaInicio, '%Y-%m-%dT%H:%i:%s') " +
            "AND p.fechaRealizado <= STR_TO_DATE(:fechaFin, '%Y-%m-%dT%H:%i:%s') " +
            "AND p.tenant.id = :tenantId " +
            "AND a.tenant.id = :tenantId")
    public List<Preventivo> traerPorTecnicoEnRangoDeFecha(@Param("tecnico") Tecnico tecnico,
                                                          @Param("fechaInicio") String fechaInicio,
                                                          @Param("fechaFin") String fechaFin,
                                                          @Param("tenantId") Long tenantId);


}
