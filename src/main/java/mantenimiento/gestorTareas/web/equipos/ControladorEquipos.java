package mantenimiento.gestorTareas.web.equipos;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import mantenimiento.gestorTareas.datos.ActivoDao;
import mantenimiento.gestorTareas.datos.AsignacionPreventivoDao;
import mantenimiento.gestorTareas.datos.RolDao;
import mantenimiento.gestorTareas.datos.UsuarioDao;
import mantenimiento.gestorTareas.dominio.*;
import mantenimiento.gestorTareas.servicio.ActivoService;
import mantenimiento.gestorTareas.servicio.PreventivoService;
import mantenimiento.gestorTareas.servicio.Servicio;
import mantenimiento.gestorTareas.servicio.TareaService;
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
public class ControladorEquipos {

    @Autowired
    Servicio servicio;
    @Autowired
    UsuarioService usuarioService;
    @Autowired
    UsuarioDao usuarioDao;
    @Autowired
    RolDao rolDao;
    @Autowired
    ActivoService activoService;
    @Autowired
    TareaService tareaService;
    @Autowired
    TecnicoService tecnicoService;
    @Autowired
    ActivoDao activo;
    @Autowired
    PreventivoService preventivoService;
    @Autowired
    AsignacionPreventivoDao asignacionPreventivoService;
//    dejo ejemplo paravolver a armar plantilla cuando sea necesario
//    @GetMapping("/aplicadoresDeAdhesivo")
//    public String aplicadoresDeAdhesivo(Model model) {
//        Activo activo = activoService.findByName("aplicadores de adhesivo");
//
//        cargarModel(model, activo);
//
//        return "equipos/activo";
//    }

