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
    @Query("SELECT i FROM Informe i " +
            "JOIN i.asignaciones a " +
            "WHERE a.tecnico = :tecnico " +
            "AND i.tenant.id = :tenantId " +
            "AND a.tenant.id = :tenantId")
    public List<Informe> traerPorTecnico(@Param("tecnico") Tecnico tecnico,
                                         @Param("tenantId") Long tenantId);

    @Query("SELECT i FROM Informe i " +
            "JOIN i.asignaciones a " +
            "WHERE a.tecnico = :tecnico " +
            "AND i.fechaDeCreacion >= STR_TO_DATE(:fechaInicio, '%Y-%m-%dT%H:%i:%s') " +
            "AND i.fechaDeCreacion <= STR_TO_DATE(:fechaFin, '%Y-%m-%dT%H:%i:%s') " +
            "AND i.tenant.id = :tenantId " +
            "AND a.tenant.id = :tenantId")
    public List<Informe> traerPorTecnicoEnRangoDeFecha(@Param("tecnico") Tecnico tecnico,
                                                       @Param("fechaInicio") String fechaInicio,
                                                       @Param("fechaFin") String fechaFin,
                                                       @Param("tenantId") Long tenantId);

    @Query("SELECT t FROM Informe t " +
            "WHERE t.estadoInforme = :estado " +
            "AND t.tenant.id = :tenantId")
    public List<Informe> traerInformesPorEstado(@Param("estado") String estado,
                                                @Param("tenantId") Long tenantId);

    @Query("SELECT i FROM Informe i " +
            "JOIN i.asignaciones a " +
            "WHERE a.tecnico = :tecnico " +
            "AND i.estadoInforme = :estado " +
            "AND i.tenant.id = :tenantId " +
            "AND a.tenant.id = :tenantId")
    public List<Informe> traerInformesPorEstadoPorTecnico(@Param("tecnico") Tecnico tecnico,
                                                          @Param("estado") String estado,
                                                          @Param("tenantId") Long tenantId);


    





}
