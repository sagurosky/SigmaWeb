package mantenimiento.gestorTareas.modulo.suscripcion;
import mantenimiento.gestorTareas.modulo.usuarios.Rol;
import mantenimiento.gestorTareas.modulo.usuarios.RolDao;
import mantenimiento.gestorTareas.modulo.usuarios.Usuario;
import mantenimiento.gestorTareas.modulo.usuarios.UsuarioService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@Slf4j
public class Pago {

    @Autowired
    private MpService mpService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private RolDao rolDao;

    @GetMapping("/api/usuario/pagoExitoso")
    public String pagoExitoso(@RequestParam(value = "preapproval_id", required = false) String preapprovalId,
                             @RequestParam(value = "preapproval_plan_id", required = false) String preapprovalPlanId,
                             @RequestParam(value = "plan_id", required = false) String planId,
                             RedirectAttributes redirectAttributes) {

        log.info("Retorno de Mercado Pago tras suscripción. preapproval_id: {}, preapproval_plan_id: {}, plan_id: {}",
                preapprovalId, preapprovalPlanId, planId);

        String idPlan = (preapprovalPlanId != null && !preapprovalPlanId.trim().isEmpty()) ? preapprovalPlanId : planId;

        if (preapprovalId == null || preapprovalId.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No se recibió identificador de suscripción de Mercado Pago.");
            return "redirect:/gestionUsuarios";
        }

        boolean aprobado = mpService.validarPreapproval(preapprovalId);

        if (!aprobado) {
            log.warn("El preapproval {} no se encuentra autorizado en Mercado Pago", preapprovalId);
            redirectAttributes.addFlashAttribute("error", "El pago de la suscripción no fue aprobado.");
            return "redirect:/gestionUsuarios";
        }

        Usuario usuario = mpService.obtenerUsuarioPorSuscripcion(preapprovalId, idPlan);

        if (usuario == null) {
            log.warn("No se encontró usuario asociado al preapprovalId {} ni al planId {}", preapprovalId, idPlan);
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado para la suscripción recibida.");
            return "redirect:/gestionUsuarios";
        }

        // Actualizamos al ID de suscripción definitivo para cancelaciones y webhooks
        usuario.setPreapprovalId(preapprovalId);
        usuario.setEstado("ACTIVO");
        usuario.setFechaUltimoPago(java.time.LocalDateTime.now());
        if (usuario.getFechaSuscripcionInicial() == null) {
            usuario.setFechaSuscripcionInicial(java.time.LocalDateTime.now());
        }
        usuarioService.guardar(usuario);
        log.info("Usuario {} activado exitosamente con preapproval_id definitivo: {}", usuario.getUsername(), preapprovalId);

        // Si es un técnico, redirigir a cargar sus datos de empresa
        boolean esTecnico = rolDao.findByUsuario(usuario).stream()
                .anyMatch(rol -> "ROLE_TECNICO".equals(rol.getNombre()));

        if (esTecnico) {
            redirectAttributes.addFlashAttribute("mensaje", "¡Suscripción confirmada! Usuario '" + usuario.getUsername() + "' activado con éxito. Por favor completa sus datos de empresa a continuación.");
            return "redirect:/tecnicoDatosEmpresa/" + usuario.getIdUsuario();
        }

        redirectAttributes.addFlashAttribute("mensaje", "¡Suscripción confirmada! Usuario '" + usuario.getUsername() + "' activado con éxito.");
        return "redirect:/gestionUsuarios";
    }

    @PostMapping("/api/usuario/pagoWebhook")
    @ResponseBody
    public ResponseEntity<String> pagoWebhook(@RequestBody Map<String, Object> payload) {
        log.info("Webhook recibido de Mercado Pago: {}", payload);

        try {
            String type = (String) payload.get("type");
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            String id = data != null ? (String) data.get("id") : null;

            if ("subscription_preapproval".equals(type) && id != null) {
                boolean autorizado = mpService.validarPreapproval(id);
                Usuario usuario = mpService.obtenerUsuarioTemporal(id);
                if (usuario != null) {
                    usuario.setEstado(autorizado ? "ACTIVO" : "PENDIENTE");
                    if (autorizado) {
                        usuario.setFechaUltimoPago(java.time.LocalDateTime.now());
                    }
                    usuarioService.guardar(usuario);
                    log.info("Webhook: Estado de usuario {} actualizado a {}", usuario.getUsername(), usuario.getEstado());
                }
            }
        } catch (Exception e) {
            log.error("Error procesando Webhook de Mercado Pago", e);
        }

        return ResponseEntity.ok("ok");
    }
}
