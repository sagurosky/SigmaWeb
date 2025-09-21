package mantenimiento.gestorTareas.web;

import lombok.extern.slf4j.Slf4j;
import mantenimiento.gestorTareas.datos.RolDao;
import mantenimiento.gestorTareas.dominio.Rol;
import mantenimiento.gestorTareas.dominio.Usuario;
import mantenimiento.gestorTareas.servicio.MpService;
import mantenimiento.gestorTareas.servicio.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@Slf4j
public class Pago {

    @Autowired
    MpService mpService;

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    RolDao rolDao;

    @GetMapping("/api/usuario/pagoExitoso")
    public String pagoExitoso(@RequestParam("preapproval_id") String preapprovalId, Model model) {

        log.info("retorno de mp, preaprovalID: "+preapprovalId);

        boolean aprobado = mpService.validarPreapproval(preapprovalId);

        if (!aprobado) {
            model.addAttribute("mensaje", "Pago no aprobado, usuario no creado");
            return "gestionUsuarios";
        }

        Usuario usuario = mpService.obtenerUsuarioTemporal(preapprovalId);

        if (usuario == null) {
            model.addAttribute("mensaje", "Usuario temporal no encontrado");
            return "gestionUsuarios";
        }

        // Asignar roles definitivos si es necesario
        List<Rol> roles = rolDao.findByUsuario(usuario);
        roles.forEach(rol -> rol.setUsuario(usuario)); // si quieres refrescar referencias

        usuario.setEstado("ACTIVO");
        usuarioService.guardar(usuario);

        model.addAttribute("mensaje", "Pago exitoso, usuario creado correctamente");
        return "gestionUsuarios"; // página del listado de usuarios
    }
    @PostMapping("/api/usuario/pagoWebhook")
    public ResponseEntity<String> pagoWebhook(@RequestBody Map<String,Object> payload) {
        log.info("Webhook MP: {}", payload);

        // Ejemplo de lectura de campos
        String type = (String) payload.get("type");
        Map<String,Object> data = (Map<String,Object>) payload.get("data");
        String id = data != null ? (String) data.get("id") : null;

        // Aquí tu lógica: por ejemplo validar que sea subscription_preapproval
        if ("subscription_preapproval".equals(type)) {
            // llamar a mpService.validarPreapproval(id);
        }

        // MP requiere una respuesta 200 OK en <22s
        return ResponseEntity.ok("ok");
    }

}
