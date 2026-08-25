package mantenimiento.gestorTareas.modulo.informes;
import mantenimiento.gestorTareas.infraestructura.multitenant.TenantContext;
import mantenimiento.gestorTareas.infraestructura.util.ArchivoExterno;
import mantenimiento.gestorTareas.infraestructura.util.Convertidor;
import mantenimiento.gestorTareas.infraestructura.util.TiempoUtils;
import mantenimiento.gestorTareas.modulo.equipos.ActivoDao;
import mantenimiento.gestorTareas.modulo.equipos.ActivoService;
import mantenimiento.gestorTareas.modulo.produccion.ProduccionService;
import mantenimiento.gestorTareas.modulo.produccion.ProductoService;
import mantenimiento.gestorTareas.modulo.tareas.Asignacion;
import mantenimiento.gestorTareas.modulo.tareas.AsignacionService;
import mantenimiento.gestorTareas.modulo.tareas.Servicio;
import mantenimiento.gestorTareas.modulo.tareas.Tarea;
import mantenimiento.gestorTareas.modulo.tareas.TareaService;
import mantenimiento.gestorTareas.modulo.tecnicos.Tecnico;
import mantenimiento.gestorTareas.modulo.tecnicos.TecnicoService;
import mantenimiento.gestorTareas.modulo.usuarios.Rol;
import mantenimiento.gestorTareas.modulo.usuarios.RolDao;
import mantenimiento.gestorTareas.modulo.usuarios.Usuario;
import mantenimiento.gestorTareas.modulo.usuarios.UsuarioDao;
import mantenimiento.gestorTareas.modulo.usuarios.UsuarioService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.time.format.DateTimeFormatter;

@Controller
@Slf4j
public class ControladorInformes {

    @Autowired
    Servicio servicio;
    @Autowired
    UsuarioService usuarioService;
    @Autowired
    UsuarioDao usuarioDao;
    @Autowired
    RolDao rolDao;
    @Autowired
    ActivoDao activo;
    @Autowired
    ActivoService activoService;
    @Autowired
    TareaService tareaService;
    @Autowired
    TecnicoService tecnicoService;
    @Autowired
    AsignacionService asignacionService;
    @Autowired
    ProduccionService produccionService;
    @Autowired
    ProductoService productoService;
    @Autowired
    InformeService informeService;

    
    @GetMapping("/informes")
    public String informes(Model model)
    {
        Usuario usuario = usuarioDao.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        String rol = "";
        for (Rol role : usuario.getRoles()) {
            if (role.getNombre().equals("ROLE_TECNICO")) rol = "tecnico";
            if (role.getNombre().equals("ROLE_MANT")) rol = "mant";
            if (role.getNombre().equals("ROLE_PROD")) rol = "prod";
            if (role.getNombre().equals("ROLE_MONITOR")) rol = "monitor";
            if (role.getNombre().equals("ROLE_ADMIN")) {
                rol = "admin";
                break;
            }
        }
        
        if (rol.equals("tecnico")) {
            Tecnico tecnico = tecnicoService.traerPorUsuario(usuario, TenantContext.getTenantId());
            List<Tarea> tareasNoEvaluadas = tareaService.traerPorTecnicoYEstadoInforme(tecnico, "noEvaluado", TiempoUtils.haceAnios(1), TiempoUtils.ahora(), TenantContext.getTenantId());
            model.addAttribute("tareasNoEvaluadas", tareasNoEvaluadas);
            List<Tarea> tareasInformePendienteTecnico = tareaService.traerPorTecnicoYEstadoInforme(tecnico, "pendiente", TiempoUtils.haceAnios(1), TiempoUtils.ahora(), TenantContext.getTenantId());
            List<Tarea> tareasInformeEnRevisionTecnico = tareaService.traerPorTecnicoYEstadoInforme(tecnico, "EnRevision", TiempoUtils.haceAnios(1), TiempoUtils.ahora(), TenantContext.getTenantId());
            tareasInformePendienteTecnico.addAll(tareasInformeEnRevisionTecnico);
            model.addAttribute("tareasInformePendienteTecnico", tareasInformePendienteTecnico);
            List<Tarea> tareasAprobados = tareaService.traerPorEstadoInforme("aprobado", TiempoUtils.haceAnios(1), TiempoUtils.ahora(), TenantContext.getTenantId());
            model.addAttribute("tareasAprobados", tareasAprobados);
        } else if (rol.equals("mant") || rol.equals("admin")) {
            List<Tarea> tareasNoEvaluadas = tareaService.traerPorEstadoInforme("noEvaluado", TiempoUtils.haceAnios(1), TiempoUtils.ahora(), TenantContext.getTenantId());
            model.addAttribute("tareasNoEvaluadas", tareasNoEvaluadas);
            List<Tarea> tareasNoAprobadas = tareaService.traerPorEstadoInforme("noAprobado", TiempoUtils.haceAnios(1), TiempoUtils.ahora(), TenantContext.getTenantId());
            model.addAttribute("tareasNoAprobadas", tareasNoAprobadas);
            List<Tarea> tareasAprobados = tareaService.traerPorEstadoInforme("aprobado", TiempoUtils.haceAnios(1), TiempoUtils.ahora(), TenantContext.getTenantId());
            model.addAttribute("tareasAprobados", tareasAprobados);
            List<Tarea> tareasInformePendiente = tareaService.traerPorEstadoInforme("pendiente", TiempoUtils.haceAnios(1), TiempoUtils.ahora(), TenantContext.getTenantId());
            List<Tarea> tareasInformeEnRevision = tareaService.traerPorEstadoInforme("EnRevision", TiempoUtils.haceAnios(1), TiempoUtils.ahora(), TenantContext.getTenantId());
            tareasInformePendiente.addAll(tareasInformeEnRevision);
            model.addAttribute("tareasInformePendiente", tareasInformePendiente);
        } else {
            List<Tarea> tareasAprobados = tareaService.traerPorEstadoInforme("aprobado", TiempoUtils.haceAnios(1), TiempoUtils.ahora(), TenantContext.getTenantId());
            model.addAttribute("tareasAprobados", tareasAprobados);
        }
        
        model.addAttribute("todosLosTecnicos", tecnicoService.findAllByTenant());
        model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());
        //DMS para el menú
        List<Tecnico> tecnicosFiltrados = tecnicoService.traerHabilitados(TenantContext.getTenantId()).stream()
                .filter(t -> t.getUsuario().getRoles().get(0).getNombre().equals("ROLE_TECNICO"))
                .collect(Collectors.toList());
        model.addAttribute("tecnicos", tecnicosFiltrados);

