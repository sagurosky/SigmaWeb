package mantenimiento.gestorTareas.web;

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
import mantenimiento.gestorTareas.datos.ActivoDao;
import mantenimiento.gestorTareas.datos.RolDao;
import mantenimiento.gestorTareas.datos.UsuarioDao;
import mantenimiento.gestorTareas.dominio.*;
import mantenimiento.gestorTareas.servicio.ActivoService;
import mantenimiento.gestorTareas.servicio.AsignacionService;
import mantenimiento.gestorTareas.servicio.InformeService;
import mantenimiento.gestorTareas.servicio.PreventivoService;
import mantenimiento.gestorTareas.servicio.ProduccionService;
import mantenimiento.gestorTareas.servicio.TareaService;
import mantenimiento.gestorTareas.servicio.Servicio;
import mantenimiento.gestorTareas.servicio.TecnicoService;
import mantenimiento.gestorTareas.servicio.UsuarioService;
import mantenimiento.gestorTareas.util.ArchivoExterno;
import mantenimiento.gestorTareas.util.TiempoUtils;
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
import mantenimiento.gestorTareas.util.Convertidor;
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
        //si el usuario logueado es un técnico y el apellido es null significa que fue recien creado
        //por lo tanto lo redirijo a tecnicoDatosPersonales para que cargue su informacion;
        String nombreUsuario = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioDao.findByUsername(nombreUsuario);
        if (usuario.getRoles().get(0).getNombre().equals("ROLE_TECNICO")) {

            Tecnico tecnico = tecnicoService.traerPorUsuario(usuario,TenantContext.getTenantId());

            if (tecnico.getApellido() == null) {
                model.addAttribute("tecnico", tecnico);
                model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());
                return "tecnicoDatosPersonales";
            }
        }
        Boolean admin = false;
        for (Rol rolUsuario : usuario.getRoles()) {
            if (rolUsuario.getNombre().equals("ROLE_ADMIN")) admin = true;
        }
        if (!admin) return "redirect:/layout";

