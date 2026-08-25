package mantenimiento.gestorTareas.modulo.usuarios;
import mantenimiento.gestorTareas.infraestructura.multitenant.TenantContext;
import mantenimiento.gestorTareas.infraestructura.multitenant.TenantDao;
import mantenimiento.gestorTareas.infraestructura.seguridad.EncriptarPassword;
import mantenimiento.gestorTareas.infraestructura.util.ArchivoExterno;
import mantenimiento.gestorTareas.modulo.equipos.ActivoService;
import mantenimiento.gestorTareas.modulo.suscripcion.MpService;
import mantenimiento.gestorTareas.modulo.tareas.Servicio;
import mantenimiento.gestorTareas.modulo.tecnicos.Tecnico;
import mantenimiento.gestorTareas.modulo.tecnicos.TecnicoService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Slf4j
public class ControladorUsuarios {

    @Autowired
    Servicio servicio;
    @Autowired
    UsuarioService usuarioService;
    @Autowired
    UsuarioDao usuarioDao;
    @Autowired
    RolDao rolDao;
    @Autowired
    TecnicoService tecnicoService;
    @Autowired
    ActivoService activoService;
    @Autowired
    MpService mpService;
    @Autowired
    TenantDao tenantDao;

    @GetMapping("/gestionUsuarios")
    public String gestionarUsuarios(Usuario usuario, Model model) {
        if (!ArchivoExterno.getString("editarUsuarios").equals("si")) return "redirect:/layout";
        var usuarios = usuarioService.listarUsuarios();
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());

        String usernameActual = SecurityContextHolder.getContext().getAuthentication() != null 
                ? SecurityContextHolder.getContext().getAuthentication().getName() 
                : null;
        model.addAttribute("usuarioActual", usernameActual);

        List<Tecnico> tecnicosFiltrados = tecnicoService.traerHabilitados(TenantContext.getTenantId()).stream()
                .filter(t -> t.getUsuario() != null && t.getUsuario().getRoles() != null && !t.getUsuario().getRoles().isEmpty() 
                        && "ROLE_TECNICO".equals(t.getUsuario().getRoles().get(0).getNombre()))
                .collect(Collectors.toList());
        model.addAttribute("tecnicos", tecnicosFiltrados);

