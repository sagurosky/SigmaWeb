package mantenimiento.gestorTareas.web;
import lombok.extern.slf4j.Slf4j;
import mantenimiento.gestorTareas.dominio.DatosDeDispositivoDTO;
import mantenimiento.gestorTareas.servicio.FcmService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
public class NotificacionesMovil {

    private final FcmService fcmService;

    public NotificacionesMovil(FcmService fcmService) {
        this.fcmService = fcmService;
    }

    @GetMapping("/notificaciones/probar")
    public String enviar() {
        try {
            fcmService.enviarNotificacionBasica();
            return "Notificación enviada";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
    
    
    
    
     @PostMapping("/notificaciones/accionDeDispositivo")
    public ResponseEntity<Void> recibirAccionDispositivo(@RequestBody DatosDeDispositivoDTO tarea) {
        log.info("Acción recibida desde dispositivo móvil:");
        log.info("taskId: {}", tarea.getTaskId());
        log.info("acción: {}", tarea.getAccion());
        log.info("usuario: {}", tarea.getUsuarioId());

        // Acá podés procesar o guardar la acción, lo que necesites...

        return ResponseEntity.ok().build();
    }
    
    
    
    
    
    
}
