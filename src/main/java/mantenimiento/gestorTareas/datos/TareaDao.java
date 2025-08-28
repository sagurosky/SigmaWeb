package mantenimiento.gestorTareas.datos;

import java.util.List;
import mantenimiento.gestorTareas.dominio.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TareaDao extends JpaRepository<Tarea,Long>{
    List<Tarea> findByTenantId(Long tenantId);

    @Query("SELECT t FROM Tarea t JOIN t.activo a WHERE (t.descripcion LIKE %?1% OR t.solicita LIKE %?1% OR a.nombre LIKE %?1% OR a.codigo LIKE %?1%) AND a.estado != 'cerrada' AND t.tenant.id = ?2")
    List<Tarea> filtrar(String palabraClave, Long tenantId);
}
