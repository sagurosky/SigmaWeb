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
import mantenimiento.gestorTareas.dominio.*;
import mantenimiento.gestorTareas.servicio.ActivoService;
import mantenimiento.gestorTareas.servicio.AsignacionService;
import mantenimiento.gestorTareas.servicio.PreventivoService;
import mantenimiento.gestorTareas.servicio.ProduccionService;
import mantenimiento.gestorTareas.servicio.TareaService;
import mantenimiento.gestorTareas.servicio.Servicio;
import mantenimiento.gestorTareas.servicio.TecnicoService;
import mantenimiento.gestorTareas.servicio.UsuarioService;
import mantenimiento.gestorTareas.util.TiempoUtils;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Slf4j
public class ControladorAjax {

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
    
    
    
    @GetMapping("/traerPreventivosAlLayout")
    @ResponseBody//con esta anotacion springboot no va a intentar abrir un htrml con el nombre de lo que pongo en return
    public Object traerPreventivosAlLayout(@RequestParam String nombre,@RequestParam String estado) {
        
        return preventivoService.traerPreventivosValidadosPorNombreActivo(Convertidor.aCamelCase(nombre), TenantContext.getTenantId());
    }
    
    
    @GetMapping("/chequearDetencionesNuevas")
    @ResponseBody//con esta anotacion springboot no va a intentar abrir un htrml con el nombre de lo que pongo en return
    public Object chequearDetencionesNuevas() {
        
        
        
        return activoService.findByStatus("detenida").size();
    }
    
    
    
    @GetMapping("/actualizarEstados")
    @ResponseBody//con esta anotacion springboot no va a intentar abrir un htrml con el nombre de lo que pongo en return
    public Object actualizarEstados() {
         //traigo todos los activos y mando a la vista variables de falla cuando estan detenidos o de cierre cuando estan liberadas y faltan cerrar
        List<Activo> activos = activo.findAllByTenant();
        String aux = "";

      //  Map<String,String> model=new HashMap<>();
        Map<String, Map<String, String>> model = new HashMap<>();

        for (Activo activo : activos) {
            aux = Convertidor.aCamelCase(activo.getNombre());

//le paso la variable disponible si la hora cargada de la disponibilidad es mayor a la hora actual  
            if(activo.getDisponibilidadHasta()!=null&&activo.getEstado().equals("disponible"))
            if(TiempoUtils.ahora().isBefore(activo.getDisponibilidadHasta()))
            {
                //DMS creo que ya no lo necesito
              //  model.put("tiempo" + aux, activo.getDisponibilidadHasta().toString());
            }else
            {
                activo.setEstado("operativa");
                Tarea tarea=tareaService.traerDisponiblePorActivo(activo,TenantContext.getTenantId()).get(0);
                tarea.setEstado("finDisponible");
                tareaService.save(tarea);
                activoDao.save(activo);
            }

            Map<String, String> datos = new HashMap<>();
            datos.put("estado", activo.getEstado());
            datos.put("layout", activo.getLayout()); // Asegúrate que getLayout() exista

            model.put(activo.getNombre(), datos);

          //  model.put(activo.getNombre(), activo.getEstado());

        }
        return model;
    }
    
    
    
    
    
    @GetMapping("/traerEstadoActivo")
    @ResponseBody//con esta anotacion springboot no va a intentar abrir un html con el nombre de lo que pongo en return
    public Object traerEstadoActivo(@RequestParam String estadoActual,@RequestParam String activo) {

        Boolean hayCambioDeEstado=false;
        Activo activoBD=activoDao.getById(Long.parseLong(activo));
        var tareasActivo = tareaService.traerNoCerradaPorActivo(activoBD,TenantContext.getTenantId());
        var tareaActivo=(tareasActivo.size()>0)?tareasActivo.get(0):null;
        
        
        
        
        if(estadoActual.equals("operativo"))estadoActual="operativa";
        if(estadoActual.equals("detenido"))estadoActual="detenida";
        if(estadoActual.equals("liberado"))estadoActual="liberada";
      
        if(activoBD.getEstado().equals("detenida"))
        {
            if(tareaActivo.getEstado().equals("enProceso")&&!estadoActual.equals("enProceso"))
            {
                hayCambioDeEstado=true;
            }
            if(tareaActivo.getEstado().equals("abierto"))
            {
                if( !activoBD.getEstado().equals(estadoActual) )
                hayCambioDeEstado=true;  
            }
        }else
        {
            if( !activoBD.getEstado().equals(estadoActual) )
            hayCambioDeEstado=true;  
        }
        return hayCambioDeEstado;
    }
    
    
    
    
    @GetMapping("/traerDatos/{id}")
    @ResponseBody//con esta anotacion springboot no va a intentar abrir un htrml con el nombre de lo que pongo en return
    public Object traerDatos(Produccion produccion) {
        Map<String,Object> datos=new HashMap<>();
        
        Produccion prod=produccionService.findById(produccion.getId()).orElse(null);
        
        datos.put("prod", prod);
        
        
        List<Tarea> tareasEnRango=new ArrayList<>();
        tareasEnRango=tareaService.traerPorLineaEnRangoDeFecha(Convertidor.deCamelCase(prod.getLinea()), prod.getInicio().toString(), prod.getFin().toString(),TenantContext.getTenantId());
        
        
        
        Long minutosInactiva=0L;
            for (Tarea tarea : tareasEnRango) {
                minutosInactiva+=Duration.between(tarea.getMomentoDetencion(), (tarea.getMomentoLiberacion().isAfter(prod.getFin()))?prod.getFin():tarea.getMomentoLiberacion()).toMinutes();
           
            }
        
        
        datos.put("minutosInactiva",minutosInactiva);
       
        String cadenciaTeorica="";
        
        if(prod.getLinea().equals("adulto2"))cadenciaTeorica=prod.getProducto().getCadenciaAdulto2();
        if(prod.getLinea().equals("adulto3"))cadenciaTeorica=prod.getProducto().getCadenciaAdulto3();
        if(prod.getLinea().equals("adulto4"))cadenciaTeorica=prod.getProducto().getCadenciaAdulto4();
        if(prod.getLinea().equals("adulto5"))cadenciaTeorica=prod.getProducto().getCadenciaAdulto5();
        if(prod.getLinea().equals("aposito"))cadenciaTeorica=prod.getProducto().getCadenciaAposito();
        
        
        datos.put("cadenciaTeorica",cadenciaTeorica);
        
        
            
        
        
        
        return datos;
    }
    
    
}
