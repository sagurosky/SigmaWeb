package mantenimiento.gestorTareas.modulo.equipos;
import mantenimiento.gestorTareas.infraestructura.multitenant.TenantContext;
import mantenimiento.gestorTareas.modulo.tecnicos.Tecnico;

import java.util.List;
import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PreventivoService extends JpaRepository<Preventivo,Long> {


    default List<Preventivo> findAllByTenant() {
        Long tenantId = TenantContext.getTenantId();
        return findByTenantId(tenantId);
    }

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

    // =========================================================================
    // CONSULTAS AGREGADAS EN BD PARA DASHBOARD (SOLAPA PREVENTIVOS)
    // =========================================================================

    @Query("SELECT COALESCE(p.estado, 'Sin Estado'), COUNT(p) FROM Preventivo p WHERE p.fechaDeCreacion BETWEEN :inicio AND :fin AND p.tenant.id = :tenantId GROUP BY p.estado")
    List<Object[]> contarPreventivosPorEstado(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin, @Param("tenantId") Long tenantId);

    @Query("SELECT COALESCE(p.frecuencia, 'Sin Frecuencia'), COUNT(p) FROM Preventivo p WHERE p.fechaDeCreacion BETWEEN :inicio AND :fin AND p.tenant.id = :tenantId GROUP BY p.frecuencia")
    List<Object[]> contarPreventivosPorFrecuencia(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin, @Param("tenantId") Long tenantId);

    @Query(value = "SELECT DATE_FORMAT(p.fecha_de_creacion, '%Y-%m') AS mes, COUNT(*) FROM preventivo p WHERE p.fecha_de_creacion BETWEEN :inicio AND :fin AND p.tenant_id = :tenantId GROUP BY DATE_FORMAT(p.fecha_de_creacion, '%Y-%m') ORDER BY mes ASC", nativeQuery = true)
    List<Object[]> contarPreventivosProgramadosPorMes(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin, @Param("tenantId") Long tenantId);

    @Query(value = "SELECT DATE_FORMAT(p.fecha_realizado, '%Y-%m') AS mes, COUNT(*) FROM preventivo p WHERE p.fecha_realizado BETWEEN :inicio AND :fin AND p.tenant_id = :tenantId GROUP BY DATE_FORMAT(p.fecha_realizado, '%Y-%m') ORDER BY mes ASC", nativeQuery = true)
    List<Object[]> contarPreventivosRealizadosPorMes(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin, @Param("tenantId") Long tenantId);

    @Query(value = "SELECT COALESCE(a.nombre, 'Sin Equipo') AS nombre, COUNT(*) AS cantidad FROM preventivo p LEFT JOIN activo a ON p.activo = a.id WHERE p.fecha_de_creacion BETWEEN :inicio AND :fin AND p.tenant_id = :tenantId GROUP BY COALESCE(a.nombre, 'Sin Equipo') ORDER BY cantidad DESC", nativeQuery = true)
    List<Object[]> contarPreventivosPorEquipoTop(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin, @Param("tenantId") Long tenantId);

    @Query("SELECT COALESCE(p.categoria, 'Sin Categoría'), COUNT(p) FROM Preventivo p WHERE p.fechaDeCreacion BETWEEN :inicio AND :fin AND p.tenant.id = :tenantId GROUP BY p.categoria")
    List<Object[]> contarPreventivosPorCategoria(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin, @Param("tenantId") Long tenantId);

}
