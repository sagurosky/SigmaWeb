package mantenimiento.gestorTareas.web;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Array;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import mantenimiento.gestorTareas.datos.ActivoDao;
import mantenimiento.gestorTareas.datos.RolDao;
import mantenimiento.gestorTareas.datos.UsuarioDao;
import mantenimiento.gestorTareas.dominio.*;
import mantenimiento.gestorTareas.servicio.ActivoService;
import mantenimiento.gestorTareas.servicio.InformeService;
import mantenimiento.gestorTareas.servicio.PreventivoService;
import mantenimiento.gestorTareas.servicio.TareaService;
import mantenimiento.gestorTareas.servicio.Servicio;
import mantenimiento.gestorTareas.servicio.TecnicoService;
import mantenimiento.gestorTareas.servicio.UsuarioService;
import mantenimiento.gestorTareas.util.ArchivoExterno;
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

@Controller
@Slf4j
public class ControladorTecnicos {

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
    PreventivoService preventivoService;
    @Autowired
    InformeService informeService;

    @GetMapping("/perfil")
    public String perfil(Model model) {
        // var tareas = tareaService.traerNoCerradas();
        // model.addAttribute("tareas", tareas);
        Usuario usuario = usuarioDao.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        Tecnico tecnico = tecnicoService.traerPorUsuario(usuario, TenantContext.getTenantId());
        model.addAttribute("tecnico", tecnico);
        model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());

        // DMS para el menú
        List<Tecnico> tecnicosFiltrados = tecnicoService.traerHabilitados(TenantContext.getTenantId()).stream()
                .filter(t -> t.getUsuario().getRoles().get(0).getNombre().equals("ROLE_TECNICO"))
                .collect(Collectors.toList());
        model.addAttribute("tecnicos", tecnicosFiltrados);

