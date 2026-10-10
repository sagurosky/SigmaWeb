package mantenimiento.gestorTareas.modulo.tecnicos;
import mantenimiento.gestorTareas.infraestructura.multitenant.TenantContext;
import mantenimiento.gestorTareas.modulo.equipos.Activo;
import mantenimiento.gestorTareas.modulo.usuarios.Usuario;

import java.util.List;
import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface TecnicoService extends JpaRepository<Tecnico,Long> {
    default List<Tecnico> findAllByTenant() {
        Long tenantId = TenantContext.getTenantId();
        return findByTenantId(tenantId);
    }
    List<Tecnico> findByTenantId(Long tenantId);

    @Query("SELECT t FROM Tecnico t " +
            "WHERE t.usuario = :usuario " +
            "AND t.tenant.id = :tenantId")
    public Tecnico traerPorUsuario(@Param("usuario") Usuario usuario,
                                   @Param("tenantId") Long tenantId);


    @Query("SELECT t FROM Tecnico t  WHERE "
        + "t.nombre !=null and t.tenant.id = :tenantId")
    public List<Tecnico> traerHabilitados( @Param("tenantId") Long tenantId );
    
    //trae los tecnicos que estan interviniendo en el activo enviado por parametro
    @Query("SELECT DISTINCT t FROM Tecnico t JOIN t.asignaciones a " +
            "WHERE a.tarea.estado = 'enProceso' " +
            "AND a.tarea.activo = :activo " +
            "AND t.tenant.id = :tenantId " +
            "AND a.tenant.id = :tenantId")
    List<Tecnico> traerPorTareaEnActivo(@Param("activo") Activo activo,
                                        @Param("tenantId") Long tenantId);

    // =========================================================================
    // CONSULTAS AGREGADAS PARA DASHBOARD (SOLAPA DESEMPEÑO TÉCNICO)
    // =========================================================================

    @Query(value = "SELECT CONCAT(tec.nombre, ' ', SUBSTRING(tec.apellido, 1, 1), '.') AS tecnico, ROUND(AVG((COALESCE(CAST(e.satisfaccion AS DECIMAL(10,2)), 0) + COALESCE(CAST(e.predisposicion AS DECIMAL(10,2)), 0) + COALESCE(CAST(e.responsabilidad AS DECIMAL(10,2)), 0) + COALESCE(CAST(e.seguridad AS DECIMAL(10,2)), 0) + COALESCE(CAST(e.conocimiento AS DECIMAL(10,2)), 0) + COALESCE(CAST(e.trato AS DECIMAL(10,2)), 0) + COALESCE(CAST(e.prolijidad AS DECIMAL(10,2)), 0) + COALESCE(CAST(e.puntualidad AS DECIMAL(10,2)), 0) + COALESCE(CAST(e.eficiencia AS DECIMAL(10,2)), 0) + COALESCE(CAST(e.calidad AS DECIMAL(10,2)), 0) + COALESCE(CAST(e.comunicacion AS DECIMAL(10,2)), 0) + COALESCE(CAST(e.trabajo_en_equipo AS DECIMAL(10,2)), 0) + COALESCE(CAST(e.resolucion AS DECIMAL(10,2)), 0) + COALESCE(CAST(e.creatividad AS DECIMAL(10,2)), 0) + COALESCE(CAST(e.iniciativa AS DECIMAL(10,2)), 0) + COALESCE(CAST(e.autogestion AS DECIMAL(10,2)), 0) + COALESCE(CAST(e.formacion_continua AS DECIMAL(10,2)), 0)) / 17.0), 1) AS promedio FROM tecnico tec JOIN asignacion a ON a.tecnico_id = tec.id JOIN tareas t ON t.id = a.tarea_id JOIN evaluacion e ON e.id = t.evaluacion WHERE tec.tenant_id = :tenantId AND t.momento_detencion BETWEEN :inicio AND :fin GROUP BY tec.id, tec.nombre, tec.apellido ORDER BY promedio DESC", nativeQuery = true)
    List<Object[]> obtenerRankingTecnicos(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin, @Param("tenantId") Long tenantId);

    @Query(value = "SELECT ROUND(AVG(CAST(e.eficiencia AS DECIMAL(10,2))), 1) AS eficiencia, ROUND(AVG(CAST(e.calidad AS DECIMAL(10,2))), 1) AS calidad, ROUND(AVG(CAST(e.comunicacion AS DECIMAL(10,2))), 1) AS comunicacion, ROUND(AVG(CAST(e.trabajo_en_equipo AS DECIMAL(10,2))), 1) AS trabajo_en_equipo, ROUND(AVG(CAST(e.resolucion AS DECIMAL(10,2))), 1) AS resolucion, ROUND(AVG(CAST(e.iniciativa AS DECIMAL(10,2))), 1) AS iniciativa, ROUND(AVG(CAST(e.seguridad AS DECIMAL(10,2))), 1) AS seguridad, ROUND(AVG(CAST(e.puntualidad AS DECIMAL(10,2))), 1) AS puntualidad, ROUND(AVG(CAST(e.conocimiento AS DECIMAL(10,2))), 1) AS conocimiento FROM evaluacion e JOIN tareas t ON t.evaluacion = e.id WHERE e.tenant_id = :tenantId AND t.momento_detencion BETWEEN :inicio AND :fin", nativeQuery = true)
    List<Object[]> obtenerRadarEquipo(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin, @Param("tenantId") Long tenantId);

    @Query(value = "SELECT CONCAT(tec.nombre, ' ', SUBSTRING(tec.apellido, 1, 1), '.') AS tecnico, COUNT(*) AS cerrados FROM tecnico tec JOIN asignacion a ON a.tecnico_id = tec.id JOIN tareas t ON t.id = a.tarea_id WHERE t.estado = 'cerrada' AND tec.tenant_id = :tenantId AND t.momento_detencion BETWEEN :inicio AND :fin GROUP BY tec.id, tec.nombre, tec.apellido ORDER BY cerrados DESC", nativeQuery = true)
    List<Object[]> obtenerCorrectivosPorTecnico(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin, @Param("tenantId") Long tenantId);

    @Query(value = "SELECT CONCAT(tec.nombre, ' ', SUBSTRING(tec.apellido, 1, 1), '.') AS tecnico, COUNT(*) AS realizados FROM tecnico tec JOIN asignacion_preventivo ap ON ap.tecnico_id = tec.id JOIN preventivo p ON p.id = ap.preventivo_id WHERE tec.tenant_id = :tenantId AND p.fecha_de_creacion BETWEEN :inicio AND :fin GROUP BY tec.id, tec.nombre, tec.apellido ORDER BY realizados DESC", nativeQuery = true)
    List<Object[]> obtenerPreventivosPorTecnico(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin, @Param("tenantId") Long tenantId);

}
