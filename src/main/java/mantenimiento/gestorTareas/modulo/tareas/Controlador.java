package mantenimiento.gestorTareas.modulo.tareas;
import mantenimiento.gestorTareas.infraestructura.multitenant.Tenant;
import mantenimiento.gestorTareas.infraestructura.multitenant.TenantContext;
import mantenimiento.gestorTareas.infraestructura.util.ArchivoExterno;
import mantenimiento.gestorTareas.infraestructura.util.Convertidor;
import mantenimiento.gestorTareas.infraestructura.util.TiempoUtils;
import mantenimiento.gestorTareas.modulo.equipos.Activo;
import mantenimiento.gestorTareas.modulo.equipos.ActivoDao;
import mantenimiento.gestorTareas.modulo.equipos.ActivoService;
import mantenimiento.gestorTareas.modulo.equipos.PreventivoService;
import mantenimiento.gestorTareas.modulo.informes.Evaluacion;
import mantenimiento.gestorTareas.modulo.informes.Informe;
import mantenimiento.gestorTareas.modulo.informes.InformeService;
import mantenimiento.gestorTareas.modulo.produccion.Produccion;
import mantenimiento.gestorTareas.modulo.produccion.ProduccionService;
import mantenimiento.gestorTareas.modulo.tecnicos.Tecnico;
import mantenimiento.gestorTareas.modulo.tecnicos.TecnicoService;
import mantenimiento.gestorTareas.modulo.usuarios.Rol;
import mantenimiento.gestorTareas.modulo.usuarios.RolDao;
import mantenimiento.gestorTareas.modulo.usuarios.Usuario;
import mantenimiento.gestorTareas.modulo.usuarios.UsuarioDao;
import mantenimiento.gestorTareas.modulo.usuarios.UsuarioService;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.ServletContext;
import javax.validation.Valid;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@Controller
@Slf4j
public class Controlador {

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
    ActivoDao activoDao;
    @Autowired
    TareaService tareaService;
    @Autowired
    TecnicoService tecnicoService;
    @Autowired
    AsignacionService asignacionService;
    @Autowired
    ProduccionService produccionService;
    @Autowired
    PreventivoService preventivoService;
    @Autowired
    InformeService informeService;
    @Autowired
    private ResourceLoader resourceLoader;
    @Autowired
    private ServletContext servletContext;

    @GetMapping("/")
    public String inicio(Model model) throws IOException {
        // si el usuario logueado es un técnico y el apellido es null significa que fue
        // recien creado
        // por lo tanto lo redirijo a tecnicoDatosPersonales para que cargue su
        // informacion;
        String nombreUsuario = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioDao.findByUsername(nombreUsuario);
        if (usuario.getRoles().get(0).getNombre().equals("ROLE_TECNICO")) {

            Tecnico tecnico = tecnicoService.traerPorUsuario(usuario, TenantContext.getTenantId());

            if (tecnico.getApellido() == null) {
                model.addAttribute("tecnico", tecnico);
                model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());
                return "tecnicos/tecnicoDatosPersonales";
            }
        }
        Boolean admin = false;
        for (Rol rolUsuario : usuario.getRoles()) {
            if (rolUsuario.getNombre().equals("ROLE_ADMIN"))
                admin = true;
        }
        if (!admin)
            return "redirect:/layout";

        // DMS si ya se generó el svg salto al layout. Si quiero volver tengo que
        // redireccionar a "/"

        Path layoutDir = Path.of(ArchivoExterno.getLayoutPath());

