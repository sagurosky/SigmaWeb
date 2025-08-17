package mantenimiento.gestorTareas.servicio;

import java.time.LocalDateTime;
import java.util.List;
import mantenimiento.gestorTareas.dominio.Produccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProduccionService extends JpaRepository<Produccion,Long> {
    
     @Query("SELECT p FROM Produccion p WHERE p.estado='abierta'")
    List<Produccion> traerAbiertas();
     @Query("SELECT p FROM Produccion p WHERE p.estado='cerrada'")
    List<Produccion> traerCerradas();


}
