package mantenimiento.gestorTareas.web;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.sql.Array;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import mantenimiento.gestorTareas.datos.ActivoDao;
import mantenimiento.gestorTareas.datos.RolDao;
import mantenimiento.gestorTareas.datos.UsuarioDao;
import mantenimiento.gestorTareas.dominio.Activo;
import mantenimiento.gestorTareas.dominio.Asignacion;
import mantenimiento.gestorTareas.dominio.Evaluacion;
import mantenimiento.gestorTareas.dominio.Informe;
import mantenimiento.gestorTareas.dominio.Produccion;
import mantenimiento.gestorTareas.dominio.Tarea;
import mantenimiento.gestorTareas.dominio.Tecnico;
import mantenimiento.gestorTareas.dominio.Usuario;
import mantenimiento.gestorTareas.servicio.ActivoService;
import mantenimiento.gestorTareas.servicio.AsignacionService;
import mantenimiento.gestorTareas.servicio.InformeService;
import mantenimiento.gestorTareas.servicio.PreventivoService;
import mantenimiento.gestorTareas.servicio.ProduccionService;
import mantenimiento.gestorTareas.servicio.TareaService;
import mantenimiento.gestorTareas.servicio.Servicio;
import mantenimiento.gestorTareas.servicio.TecnicoService;
import mantenimiento.gestorTareas.servicio.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import mantenimiento.gestorTareas.util.Convertidor;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

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

    @GetMapping("/")
    public String inicio(Model model) {
        //si el usuario logueado es un técnico y el apellido es null significa que fue recien creado
        //por lo tanto lo redirijo a tecnicoDatosPersonales para que cargue su informacion;
        String nombreUsuario=SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioDao.findByUsername(nombreUsuario);
        if (usuario.getRoles().get(0).getNombre().equals("ROLE_TECNICO")) {
            Tecnico tecnico = tecnicoService.traerPorUsuario(usuario);
      
            if (tecnico.getApellido() == null) {
                model.addAttribute("tecnico", tecnico);
                
                return "tecnicoDatosPersonales";
            }
        }
        
        
            return layout(model);
        
    }

    @GetMapping("/tareas")
    public String tareas(Model model) {
        var tareas = tareaService.traerNoCerradas();
        model.addAttribute("tareas", tareas);
        model.addAttribute("todosLosTecnicos", tecnicoService.findAll());
        model.addAttribute("cantidadActivosDetenidos",activoService.findByStatus("detenida").size());
        return "tareas";
    }

    @GetMapping("/layout")
    public String layout(Model model) {

        //traigo todos los activos y mando a la vista variables de falla cuando estan detenidos o de cierre cuando estan liberadas y faltan cerrar
        List<Activo> activos = activo.findAll();
        
        
        
        for (Activo activo : activos) {
//le paso la variable disponible si la hora cargada de la disponibilidad es mayor a la hora actual  
            if(activo.getDisponibilidadHasta()!=null&&activo.getEstado().equals("disponible"))
            if(LocalDateTime.now().isAfter(activo.getDisponibilidadHasta()))
            {
                activo.setEstado("operativa");
                activoService.save(activo);
                
            }

        }
        model.addAttribute("tecnicos", tecnicoService.findAll());
        
        List<Produccion> oTs = produccionService.traerAbiertas();

        if(oTs!=null)
        model.addAttribute("oTs", oTs);
        
        
        //DMS 27/3 datos para notificaciones toastr
        model.addAttribute("cantidadActivosDetenidos",activoService.findByStatus("detenida").size());        
        model.addAttribute("preventivosNoValidados",preventivoService.traerPreventivosNoValidados());
        
        List <Informe> informesSupervisor=new ArrayList<>();
        List<Tarea> tareasNoEvaluadas=tareaService.traerPorEstadoInforme("noEvaluado");;
        tareasNoEvaluadas.addAll(tareaService.traerPorEstadoInforme("noAprobado"));
        
        for (Tarea tarea : tareasNoEvaluadas) {
            informesSupervisor.add(tarea.getInforme());
        }
        
        
        
        
        model.addAttribute("informesPendientesSupervisor",informesSupervisor);
        
        String nombreUsuario=SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioDao.findByUsername(nombreUsuario);
        if (usuario.getRoles().get(0).getNombre().equals("ROLE_TECNICO")) {
            Tecnico tecnico=tecnicoService.traerPorUsuario(usuario);
             List<Tarea> tareasInformePendienteTecnico=tareaService.traerPorTecnicoYEstadoInforme(tecnico,"pendiente");
              List<Tarea> tareasInformeEnRevisionTecnico=tareaService.traerPorTecnicoYEstadoInforme(tecnico,"EnRevision");
              tareasInformePendienteTecnico.addAll(tareasInformeEnRevisionTecnico);
            
            model.addAttribute("tareasConInformesPendientesTecnico",tareasInformePendienteTecnico);
        }        
        //DMS 27/3 fin datos para notificaciones toastr
        
        return "layoutPlanta3";
    }

    @GetMapping("/layoutPlanta2")
    public String layoutPlanta2(Model model) {

        //traigo todos los activos y mando a la vista variables de falla cuando estan detenidos o de cierre cuando estan liberadas y faltan cerrar
        List<Activo> activos = activo.findAll();
        String aux = "";
        Boolean fallaPlanta3 = false;
        Boolean cierrePlanta3 = false;
        for (Activo activo : activos) {
             if(activo.getDisponibilidadHasta()!=null&&activo.getEstado().equals("disponible"))
            if(LocalDateTime.now().isAfter(activo.getDisponibilidadHasta()))
            {
                activo.setEstado("operativa");
                activoService.save(activo);
                
            }

        }
        model.addAttribute("tecnicos", tecnicoService.findAll());
        model.addAttribute("cantidadActivosDetenidos",activoService.findByStatus("detenida").size());
        return "layoutPlanta2";
    }

    

    @GetMapping("/crearTarea/{id}")
    public String modificar(Model model, Tarea tarea, Activo activoRequest) {

        Activo activoSeleccionado = activo.findById(activoRequest.getId()).orElse(null);
        tarea.setActivo(activoSeleccionado);
        tarea.setAfectaProduccion("si");
        model.addAttribute("tarea", tarea);
        model.addAttribute("activos", activo.findAll());
        
        model.addAttribute("cantidadActivosDetenidos",activoService.findByStatus("detenida").size());

//        model.addAttribute("estados",Arrays.asList("detenida","operativa","disponible para preventivo"));
        return "crearTarea";
    }

    @PostMapping("/guardar")
    public String guardar(Model model, @Valid Tarea tarea, Errors errores, @RequestParam("file") MultipartFile imagen,@RequestParam(value="activo", required = false) String activoReq ) {
        
        if (errores.hasErrors()) {
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
        tarea.getActivo().setMomentoDetencion(LocalDateTime.now());
        tarea.setMomentoDetencion(LocalDateTime.now());
        if(tarea.getAfectaProduccion().equals("no"))tarea.getActivo().setEstado("operativa condicionada");
        else tarea.getActivo().setEstado("detenida");
        
        
        // si no es de mantenimiento no genero informe
        if(tarea.getDepartamentoResponsable().equals("mantenimiento"))
        {
        Informe informe=new Informe();
        informe.setEstadoInforme("noEvaluado");
        tarea.setInforme(informe);
        
        }
        activo.save(tarea.getActivo());

        servicio.guardar(tarea);
        model.addAttribute("tareas", tareaService.traerNoCerradas());
        String url=activoService.findById(Long.parseLong(activoReq)).orElse(null).getNombre();
        if(activoReq!=null)
        {
            return "redirect:/"+Convertidor.aCamelCase(url);
        }
        return "redirect:/tareas";
    }

    @GetMapping("/editar/{id}")
    public String editar(Tarea tarea, Model model) {
        model.addAttribute("activos", activo.findAll());
        model.addAttribute("estados", Arrays.asList("detenida", "operativa", "disponible"));
           model.addAttribute("estadosTareas", Arrays.asList("abierto", "enProceso","liberada","cerrada"));
        model.addAttribute("tarea", servicio.encontrar(tarea));
        model.addAttribute("todosLosTecnicos", tecnicoService.findAll());
//        model.addAttribute("asignacion", asignacionService.traerPorTarea(tarea));

        // Crear lista de IDs de técnicos asignados
        Tarea t = servicio.encontrar(tarea);
        List<Long> idsTecnicosAsignados = t.getAsignaciones().stream()
                .map(asignacion -> asignacion.getTecnico().getId())
                .collect(Collectors.toList());

        model.addAttribute("idsTecnicosAsignados", idsTecnicosAsignados);
        model.addAttribute("cantidadActivosDetenidos",activoService.findByStatus("detenida").size());
        return "modificar";
    }

    @PostMapping("/guardarEdicion")
    public String guardarEdicion(Model model, @Valid Tarea tarea, @RequestParam(value = "tecnicosIds", required = false) List<Long> tecnicosIds) {
       
        

        
        
        
        List<Asignacion> asignaciones = asignacionService.traerPorTarea(tarea);

        
        if(tecnicosIds!=null)
        {
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
        
        model.addAttribute("tareas", tareaService.traerNoCerradas());
        return "redirect:/tareas";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(Model model, Tarea tarea) {

        servicio.eliminar(tarea);
        model.addAttribute("tareas", tareaService.traerNoCerradas());
        return "redirect:/tareas";
    }

    @GetMapping("/liberarSolicitud/{id}")
    public String liberar( @RequestHeader(value = "Referer", required = false) String origen, Model model, Tarea tarea) {
        Tarea t = servicio.encontrar(tarea);
        t.setEstado("liberada");
        t.getActivo().setEstado("liberada");
        t.setMomentoLiberacion(LocalDateTime.now());
        servicio.guardar(t);
          if(origen.contains(Convertidor.aCamelCase( t.getActivo().getNombre())))
          {
                String url=activoService.findById(t.getActivo().getId()).orElse(null).getNombre();
                 return "redirect:/"+Convertidor.aCamelCase(url);
          }
       
        model.addAttribute("tareas", tareaService.traerNoCerradas());
        return "redirect:/tareas";
    }

    @GetMapping("/asignarSolicitud/{id}")
    public String asignar(
            @RequestParam("motivoDemoraAsignacion") String motivoDemoraAsignacion, 
           @RequestParam(value="activoReq", required = false) String activoReq, 
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
        t.setMomentoAsignacion(LocalDateTime.now());
        t.setMotivoDemoraAsignacion(motivoDemoraAsignacion);
        servicio.guardar(t);
        model.addAttribute("tareas", tareaService.traerNoCerradas());
        model.addAttribute("tarea", t);
        String url=activoService.findById(Long.parseLong(activoReq)).orElse(null).getNombre();
        if(activoReq!=null)
        {
            return "redirect:/"+Convertidor.aCamelCase(url);
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

        t.setMomentoCierre(LocalDateTime.now());
        
        
        if(!t.getDepartamentoResponsable().equals("mantenimiento"))
        {
             t.setMomentoLiberacion(LocalDateTime.now());
        }
        
//        t.getActivo().setMomentoDetencion(null);
        servicio.guardar(t);
        
        if(origen.contains(Convertidor.aCamelCase( t.getActivo().getNombre())))
          {
                String url=activoService.findById(t.getActivo().getId()).orElse(null).getNombre();
                 return "redirect:/"+Convertidor.aCamelCase(url);
          }
        
        
        
        model.addAttribute("tareas", tareaService.traerNoCerradas());
        return "redirect:/tareas";
    }

    @GetMapping("/registro/{url}")
    public String registroHistorico(@PathVariable("url") String url, Model model) {

        model.addAttribute("tareas", tareaService.traerCerradas());
        model.addAttribute("url", url);
        model.addAttribute("cantidadActivosDetenidos",activoService.findByStatus("detenida").size());
        return "registro";
    }

    @GetMapping("/registroActivo/{id}")
    public String registroHistoricoActivo(Model model, Activo activo) {

        List<Tarea> tareas = tareaService.traerCerradas();
        model.addAttribute("url", Convertidor.aCamelCase(activoService.findById(activo.getId()).orElse(null).getNombre()));
        model.addAttribute("tareas", tareaService.traerCerradasPorActivo(activo));
        model.addAttribute("cantidadActivosDetenidos",activoService.findByStatus("detenida").size());
        return "registro";
    }
    
   
    
    
    
    

//    
//    // Método para calcular la diferencia de tiempo entre dos LocalDateTime
//    public String calcularDiferenciaTiempo(LocalDateTime momentoDetencion, LocalDateTime momentoLiberacion) {
//        // Calcular la diferencia de tiempo
//        Duration duracion = Duration.between(momentoDetencion, momentoLiberacion);
//
//        // Formatear la duración en un formato legible (días, horas, minutos, segundos)
//        long dias = duracion.toDays();
//        long horas = duracion.toHours() % 24;
//        long minutos = duracion.toMinutes() % 60;
//        long segundos = duracion.getSeconds() % 60;
//
//        // Construir la cadena de texto
//        String diferenciaTiempo = dias + " días, " + horas + " horas, " + minutos + " minutos, " + segundos + " segundos";
//        
//        return diferenciaTiempo;
//    }
//    
}
