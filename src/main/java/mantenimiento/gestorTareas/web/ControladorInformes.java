package mantenimiento.gestorTareas.web;

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
import mantenimiento.gestorTareas.datos.ActivoDao;
import mantenimiento.gestorTareas.datos.RolDao;
import mantenimiento.gestorTareas.datos.UsuarioDao;
import mantenimiento.gestorTareas.dominio.*;
import mantenimiento.gestorTareas.servicio.ActivoService;
import mantenimiento.gestorTareas.servicio.AsignacionService;
import mantenimiento.gestorTareas.servicio.InformeService;
import mantenimiento.gestorTareas.servicio.ProduccionService;
import mantenimiento.gestorTareas.servicio.ProductoService;
import mantenimiento.gestorTareas.servicio.TareaService;
import mantenimiento.gestorTareas.servicio.Servicio;
import mantenimiento.gestorTareas.servicio.TecnicoService;
import mantenimiento.gestorTareas.servicio.UsuarioService;
import mantenimiento.gestorTareas.util.ArchivoExterno;
import mantenimiento.gestorTareas.util.Convertidor;
import mantenimiento.gestorTareas.util.TiempoUtils;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

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
            String rol="";
            for (Rol role : usuario.getRoles()) {
            
                if (role.getNombre().equals("ROLE_TECNICO"))rol="tecnico";
                if (role.getNombre().equals("ROLE_MANT"))rol="mant";
                if (role.getNombre().equals("ROLE_PROD"))rol="prod";
                if (role.getNombre().equals("ROLE_MONITOR"))rol="monitor";
                if (role.getNombre().equals("ROLE_ADMIN"))
                {
                    rol="admin";
                    break;
                }
        }
            
            //al cerrarse la intervencion se genera el objeto informe con estado noEvaluado,
            //primero debe seleccionarse la intervencion que merezca tener un informe ligado, esto lo puede hacer el técnico o el supervisor, el técnico no puede descartarla
            //al ser seleccionada la intervencion su informe pasa a tener estado "pendiente" y se mostrará en el listado para generar informes,
            //al terminar el informe pasa a estado "noAprobado" para que el supervisor de el visto bueno y lo valide
            //al al validar el informe pasa a estado aprobado y pasa a formar parte de los registros para consulta
      
            if (rol.equals("tecnico"))
        {
            
            Tecnico tecnico = tecnicoService.traerPorUsuario(usuario,TenantContext.getTenantId());
            List<Tarea> tareasNoEvaluadas=tareaService.traerPorTecnicoYEstadoInforme(tecnico,"noEvaluado", TiempoUtils.haceAnios(1), TiempoUtils.ahora(), TenantContext.getTenantId());
            model.addAttribute("tareasNoEvaluadas",tareasNoEvaluadas);
             List<Tarea> tareasInformePendienteTecnico=tareaService.traerPorTecnicoYEstadoInforme(tecnico,"pendiente",TiempoUtils.haceAnios(1), TiempoUtils.ahora(),TenantContext.getTenantId());
              List<Tarea> tareasInformeEnRevisionTecnico=tareaService.traerPorTecnicoYEstadoInforme(tecnico,"EnRevision",TiempoUtils.haceAnios(1), TiempoUtils.ahora(),TenantContext.getTenantId());
              tareasInformePendienteTecnico.addAll(tareasInformeEnRevisionTecnico);
            model.addAttribute("tareasInformePendienteTecnico",tareasInformePendienteTecnico);
          List<Tarea> tareasAprobados=tareaService.traerPorEstadoInforme("aprobado",TiempoUtils.haceAnios(1), TiempoUtils.ahora(),TenantContext.getTenantId());
            model.addAttribute("tareasAprobados",tareasAprobados);
            
        }
        else 
        if (rol.equals("mant")||rol.equals("admin"))
        {
//            List<Tarea> tareasNoEvaluadas=tareaService.traerPorEstadoInforme("noEvaluado");
//            model.addAttribute("tareasNoEvaluadas",tareasNoEvaluadas);
//            List<Tarea> tareasNoValidadas=tareaService.traerPorEstadoInforme("noValidado");
//            model.addAttribute("tareasNoValidadas",tareasNoValidadas);
            List<Tarea> tareasNoEvaluadas=tareaService.traerPorEstadoInforme("noEvaluado",TiempoUtils.haceAnios(1), TiempoUtils.ahora(),TenantContext.getTenantId());
            model.addAttribute("tareasNoEvaluadas",tareasNoEvaluadas);
            List<Tarea> tareasNoAprobadas=tareaService.traerPorEstadoInforme("noAprobado",TiempoUtils.haceAnios(1), TiempoUtils.ahora(),TenantContext.getTenantId());
            model.addAttribute("tareasNoAprobadas",tareasNoAprobadas);
            List<Tarea> tareasAprobados=tareaService.traerPorEstadoInforme("aprobado",TiempoUtils.haceAnios(1), TiempoUtils.ahora(),TenantContext.getTenantId());
            model.addAttribute("tareasAprobados",tareasAprobados);
            List<Tarea> tareasInformePendiente=tareaService.traerPorEstadoInforme("pendiente",TiempoUtils.haceAnios(1), TiempoUtils.ahora(),TenantContext.getTenantId());
             List<Tarea> tareasInformeEnRevision=tareaService.traerPorEstadoInforme("EnRevision",TiempoUtils.haceAnios(1), TiempoUtils.ahora(),TenantContext.getTenantId());
              tareasInformePendiente.addAll(tareasInformeEnRevision);
            model.addAttribute("tareasInformePendiente",tareasInformePendiente);
        }
       
        else
        {
          List<Tarea> tareasAprobados=tareaService.traerPorEstadoInforme("aprobado",TiempoUtils.haceAnios(1), TiempoUtils.ahora(),TenantContext.getTenantId());
            model.addAttribute("tareasAprobados",tareasAprobados);
        }
        
         model.addAttribute("todosLosTecnicos",tecnicoService.findAll());
        model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());
        //DMS para el menú
        List<Tecnico> tecnicosFiltrados = tecnicoService.traerHabilitados(TenantContext.getTenantId()).stream()
                .filter(t -> t.getUsuario().getRoles().get(0).getNombre().equals("ROLE_TECNICO"))
                .collect(Collectors.toList());
        model.addAttribute("tecnicos", tecnicosFiltrados);

        model.addAttribute("habilitarGestionUsuarios", ArchivoExterno.getString("editarUsuarios"));
        model.addAttribute("habilitarEditorLayout", ArchivoExterno.getString("editorLayout"));
        model.addAttribute("tiempoRefresco", ArchivoExterno.getString("tiempoRefresco"));
         return "informes";
    } 
    @GetMapping("/generarInforme/{id}")
    public String generarInforme(Model model,@PathVariable("id") Long id)
    {
           Tarea tareaBD =tareaService.findById(id).orElse(null);
           model.addAttribute("tarea",tareaBD);
           model.addAttribute("informe",tareaBD.getInforme());
           model.addAttribute("todosLosTecnicos",tecnicoService.findAll());
           

           // Calcular la diferencia
        Duration duracion = Duration.between(tareaBD.getMomentoDetencion(), tareaBD.getMomentoLiberacion());

        // Formatear la diferencia en horas, minutos y segundos
        String diferenciaFormateada = String.format("%02d:%02d:%02d",
            duracion.toHours(),
            duracion.toMinutesPart(),
            duracion.toSecondsPart()
        );
           model.addAttribute("tiempoDetenido",diferenciaFormateada);
        model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());

        return "/informe";
    } 
    @GetMapping("/seleccionarTareaParaInforme/{id}")
    public String seleccionarTareaParaInforme(Model model,@PathVariable("id") Long id)
    {
           Tarea tareaBD =tareaService.findById(id).orElse(null);
           tareaBD.getInforme().setEstadoInforme("pendiente");
           informeService.save(tareaBD.getInforme());
           
           model.addAttribute("tarea",tareaBD);
        return informes(model);
    } 
    @GetMapping("/descartarTareaParaInforme/{id}")
    public String descartarTareaParaInforme(Model model,@PathVariable("id") Long id)
    {
           Tarea tareaBD =tareaService.findById(id).orElse(null);
           //esto me guardaba los objetos informes vacíos con el estado "descartado" ahora voy a eliminar los registros para mantener limpia la BD, si da problemas 
           //vuelvo a guardarlos con estado descartado
           tareaBD.getInforme().setEstadoInforme("descartado");
           informeService.save(tareaBD.getInforme());
           model.addAttribute("tarea",tareaBD);
        return informes(model);
    } 
    
    
    
    
    @PostMapping("/guardarInforme/{id}")
    public String guardar(Model model, @Param("url") String url, Informe informe ) {
        
        
       Usuario usuario = usuarioDao.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        Tecnico tecnicoGenerador=tecnicoService.traerPorUsuario(usuario,TenantContext.getTenantId());
        
        
        
        
        
        informe.setEstadoInforme("noAprobado");
        
        informeService.save(informe);
         model.addAttribute("url",url);
        
        return informes(model);
    }
    
    
    
    @GetMapping("/verInforme/{id}")
    public String verInforme(Model model,@PathVariable("id") Long id)
    {
           Tarea tareaBD =tareaService.findById(id).orElse(null);
           model.addAttribute("tarea",tareaBD);
           model.addAttribute("informe",tareaBD.getInforme());
           model.addAttribute("todosLosTecnicos",tecnicoService.findAll());
           

           // Calcular la diferencia
        Duration duracion = Duration.between(tareaBD.getMomentoDetencion(), tareaBD.getMomentoLiberacion());

        // Formatear la diferencia en horas, minutos y segundos
        String diferenciaFormateada = String.format("%02d:%02d:%02d",
            duracion.toHours(),
            duracion.toMinutesPart(),
            duracion.toSecondsPart()
        );
           model.addAttribute("tiempoDetenido",diferenciaFormateada);
        model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());

        //DMS para el menú
        List<Tecnico> tecnicosFiltrados = tecnicoService.traerHabilitados(TenantContext.getTenantId()).stream()
                .filter(t -> t.getUsuario().getRoles().get(0).getNombre().equals("ROLE_TECNICO"))
                .collect(Collectors.toList());
        model.addAttribute("tecnicos", tecnicosFiltrados);

        model.addAttribute("habilitarGestionUsuarios", ArchivoExterno.getString("editarUsuarios"));
        model.addAttribute("habilitarEditorLayout", ArchivoExterno.getString("editorLayout"));
        model.addAttribute("tiempoRefresco", ArchivoExterno.getString("tiempoRefresco"));


        return "/informe";
    } 
    @GetMapping("/validar/{id}")
    public String validar(Model model,@PathVariable("id") Long id)
    {
           Tarea tareaBD =tareaService.findById(id).orElse(null);
           Informe informe=tareaBD.getInforme();
           informe.setEstadoInforme("aprobado");
           informe.setFechaDeCreacion(TiempoUtils.ahora());
           
           
           
        Tecnico tec=new Tecnico();
        
        for (Asignacion asignacionTarea : tareaBD.getAsignaciones()) {
            
            AsignacionInforme asignacion = new AsignacionInforme();
            tec=tecnicoService.findById(asignacionTarea.getTecnico().getId()).orElse(null);
            asignacion.setInforme(informe);
            asignacion.setTecnico(tec);
            informe.getAsignaciones().add(asignacion);
        }
           
           
           
           
           
           
           informeService.save(informe);
           
           
           
        return informes(model);
    } 
    
    @PostMapping("/enviarARevision/{id}")
    public String enviarARevision(Model model, @Param("url") String url, Informe informe ) 
    {
        Informe informeBd=informeService.findById(informe.getId()).orElse(null);
           informeBd.setRevision(informe.getRevision());
           informeBd.setEstadoInforme("enRevision");
           informeService.save(informeBd);
           
           
        return informes(model);
    } 
    
    
    
    
    
    
    
}
