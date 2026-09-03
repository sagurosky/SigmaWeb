package mantenimiento.gestorTareas.modulo.tecnicos;
import mantenimiento.gestorTareas.infraestructura.multitenant.TenantContext;
import mantenimiento.gestorTareas.modulo.equipos.Activo;
import mantenimiento.gestorTareas.modulo.usuarios.Usuario;

import java.util.List;

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








}
