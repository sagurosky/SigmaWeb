package mantenimiento.gestorTareas.servicio;

import java.time.LocalDateTime;
import java.util.List;
import mantenimiento.gestorTareas.dominio.Produccion;
import mantenimiento.gestorTareas.dominio.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductoService extends JpaRepository<Producto,Long> {
    List<Producto> findByTenantId(Long tenantId);
}
