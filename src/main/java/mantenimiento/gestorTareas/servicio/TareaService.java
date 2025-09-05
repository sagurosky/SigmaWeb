package mantenimiento.gestorTareas.servicio;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import mantenimiento.gestorTareas.dominio.Activo;
import mantenimiento.gestorTareas.dominio.Tarea;
import mantenimiento.gestorTareas.dominio.Tecnico;
import mantenimiento.gestorTareas.dominio.TenantContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TareaService extends JpaRepository<Tarea,Long> {


    //DMS limito las tareas que levanta a un año atrás para cuando crezca mucho la base de datos
    @Query("SELECT t FROM Tarea t WHERE " +
            "t.estado != 'cerrada' AND " +
            "t.estado != 'disponible' AND " +
            "t.estado != 'finDisponible' AND " +
            "t.afectaProduccion = 'si' AND " +
            "t.momentoDetencion BETWEEN :fechaInicio AND :fechaFin AND " +
            "t.tenant.id = :tenantId")

    public List<Tarea> traerNoCerradas(@Param("fechaInicio") LocalDateTime fechaInicio,
                                       @Param("fechaFin") LocalDateTime fechaFin,
                                       @Param("tenantId") Long tenantId);

    @Query("SELECT t FROM Tarea t WHERE " +
            "t.estado = 'cerrada' AND " +
            "t.afectaProduccion = 'si' AND " +
            "t.momentoDetencion BETWEEN :fechaInicio AND :fechaFin AND " +
            "t.tenant.id = :tenantId")
    List<Tarea> traerCerradas(@Param("fechaInicio") LocalDateTime fechaInicio,
                              @Param("fechaFin") LocalDateTime fechaFin,
                              @Param("tenantId") Long tenantId);


    @Query("SELECT t FROM Tarea t WHERE " +
            "t.estado = 'cerrada' AND " +
            "t.activo = :activo AND " +
            "t.afectaProduccion = 'si' AND " +
            "t.momentoDetencion BETWEEN :fechaInicio AND :fechaFin AND " +
            "t.tenant.id = :tenantId")
    List<Tarea> traerCerradasPorActivo(@Param("activo") Activo activo,
                                       @Param("fechaInicio") LocalDateTime fechaInicio,
                                       @Param("fechaFin") LocalDateTime fechaFin,
                                       @Param("tenantId") Long tenantId);

    @Query("SELECT t FROM Tarea t WHERE " +
            "t.estado = 'disponible' AND " +
            "t.activo = :activo AND " +
            "t.tenant.id = :tenantId " +
            "ORDER BY t.momentoDetencion DESC")
    List<Tarea> traerDisponiblePorActivo(@Param("activo") Activo activo,
                                         @Param("tenantId") Long tenantId);

    @Query("SELECT t FROM Tarea t WHERE " +
            "t.estado <> 'cerrada' AND " +
            "t.estado <> 'disponible' AND " +
            "t.estado <> 'finDisponible' AND " +
            "t.activo = ?1 AND t.tenant.id = ?2")
    List<Tarea> traerNoCerradaPorActivo(Activo activo, Long tenantId);





    @Query(value = """
SELECT
  -- MTTR en minutos
  SUM(
    CASE
      WHEN departamento_responsable = 'mantenimiento'
           AND estado NOT IN ('disponible', 'finDisponible')
      THEN TIMESTAMPDIFF(SECOND,
             GREATEST(momento_detencion, :inicio),
             LEAST(momento_liberacion, :fin)
           ) / 60.0
      ELSE 0
    END
  ) / NULLIF(SUM(
    CASE
      WHEN departamento_responsable = 'mantenimiento'
           AND estado NOT IN ('disponible', 'finDisponible')
      THEN 1
      ELSE 0
    END
  ), 0) AS mttr,

  -- MTBF en minutos
  (
    TIMESTAMPDIFF(SECOND, :inicio, :fin) / 60.0

    -- menos paradas no por falla
    - SUM(
        CASE
          WHEN departamento_responsable <> 'mantenimiento'
               OR estado IN ('disponible', 'finDisponible')
          THEN TIMESTAMPDIFF(SECOND,
                 GREATEST(momento_detencion, :inicio),
                 LEAST(momento_liberacion, :fin)
               ) / 60.0
          ELSE 0
        END
      )

    -- menos tiempo de fallas reales
    - SUM(
        CASE
          WHEN departamento_responsable = 'mantenimiento'
               AND estado NOT IN ('disponible', 'finDisponible')
          THEN TIMESTAMPDIFF(SECOND,
                 GREATEST(momento_detencion, :inicio),
                 LEAST(momento_liberacion, :fin)
               ) / 60.0
          ELSE 0
        END
      )

  ) / NULLIF(SUM(
        CASE
          WHEN departamento_responsable = 'mantenimiento'
               AND estado NOT IN ('disponible', 'finDisponible')
          THEN 1
          ELSE 0
        END
      ), 0) AS mtbf
FROM tareas
WHERE
  momento_liberacion > :inicio
  AND momento_detencion < :fin
   AND tareas.activo = :activo
  AND tareas.tenant_id = :tenantId
""", nativeQuery = true)
    Map<String, Object> calcularIndicadores(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin, @Param("activo") Activo activo,  @Param("tenantId") Long tenantId);









    @Query("SELECT t FROM Tarea t WHERE " +
            "t.estado = 'cerrada' AND " +
            "t.departamentoResponsable = 'mantenimiento' AND " +
            "t.activo = ?1 AND " +
            "t.momentoDetencion >= STR_TO_DATE(?2, '%Y-%m-%dT%H:%i:%s') AND " +
            "t.momentoDetencion <= STR_TO_DATE(?3, '%Y-%m-%dT%H:%i:%s') AND " +
            "t.tenant.id = ?4")
    List<Tarea> traerCerradasPorActivoEnRangoDeFecha(Activo activo, String fechaInicio, String fechaFin, Long tenantId);


    @Query("SELECT t FROM Tarea t WHERE " +
            "(t.estado = 'cerrada' OR t.estado = 'finDisponible') AND " +
            "(t.departamentoResponsable <> 'mantenimiento' OR t.departamentoResponsable IS NULL) AND " +
            "t.activo = ?1 AND " +
            "t.momentoDetencion >= STR_TO_DATE(?2, '%Y-%m-%dT%H:%i:%s') AND " +
            "t.momentoDetencion <= STR_TO_DATE(?3, '%Y-%m-%dT%H:%i:%s') AND " +
            "t.tenant.id = ?4")
    List<Tarea> traerNoMantenimientoPorActivoEnRangoDeFecha(Activo activo, String fechaInicio, String fechaFin, Long tenantId);





    @Query("SELECT t FROM Tarea t WHERE " +
            "t.activo.nombre LIKE CONCAT('%', ?1, '%') AND " +
            "t.momentoDetencion >= STR_TO_DATE(?2, '%Y-%m-%dT%H:%i:%s') AND " +
            "t.momentoDetencion <= STR_TO_DATE(?3, '%Y-%m-%dT%H:%i:%s') AND " +
            "t.tenant.id = ?4")
    List<Tarea> traerPorLineaEnRangoDeFecha(String linea, String fechaInicio, String fechaFin, Long tenantId);


    @Query("SELECT t FROM Tarea t JOIN t.asignaciones a WHERE " +
            "a.tecnico = :tecnico AND t.estado = 'cerrada' AND " +
            "t.momentoDetencion BETWEEN :fechaInicio AND :fechaFin AND t.tenant.id = :tenantId")
    List<Tarea> traerPorTecnico(@Param("tecnico") Tecnico tecnico,
                                @Param("fechaInicio") LocalDateTime fechaInicio,
                                @Param("fechaFin") LocalDateTime fechaFin,
                                @Param("tenantId") Long tenantId);

    @Query("SELECT t FROM Tarea t JOIN t.asignaciones a WHERE " +
            "a.tecnico = :tecnico AND t.estado = 'cerrada' AND " +
            "t.informe.estadoInforme = :estado AND " +
            "t.momentoDetencion BETWEEN :fechaInicio AND :fechaFin AND t.tenant.id = :tenantId")
    List<Tarea> traerPorTecnicoYEstadoInforme(@Param("tecnico") Tecnico tecnico,
                                              @Param("estado") String estado,
                                              @Param("fechaInicio") LocalDateTime fechaInicio,
                                              @Param("fechaFin") LocalDateTime fechaFin,
                                              @Param("tenantId") Long tenantId);

    @Query("SELECT t FROM Tarea t WHERE " +
            "t.estado = 'cerrada' AND " +
            "t.informe.estadoInforme = :estado AND " +
            "t.momentoDetencion BETWEEN :fechaInicio AND :fechaFin AND t.tenant.id = :tenantId")
    List<Tarea> traerPorEstadoInforme(@Param("estado") String estado,
                                      @Param("fechaInicio") LocalDateTime fechaInicio,
                                      @Param("fechaFin") LocalDateTime fechaFin,
                                      @Param("tenantId") Long tenantId);


}