        model.addAttribute("habilitarGestionUsuarios", ArchivoExterno.getString("editarUsuarios"));
        model.addAttribute("habilitarEditorLayout", ArchivoExterno.getString("editorLayout"));
        model.addAttribute("tiempoRefresco", ArchivoExterno.getString("tiempoRefresco"));
        return "tecnicoDatosPersonales";
    }

    // cuando el administrador llama desde layout lleva a la vista con los datos
    // completos
    @GetMapping("/perfilTecnico")
    public String verPerfilTecnico(@RequestParam("id") Long id, @RequestParam("url") String url, Model model) {
        Tecnico tecnico = tecnicoService.findById(id).orElse(null);
        model.addAttribute("tecnico", tecnico);
        if (!(url.equals("/layout") || url.equals("/layoutPlanta2"))) {
            model.addAttribute("url", "/activo/" + Convertidor.aCamelCase(url));
        } else {
            model.addAttribute("url", url);
        }

        // me queda pendiente usar reflexion para hacer automatico al agregar una
        // propiedad a la entidad

        Double satisfaccion = 0.0;
        Double predisposicion = 0.0;
        Double responsabilidad = 0.0;
        Double seguridad = 0.0;
        Double conocimiento = 0.0;
        Double trato = 0.0;
        Double prolijidad = 0.0;
        Double puntualidad = 0.0;
        Double eficiencia = 0.0;
        Double calidad = 0.0;
        Double comunicacion = 0.0;
        Double trabajoEnEquipo = 0.0;
        Double resolucion = 0.0;
        Double creatividad = 0.0;
        Double iniciativa = 0.0;
        Double autogestion = 0.0;
        Double formacionContinua = 0.0;

        // variables para incrementar cuando la calificacion es null o vacia y despues
        // calculo bien el promedio
        int satisfaccionCantidadnNull = 0;
        int predisposicionCantidadnNull = 0;
        int responsabilidadCantidadnNull = 0;
        int seguridadCantidadnNull = 0;
        int conocimientoCantidadnNull = 0;
        int tratoCantidadnNull = 0;
        int prolijidadCantidadnNull = 0;
        int puntualidadCantidadnNull = 0;
        int eficienciaCantidadnNull = 0;
        int calidadCantidadnNull = 0;
        int comunicacionCantidadnNull = 0;
        int trabajoEnEquipoCantidadnNull = 0;
        int resolucionCantidadnNull = 0;
        int creatividadCantidadnNull = 0;
        int iniciativaCantidadnNull = 0;
        int autogestionCantidadnNull = 0;
        int formacionContinuaCantidadnNull = 0;

        // traigo todas ls tareas en las que participó el tecnico en cuestion
        List<Tarea> tareas = tareaService.traerPorTecnico(tecnico, TiempoUtils.haceAnios(1), TiempoUtils.ahora(),
                TenantContext.getTenantId());
        for (Tarea tarea : tareas) {
            if (tarea == null)
                continue;
            if (tarea.getEvaluacion() == null)
                continue;
            // Safely extract each evaluation field, ignoring null or blank values
            String val;
            // Satisfaccion
            val = tarea.getEvaluacion().getSatisfaccion();
            if (val != null && !val.isBlank()) {
                satisfaccion += Double.parseDouble(val);
            } else {
                satisfaccionCantidadnNull++;
            }
            // Predisposicion
            val = tarea.getEvaluacion().getPredisposicion();
            if (val != null && !val.isBlank()) {
                predisposicion += Double.parseDouble(val);
            } else {
                predisposicionCantidadnNull++;
            }
            // Responsabilidad
            val = tarea.getEvaluacion().getResponsabilidad();
            if (val != null && !val.isBlank()) {
                responsabilidad += Double.parseDouble(val);
            } else {
                responsabilidadCantidadnNull++;
            }
            // Seguridad
            val = tarea.getEvaluacion().getSeguridad();
            if (val != null && !val.isBlank()) {
                seguridad += Double.parseDouble(val);
            } else {
                seguridadCantidadnNull++;
            }
            // Conocimiento
            val = tarea.getEvaluacion().getConocimiento();
            if (val != null && !val.isBlank()) {
                conocimiento += Double.parseDouble(val);
            } else {
                conocimientoCantidadnNull++;
            }
            // Trato
            val = tarea.getEvaluacion().getTrato();
            if (val != null && !val.isBlank()) {
                trato += Double.parseDouble(val);
            } else {
                tratoCantidadnNull++;
            }
            // Prolijidad
            val = tarea.getEvaluacion().getProlijidad();
            if (val != null && !val.isBlank()) {
                prolijidad += Double.parseDouble(val);
            } else {
                prolijidadCantidadnNull++;
            }
            // Puntualidad
            val = tarea.getEvaluacion().getPuntualidad();
            if (val != null && !val.isBlank()) {
                puntualidad += Double.parseDouble(val);
            } else {
                puntualidadCantidadnNull++;
            }
            // Eficiencia
            val = tarea.getEvaluacion().getEficiencia();
            if (val != null && !val.isBlank()) {
                eficiencia += Double.parseDouble(val);
            } else {
                eficienciaCantidadnNull++;
            }
            // Calidad
            val = tarea.getEvaluacion().getCalidad();
            if (val != null && !val.isBlank()) {
                calidad += Double.parseDouble(val);
            } else {
                calidadCantidadnNull++;
            }
            // Comunicacion
            val = tarea.getEvaluacion().getComunicacion();
            if (val != null && !val.isBlank()) {
                comunicacion += Double.parseDouble(val);
            } else {
                comunicacionCantidadnNull++;
            }
            // TrabajoEnEquipo
            val = tarea.getEvaluacion().getTrabajoEnEquipo();
            if (val != null && !val.isBlank()) {
                trabajoEnEquipo += Double.parseDouble(val);
            } else {
                trabajoEnEquipoCantidadnNull++;
            }
            // Resolucion
            val = tarea.getEvaluacion().getResolucion();
            if (val != null && !val.isBlank()) {
                resolucion += Double.parseDouble(val);
            } else {
                resolucionCantidadnNull++;
            }
            // Creatividad
            val = tarea.getEvaluacion().getCreatividad();
            if (val != null && !val.isBlank()) {
                creatividad += Double.parseDouble(val);
            } else {
                creatividadCantidadnNull++;
            }
            // Iniciativa
            val = tarea.getEvaluacion().getIniciativa();
            if (val != null && !val.isBlank()) {
                iniciativa += Double.parseDouble(val);
            } else {
                iniciativaCantidadnNull++;
            }
            // Autogestion
            val = tarea.getEvaluacion().getAutogestion();
            if (val != null && !val.isBlank()) {
                autogestion += Double.parseDouble(val);
            } else {
                autogestionCantidadnNull++;
            }
            // FormacionContinua
            val = tarea.getEvaluacion().getFormacionContinua();
            if (val != null && !val.isBlank()) {
                formacionContinua += Double.parseDouble(val);
            } else {
                formacionContinuaCantidadnNull++;
            }
        }

        // promedio con la cantidad de tareas menos la cantidad en las que no se
        // calificó

        satisfaccion = satisfaccion / (tareas.size() - satisfaccionCantidadnNull);
        predisposicion = predisposicion / (tareas.size() - predisposicionCantidadnNull);
        responsabilidad = responsabilidad / (tareas.size() - responsabilidadCantidadnNull);
        seguridad = seguridad / (tareas.size() - seguridadCantidadnNull);
        conocimiento = conocimiento / (tareas.size() - conocimientoCantidadnNull);
        trato = trato / (tareas.size() - tratoCantidadnNull);
        prolijidad = prolijidad / (tareas.size() - prolijidadCantidadnNull);
        puntualidad = puntualidad / (tareas.size() - puntualidadCantidadnNull);
        eficiencia = eficiencia / (tareas.size() - eficienciaCantidadnNull);
        calidad = calidad / (tareas.size() - calidadCantidadnNull);
        comunicacion = comunicacion / (tareas.size() - comunicacionCantidadnNull);
        trabajoEnEquipo = trabajoEnEquipo / (tareas.size() - trabajoEnEquipoCantidadnNull);
        resolucion = resolucion / (tareas.size() - resolucionCantidadnNull);
        creatividad = creatividad / (tareas.size() - creatividadCantidadnNull);
        iniciativa = iniciativa / (tareas.size() - iniciativaCantidadnNull);
        autogestion = autogestion / (tareas.size() - autogestionCantidadnNull);
        formacionContinua = formacionContinua / (tareas.size() - formacionContinuaCantidadnNull);

        model.addAttribute("satisfaccion", Math.round(satisfaccion * 100.0) / 100.0);
        model.addAttribute("predisposicion", Math.round(predisposicion * 100.0) / 100.0);
        model.addAttribute("responsabilidad", Math.round(responsabilidad * 100.0) / 100.0);
        model.addAttribute("seguridad", Math.round(seguridad * 100.0) / 100.0);
        model.addAttribute("conocimiento", Math.round(conocimiento * 100.0) / 100.0);
        model.addAttribute("trato", Math.round(trato * 100.0) / 100.0);
        model.addAttribute("prolijidad", Math.round(prolijidad * 100.0) / 100.0);
        model.addAttribute("puntualidad", Math.round(puntualidad * 100.0) / 100.0);
        model.addAttribute("eficiencia", Math.round(eficiencia * 100.0) / 100.0);
        model.addAttribute("calidad", Math.round(calidad * 100.0) / 100.0);
        model.addAttribute("comunicacion", Math.round(comunicacion * 100.0) / 100.0);
        model.addAttribute("trabajoEnEquipo", Math.round(trabajoEnEquipo * 100.0) / 100.0);
        model.addAttribute("resolucion", Math.round(resolucion * 100.0) / 100.0);
        model.addAttribute("creatividad", Math.round(creatividad * 100.0) / 100.0);
        model.addAttribute("iniciativa", Math.round(iniciativa * 100.0) / 100.0);
        model.addAttribute("autogestion", Math.round(autogestion * 100.0) / 100.0);
        model.addAttribute("formacionContinua", Math.round(formacionContinua * 100.0) / 100.0);

        int cuentaNull = 0;

        Double promedioGral = 0.0;

        promedioGral = ((!Double.isNaN(satisfaccion) ? satisfaccion : cuentaNull++) +
                (!Double.isNaN(predisposicion) ? predisposicion : cuentaNull++) +
                (!Double.isNaN(responsabilidad) ? responsabilidad : cuentaNull++) +
                (!Double.isNaN(seguridad) ? seguridad : cuentaNull++) +
                (!Double.isNaN(conocimiento) ? conocimiento : cuentaNull++) +
                (!Double.isNaN(trato) ? trato : cuentaNull++) +
                (!Double.isNaN(prolijidad) ? prolijidad : cuentaNull++) +
                (!Double.isNaN(puntualidad) ? puntualidad : cuentaNull++) +
                (!Double.isNaN(eficiencia) ? eficiencia : cuentaNull++) +
                (!Double.isNaN(calidad) ? calidad : cuentaNull++) +
                (!Double.isNaN(comunicacion) ? comunicacion : cuentaNull++) +
                (!Double.isNaN(trabajoEnEquipo) ? trabajoEnEquipo : cuentaNull++) +
                (!Double.isNaN(resolucion) ? resolucion : cuentaNull++) +
                (!Double.isNaN(creatividad) ? creatividad : cuentaNull++) +
                (!Double.isNaN(iniciativa) ? iniciativa : cuentaNull++) +
                (!Double.isNaN(autogestion) ? autogestion : cuentaNull++) +
                (!Double.isNaN(formacionContinua) ? formacionContinua : cuentaNull++)) / (17 - cuentaNull);
        if (cuentaNull == 17)
            promedioGral = 0.0;

        tecnico.setPromedioEvaluaciones(Double.toString(Math.round(promedioGral * 100.0) / 100.0));

        Integer preventivosTotal = preventivoService.traerPorTecnico(tecnico, TenantContext.getTenantId()).size();
        Integer preventivosMes = preventivoService
                .traerPorTecnicoEnRangoDeFecha(tecnico, TiempoUtils.ahora().minusMonths(1).toString(),
                        TiempoUtils.ahora().toString(), TenantContext.getTenantId())
                .size();
        Integer preventivosAnio = preventivoService.traerPorTecnicoEnRangoDeFecha(tecnico,
                TiempoUtils.haceAnios(1).toString(), TiempoUtils.ahora().toString(), TenantContext.getTenantId())
                .size();

        model.addAttribute("cantidadPreventivosTotal", preventivosTotal);
        model.addAttribute("cantidadPreventivosAnio", preventivosAnio);
        model.addAttribute("cantidadPreventivosMes", preventivosMes);

        Integer informesTotal = informeService.traerPorTecnico(tecnico, TenantContext.getTenantId()).size();
        Integer informesMes = informeService
                .traerPorTecnicoEnRangoDeFecha(tecnico, TiempoUtils.ahora().minusMonths(1).toString(),
                        TiempoUtils.ahora().toString(), TenantContext.getTenantId())
                .size();
        Integer informesAnio = informeService.traerPorTecnicoEnRangoDeFecha(tecnico,
                TiempoUtils.haceAnios(1).toString(), TiempoUtils.ahora().toString(), TenantContext.getTenantId())
                .size();

        Integer informesPendientes = tareaService
                .traerPorTecnicoYEstadoInforme(tecnico, "pendiente", TiempoUtils.haceAnios(1), TiempoUtils.ahora(),
                        TenantContext.getTenantId())
                .size()
                + tareaService.traerPorTecnicoYEstadoInforme(tecnico, "EnRevision", TiempoUtils.haceAnios(1),
                        TiempoUtils.ahora(), TenantContext.getTenantId()).size();

        model.addAttribute("cantidadInformesTotal", informesTotal);
        model.addAttribute("cantidadInformesAnio", informesAnio);
        model.addAttribute("cantidadInformesMes", informesMes);
        model.addAttribute("cantidadInformesPendientes", informesPendientes);

        // continuar
        model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());

        // DMS para el menú
        List<Tecnico> tecnicosFiltrados = tecnicoService.traerHabilitados(TenantContext.getTenantId()).stream()
                .filter(t -> t.getUsuario().getRoles().get(0).getNombre().equals("ROLE_TECNICO"))
                .collect(Collectors.toList());
        model.addAttribute("tecnicos", tecnicosFiltrados);

        model.addAttribute("habilitarGestionUsuarios", ArchivoExterno.getString("editarUsuarios"));
        model.addAttribute("habilitarEditorLayout", ArchivoExterno.getString("editorLayout"));
        model.addAttribute("tiempoRefresco", ArchivoExterno.getString("tiempoRefresco"));

        return "perfilTecnico";
    }

    // cuando el administrador o el supervisor de mantenimiento hace algun cambio en
    // el perfil de tecnico
    @PostMapping("/modificarTecnico")
    public String modificarTecnico(Model model, Tecnico tecnico) {

        // DMS tengo que hacer esta manganeta porque no encontré como solucionar un
        // error raro, me borraba el usuario al modificar el perfil tecnico
        Tecnico tecnicoBD = tecnicoService.findById(tecnico.getId()).orElse(null);
        tecnico.setUsuario(tecnicoBD.getUsuario());

        tecnicoService.save(tecnico);

        return "redirect:/layout";
    }

    @PostMapping("/guardarTecnicoEmpresa")
    public String guardarTecnicoEmpresa(Model model, Tecnico tecnico) {

        tecnicoService.save(tecnico);
        return "redirect:/gestionUsuarios";
    }

    @PostMapping("/guardarTecnicoPersonal")
    public String guardarTecnicoPersonal(Model model, Tecnico tecnico) {
        // tenfo que levantar de nuevo el usuario porque no lo podia poner como hidden
        // en el html, daba error de recirculacion
        Usuario usuario = usuarioDao.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        tecnico.setUsuario(usuario);
        tecnicoService.save(tecnico);
        return "redirect:/layout";
    }

}