        model.addAttribute("habilitarGestionUsuarios", ArchivoExterno.getString("editarUsuarios"));
        model.addAttribute("habilitarEditorLayout", ArchivoExterno.getString("editorLayout"));
        model.addAttribute("tiempoRefresco", ArchivoExterno.getString("tiempoRefresco"));
        return "informes/informes";
    }

    @GetMapping("/generarInforme/{id}")
    public String generarInforme(Model model, @PathVariable("id") Long id) {
        Tarea tareaBD = tareaService.findById(id).orElse(null);
        model.addAttribute("tarea", tareaBD);
        model.addAttribute("informe", tareaBD != null ? tareaBD.getInforme() : null);
        model.addAttribute("todosLosTecnicos", tecnicoService.findAllByTenant());

        String diferenciaFormateada = "00:00:00";
        if (tareaBD != null && tareaBD.getMomentoDetencion() != null && tareaBD.getMomentoLiberacion() != null) {
            Duration duracion = Duration.between(tareaBD.getMomentoDetencion(), tareaBD.getMomentoLiberacion());
            diferenciaFormateada = String.format("%02d:%02d:%02d",
                duracion.toHours(),
                duracion.toMinutesPart(),
                duracion.toSecondsPart()
            );
        }
        model.addAttribute("tiempoDetenido", diferenciaFormateada);
        model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());

        //DMS para el menú
        List<Tecnico> tecnicosFiltrados = tecnicoService.traerHabilitados(TenantContext.getTenantId()).stream()
                .filter(t -> t.getUsuario().getRoles().get(0).getNombre().equals("ROLE_TECNICO"))
                .collect(Collectors.toList());
        model.addAttribute("tecnicos", tecnicosFiltrados);

        model.addAttribute("habilitarGestionUsuarios", ArchivoExterno.getString("editarUsuarios"));
        model.addAttribute("habilitarEditorLayout", ArchivoExterno.getString("editorLayout"));
        model.addAttribute("tiempoRefresco", ArchivoExterno.getString("tiempoRefresco"));

        return "informes/informe";
    }

    @GetMapping("/seleccionarTareaParaInforme/{id}")
    public String seleccionarTareaParaInforme(Model model, @PathVariable("id") Long id) {
        Tarea tareaBD = tareaService.findById(id).orElse(null);
        if (tareaBD != null && tareaBD.getInforme() != null) {
            tareaBD.getInforme().setEstadoInforme("pendiente");
            informeService.save(tareaBD.getInforme());
        }
        model.addAttribute("tarea", tareaBD);
        return informes(model);
    }

    @GetMapping("/descartarTareaParaInforme/{id}")
    public String descartarTareaParaInforme(Model model, @PathVariable("id") Long id) {
        Tarea tareaBD = tareaService.findById(id).orElse(null);
        if (tareaBD != null && tareaBD.getInforme() != null) {
            tareaBD.getInforme().setEstadoInforme("descartado");
            informeService.save(tareaBD.getInforme());
        }
        model.addAttribute("tarea", tareaBD);
        return informes(model);
    }

    @PostMapping("/guardarInforme/{id}")
    public String guardar(Model model, @Param("url") String url, Informe informe) {
        Usuario usuario = usuarioDao.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        Tecnico tecnicoGenerador = tecnicoService.traerPorUsuario(usuario, TenantContext.getTenantId());
        
        informe.setEstadoInforme("noAprobado");
        informeService.save(informe);
        
        return "redirect:/informes";
    }

    @GetMapping("/verInforme/{id}")
    public String verInforme(Model model, @PathVariable("id") Long id) {
        Tarea tareaBD = tareaService.findById(id).orElse(null);
        model.addAttribute("tarea", tareaBD);
        model.addAttribute("informe", tareaBD != null ? tareaBD.getInforme() : null);
        model.addAttribute("todosLosTecnicos", tecnicoService.findAllByTenant());

        String diferenciaFormateada = "00:00:00";
        if (tareaBD != null && tareaBD.getMomentoDetencion() != null && tareaBD.getMomentoLiberacion() != null) {
            Duration duracion = Duration.between(tareaBD.getMomentoDetencion(), tareaBD.getMomentoLiberacion());
            diferenciaFormateada = String.format("%02d:%02d:%02d",
                duracion.toHours(),
                duracion.toMinutesPart(),
                duracion.toSecondsPart()
            );
        }
        model.addAttribute("tiempoDetenido", diferenciaFormateada);
        model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());

        //DMS para el menú
        List<Tecnico> tecnicosFiltrados = tecnicoService.traerHabilitados(TenantContext.getTenantId()).stream()
                .filter(t -> t.getUsuario().getRoles().get(0).getNombre().equals("ROLE_TECNICO"))
                .collect(Collectors.toList());
        model.addAttribute("tecnicos", tecnicosFiltrados);

        model.addAttribute("habilitarGestionUsuarios", ArchivoExterno.getString("editarUsuarios"));
        model.addAttribute("habilitarEditorLayout", ArchivoExterno.getString("editorLayout"));
        model.addAttribute("tiempoRefresco", ArchivoExterno.getString("tiempoRefresco"));

        return "informes/informe";
    }

    @GetMapping("/validar/{id}")
    public String validar(Model model, @PathVariable("id") Long id) {
        Tarea tareaBD = tareaService.findById(id).orElse(null);
        if (tareaBD != null && tareaBD.getInforme() != null) {
            Informe informe = tareaBD.getInforme();
            informe.setEstadoInforme("aprobado");
            informe.setFechaDeCreacion(TiempoUtils.ahora());

            if (tareaBD.getAsignaciones() != null) {
                for (Asignacion asignacionTarea : tareaBD.getAsignaciones()) {
                    AsignacionInforme asignacion = new AsignacionInforme();
                    Tecnico tec = tecnicoService.findById(asignacionTarea.getTecnico().getId()).orElse(null);
                    asignacion.setInforme(informe);
                    asignacion.setTecnico(tec);
                    informe.getAsignaciones().add(asignacion);
                }
            }
            informeService.save(informe);
        }
        return informes(model);
    }

    @PostMapping("/enviarARevision/{id}")
    public String enviarARevision(Model model, @Param("url") String url, Informe informe) {
        Informe informeBd = informeService.findById(informe.getId()).orElse(null);
        if (informeBd != null) {
            informeBd.setRevision(informe.getRevision());
            informeBd.setEstadoInforme("enRevision");
            informeService.save(informeBd);
        }
        return "redirect:/informes";
    }

    @RequestMapping(value = "/api/informes/seleccionar/{id}", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public ResponseEntity<?> seleccionarTareaAjax(@PathVariable("id") Long id) {
        Tarea tareaBD = tareaService.findById(id).orElse(null);
        if (tareaBD == null) {
            return ResponseEntity.badRequest().body("Tarea no encontrada");
        }
        tareaBD.getInforme().setEstadoInforme("pendiente");
        informeService.save(tareaBD.getInforme());

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("id", id);
        resp.put("activoNombre", tareaBD.getActivo() != null ? tareaBD.getActivo().getNombre() : "");
        resp.put("descripcion", tareaBD.getDescripcion() != null ? tareaBD.getDescripcion() : "");
        resp.put("estadoInforme", tareaBD.getInforme().getEstadoInforme());
        
        String fecha = "";
        String hora = "";
        if (tareaBD.getMomentoLiberacion() != null) {
            fecha = tareaBD.getMomentoLiberacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            hora = tareaBD.getMomentoLiberacion().format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        resp.put("fecha", fecha);
        resp.put("hora", hora);

        List<String> tecnicosNombres = new ArrayList<>();
        boolean esTecnicoAsignado = false;
        Usuario usuario = usuarioDao.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        Tecnico tecUsuario = tecnicoService.traerPorUsuario(usuario, TenantContext.getTenantId());

        if (tareaBD.getAsignaciones() != null) {
            for (Asignacion asig : tareaBD.getAsignaciones()) {
                if (asig.getTecnico() != null) {
                    tecnicosNombres.add(asig.getTecnico().getApellido() + " " + asig.getTecnico().getNombre());
                    if (tecUsuario != null && asig.getTecnico().getId().equals(tecUsuario.getId())) {
                        esTecnicoAsignado = true;
                    }
                }
            }
        }
        resp.put("tecnicos", tecnicosNombres);
        resp.put("esTecnicoAsignado", esTecnicoAsignado);

        return ResponseEntity.ok(resp);
    }

    @RequestMapping(value = "/api/informes/descartar/{id}", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public ResponseEntity<?> descartarTareaAjax(@PathVariable("id") Long id) {
        Tarea tareaBD = tareaService.findById(id).orElse(null);
        if (tareaBD == null) {
            return ResponseEntity.badRequest().body("Tarea no encontrada");
        }
        tareaBD.getInforme().setEstadoInforme("descartado");
        informeService.save(tareaBD.getInforme());

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("id", id);
        return ResponseEntity.ok(resp);
    }

    @RequestMapping(value = "/api/informes/validar/{id}", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public ResponseEntity<?> validarInformeAjax(@PathVariable("id") Long id) {
        Tarea tareaBD = tareaService.findById(id).orElse(null);
        if (tareaBD == null) {
            return ResponseEntity.badRequest().body("Tarea no encontrada");
        }
        Informe informe = tareaBD.getInforme();
        informe.setEstadoInforme("aprobado");
        informe.setFechaDeCreacion(TiempoUtils.ahora());

        if (tareaBD.getAsignaciones() != null) {
            for (Asignacion asignacionTarea : tareaBD.getAsignaciones()) {
                AsignacionInforme asignacion = new AsignacionInforme();
                Tecnico tec = tecnicoService.findById(asignacionTarea.getTecnico().getId()).orElse(null);
                asignacion.setInforme(informe);
                asignacion.setTecnico(tec);
                informe.getAsignaciones().add(asignacion);
            }
        }
        informeService.save(informe);

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("id", id);
        resp.put("activoNombre", tareaBD.getActivo() != null ? tareaBD.getActivo().getNombre() : "");
        resp.put("descripcion", tareaBD.getDescripcion() != null ? tareaBD.getDescripcion() : "");
        
        String fecha = "";
        String hora = "";
        if (tareaBD.getMomentoLiberacion() != null) {
            fecha = tareaBD.getMomentoLiberacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            hora = tareaBD.getMomentoLiberacion().format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        resp.put("fecha", fecha);
        resp.put("hora", hora);

        return ResponseEntity.ok(resp);
    }

}