    //planta 3
    @GetMapping("/inicio/activo/{nombre}")
    public String activo(Model model, @PathVariable String nombre) throws IOException {
        log.info("#### "+nombre+" tenant "+TenantContext.getTenantId());
           Activo activo = activoService.findByName(nombre);
//        log.info("#### "+activo!=null?activo.getNombre():"nada");
           cargarModel(model, activo);
        model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());
            return "equipos/activo";
        }


    
    //endpoint usado para volver a la pagina del activo desde crear tarea
    @GetMapping("/inicio/activoReturn/{id}")
    public String activoVolver(Model model, Tarea tarea, Activo activoRequest) {

        Activo activoSeleccionado = activo.findById(activoRequest.getId()).orElse(null);
        String url = Convertidor.aCamelCase(activoSeleccionado.getNombre());
        
        //me aseguro que el primer caracter sea minuscula sino falla
        char primerCaracterMinuscula = Character.toLowerCase(url.charAt(0));
        url=url.substring(1);
        url=primerCaracterMinuscula+url;
        
        return "redirect:/activo/" + url;
    }

    @PostMapping("/cambiarEstado/{id}")
    public String cambiarEstado( Model model,  Activo activoRequest) {
        Activo activoSeleccionado = activo.findById(activoRequest.getId()).orElse(null);
        activoSeleccionado.setEstado(activoRequest.getEstado());
        activo.save(activoSeleccionado);
        
        String url = Convertidor.aCamelCase(activoSeleccionado.getNombre());
        
        //me aseguro que el primer caracter sea minuscula sino falla
        char primerCaracterMinuscula = Character.toLowerCase(url.charAt(0));
        url=url.substring(1);
        url=primerCaracterMinuscula+url;
        
        return "redirect:/activo/" + url;
    }


    @PostMapping("/cambiarPromedioMovil/{id}")
    public String cambiarPromedioMovil( Model model,  Activo activoRequest) {
        Activo activoSeleccionado = activo.findById(activoRequest.getId()).orElse(null);
        activoSeleccionado.setPromedioMovil(activoRequest.getPromedioMovil());
        activo.save(activoSeleccionado);

        String url = Convertidor.aCamelCase(activoSeleccionado.getNombre());

        //me aseguro que el primer caracter sea minuscula sino falla
        char primerCaracterMinuscula = Character.toLowerCase(url.charAt(0));
        url=url.substring(1);
        url=primerCaracterMinuscula+url;

        return "redirect:/activo/" + url;
    }
    
    
    
    @PostMapping("/ponerADisponibilidad/{id}")
    public String ponerADisponibilidad( Model model,  Activo activoRequest) {
        Activo activoSeleccionado = activo.findById(activoRequest.getId()).orElse(null);
        activoSeleccionado.setEstado("disponible");
        activoSeleccionado.setDisponibilidadHasta(activoRequest.getDisponibilidadHasta());
        activoSeleccionado.setDisponibilidadDesde(TiempoUtils.ahora());

        //genero una tarea con estado "disponible" para los cálculos de indicadores
        Tarea tarea=new Tarea();
        tarea.setActivo(activoSeleccionado);
        tarea.setEstado("disponible");
        tarea.setMomentoDetencion(TiempoUtils.ahora());
        tarea.setMomentoLiberacion(activoRequest.getDisponibilidadHasta());

        tareaService.save(tarea);

        activo.save(activoSeleccionado);
        
        String url = Convertidor.aCamelCase(activoSeleccionado.getNombre());
        
        //me aseguro que el primer caracter sea minuscula sino falla
        char primerCaracterMinuscula = Character.toLowerCase(url.charAt(0));
        url=url.substring(1);
        url=primerCaracterMinuscula+url;

        return "redirect:/activo/" + url;
    }
    
    
    @PostMapping("/cancelarDisponibilidad/{id}")
    public String cancelarDisponibilidad( Model model,  Activo activoRequest) {
        Activo activoSeleccionado = activo.findById(activoRequest.getId()).orElse(null);
        activoSeleccionado.setEstado("operativa");
        activoSeleccionado.setDisponibilidadHasta(null);
//        
        activo.save(activoSeleccionado);

        Tarea tarea=tareaService.traerDisponiblePorActivo(activoSeleccionado, TenantContext.getTenantId()).get(0);

        tarea.setEstado("finDisponible");
        tarea.setMomentoLiberacion(TiempoUtils.ahora());

        tareaService.save(tarea);
        
        String url = Convertidor.aCamelCase(activoSeleccionado.getNombre());
        
        //me aseguro que el primer caracter sea minuscula sino falla
        char primerCaracterMinuscula = Character.toLowerCase(url.charAt(0));
        url=url.substring(1);
        url=primerCaracterMinuscula+url;
        
        return "redirect:/activo/" + url;
    }

    @PostMapping("/cerrarCondicionada/{id}")
    public String cerrarCondicionada( Model model,  Activo activoRequest) {
        Activo activoSeleccionado = activo.findById(activoRequest.getId()).orElse(null);
        activoSeleccionado.setEstado("operativa");
        activoSeleccionado.setDisponibilidadHasta(null);
//
        activo.save(activoSeleccionado);

        Tarea tarea=tareaService.traerNoCerradaPorActivo(activoSeleccionado,TenantContext.getTenantId()).get(0);

        tarea.setEstado("cerrada");
        tarea.setMomentoLiberacion(TiempoUtils.ahora());

        tareaService.save(tarea);

        String url = Convertidor.aCamelCase(activoSeleccionado.getNombre());

        //me aseguro que el primer caracter sea minuscula sino falla
        char primerCaracterMinuscula = Character.toLowerCase(url.charAt(0));
        url=url.substring(1);
        url=primerCaracterMinuscula+url;

        return "redirect:/activo/" + url;
    }



    
    
    private void cargarModel(Model model, Activo activo) throws IOException {


        model.addAttribute("activo", activo);
        List<Tecnico> tecnicos=tecnicoService.traerPorTareaEnActivo(activo,TenantContext.getTenantId());
        model.addAttribute("tecnicosAsignar", tecnicos);
        List<Tarea> tareas = servicio.listar();
        Integer cantidadMecanicas = 0;
        Integer cantidadElectronicas = 0;
        Integer cantidadNeumaticas = 0;
        Integer cantidadHidraulicas = 0;
        Integer cantidadProgramacion = 0;

        for (Tarea tarea : tareas) {
            if(tarea.getCategoriaTecnica()!=null) {
                if (tarea.getCategoriaTecnica().equals("mecánica")) {
                    cantidadMecanicas++;
                }
                if (tarea.getCategoriaTecnica().equals("hidráulica")) {
                    cantidadHidraulicas++;
                }
                if (tarea.getCategoriaTecnica().equals("neumática")) {
                    cantidadNeumaticas++;
                }
                if (tarea.getCategoriaTecnica().equals("electrónica")) {
                    cantidadElectronicas++;
                }
                if (tarea.getCategoriaTecnica().equals("programación")) {
                    cantidadProgramacion++;
                }
            }
        }
        model.addAttribute("cantidadMecanicas", cantidadMecanicas);
        model.addAttribute("cantidadHidraulicas", cantidadHidraulicas);
        model.addAttribute("cantidadNeumaticas", cantidadNeumaticas);
        model.addAttribute("cantidadElectronicas", cantidadElectronicas);
        model.addAttribute("cantidadProgramacion", cantidadProgramacion);
        model.addAttribute("linkFoto", "/recursos/imagenes/" + activo.getNombreCamelCase().replace(" ", "") +"Tenant"+ TenantContext.getTenantId()+".jpg");

        //indicadores
        //mtbf = promedio de minutos en funcionamiento entre fallas, 
        //mttr= promedio de minutos de parada
        Double auxCalculoMtbf = 0.0;
        Double auxCalculoMttr = 0.0;
        Double auxCalculoDisponibilidad = 0.0;
        Double auxCalculoEficiancia = 0.0;
        Double totalMinutos = 0.0;

        
        
         //lógica para llevar los ultimos 12 meses anteriores al mes actual para graficar los minutos de detencion de cada mes
        LocalDate fechaActual = LocalDate.now();


         //aca es donde tengo que determinar el tipo de calculo, si es promedio movil por dias o detenciones, si es de acuerdo al ultimo periodo....
        //una vez definido traigo las tareas correspondientes, hay que programarlo en los Dao.
        
        //promedioMovil mes, 3 meses, 6 meses, 1 año. ver como configurarlo, lo hardcodeo a un año
        String haceUnAnio= TiempoUtils.haceAnios(1).toString();
        String hoy=TiempoUtils.ahora().toString();
        List<Tarea> tareasUltimoAnio = tareaService.traerCerradasPorActivoEnRangoDeFecha(activo,haceUnAnio,hoy,TenantContext.getTenantId());

        Map<String,Long> minutosMes=new LinkedHashMap<>();

        // Obtener los últimos 12 meses en orden inverso
            Long minutosDetencion=0L;
        for (int i = 11; i >= 0; i--) {
            LocalDateTime mesAnterior = fechaActual.minusMonths(i).atStartOfDay().withDayOfMonth(1);

            LocalDateTime finMesAnterior = mesAnterior.withDayOfMonth(mesAnterior.toLocalDate().lengthOfMonth())
                                          .withHour(23)
                                          .withMinute(59)
                                          .withSecond(59);

            
            for (Tarea tarea : tareasUltimoAnio) {
                if(tarea.getMomentoDetencion().isAfter(mesAnterior)&&tarea.getMomentoDetencion().isBefore(finMesAnterior))
                {
                    minutosDetencion+=Duration.between(tarea.getMomentoDetencion(), tarea.getMomentoLiberacion()).toMinutes();
                }   
            }
            
            String nombreMes = mesAnterior.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
            
            minutosMes.put(nombreMes,minutosDetencion );
            minutosDetencion=0L;
           
        }
        model.addAttribute("minutosMes",minutosMes);

LocalDateTime desde=TiempoUtils.haceAnios(1);
if(activo.getPromedioMovil()==null)activo.setPromedioMovil("1 anio");
switch(activo.getPromedioMovil())
{
    case "1 anio": desde=TiempoUtils.haceAnios(1);break;
    case "6 meses": desde=TiempoUtils.ahora().minusMonths(6);break;
    case "3 meses": desde=TiempoUtils.ahora().minusMonths(3);break;
    case "1 mes": desde=TiempoUtils.ahora().minusMonths(1);break;
}

//para que calcule bien los indicadores al principio le pongo la fecha de inicio de actividades
// sino toma todo lo anterior como maquina operativa



//LocalDateTime inicioDeActividades=LocalDateTime.of(2025,6,5,19,8);
LocalDateTime inicioDeActividades= ArchivoExterno.getDateTime("inicioDeActividades");
log.info("inicio de actividades: "+inicioDeActividades);
if(inicioDeActividades!=null)
{
    if(desde.isBefore(inicioDeActividades))desde=inicioDeActividades;
}



        Map<String, Object> resultado = tareaService.calcularIndicadores(desde, TiempoUtils.ahora(),activo,TenantContext.getTenantId());
//        for (Map.Entry<String, Object> entry : resultado.entrySet()) {
//            System.out.println("Clave: " + entry.getKey() + ", Valor: " + entry.getValue());
//        }
        BigDecimal mttrBD = (BigDecimal) resultado.get("mttr");
        BigDecimal mtbfBD = (BigDecimal) resultado.get("mtbf");

        Double mttr = mttrBD != null ? mttrBD.doubleValue() : 0.0;
        Double mtbf = mtbfBD != null ? mtbfBD.doubleValue() : 0.0;


        String mtbfFormateado = String.format("%.2f", mtbf);
        model.addAttribute("mtbf", mtbfFormateado);

        String mttrFormateado = String.format("%.2f", mttr);
        model.addAttribute("mttr", mttrFormateado);

        Double disponibilidad = 0.0;
        if (mtbf + mttr > 0.0) {
            disponibilidad = (mtbf / (mtbf + mttr)) * 100.0;
        }
        String disponibilidadFormateado = String.format("%.2f", disponibilidad);
        model.addAttribute("disponibilidad", disponibilidadFormateado);

        // confiabilidad:  e elevado a (-t/mtbf) resultado multiplicado por 100 para tenerlo en porcentaje
        //voy a usar t=43200 que son los minutos en 30 dias
        //la linea de abajo lo resolvería, considerar si mtbf es 0 para evitar error
        //       Math.exp(-t / MTBF)


        Double confiabilidad = 0.0;
        if (mtbf > 0.0) {
            confiabilidad = Math.exp(-1440.0 / mtbf) * 100.0;
//            log.info("confiabilidad: " + confiabilidad);
        }
        String confiabilidadFormateado = String.format("%.2f", confiabilidad);
        model.addAttribute("confiabilidad", confiabilidadFormateado);

        model.addAttribute("estados", Arrays.asList("detenida", "operativa", "disponible","operativa condicionada"));
        
        var tareasActivo = tareaService.traerNoCerradaPorActivo(activo,TenantContext.getTenantId());
        var tareaActivo=(tareasActivo.size()>0)?tareasActivo.get(0):null;
        model.addAttribute("tarea", tareaActivo);
        model.addAttribute("todosLosTecnicos", tecnicoService.findAllByTenant());
        model.addAttribute("cantidadActivosDetenidos",activoService.findByStatus("detenida").size());
        model.addAttribute("promedios", Arrays.asList("1 anio","6 meses","3 meses","1 mes"));

//        if(tareasIndicadores.size()>1)
            model.addAttribute("pocosDatos",false);
//        else
            model.addAttribute("pocosDatos",true);

        Path carpeta=null;
        if(ArchivoExterno.getString("nube").equals("si"))
        {
             carpeta = Path.of("/media/sf_personal/sigmaweb/recursos/layouts/");
        }else
        {
             carpeta = Path.of("/app/recursos/layouts/");
        }


        List<String> nombresLayouts = Files.list(carpeta)
                .filter(p -> p.toString().endsWith(".svg"))
                .sorted(Comparator.comparingLong(p -> p.toFile().lastModified()))
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
        model.addAttribute("nombresLayouts", nombresLayouts);



        //DMS para el menú
        List<Tecnico> tecnicosFiltrados = tecnicoService.traerHabilitados(TenantContext.getTenantId()).stream()
                .filter(t -> t.getUsuario().getRoles().get(0).getNombre().equals("ROLE_TECNICO"))
                .collect(Collectors.toList());
        model.addAttribute("tecnicos", tecnicosFiltrados);

        model.addAttribute("habilitarGestionUsuarios", ArchivoExterno.getString("editarUsuarios"));
        model.addAttribute("habilitarEditorLayout", ArchivoExterno.getString("editorLayout"));
        model.addAttribute("tiempoRefresco", ArchivoExterno.getString("tiempoRefresco"));

    }

}
