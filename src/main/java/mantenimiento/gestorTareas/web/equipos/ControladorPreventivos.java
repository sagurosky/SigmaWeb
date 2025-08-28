package mantenimiento.gestorTareas.web.equipos;

import lombok.extern.slf4j.Slf4j;
import mantenimiento.gestorTareas.datos.ActivoDao;
import mantenimiento.gestorTareas.datos.AsignacionPreventivoDao;
import mantenimiento.gestorTareas.datos.RolDao;
import mantenimiento.gestorTareas.datos.UsuarioDao;
import mantenimiento.gestorTareas.dominio.*;
import mantenimiento.gestorTareas.servicio.*;
import mantenimiento.gestorTareas.util.ArchivoExterno;
import mantenimiento.gestorTareas.util.Convertidor;
import mantenimiento.gestorTareas.util.TiempoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@Slf4j
public class ControladorPreventivos {

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




    @GetMapping("/preventivos/{id}")
    public String Preventivos(@PathVariable Long id, Model model) {
        Activo activoSeleccionado = activo.findById(id).orElse(null);

        Preventivo preventivoNuevo = new Preventivo();
        preventivoNuevo.setActivo(activoSeleccionado); // si querés precargarlo

        String url = Convertidor.aCamelCase(activoSeleccionado.getNombre());
        char primerCaracterMinuscula = Character.toLowerCase(url.charAt(0));
        url = primerCaracterMinuscula + url.substring(1);

        model.addAttribute("url", url);
        model.addAttribute("activo", activoSeleccionado);
        model.addAttribute("preventivos", preventivoService.traerPorActivo(activoSeleccionado,TenantContext.getTenantId()));
        model.addAttribute("todosLosTecnicos", tecnicoService.findAll());
        model.addAttribute("cantidadActivosDetenidos", activoService.findByStatus("detenida").size());

        model.addAttribute("preventivo", preventivoNuevo); // 👈 esto es lo que faltaba
        model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());

        //DMS para el menú
        List<Tecnico> tecnicosFiltrados = tecnicoService.traerHabilitados(TenantContext.getTenantId()).stream()
                .filter(t -> t.getUsuario().getRoles().get(0).getNombre().equals("ROLE_TECNICO"))
                .collect(Collectors.toList());
        model.addAttribute("tecnicos", tecnicosFiltrados);

        model.addAttribute("habilitarGestionUsuarios", ArchivoExterno.getString("editarUsuarios"));
        model.addAttribute("habilitarEditorLayout", ArchivoExterno.getString("editorLayout"));
        model.addAttribute("tiempoRefresco", ArchivoExterno.getString("tiempoRefresco"));


        return "preventivos";
    }
    

    
    @PostMapping("/guardarSugerencia/{id}")
//    public String guardarSugerencia(@Param("descripcion")String descripcion, Model model,  Activo activoRequest,Preventivo preventivo) {
    public String guardarSugerencia( Model model, @RequestParam("file") MultipartFile imagen,  Activo activoRequest,Preventivo preventivo) {
      
//        Preventivo preventivo=new Preventivo();
//        preventivo.setDescripcion(descripcion);
        
        Activo activoSeleccionado = activo.findById(activoRequest.getId()).orElse(null);
         preventivo.setActivo(activoSeleccionado);
         preventivo.setEstado("pendiente");
         preventivo.setFechaDeCreacion(TiempoUtils.ahora());

          Authentication aut = SecurityContextHolder.getContext().getAuthentication();
        preventivo.setSolicita(aut.getName());
         
        
        //el metodo save devuelve la instancia actualizada (con el id). una maravilla! siempre se sigue aprendiendo
         preventivo= preventivoService.save(preventivo);
        
        if (!imagen.isEmpty()) {
            // Path directorioImagenes = Paths.get("src//main//resources//static//imagenes");
            // String ruta = directorioImagenes.toFile().getAbsolutePath();
            //voy a usar un directorio no relativo para evitar la necesidad de actualizar
            //cada vez que se agrega una imagen nueva

            String ruta=null;
            if(ArchivoExterno.getString("nube").equals("si"))
            {
                ruta = "/media/sf_personal/sigmaweb/recursos/imagenes/";
            }else
            {
                ruta = "/app/recursos/imagenes/";
            }
            

            try {
                byte[] bytes = imagen.getBytes();
                //tengo que trimearlo
                Path rutaCompleta = Paths.get(ruta + "//" +preventivo.getId()+ imagen.getOriginalFilename().replace(" ",""));
                Files.write(rutaCompleta, bytes);
                preventivo.setImagen(""+preventivo.getId()+imagen.getOriginalFilename().replace(" ",""));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        
        
        
//        activo.save(activoSeleccionado);
        preventivoService.save(preventivo);
        String url = Convertidor.aCamelCase(activoSeleccionado.getNombre());
        
        //me aseguro que el primer caracter sea minuscula sino falla
        char primerCaracterMinuscula = Character.toLowerCase(url.charAt(0));
        url=url.substring(1);
        url=primerCaracterMinuscula+url;
        
        return "redirect:/preventivos/" + activoSeleccionado.getId();
    }

    @GetMapping("/eliminarPreventivo/{id}")
    public String eliminarPreventivo(@PathVariable Long id) {

        // 1. Buscar el preventivo
        Preventivo preventivo = preventivoService.findById(id).orElse(null);
        if (preventivo == null) {
            return "redirect:/preventivos"; // manejar caso no encontrado
        }

        Long activoId = preventivo.getActivo().getId(); // lo usás para el redirect

        // 2. Borrar asignaciones relacionadas
        List<AsignacionPreventivo> asignaciones = preventivo.getAsignaciones();
        if (asignaciones != null && !asignaciones.isEmpty()) {
            asignacionPreventivoService.deleteAll(asignaciones);
        }

        // 3. Borrar el preventivo
        preventivoService.delete(preventivo);

        return "redirect:/preventivos/" + activoId;
    }
    
    @GetMapping("/validarPreventivo/{id}")
    public String validarPreventivo(Model model, Preventivo preventivo) {
        
        Preventivo preventivoBd=preventivoService.findById(preventivo.getId()).orElse(null);
        preventivoBd.setEstado("validado");
       preventivoService.save(preventivoBd);
       
        return "redirect:/preventivos/" + preventivoBd.getActivo().getId();
    }
    
//    @GetMapping("/cerrarPreventivo/{id}")
//    public String cerrarPreventivo(Model model, Preventivo preventivo) {
        
    @PostMapping("/cerrarPreventivo/{id}")
    public String cerrarPreventivo(@PathVariable Long id, @RequestParam("tecnicosIds") List<Long> tecnicosIds) {
    
        
         Preventivo preventivoBd=preventivoService.findById(id).orElse(null);
        preventivoBd.setEstado("cerrado");
        preventivoBd.setFechaRealizado(TiempoUtils.ahora());
        
        
        Tecnico tec=new Tecnico();
        
        for (Long tecnicosId : tecnicosIds) {
            AsignacionPreventivo asignacion = new AsignacionPreventivo();
            tec=tecnicoService.findById(tecnicosId).orElse(null);
            asignacion.setPreventivo(preventivoBd);
            asignacion.setTecnico(tec);
            preventivoBd.getAsignaciones().add(asignacion);
        }
        
       preventivoService.save(preventivoBd);
       
        return "redirect:/preventivos/" + preventivoBd.getActivo().getId();
    }
    
    


}
