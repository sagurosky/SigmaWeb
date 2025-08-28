package mantenimiento.gestorTareas.servicio;

import java.util.List;
import mantenimiento.gestorTareas.dominio.Activo;
import mantenimiento.gestorTareas.dominio.Tarea;
import mantenimiento.gestorTareas.dominio.Tecnico;
import mantenimiento.gestorTareas.dominio.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface TecnicoService extends JpaRepository<Tecnico,Long> {

    List<Tecnico> findByTenantId(Long tenantId);
    
    @Query("SELECT t FROM Tecnico t  WHERE "
        + "t.usuario =?1 and"+
            "t.tenant.id = :tenantId")
    public Tecnico traerPorUsuario(Usuario usuario, @Param("tenantId") Long tenantId);

    @Query("SELECT t FROM Tecnico t  WHERE "
        + "t.nombre !=null and t.tenant.id = :tenantId")
    public List<Tecnico> traerHabilitados( @Param("tenantId") Long tenantId );
    
    //trae los tecnicos que estan interviniendo en el activo enviado por parametro
      @Query("SELECT t FROM Tecnico t JOIN t.asignaciones a WHERE a.tarea.estado='enProceso' AND a.tarea.activo= ?1 and t.tenant.id = :tenantId and a.tenant.id=:tenant.id")
    public List<Tecnico> traerPorTareaEnActivo(Activo activo,@Param("tenantId") Long tenantId );


  
    
    
    
    
}
