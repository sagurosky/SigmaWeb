package mantenimiento.gestorTareas.modulo.equipos;
import mantenimiento.gestorTareas.infraestructura.multitenant.TenantContext;
import mantenimiento.gestorTareas.infraestructura.util.ArchivoExterno;
import mantenimiento.gestorTareas.infraestructura.util.Convertidor;
import mantenimiento.gestorTareas.infraestructura.util.TiempoUtils;
import mantenimiento.gestorTareas.modulo.tareas.Servicio;
import mantenimiento.gestorTareas.modulo.tareas.TareaService;
import mantenimiento.gestorTareas.modulo.tecnicos.Tecnico;
import mantenimiento.gestorTareas.modulo.tecnicos.TecnicoService;
import mantenimiento.gestorTareas.modulo.usuarios.RolDao;
import mantenimiento.gestorTareas.modulo.usuarios.UsuarioDao;
import mantenimiento.gestorTareas.modulo.usuarios.UsuarioService;

import lombok.extern.slf4j.Slf4j;
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
        model.addAttribute("todosLosTecnicos", tecnicoService.findAllByTenant());
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


        return "equipos/preventivos";
    }
    

    @PostMapping("/guardarSugerencia/{activoId}")
    public String guardarSugerencia(@PathVariable("activoId") Long activoId, Model model, @RequestParam("file") MultipartFile imagen, Preventivo preventivo) {

        Activo activoSeleccionado = activo.findById(activoId).orElse(null);
        preventivo.setId(null); // garantizar INSERT y no UPDATE por binding accidental del path {id}
        preventivo.setActivo(activoSeleccionado);
        preventivo.setEstado("pendiente");
        preventivo.setFechaDeCreacion(TiempoUtils.ahora());

        Authentication aut = SecurityContextHolder.getContext().getAuthentication();
        preventivo.setSolicita(aut.getName());

        // el metodo save devuelve la instancia actualizada (con el id). una maravilla! siempre se sigue aprendiendo
        preventivo = preventivoService.save(preventivo);

        if (!imagen.isEmpty()) {
            String ruta = ArchivoExterno.getImagenesPath();
            Path carpetaImagenes = Paths.get(ruta);
            if (!Files.exists(carpetaImagenes)) {
                try {
                    Files.createDirectories(carpetaImagenes);
                } catch (IOException e) {
                    // Manejar error si ocurre al crear la carpeta
                }
            }

            try {
                byte[] bytes = imagen.getBytes();
                Path rutaCompleta = Paths.get(ruta + "//" + preventivo.getId() + imagen.getOriginalFilename().replace(" ", ""));
                Files.write(rutaCompleta, bytes);
                preventivo.setImagen("" + preventivo.getId() + imagen.getOriginalFilename().replace(" ", ""));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        preventivoService.save(preventivo);
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
