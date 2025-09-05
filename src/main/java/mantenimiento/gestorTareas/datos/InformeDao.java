package mantenimiento.gestorTareas.datos;

import java.util.List;

import mantenimiento.gestorTareas.dominio.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InformeDao extends JpaRepository<Informe,Long>{
    default List<Informe> findAllByTenant() {
        Long tenantId = TenantContext.getTenantId();
        return findByTenantId(tenantId);
    }

    List<Informe> findByTenantId(Long tenantId);

    @Query("SELECT i FROM Informe i JOIN i.asignaciones a WHERE a.tecnico = ?1 AND i.tenant.id = ?2")
    List<Informe> traerPorTecnico(Tecnico tecnico, Long tenantId);

    @Query("SELECT i FROM Informe i JOIN i.asignaciones a WHERE a.tecnico = ?1 AND i.fechaDeCreacion >= STR_TO_DATE(?2, '%Y-%m-%dT%H:%i:%s') AND i.fechaDeCreacion <= STR_TO_DATE(?3, '%Y-%m-%dT%H:%i:%s') AND i.tenant.id = ?4")
    List<Informe> traerPorTecnicoEnRangoDeFecha(Tecnico tecnico, String fechaInicio, String fechaFin, Long tenantId);

    @Query("SELECT i FROM Informe i JOIN i.asignaciones a WHERE a.tecnico = ?1 AND i.estadoInforme=?2 AND i.tenant.id = ?3")
    List<Informe> traerInformesPorEstadoPorTecnico(Tecnico tecnico, String estado, Long tenantId);
}