        boolean existeLayout = false;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(layoutDir,
                "*Tenant" + TenantContext.getTenantId() + ".svg")) {
            existeLayout = stream.iterator().hasNext();
        } catch (IOException e) {
            log.error("Error buscando layouts: " + e.getMessage());
        }

        model.addAttribute("existeLayout", existeLayout);

        if (existeLayout)
            return "redirect:/layout";
        model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());
        model.addAttribute("noTraerEstados", "no");
        return "tareas/crearLayout";

    }

    @GetMapping("/recargar-config")
    public String recargar() {
        log.info("configuracion cargada ok");
        ArchivoExterno.recargar();

        return "redirect:/";
    }

    @GetMapping("/crearLayout")
    public String crearLayout(Model model) throws IOException {
        if (!ArchivoExterno.getString("editorLayout").equals("si"))
            return "redirect:/layout";

        model.addAttribute("existeLayout", !ArchivoExterno.nombresLayouts().isEmpty());
        model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());
        model.addAttribute("noTraerEstados", "no");

        return "tareas/crearLayout";

    }

    @PostMapping("/eliminarLayout/{nombre}")
    public String eliminarLayout(@PathVariable String nombre) {
        try {

            nombre = nombre.substring(0, nombre.length() - 4);
            Path path = Paths.get(ArchivoExterno.getLayoutPath(),
                    nombre + "Tenant" + TenantContext.getTenantId() + ".svg");

            Files.deleteIfExists(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "redirect:/crearLayout";
    }

    @GetMapping("/tareas")
    public String tareas(Model model) {
        var tareas = tareaService.traerNoCerradas(TiempoUtils.haceAnios(1), TiempoUtils.ahora(),
                TenantContext.getTenantId());
        log.info("cantidad: " + tareas.size());
        model.addAttribute("tareas", tareas);
        model.addAttribute("todosLosTecnicos", tecnicoService.findAllByTenant());
        model.addAttribute("cantidadActivosDetenidos", activoService.findByStatus("detenida").size());

        model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());
        // DMS para el menú
        List<Tecnico> tecnicosFiltrados = tecnicoService.traerHabilitados(TenantContext.getTenantId()).stream()
                .filter(t -> t.getUsuario().getRoles().get(0).getNombre().equals("ROLE_TECNICO"))
                .collect(Collectors.toList());
        model.addAttribute("tecnicos", tecnicosFiltrados);

        model.addAttribute("habilitarGestionUsuarios", ArchivoExterno.getString("editarUsuarios"));
        model.addAttribute("habilitarEditorLayout", ArchivoExterno.getString("editorLayout"));
        model.addAttribute("tiempoRefresco", ArchivoExterno.getString("tiempoRefresco"));
        return "tareas/tareas";
    }

    @GetMapping("/layout")
    public String layoutDefault(Model model) throws IOException {
        cargarDatosGenerales(model);
        Path carpeta = Path.of(ArchivoExterno.getLayoutPath());
        List<String> nombresLayouts = new ArrayList<>();
        String svgContent = "";
        if (Files.exists(carpeta) && Files.isDirectory(carpeta)) {
            List<Path> archivosSvg = Files.list(carpeta)
                    .filter(p -> p.toString().endsWith("Tenant" + TenantContext.getTenantId() + ".svg"))
                    .sorted(Comparator.comparingLong(p -> p.toFile().lastModified()))
                    .collect(Collectors.toList());

            if (!archivosSvg.isEmpty()) {
                Path primero = archivosSvg.get(0);
                svgContent = Files.readString(primero);
                nombresLayouts = archivosSvg.stream().map(p -> p.getFileName().toString()).collect(Collectors.toList());
            } else {
                svgContent = "No se encontraron archivos SVG en la carpeta.";
            }
        }
        model.addAttribute("svgContent", svgContent);
        model.addAttribute("existeLayout", !ArchivoExterno.nombresLayouts().isEmpty());
        model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());
        return "tareas/layout";
    }

    @GetMapping("/layout/{nombreArchivo}")
    public String layoutPorNombre(@PathVariable String nombreArchivo, Model model) throws IOException {
        cargarDatosGenerales(model);

        Path carpeta = Path.of(ArchivoExterno.getLayoutPath());
        nombreArchivo = nombreArchivo.substring(0, nombreArchivo.length() - 4);

        Path archivo = carpeta.resolve(nombreArchivo + "Tenant" + TenantContext.getTenantId() + ".svg");
        String svgContent;
        if (Files.exists(archivo)) {
            svgContent = Files.readString(archivo);
        } else {
            svgContent = "Archivo no encontrado: " + nombreArchivo;
        }

        model.addAttribute("svgContent", svgContent);
        model.addAttribute("existeLayout", !ArchivoExterno.nombresLayouts().isEmpty());
        model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());
        return "tareas/layout";
    }

    private void cargarDatosGenerales(Model model) {
        List<Activo> activos = activo.findAllByTenant();
        for (Activo activo : activos) {
            if (activo.getDisponibilidadHasta() != null && activo.getEstado().equals("disponible"))
                if (TiempoUtils.ahora().isAfter(activo.getDisponibilidadHasta())) {
                    activo.setEstado("operativa");
                    activoService.save(activo);
                    Tarea tarea = tareaService.traerDisponiblePorActivo(activo, TenantContext.getTenantId()).get(0);
                    tarea.setEstado("finDisponible");
                    tareaService.save(tarea);
                }
        }

        List<Tecnico> tecnicosFiltrados = tecnicoService.traerHabilitados(TenantContext.getTenantId()).stream()
                .filter(t -> t.getUsuario().getRoles().get(0).getNombre().equals("ROLE_TECNICO"))
                .collect(Collectors.toList());
        model.addAttribute("tecnicos", tecnicosFiltrados);

        List<Produccion> oTs = produccionService.traerAbiertas(TenantContext.getTenantId());
        if (oTs != null)
            model.addAttribute("oTs", oTs);

        model.addAttribute("cantidadActivosDetenidos", activoService.findByStatus("detenida").size());
        model.addAttribute("preventivosNoValidados",
                preventivoService.traerPreventivosNoValidados(TenantContext.getTenantId()));

        List<Tarea> tareasNoEvaluadas = tareaService.traerPorEstadoInforme("noEvaluado", TiempoUtils.haceAnios(1),
                TiempoUtils.ahora(), TenantContext.getTenantId());
        tareasNoEvaluadas.addAll(tareaService.traerPorEstadoInforme("noAprobado", TiempoUtils.haceAnios(1),
                TiempoUtils.ahora(), TenantContext.getTenantId()));
        List<Informe> informesSupervisor = tareasNoEvaluadas.stream().map(Tarea::getInforme)
                .collect(Collectors.toList());
        model.addAttribute("informesPendientesSupervisor", informesSupervisor);

        String nombreUsuario = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioDao.findByUsername(nombreUsuario);
        if (usuario.getRoles().get(0).getNombre().equals("ROLE_TECNICO")) {
            Tecnico tecnico = tecnicoService.traerPorUsuario(usuario, TenantContext.getTenantId());
            List<Tarea> pendientes = tareaService.traerPorTecnicoYEstadoInforme(tecnico, "pendiente",
                    TiempoUtils.haceAnios(1), TiempoUtils.ahora(), TenantContext.getTenantId());
            pendientes.addAll(tareaService.traerPorTecnicoYEstadoInforme(tecnico, "EnRevision",
                    TiempoUtils.haceAnios(1), TiempoUtils.ahora(), TenantContext.getTenantId()));
            model.addAttribute("tareasConInformesPendientesTecnico", pendientes);
        }

        model.addAttribute("habilitarGestionUsuarios", ArchivoExterno.getString("editarUsuarios"));
        model.addAttribute("habilitarEditorLayout", ArchivoExterno.getString("editorLayout"));
        model.addAttribute("tiempoRefresco", ArchivoExterno.getString("tiempoRefresco"));
    }

    @GetMapping("/crearTarea/{id}")
    public String modificar(Model model, Tarea tarea, Activo activoRequest) {

        Activo activoSeleccionado = activo.findById(activoRequest.getId()).orElse(null);
        tarea.setActivo(activoSeleccionado);
        tarea.setAfectaProduccion("si");
        model.addAttribute("tarea", tarea);
        model.addAttribute("activos", activo.findAllByTenant());
        model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());
        // DMS para el menú
        List<Tecnico> tecnicosFiltrados = tecnicoService.traerHabilitados(TenantContext.getTenantId()).stream()
                .filter(t -> t.getUsuario().getRoles().get(0).getNombre().equals("ROLE_TECNICO"))
                .collect(Collectors.toList());
        model.addAttribute("tecnicos", tecnicosFiltrados);

        model.addAttribute("habilitarGestionUsuarios", ArchivoExterno.getString("editarUsuarios"));
        model.addAttribute("habilitarEditorLayout", ArchivoExterno.getString("editorLayout"));
        model.addAttribute("tiempoRefresco", ArchivoExterno.getString("tiempoRefresco"));

        return "tareas/crearTarea";
    }

    @PostMapping("/guardar")
    public String guardar(Model model, @Valid Tarea tarea, Errors errores, @RequestParam("file") MultipartFile imagen,
            @RequestParam(value = "activo", required = false) String activoReq) {

        if (errores.hasErrors()) {
            model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());
            return "tareas/crearTarea";
        }
        if (!imagen.isEmpty()) {
            // Path directorioImagenes =
            // Paths.get("src//main//resources//static//imagenes");
            // String ruta = directorioImagenes.toFile().getAbsolutePath();
            // voy a usar un directorio no relativo para evitar la necesidad de actualizar
            // cada vez que se agrega una imagen nueva
            String ruta = ArchivoExterno.getBasePath();
            try {
                byte[] bytes = imagen.getBytes();
                Path rutaCompleta = Paths.get(ruta + "//" + imagen.getOriginalFilename());
                Files.write(rutaCompleta, bytes);
                tarea.setImagen(imagen.getOriginalFilename());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Authentication aut = SecurityContextHolder.getContext().getAuthentication();
        tarea.setSolicita(aut.getName());
        // al solicitar la tarea pasa a estado abierto automaticamente
        tarea.setEstado("abierto");
        // se guarda el momento de la solicitud para calcular el tiempo de parada
        tarea.getActivo().setMomentoDetencion(TiempoUtils.ahora());
        tarea.setMomentoDetencion(TiempoUtils.ahora());
        if (tarea.getAfectaProduccion().equals("no"))
            tarea.getActivo().setEstado("operativa condicionada");
        else
            tarea.getActivo().setEstado("detenida");

        // si no es de mantenimiento no genero informe
        if (tarea.getDepartamentoResponsable().equals("mantenimiento")) {
            Informe informe = new Informe();
            informe.setEstadoInforme("noEvaluado");
            tarea.setInforme(informe);

        }
        activoService.save(tarea.getActivo());

        servicio.guardar(tarea);
        model.addAttribute("tareas", tareaService.traerNoCerradas(TiempoUtils.haceAnios(1), TiempoUtils.ahora(),
                TenantContext.getTenantId()));
        String url = activoDao.findById(Long.parseLong(activoReq)).orElse(null).getNombre();
        if (activoReq != null) {
            return "redirect:/activo/" + Convertidor.aCamelCase(url);
        }
        return "redirect:/tareas";
    }

    @GetMapping("/editar/{id}")
    public String editar(Tarea tarea, Model model) {
        model.addAttribute("activos", activo.findAllByTenant());
        model.addAttribute("estados", Arrays.asList("detenida", "operativa", "disponible"));
        model.addAttribute("estadosTareas", Arrays.asList("abierto", "enProceso", "liberada", "cerrada"));
        model.addAttribute("tarea", servicio.encontrar(tarea));
        model.addAttribute("todosLosTecnicos", tecnicoService.findAllByTenant());
        // model.addAttribute("asignacion", asignacionService.traerPorTarea(tarea));

        // Crear lista de IDs de técnicos asignados
        Tarea t = servicio.encontrar(tarea);
        List<Long> idsTecnicosAsignados = t.getAsignaciones().stream()
                .map(asignacion -> asignacion.getTecnico().getId())
                .collect(Collectors.toList());

        model.addAttribute("idsTecnicosAsignados", idsTecnicosAsignados);
        model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());
        return "tareas/modificar";
    }

    @PostMapping("/guardarEdicion")
    public String guardarEdicion(Model model, @Valid Tarea tarea,
            @RequestParam(value = "tecnicosIds", required = false) List<Long> tecnicosIds) {

        List<Asignacion> asignaciones = asignacionService.traerPorTarea(tarea, TenantContext.getTenantId());

        if (tecnicosIds != null) {
            for (Asignacion asignacion : asignaciones) {
                asignacionService.delete(asignacion);
            }

            for (Long idTecnico : tecnicosIds) {
                Tecnico tecnico = tecnicoService.getById(idTecnico);
                Asignacion asignacion = new Asignacion();
                asignacion.setTecnico(tecnico);
                asignacion.setTarea(tarea);
                asignacionService.save(asignacion);
            }
        }

        servicio.guardar(tarea);

        model.addAttribute("tareas", tareaService.traerNoCerradas(TiempoUtils.haceAnios(1), TiempoUtils.ahora(),
                TenantContext.getTenantId()));
        return "redirect:/tareas";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(Model model, Tarea tarea) {

        servicio.eliminar(tarea);
        model.addAttribute("tareas", tareaService.traerNoCerradas(TiempoUtils.haceAnios(1), TiempoUtils.ahora(),
                TenantContext.getTenantId()));
        return "redirect:/tareas";
    }

    @GetMapping("/liberarSolicitud/{id}")
    public String liberar(@RequestHeader(value = "Referer", required = false) String origen, Model model, Tarea tarea) {
        Tarea t = servicio.encontrar(tarea);
        servicio.liberarSolicitud(t);

        if (origen != null && origen.contains(Convertidor.aCamelCase(t.getActivo().getNombre()))) {
            String url = activoDao.findById(t.getActivo().getId()).orElse(null).getNombre();
            return "redirect:/activo/" + Convertidor.aCamelCase(url);
        }

        model.addAttribute("tareas", tareaService.traerNoCerradas(TiempoUtils.haceAnios(1), TiempoUtils.ahora(),
                TenantContext.getTenantId()));
        return "redirect:/tareas";
    }

    @GetMapping("/asignarSolicitud/{id}")
    public String asignar(
            @RequestParam("motivoDemoraAsignacion") String motivoDemoraAsignacion,
            @RequestParam(value = "activoReq", required = false) String activoReq,
            @RequestParam(value = "tecnicosIds", required = false) List<Long> tecnicosIds,
            Model model, Tarea tarea) {

        servicio.asignarSolicitud(tarea, tecnicosIds, motivoDemoraAsignacion);

        model.addAttribute("tareas", tareaService.traerNoCerradas(TiempoUtils.haceAnios(1), TiempoUtils.ahora(),
                TenantContext.getTenantId()));
        Tarea t = servicio.encontrar(tarea);
        model.addAttribute("tarea", t);

        if (activoReq != null) {
            String url = activoDao.findById(Long.parseLong(activoReq)).orElse(null).getNombre();
            return "redirect:/activo/" + Convertidor.aCamelCase(url);
        }
        return "redirect:/tareas";
    }

    @GetMapping("/CerrarSolicitud/{id}")
    public String CerrarSolicitud(
            @RequestParam(required = false, name = "satisfaccion") String satisfaccion,
            @RequestParam(required = false, name = "predisposicion") String predisposicion,
            @RequestParam(required = false, name = "responsabilidad") String responsabilidad,
            @RequestParam(required = false, name = "seguridad") String seguridad,
            @RequestParam(required = false, name = "conocimiento") String conocimiento,
            @RequestParam(required = false, name = "trato") String trato,
            @RequestParam(required = false, name = "prolijidad") String prolijidad,
            @RequestParam(required = false, name = "puntualidad") String puntualidad,
            @RequestParam(required = false, name = "eficiencia") String eficiencia,
            @RequestParam(required = false, name = "calidad") String calidad,
            @RequestParam(required = false, name = "comunicacion") String comunicacion,
            @RequestParam(required = false, name = "trabajoEnEquipo") String trabajoEnEquipo,
            @RequestParam(required = false, name = "resolucion") String resolucion,
            @RequestParam(required = false, name = "creatividad") String creatividad,
            @RequestParam(required = false, name = "iniciativa") String iniciativa,
            @RequestParam(required = false, name = "autogestion") String autogestion,
            @RequestParam(required = false, name = "formacionContinua") String formacionContinua,
            @RequestHeader(value = "Referer", required = false) String origen,
            Model model, Tarea tarea) {

        Evaluacion evaluacion = new Evaluacion();
        evaluacion.setSatisfaccion(satisfaccion);
        evaluacion.setPredisposicion(predisposicion);
        evaluacion.setResponsabilidad(responsabilidad);
        evaluacion.setSeguridad(seguridad);
        evaluacion.setConocimiento(conocimiento);
        evaluacion.setTrato(trato);
        evaluacion.setProlijidad(prolijidad);
        evaluacion.setPuntualidad(puntualidad);
        evaluacion.setEficiencia(eficiencia);
        evaluacion.setCalidad(calidad);
        evaluacion.setComunicacion(comunicacion);
        evaluacion.setTrabajoEnEquipo(trabajoEnEquipo);
        evaluacion.setResolucion(resolucion);
        evaluacion.setCreatividad(creatividad);
        evaluacion.setIniciativa(iniciativa);
        evaluacion.setAutogestion(autogestion);
        evaluacion.setFormacionContinua(formacionContinua);

        servicio.cerrarSolicitud(tarea, evaluacion);
        Tarea t = servicio.encontrar(tarea);

        if (origen != null && origen.contains(Convertidor.aCamelCase(t.getActivo().getNombre()))) {
            String url = activoDao.findById(t.getActivo().getId()).orElse(null).getNombre();
            return "redirect:/activo/" + Convertidor.aCamelCase(url);
        }

        model.addAttribute("tareas", tareaService.traerNoCerradas(TiempoUtils.haceAnios(1), TiempoUtils.ahora(),
                TenantContext.getTenantId()));
        return "redirect:/tareas";
    }

    @GetMapping("/registro")
    public String registroHistorico(Model model) {

        model.addAttribute("tareas",
                tareaService.traerCerradas(TiempoUtils.haceAnios(1), TiempoUtils.ahora(), TenantContext.getTenantId()));
        model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());
        // DMS para el menú
        List<Tecnico> tecnicosFiltrados = tecnicoService.traerHabilitados(TenantContext.getTenantId()).stream()
                .filter(t -> t.getUsuario().getRoles().get(0).getNombre().equals("ROLE_TECNICO"))
                .collect(Collectors.toList());
        model.addAttribute("tecnicos", tecnicosFiltrados);

        model.addAttribute("habilitarGestionUsuarios", ArchivoExterno.getString("editarUsuarios"));
        model.addAttribute("habilitarEditorLayout", ArchivoExterno.getString("editorLayout"));
        model.addAttribute("tiempoRefresco", ArchivoExterno.getString("tiempoRefresco"));
        return "suscripcion/registro";
    }

    @GetMapping("/registroActivo/{id}")
    public String registroHistoricoActivo(Model model, Activo activo) {

        // List<Tarea> tareas = tareaService.traerCerradas(TiempoUtils.haceAnios(1),
        // TiempoUtils.ahora(),TenantContext.getTenantId());
        model.addAttribute("url", Convertidor.aCamelCase(activoDao.findById(activo.getId()).orElse(null).getNombre()));
        model.addAttribute("tareas", tareaService.traerCerradasPorActivo(activo, TiempoUtils.haceAnios(1),
                TiempoUtils.ahora(), TenantContext.getTenantId()));
        model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());
        // DMS para el menú
        List<Tecnico> tecnicosFiltrados = tecnicoService.traerHabilitados(TenantContext.getTenantId()).stream()
                .filter(t -> t.getUsuario().getRoles().get(0).getNombre().equals("ROLE_TECNICO"))
                .collect(Collectors.toList());
        model.addAttribute("tecnicos", tecnicosFiltrados);

        model.addAttribute("habilitarGestionUsuarios", ArchivoExterno.getString("editarUsuarios"));
        model.addAttribute("habilitarEditorLayout", ArchivoExterno.getString("editorLayout"));
        model.addAttribute("tiempoRefresco", ArchivoExterno.getString("tiempoRefresco"));

        return "suscripcion/registro";
    }

    // @CrossOrigin(origins = "*")
    @PostMapping(value = "/guardarSvg", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> saveSvg(
            @RequestParam("svg") MultipartFile svgFile,
            @RequestParam("nombre") String nombreLayout,
            @RequestParam Map<String, MultipartFile> imagenesAdjuntas) { // Recibe todas las imágenes
        try {
            // Guarda el SVG
            Path svgDestino = Paths.get(
                    ArchivoExterno.getLayoutPath() + nombreLayout + "Tenant" + TenantContext.getTenantId() + ".svg");
            Path carpetaLayouts = Paths.get(ArchivoExterno.getLayoutPath());

            // Crear carpeta layouts si no existe
            if (!Files.exists(carpetaLayouts)) {
                Files.createDirectories(carpetaLayouts);
                log.info("Carpeta 'layouts' creada en: " + carpetaLayouts.toAbsolutePath());
            }

            Files.write(svgDestino, svgFile.getBytes(), StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            // Guarda las imágenes en la subcarpeta "imagenes/"

            Path carpetaImagenes = Paths.get(ArchivoExterno.getImagenesPath());

            // Crear la carpeta si no existe
            if (!Files.exists(carpetaImagenes)) {
                Files.createDirectories(carpetaImagenes);
            }

            for (Map.Entry<String, MultipartFile> entrada : imagenesAdjuntas.entrySet()) {
                if ("svg".equals(entrada.getKey())) {
                    continue; // Saltar la clave "svg" para que no se procese como imagen
                }
                String nombreArchivo = toCamelCase(entrada.getKey()) + "Tenant" + TenantContext.getTenantId() + ".jpg"; // Mantiene
                                                                                                                        // el
                                                                                                                        // nombre
                                                                                                                        // correcto
                                                                                                                        // (idName)
                MultipartFile archivo = entrada.getValue();

                // Ajuste para evitar errores en la ruta
                Path rutaImagen = carpetaImagenes.resolve(nombreArchivo);

                if (!Files.exists(rutaImagen.getParent())) {
                    Files.createDirectories(rutaImagen.getParent()); // Asegurar que la carpeta esté creada
                }

                Files.write(rutaImagen, archivo.getBytes(), StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
                log.info("Imagen guardada: " + rutaImagen);
            }

            generarDatos(new String(svgFile.getBytes(), StandardCharsets.UTF_8), nombreLayout);

            return ResponseEntity.ok().build();

        } catch (IOException e) {
            log.error("Error al guardar archivos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private void generarDatos(String svgContent, String nombreLayout) {
        List<String> ids = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource source = new InputSource(new StringReader(svgContent));
            Document doc = builder.parse(source);
            NodeList paths = doc.getElementsByTagName("path");

            for (int i = 0; i < paths.getLength(); i++) {
                Element path = (Element) paths.item(i);
                String id = path.getAttribute("id").trim();
                if (!id.isEmpty()) {
                    ids.add(id);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Para cada ID, crear un nuevo activo si no existe
        ids.forEach(id -> {
            // if (!activoService.existsByNombre(id)) { // Asegurate que este método exista
            // en el service/repository
            if (activoService.findByName(toCamelCase(id)) == null) { // Asegurate que este método exista en el
                                                                     // service/repository
                Activo activo = new Activo();
                activo.setNombreCamelCase(Convertidor.aCamelCase(id));
                activo.setNombre(id);
                activo.setEstado("operativa");
                activo.setLayout(nombreLayout);
                activoDao.save(activo);
            }
        });
    }

    // DMS si llego a necesitar la funcion to camel case aca esta
    private String toCamelCase(String str) {
        // Normaliza y elimina acentos
        String normalized = Normalizer.normalize(str, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}", "");

        // Elimina caracteres especiales y convierte a minúsculas
        normalized = normalized.replaceAll("[^a-zA-Z0-9\\s]", "").toLowerCase();

        // Convierte a camelCase
        String[] words = normalized.split("\\s+");
        StringBuilder camelCaseString = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            if (i == 0) {
                camelCaseString.append(words[i]); // Primera palabra en minúsculas
            } else {
                camelCaseString.append(Character.toUpperCase(words[i].charAt(0)))
                        .append(words[i].substring(1));
            }

        }
        return camelCaseString.toString();

    }
}