        model.addAttribute("habilitarGestionUsuarios", ArchivoExterno.getString("editarUsuarios"));
        model.addAttribute("habilitarEditorLayout", ArchivoExterno.getString("editorLayout"));
        model.addAttribute("tiempoRefresco", ArchivoExterno.getString("tiempoRefresco"));
        return "usuarios/gestionUsuarios";
    }

    @GetMapping("/crearUsuario")
    public String crearUsuarios(Usuario usuario, Rol rol, Model model) {
        if (!ArchivoExterno.getString("editarUsuarios").equals("si")) return "redirect:/layout";
        model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());
        return "usuarios/crearUsuario";
    }

    // Flujo con integración a MercadoPago (preservado para uso futuro)
    public String gestionarMercadoPago(@Valid Usuario usuario, Errors errores, Rol rol, Model model) {
        Boolean yaExiste = usuarioDao.findByUsername(usuario.getUsername()) != null;
        if (errores.hasErrors() || yaExiste) {
            if (yaExiste) {
                errores.rejectValue("username", "500", "ya existe ese usuario");
            }
            model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());
            return "usuarios/crearUsuario";
        }

        usuario.setPasswordClaro(usuario.getPassword());
        usuario.setPassword(EncriptarPassword.encriptarPassword(usuario.getPassword()));
        usuario.setEstado("PENDIENTE");
        usuarioService.guardar(usuario);

        rol.setUsuario(usuario);
        rolDao.save(rol);

        Double monto = Double.parseDouble(ArchivoExterno.getString("monto"));
        String initPoint = mpService.crearPreapproval(usuario,
                tenantDao.findById(TenantContext.getTenantId()).orElse(null).getEmailContacto(), monto);

        return "redirect:" + initPoint;
    }

    @PostMapping("/gestionar")
    public String gestionar(@Valid Usuario usuario, Errors errores, Rol rol, Model model) {
        Boolean yaExiste = usuarioDao.findByUsername(usuario.getUsername()) != null;

        if (errores.hasErrors() || yaExiste) {
            if (yaExiste) {
                errores.rejectValue("username", "500", "ya existe ese usuario");
            }
            model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());
            return "usuarios/crearUsuario";
        }

        rol.setUsuario(usuario);
        if (rol.getNombre() != null) {
            if (rol.getNombre().equals("MANTENIMIENTO")) {
                rol.setNombre("ROLE_MANT");
            } else if (rol.getNombre().equals("PRODUCCION")) {
                rol.setNombre("ROLE_PROD");
            } else if (rol.getNombre().equals("ADMINISTRADOR")) {
                rol.setNombre("ROLE_ADMIN");
            } else if (rol.getNombre().equals("TECNICO")) {
                rol.setNombre("ROLE_TECNICO");
            } else if (rol.getNombre().equals("MONITOR")) {
                rol.setNombre("ROLE_MONITOR");
            }
        }

        usuario.setPasswordClaro(usuario.getPassword());
        usuario.setPassword(EncriptarPassword.encriptarPassword(usuario.getPassword()));

        usuarioService.guardar(usuario);
        usuario = usuarioDao.findByUsername(usuario.getUsername());
        rol.setUsuario(usuario);
        rolDao.save(rol);

        if ("ROLE_ADMIN".equals(rol.getNombre())) {
            String[] rolesAdmin = {"ROLE_MANT", "ROLE_PROD", "ROLE_TECNICO", "ROLE_MONITOR"};
            for (String rName : rolesAdmin) {
                Rol rAux = new Rol();
                rAux.setUsuario(usuario);
                rAux.setNombre(rName);
                rolDao.save(rAux);
            }
        }

        if ("ROLE_TECNICO".equals(rol.getNombre())) {
            return "redirect:/tecnicoDatosEmpresa/" + usuario.getIdUsuario();
        }
        return "redirect:/gestionUsuarios";
    }

    @GetMapping("/tecnicoDatosEmpresa/{idUsuario}")
    public String tecnicoDatosEmpresa(@PathVariable("idUsuario") Long idUsuario, Model model) {
        Usuario usuario = usuarioDao.findById(idUsuario).orElse(null);
        Tecnico tecnico = new Tecnico();
        tecnico.setUsuario(usuario);
        model.addAttribute("tecnico", tecnico);
        model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());
        return "tecnicos/tecnicoDatosEmpresa";
    }

    @GetMapping("/editarUsuario/{idUsuario}")
    public String editarUsuario(@PathVariable("idUsuario") Long idUsuario, Model model) {
        Usuario usuario = usuarioDao.findById(idUsuario).orElse(null);
        if (usuario == null) {
            return "redirect:/gestionUsuarios";
        }
        model.addAttribute("usuario", usuario);
        model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());
        return "usuarios/editarUsuario";
    }

    @PostMapping("/guardarUsuarioEditado")
    public String guardarUsuarioEditado(
            @RequestParam("idUsuario") Long idUsuario,
            @RequestParam("username") String username,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "rol", required = false) String[] rolesReq) {

        Usuario usuarioBD = usuarioDao.findById(idUsuario).orElse(null);
        if (usuarioBD == null) {
            return "redirect:/gestionUsuarios";
        }

        usuarioBD.setUsername(username);

        if (password != null && !password.trim().isEmpty()) {
            usuarioBD.setPasswordClaro(password);
            usuarioBD.setPassword(EncriptarPassword.encriptarPassword(password));
        }

        usuarioBD.getRoles().clear();
        if (rolesReq != null) {
            boolean esAdmin = false;
            for (String r : rolesReq) {
                if ("ROLE_ADMIN".equals(r)) {
                    esAdmin = true;
                    break;
                }
            }

            if (esAdmin) {
                String[] todosLosRoles = {"ROLE_ADMIN", "ROLE_MANT", "ROLE_PROD", "ROLE_TECNICO", "ROLE_MONITOR"};
                for (String nombreRol : todosLosRoles) {
                    Rol rObj = new Rol();
                    rObj.setNombre(nombreRol);
                    rObj.setUsuario(usuarioBD);
                    usuarioBD.getRoles().add(rObj);
                }
            } else {
                for (String nombreRol : rolesReq) {
                    Rol rObj = new Rol();
                    rObj.setNombre(nombreRol);
                    rObj.setUsuario(usuarioBD);
                    usuarioBD.getRoles().add(rObj);
                }
            }
        }

        usuarioDao.save(usuarioBD);

        if (usuarioBD.getRoles().stream().anyMatch(rol -> "ROLE_TECNICO".equals(rol.getNombre())) &&
            usuarioBD.getRoles().stream().noneMatch(rol -> "ROLE_ADMIN".equals(rol.getNombre()))) {
            Tecnico tecnico = tecnicoService.traerPorUsuario(usuarioBD, TenantContext.getTenantId());
            if (tecnico != null) {
                tecnico.setUsuario(usuarioBD);
                tecnicoService.save(tecnico);
            }
        }

        return "redirect:/gestionUsuarios";
    }

    @PostMapping("/eliminarUsuario/{idUsuario}")
    public String eliminarUsuario(@PathVariable("idUsuario") Long idUsuario, RedirectAttributes redirectAttributes) {
        return ejecutarEliminacionUsuario(idUsuario, redirectAttributes);
    }

    @GetMapping("/eliminarUsuario/{idUsuario}")
    public String eliminarUsuarioGet(@PathVariable("idUsuario") Long idUsuario, RedirectAttributes redirectAttributes) {
        return ejecutarEliminacionUsuario(idUsuario, redirectAttributes);
    }

    private String ejecutarEliminacionUsuario(Long idUsuario, RedirectAttributes redirectAttributes) {
        if (idUsuario != null) {
            Usuario usuarioReq = usuarioDao.findById(idUsuario).orElse(null);
            if (usuarioReq != null) {
                String usernameActual = SecurityContextHolder.getContext().getAuthentication() != null 
                        ? SecurityContextHolder.getContext().getAuthentication().getName() 
                        : null;

                if (usuarioReq.getUsername().equals(usernameActual)) {
                    if (redirectAttributes != null) {
                        redirectAttributes.addFlashAttribute("mensaje", "No podés eliminar tu propio usuario mientras tenés la sesión activa.");
                    }
                    return "redirect:/gestionUsuarios";
                }

                Tecnico tecnico = tecnicoService.traerPorUsuario(usuarioReq, TenantContext.getTenantId());
                if (tecnico != null) {
                    tecnicoService.delete(tecnico);
                }
                usuarioDao.delete(usuarioReq);
            }
        }
        return "redirect:/gestionUsuarios";
    }
}