//DMS si ya se generó el svg salto al layout. Si quiero volver tengo que redireccionar a "/"

        Path layoutDir=null;
        if(ArchivoExterno.getString("nube").equals("si"))
        {
            layoutDir = Path.of("/media/sf_personal/sigmaweb/recursos/layouts/");
        }else
        {
            layoutDir = Path.of("/app/recursos/layouts/");
        }


        boolean existeLayout = false;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(layoutDir, "*.svg")) {
            existeLayout = stream.iterator().hasNext(); // true si hay al menos un archivo .svg
        } catch (IOException e) {
            e.printStackTrace(); // Manejo básico de errores. Puedes loguearlo o lanzar excepción si lo prefieres.
        }

        model.addAttribute("existeLayout", existeLayout);

        if (existeLayout) return "redirect:/layout";
        model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());
        model.addAttribute("noTraerEstados", "no");
        return "crearLayout";


    }
    @GetMapping("/recargar-config")
    public String recargar() {
        log.info("configuracion cargada ok");
        ArchivoExterno.recargar();

    return "/";
    }
    @GetMapping("/crearLayout")
    public String crearLayout(Model model) throws IOException {
        if(!ArchivoExterno.getString("editorLayout").equals("si"))return "redirect:/layout";

        model.addAttribute("existeLayout", !ArchivoExterno.nombresLayouts().isEmpty());
        model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());
        model.addAttribute("noTraerEstados", "no");

        return "crearLayout";


    }

    @PostMapping("/eliminarLayout/{nombre}")
    public String eliminarLayout(@PathVariable String nombre) {
        try {

            Path path=null;
            if(ArchivoExterno.getString("nube").equals("si"))
            {
                 path = Paths.get("/media/sf_personal/sigmaweb/recursos/layouts/", nombre);
            }else
            {
                path = Paths.get("/app/recursos/layouts/",nombre);
            }

            Files.deleteIfExists(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "redirect:/crearLayout";
    }










    @GetMapping("/tareas")
    public String tareas(Model model) {
        var tareas = tareaService.traerNoCerradas(TiempoUtils.haceAnios(1), TiempoUtils.ahora(),TenantContext.getTenantId());
        log.info("cantidad: "+tareas.size());
        model.addAttribute("tareas", tareas);
        model.addAttribute("todosLosTecnicos", tecnicoService.findAll());
        model.addAttribute("cantidadActivosDetenidos", activoService.findByStatus("detenida").size());


        model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());
        //DMS para el menú
        List<Tecnico> tecnicosFiltrados = tecnicoService.traerHabilitados(TenantContext.getTenantId()).stream()
                .filter(t -> t.getUsuario().getRoles().get(0).getNombre().equals("ROLE_TECNICO"))
                .collect(Collectors.toList());
        model.addAttribute("tecnicos", tecnicosFiltrados);

        model.addAttribute("habilitarGestionUsuarios", ArchivoExterno.getString("editarUsuarios"));
        model.addAttribute("habilitarEditorLayout", ArchivoExterno.getString("editorLayout"));
        model.addAttribute("tiempoRefresco", ArchivoExterno.getString("tiempoRefresco"));
        return "tareas";
    }
//DMS controlador antes de que me lo de vuelta chatGPT
   /* @GetMapping("/layout")
    public String layout(Model model) throws IOException {

        //traigo todos los activos y mando a la vista variables de falla cuando estan detenidos o de cierre cuando estan liberadas y faltan cerrar
        List<Activo> activos = activo.findAll();


        for (Activo activo : activos) {
//le paso la variable disponible si la hora cargada de la disponibilidad es mayor a la hora actual  
            if (activo.getDisponibilidadHasta() != null && activo.getEstado().equals("disponible"))
                if (TiempoUtils.ahora().isAfter(activo.getDisponibilidadHasta())) {
                    activo.setEstado("operativa");
                    activoService.save(activo);
                    Tarea tarea=tareaService.traerDisponiblePorActivo(activo).get(0);
                    tarea.setEstado("finDisponible");
                    tareaService.save(tarea);

                }

        }

        //cuando el tecnico no tiene rol tecnico(porque lo promovieron) lko saco de la lista
        List<Tecnico> tecnicos=tecnicoService.traerHabilitados();
        List<Tecnico> tecnicosFiltrados=new ArrayList<>();

        for(Tecnico tecnico:tecnicos)
        {
            if(tecnico.getUsuario().getRoles().get(0).getNombre().equals("ROLE_TECNICO"))tecnicosFiltrados.add(tecnico);
        }
        model.addAttribute("tecnicos", tecnicosFiltrados);

        List<Produccion> oTs = produccionService.traerAbiertas();

        if (oTs != null)
            model.addAttribute("oTs", oTs);


        //DMS 27/3 datos para notificaciones toastr
        model.addAttribute("cantidadActivosDetenidos", activoService.findByStatus("detenida").size());
        model.addAttribute("preventivosNoValidados", preventivoService.traerPreventivosNoValidados());

        List<Informe> informesSupervisor = new ArrayList<>();
        List<Tarea> tareasNoEvaluadas = tareaService.traerPorEstadoInforme("noEvaluado",TiempoUtils.haceAnios(1), TiempoUtils.ahora());
        ;
        tareasNoEvaluadas.addAll(tareaService.traerPorEstadoInforme("noAprobado",TiempoUtils.haceAnios(1), TiempoUtils.ahora()));

        for (Tarea tarea : tareasNoEvaluadas) {
            informesSupervisor.add(tarea.getInforme());
        }


        model.addAttribute("informesPendientesSupervisor", informesSupervisor);

        String nombreUsuario = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioDao.findByUsername(nombreUsuario);
        if (usuario.getRoles().get(0).getNombre().equals("ROLE_TECNICO")) {
            Tecnico tecnico = tecnicoService.traerPorUsuario(usuario);
            List<Tarea> tareasInformePendienteTecnico = tareaService.traerPorTecnicoYEstadoInforme(tecnico, "pendiente",TiempoUtils.haceAnios(1), TiempoUtils.ahora());
            List<Tarea> tareasInformeEnRevisionTecnico = tareaService.traerPorTecnicoYEstadoInforme(tecnico, "EnRevision",TiempoUtils.haceAnios(1), TiempoUtils.ahora());
            tareasInformePendienteTecnico.addAll(tareasInformeEnRevisionTecnico);

            model.addAttribute("tareasConInformesPendientesTecnico", tareasInformePendienteTecnico);
        }
        //DMS 27/3 fin datos para notificaciones toastr
        String carpetaLayouts = "/media/sf_personal/sigmaweb/recursos/layouts/";
        Path carpeta = Path.of(carpetaLayouts);
        String svgContent = "";
        List<String> nombresLayouts = new ArrayList<>();

        try {
            if (Files.exists(carpeta) && Files.isDirectory(carpeta)) {
                // Obtener todos los archivos .svg ordenados por fecha de modificación (más antiguos primero)
                List<Path> archivosSvg = Files.list(carpeta)
                        .filter(p -> p.toString().endsWith(".svg"))
                        .sorted(Comparator.comparingLong(p -> p.toFile().lastModified()))
                        .collect(Collectors.toList());

                // Cargar el contenido del más antiguo
                if (!archivosSvg.isEmpty()) {
                    Path archivoMasAntiguo = archivosSvg.get(0);
                    svgContent = Files.readString(archivoMasAntiguo);

                    // Obtener los nombres de todos los archivos (ordenados)
                    nombresLayouts = archivosSvg.stream()
                            .map(p -> p.getFileName().toString())
                            .collect(Collectors.toList());
                } else {
                    svgContent = "No se encontraron archivos SVG en la carpeta.";
                }
            } else {
                svgContent = "La carpeta de layouts no existe.";
            }
        } catch (IOException e) {
            svgContent = "Error al leer archivos SVG.";
            e.printStackTrace();
        }

// Agregar al modelo
        model.addAttribute("svgContent", svgContent);
        model.addAttribute("existeLayout", !nombresLayouts.isEmpty());
        model.addAttribute("nombresLayouts", nombresLayouts);

        model.addAttribute("habilitarGestionUsuarios", ArchivoExterno.getString("editarUsuarios"));
        model.addAttribute("habilitarEditorLayout", ArchivoExterno.getString("editorLayout"));
        model.addAttribute("tiempoRefresco", ArchivoExterno.getString("tiempoRefresco"));

        return "layout";
    }

*/


   @GetMapping("/layout")
   public String layoutDefault(Model model) throws IOException {
       cargarDatosGenerales(model);
       Path carpeta=null;
       if(ArchivoExterno.getString("nube").equals("si"))
       {
           carpeta = Path.of("/media/sf_personal/sigmaweb/recursos/layouts/");
       }else
       {
           carpeta = Path.of("/app/recursos/layouts/");
       }
       List<String> nombresLayouts = new ArrayList<>();
       String svgContent = "";
       if (Files.exists(carpeta) && Files.isDirectory(carpeta)) {
           List<Path> archivosSvg = Files.list(carpeta)
                   .filter(p -> p.toString().endsWith(".svg"))
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
       return "layout";
   }
    @GetMapping("/layout/{nombreArchivo}")
    public String layoutPorNombre(@PathVariable String nombreArchivo, Model model) throws IOException {
        cargarDatosGenerales(model);

        Path carpeta=null;
        if(ArchivoExterno.getString("nube").equals("si"))
        {
            carpeta = Path.of("/media/sf_personal/sigmaweb/recursos/layouts/");
        }else
        {
            carpeta = Path.of("/app/recursos/layouts/");
        }
        Path archivo = carpeta.resolve(nombreArchivo);
        String svgContent;

        if (Files.exists(archivo)) {
            svgContent = Files.readString(archivo);
        } else {
            svgContent = "Archivo no encontrado: " + nombreArchivo;
        }



        model.addAttribute("svgContent", svgContent);
        model.addAttribute("existeLayout", !ArchivoExterno.nombresLayouts().isEmpty());
        model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());
        return "layout";
    }

    private void cargarDatosGenerales(Model model) {
       List<Activo> activos = activo.findAll();
       for (Activo activo : activos) {
           if (activo.getDisponibilidadHasta() != null && activo.getEstado().equals("disponible"))
               if (TiempoUtils.ahora().isAfter(activo.getDisponibilidadHasta())) {
                   activo.setEstado("operativa");
                   activoDao.save(activo);
                   Tarea tarea = tareaService.traerDisponiblePorActivo(activo,TenantContext.getTenantId()).get(0);
                   tarea.setEstado("finDisponible");
                   tareaService.save(tarea);
               }
       }

       List<Tecnico> tecnicosFiltrados = tecnicoService.traerHabilitados(TenantContext.getTenantId()).stream()
               .filter(t -> t.getUsuario().getRoles().get(0).getNombre().equals("ROLE_TECNICO"))
               .collect(Collectors.toList());
       model.addAttribute("tecnicos", tecnicosFiltrados);

       List<Produccion> oTs = produccionService.traerAbiertas(TenantContext.getTenantId());
       if (oTs != null) model.addAttribute("oTs", oTs);

       model.addAttribute("cantidadActivosDetenidos", activoService.findByStatus("detenida").size());
       model.addAttribute("preventivosNoValidados", preventivoService.traerPreventivosNoValidados(TenantContext.getTenantId()));

       List<Tarea> tareasNoEvaluadas = tareaService.traerPorEstadoInforme("noEvaluado", TiempoUtils.haceAnios(1), TiempoUtils.ahora(),TenantContext.getTenantId());
       tareasNoEvaluadas.addAll(tareaService.traerPorEstadoInforme("noAprobado", TiempoUtils.haceAnios(1), TiempoUtils.ahora(),TenantContext.getTenantId()));
       List<Informe> informesSupervisor = tareasNoEvaluadas.stream().map(Tarea::getInforme).collect(Collectors.toList());
       model.addAttribute("informesPendientesSupervisor", informesSupervisor);

       String nombreUsuario = SecurityContextHolder.getContext().getAuthentication().getName();
       Usuario usuario = usuarioDao.findByUsername(nombreUsuario);
       if (usuario.getRoles().get(0).getNombre().equals("ROLE_TECNICO")) {
           Tecnico tecnico = tecnicoService.traerPorUsuario(usuario,TenantContext.getTenantId());
           List<Tarea> pendientes = tareaService.traerPorTecnicoYEstadoInforme(tecnico, "pendiente", TiempoUtils.haceAnios(1), TiempoUtils.ahora(),TenantContext.getTenantId());
           pendientes.addAll(tareaService.traerPorTecnicoYEstadoInforme(tecnico, "EnRevision", TiempoUtils.haceAnios(1), TiempoUtils.ahora(),TenantContext.getTenantId()));
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
        model.addAttribute("activos", activo.findAll());
        model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());
        //DMS para el menú
        List<Tecnico> tecnicosFiltrados = tecnicoService.traerHabilitados(TenantContext.getTenantId()).stream()
                .filter(t -> t.getUsuario().getRoles().get(0).getNombre().equals("ROLE_TECNICO"))
                .collect(Collectors.toList());
        model.addAttribute("tecnicos", tecnicosFiltrados);

        model.addAttribute("habilitarGestionUsuarios", ArchivoExterno.getString("editarUsuarios"));
        model.addAttribute("habilitarEditorLayout", ArchivoExterno.getString("editorLayout"));
        model.addAttribute("tiempoRefresco", ArchivoExterno.getString("tiempoRefresco"));

        return "crearTarea";
    }

    @PostMapping("/guardar")
    public String guardar(Model model, @Valid Tarea tarea, Errors errores, @RequestParam("file") MultipartFile imagen, @RequestParam(value = "activo", required = false) String activoReq) {

        if (errores.hasErrors()) {
            model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());
            return "crearTarea";
        }
        if (!imagen.isEmpty()) {
            // Path directorioImagenes = Paths.get("src//main//resources//static//imagenes");
            // String ruta = directorioImagenes.toFile().getAbsolutePath();
            //voy a usar un directorio no relativo para evitar la necesidad de actualizar
            //cada vez que se agrega una imagen nueva
            String ruta = "c://AppTareas//recursos";
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
        //al solicitar la tarea pasa a estado abierto automaticamente
        tarea.setEstado("abierto");
        //se guarda el momento de la solicitud para calcular el tiempo de parada
        tarea.getActivo().setMomentoDetencion(TiempoUtils.ahora());
        tarea.setMomentoDetencion(TiempoUtils.ahora());
        if (tarea.getAfectaProduccion().equals("no")) tarea.getActivo().setEstado("operativa condicionada");
        else tarea.getActivo().setEstado("detenida");


        // si no es de mantenimiento no genero informe
        if (tarea.getDepartamentoResponsable().equals("mantenimiento")) {
            Informe informe = new Informe();
            informe.setEstadoInforme("noEvaluado");
            tarea.setInforme(informe);

        }
        activo.save(tarea.getActivo());

        servicio.guardar(tarea);
        model.addAttribute("tareas", tareaService.traerNoCerradas(TiempoUtils.haceAnios(1), TiempoUtils.ahora(),TenantContext.getTenantId()));
        String url = activoDao.findById(Long.parseLong(activoReq)).orElse(null).getNombre();
        if (activoReq != null) {
            return "redirect:/activo/" + Convertidor.aCamelCase(url);
        }
        return "redirect:/tareas";
    }

    @GetMapping("/editar/{id}")
    public String editar(Tarea tarea, Model model) {
        model.addAttribute("activos", activo.findAll());
        model.addAttribute("estados", Arrays.asList("detenida", "operativa", "disponible"));
        model.addAttribute("estadosTareas", Arrays.asList("abierto", "enProceso", "liberada", "cerrada"));
        model.addAttribute("tarea", servicio.encontrar(tarea));
        model.addAttribute("todosLosTecnicos", tecnicoService.findAll());
//        model.addAttribute("asignacion", asignacionService.traerPorTarea(tarea));

        // Crear lista de IDs de técnicos asignados
        Tarea t = servicio.encontrar(tarea);
        List<Long> idsTecnicosAsignados = t.getAsignaciones().stream()
                .map(asignacion -> asignacion.getTecnico().getId())
                .collect(Collectors.toList());

        model.addAttribute("idsTecnicosAsignados", idsTecnicosAsignados);
        model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());
        return "modificar";
    }

    @PostMapping("/guardarEdicion")
    public String guardarEdicion(Model model, @Valid Tarea tarea, @RequestParam(value = "tecnicosIds", required = false) List<Long> tecnicosIds) {


        List<Asignacion> asignaciones = asignacionService.traerPorTarea(tarea,TenantContext.getTenantId());


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

        model.addAttribute("tareas", tareaService.traerNoCerradas(TiempoUtils.haceAnios(1), TiempoUtils.ahora(),TenantContext.getTenantId()));
        return "redirect:/tareas";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(Model model, Tarea tarea) {

        servicio.eliminar(tarea);
        model.addAttribute("tareas", tareaService.traerNoCerradas(TiempoUtils.haceAnios(1), TiempoUtils.ahora(),TenantContext.getTenantId()));
        return "redirect:/tareas";
    }

    @GetMapping("/liberarSolicitud/{id}")
    public String liberar(@RequestHeader(value = "Referer", required = false) String origen, Model model, Tarea tarea) {
        Tarea t = servicio.encontrar(tarea);
        t.setEstado("liberada");
        t.getActivo().setEstado("liberada");
        t.setMomentoLiberacion(TiempoUtils.ahora());
        servicio.guardar(t);
        if (origen.contains(Convertidor.aCamelCase(t.getActivo().getNombre()))) {
            String url = activoDao.findById(t.getActivo().getId()).orElse(null).getNombre();
            return "redirect:/activo/" + Convertidor.aCamelCase(url);
        }

        model.addAttribute("tareas", tareaService.traerNoCerradas(TiempoUtils.haceAnios(1), TiempoUtils.ahora(),TenantContext.getTenantId()));
        return "redirect:/tareas";
    }

    @GetMapping("/asignarSolicitud/{id}")
    public String asignar(
            @RequestParam("motivoDemoraAsignacion") String motivoDemoraAsignacion,
            @RequestParam(value = "activoReq", required = false) String activoReq,
            @RequestParam(value = "tecnicosIds", required = false) List<Long> tecnicosIds,
            Model model, Tarea tarea) {
        Tarea t = servicio.encontrar(tarea);
        List<Asignacion> asignaciones = new ArrayList<>();

        for (Long idTecnico : tecnicosIds) {
            Tecnico tecnico = tecnicoService.getById(idTecnico);
            Asignacion asignacion = new Asignacion();
            asignacion.setTecnico(tecnico);
            asignacion.setTarea(tarea);
            asignaciones.add(asignacion);
        }

        t.setAsignaciones(asignaciones);
        t.setEstado("enProceso");
        t.setMomentoAsignacion(TiempoUtils.ahora());
        t.setMotivoDemoraAsignacion(motivoDemoraAsignacion);
        servicio.guardar(t);
        model.addAttribute("tareas", tareaService.traerNoCerradas(TiempoUtils.haceAnios(1), TiempoUtils.ahora(),TenantContext.getTenantId()));
        model.addAttribute("tarea", t);
        String url = activoDao.findById(Long.parseLong(activoReq)).orElse(null).getNombre();
        if (activoReq != null) {
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

        Tarea t = servicio.encontrar(tarea);
        t.getActivo().setEstado("operativa");
        t.setEstado("cerrada");

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

        t.setEvaluacion(evaluacion);

        t.setMomentoCierre(TiempoUtils.ahora());


        if (!t.getDepartamentoResponsable().equals("mantenimiento")) {
            t.setMomentoLiberacion(TiempoUtils.ahora());
        }

//        t.getActivo().setMomentoDetencion(null);
        servicio.guardar(t);

        if (origen.contains(Convertidor.aCamelCase(t.getActivo().getNombre()))) {
            String url = activoDao.findById(t.getActivo().getId()).orElse(null).getNombre();
            return "redirect:/activo/" + Convertidor.aCamelCase(url);
        }


        model.addAttribute("tareas", tareaService.traerNoCerradas(TiempoUtils.haceAnios(1), TiempoUtils.ahora(),TenantContext.getTenantId()));
        return "redirect:/tareas";
    }

    @GetMapping("/registro")
    public String registroHistorico(Model model) {

        model.addAttribute("tareas", tareaService.traerCerradas(TiempoUtils.haceAnios(1), TiempoUtils.ahora(),TenantContext.getTenantId()));
        model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());
        //DMS para el menú
        List<Tecnico> tecnicosFiltrados = tecnicoService.traerHabilitados(TenantContext.getTenantId()).stream()
                .filter(t -> t.getUsuario().getRoles().get(0).getNombre().equals("ROLE_TECNICO"))
                .collect(Collectors.toList());
        model.addAttribute("tecnicos", tecnicosFiltrados);

        model.addAttribute("habilitarGestionUsuarios", ArchivoExterno.getString("editarUsuarios"));
        model.addAttribute("habilitarEditorLayout", ArchivoExterno.getString("editorLayout"));
        model.addAttribute("tiempoRefresco", ArchivoExterno.getString("tiempoRefresco"));
        return "registro";
    }

    @GetMapping("/registroActivo/{id}")
    public String registroHistoricoActivo(Model model, Activo activo) {

        List<Tarea> tareas = tareaService.traerCerradas(TiempoUtils.haceAnios(1), TiempoUtils.ahora(),TenantContext.getTenantId());
        model.addAttribute("url", Convertidor.aCamelCase(activoDao.findById(activo.getId()).orElse(null).getNombre()));
        model.addAttribute("tareas", tareaService.traerCerradasPorActivo(activo,TiempoUtils.haceAnios(1), TiempoUtils.ahora(),TenantContext.getTenantId()));
        model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());
        //DMS para el menú
        List<Tecnico> tecnicosFiltrados = tecnicoService.traerHabilitados(TenantContext.getTenantId()).stream()
                .filter(t -> t.getUsuario().getRoles().get(0).getNombre().equals("ROLE_TECNICO"))
                .collect(Collectors.toList());
        model.addAttribute("tecnicos", tecnicosFiltrados);

        model.addAttribute("habilitarGestionUsuarios", ArchivoExterno.getString("editarUsuarios"));
        model.addAttribute("habilitarEditorLayout", ArchivoExterno.getString("editorLayout"));
        model.addAttribute("tiempoRefresco", ArchivoExterno.getString("tiempoRefresco"));

        return "registro";
    }


    //@CrossOrigin(origins = "*")
    @PostMapping(value = "/guardarSvg", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> saveSvg(
            @RequestParam("svg") MultipartFile svgFile,
            @RequestParam("nombre") String nombreLayout,
            @RequestParam Map<String, MultipartFile> imagenesAdjuntas) { // Recibe todas las imágenes
        try {
            // Guarda el SVG
            Path svgDestino=null;
            Path carpetaLayouts=null;
            if(ArchivoExterno.getString("nube").equals("si"))
            {
                svgDestino = Paths.get("/media/sf_personal/sigmaweb/recursos/layouts/" + nombreLayout + TenantContext.getTenantId()+".svg");
                 carpetaLayouts = Paths.get("/media/sf_personal/sigmaweb/recursos/layouts/");
            }else
            {
                svgDestino = Paths.get("/app/recursos/layouts/" + nombreLayout + TenantContext.getTenantId()+".svg");
                 carpetaLayouts = Paths.get("/app/recursos/layouts/");
            }


log.info(""+svgDestino);

            // Crear carpeta layouts si no existe
            if (!Files.exists(carpetaLayouts)) {
                Files.createDirectories(carpetaLayouts);
                log.info("Carpeta 'layouts' creada en: " + carpetaLayouts.toAbsolutePath());
            }



            Files.write(svgDestino, svgFile.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // Guarda las imágenes en la subcarpeta "imagenes/"

            Path carpetaImagenes=null;
            if(ArchivoExterno.getString("nube").equals("si"))
            {
                carpetaImagenes = Paths.get("/media/sf_personal/sigmaweb/recursos/imagenes/");
            }else
            {
                carpetaImagenes = Paths.get("/app/recursos/imagenes/");
            }

            // Crear la carpeta si no existe
            if (!Files.exists(carpetaImagenes)) {
                Files.createDirectories(carpetaImagenes);
            }

            for (Map.Entry<String, MultipartFile> entrada : imagenesAdjuntas.entrySet()) {
                if ("svg".equals(entrada.getKey())) {
                    continue; // Saltar la clave "svg" para que no se procese como imagen
                }
                String nombreArchivo = toCamelCase(entrada.getKey()) + ".jpg"; // Mantiene el nombre correcto (idName)
                MultipartFile archivo = entrada.getValue();

                // Ajuste para evitar errores en la ruta
                Path rutaImagen = carpetaImagenes.resolve(nombreArchivo);

                if (!Files.exists(rutaImagen.getParent())) {
                    Files.createDirectories(rutaImagen.getParent()); // Asegurar que la carpeta esté creada
                }

                Files.write(rutaImagen, archivo.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                log.info("Imagen guardada: " + rutaImagen);
            }


            generarDatos(new String(svgFile.getBytes(), StandardCharsets.UTF_8),nombreLayout);

            return ResponseEntity.ok().build();

        } catch (IOException e) {
            log.error("Error al guardar archivos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    private void generarDatos(String svgContent,String nombreLayout) {
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
//            if (!activoService.existsByNombre(id)) { // Asegurate que este método exista en el service/repository
            if (activoService.findByName(toCamelCase(id))==null) { // Asegurate que este método exista en el service/repository
                Activo activo = new Activo();
                activo.setNombreCamelCase(Convertidor.aCamelCase(id));
                activo.setNombre(id);
                activo.setEstado("operativa");
                activo.setLayout(nombreLayout);
                activoDao.save(activo);
            }
        });
    }


    //    DMS si llego a necesitar la funcion to camel case aca esta
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

