package mantenimiento.gestorTareas.web;

import lombok.extern.slf4j.Slf4j;
import mantenimiento.gestorTareas.datos.RolDao;
import mantenimiento.gestorTareas.dominio.Rol;
import mantenimiento.gestorTareas.dominio.Usuario;
import mantenimiento.gestorTareas.servicio.MpService;
import mantenimiento.gestorTareas.servicio.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@Controller
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

}
