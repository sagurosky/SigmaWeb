package mantenimiento.gestorTareas.modulo.tareas;

import mantenimiento.gestorTareas.infraestructura.multitenant.TenantContext;
import mantenimiento.gestorTareas.infraestructura.util.ArchivoExterno;
import mantenimiento.gestorTareas.modulo.tecnicos.Tecnico;
import mantenimiento.gestorTareas.modulo.tecnicos.TecnicoService;

import mantenimiento.gestorTareas.modulo.equipos.PreventivoService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controlador del Dashboard de Mantenimiento.
 */
@Controller
public class ControladorDashboard {

    @Autowired
    private TecnicoService tecnicoService;

    @Autowired
    private TareaService tareaService;

    @Autowired
    private PreventivoService preventivoService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());

        List<Tecnico> tecnicosFiltrados = tecnicoService.traerHabilitados(TenantContext.getTenantId()).stream()
                .filter(t -> t.getUsuario() != null && t.getUsuario().getRoles() != null
                        && !t.getUsuario().getRoles().isEmpty()
                        && "ROLE_TECNICO".equals(t.getUsuario().getRoles().get(0).getNombre()))
                .collect(Collectors.toList());
        model.addAttribute("tecnicos", tecnicosFiltrados);

        model.addAttribute("habilitarGestionUsuarios", ArchivoExterno.getString("editarUsuarios"));
        model.addAttribute("habilitarEditorLayout", ArchivoExterno.getString("editorLayout"));
        model.addAttribute("tiempoRefresco", ArchivoExterno.getString("tiempoRefresco"));

        return "dashboard";
    }

    /**
     * Endpoint API AJAX para obtener datos agregados de la solapa Correctivos.
     */
    @GetMapping("/api/dashboard/correctivos")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerCorrectivos(
            @RequestParam(value = "desde", required = false) String desdeStr,
            @RequestParam(value = "hasta", required = false) String hastaStr) {

        LocalDateTime desde = (desdeStr != null && !desdeStr.isBlank())
                ? LocalDate.parse(desdeStr).atStartOfDay()
                : LocalDateTime.now().minusDays(30);

        LocalDateTime hasta = (hastaStr != null && !hastaStr.isBlank())
                ? LocalDate.parse(hastaStr).atTime(LocalTime.MAX)
                : LocalDateTime.now();

        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            tenantId = 0L; // Fallback seguro: no devuelve datos de otros tenants
        }

        Map<String, Object> kpisRaw = tareaService.obtenerKpisCorrectivosGlobales(desde, hasta, tenantId);
        Map<String, Object> kpis = new HashMap<>();
        if (kpisRaw != null) {
            for (Map.Entry<String, Object> entry : kpisRaw.entrySet()) {
                if (entry.getKey() != null) {
                    kpis.put(entry.getKey().toLowerCase(), entry.getValue());
                }
            }
        }

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("kpis", kpis);
        respuesta.put("estadoTareas",
                convertirChartData(tareaService.contarCorrectivosPorEstado(desde, hasta, tenantId)));
        respuesta.put("afectaProduccion",
                convertirChartData(tareaService.contarCorrectivosPorAfectaProduccion(desde, hasta, tenantId)));
        respuesta.put("evolucionFallas",
                convertirChartData(tareaService.contarCorrectivosEvolucionMensual(desde, hasta, tenantId)));

        List<Object[]> topEquipos = tareaService.contarCorrectivosPorEquipoTop(desde, hasta, tenantId);
        if (topEquipos != null && topEquipos.size() > 10) {
            topEquipos = topEquipos.subList(0, 10);
        }
        respuesta.put("fallasPorEquipo", convertirChartData(topEquipos));

        respuesta.put("mttrMensual", convertirChartData(tareaService.obtenerMttrMensual(desde, hasta, tenantId)));
        respuesta.put("categoriaTecnica",
                convertirChartData(tareaService.contarCorrectivosPorCategoriaTecnica(desde, hasta, tenantId)));
        respuesta.put("porDepartamento",
                convertirChartData(tareaService.contarCorrectivosPorDepartamento(desde, hasta, tenantId)));

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Endpoint API AJAX para obtener datos agregados de la solapa Preventivos.
     */
    @GetMapping("/api/dashboard/preventivos")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerPreventivos(
            @RequestParam(value = "desde", required = false) String desdeStr,
            @RequestParam(value = "hasta", required = false) String hastaStr) {

        LocalDateTime desde = (desdeStr != null && !desdeStr.isBlank())
                ? LocalDate.parse(desdeStr).atStartOfDay()
                : LocalDateTime.now().minusDays(365);

        LocalDateTime hasta = (hastaStr != null && !hastaStr.isBlank())
                ? LocalDate.parse(hastaStr).atTime(LocalTime.MAX)
                : LocalDateTime.now();

        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            tenantId = 0L;
        }

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("estadoPreventivos",
                convertirChartData(preventivoService.contarPreventivosPorEstado(desde, hasta, tenantId)));
        respuesta.put("frecuenciaPreventivos",
                convertirChartData(preventivoService.contarPreventivosPorFrecuencia(desde, hasta, tenantId)));

        List<Object[]> prog = preventivoService.contarPreventivosProgramadosPorMes(desde, hasta, tenantId);
        List<Object[]> real = preventivoService.contarPreventivosRealizadosPorMes(desde, hasta, tenantId);
        respuesta.put("preventivosVsRealizados", construirPreventivosVsRealizados(prog, real));

        List<Object[]> topEquipos = preventivoService.contarPreventivosPorEquipoTop(desde, hasta, tenantId);
        if (topEquipos != null && topEquipos.size() > 10) {
            topEquipos = topEquipos.subList(0, 10);
        }
        respuesta.put("equiposPreventivos", convertirChartData(topEquipos));

        respuesta.put("categoriaPreventivos",
                convertirChartData(preventivoService.contarPreventivosPorCategoria(desde, hasta, tenantId)));

        return ResponseEntity.ok(respuesta);
    }

    private Map<String, Object> construirPreventivosVsRealizados(
            List<Object[]> programados, List<Object[]> realizados) {
        Set<String> mesesSet = new TreeSet<>();
        Map<String, Integer> progMap = new HashMap<>();
        Map<String, Integer> realMap = new HashMap<>();

        if (programados != null) {
            for (Object[] row : programados) {
                if (row[0] != null) {
                    String mes = row[0].toString();
                    mesesSet.add(mes);
                    progMap.put(mes, ((Number) row[1]).intValue());
                }
            }
        }
        if (realizados != null) {
            for (Object[] row : realizados) {
                if (row[0] != null) {
                    String mes = row[0].toString();
                    mesesSet.add(mes);
                    realMap.put(mes, ((Number) row[1]).intValue());
                }
            }
        }

        List<String> labels = new ArrayList<>(mesesSet);
        List<Integer> progList = new ArrayList<>();
        List<Integer> realList = new ArrayList<>();

        for (String mes : labels) {
            progList.add(progMap.getOrDefault(mes, 0));
            realList.add(realMap.getOrDefault(mes, 0));
        }

        Map<String, Object> map = new HashMap<>();
        map.put("labels", labels);
        map.put("programados", progList);
        map.put("realizados", realList);
        return map;
    }

    /**
     * Endpoint API AJAX para obtener datos agregados de la solapa Desempeño Técnico.
     */
    @GetMapping("/api/dashboard/desempeno")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerDesempeno(
            @RequestParam(value = "desde", required = false) String desdeStr,
            @RequestParam(value = "hasta", required = false) String hastaStr) {

        LocalDateTime desde = (desdeStr != null && !desdeStr.isBlank())
                ? LocalDate.parse(desdeStr).atStartOfDay()
                : LocalDateTime.now().minusDays(365);

        LocalDateTime hasta = (hastaStr != null && !hastaStr.isBlank())
                ? LocalDate.parse(hastaStr).atTime(LocalTime.MAX)
                : LocalDateTime.now();

        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            tenantId = 0L;
        }

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("rankingTecnicos",
                convertirChartData(tecnicoService.obtenerRankingTecnicos(desde, hasta, tenantId)));
        respuesta.put("radarEquipo",
                construirRadarData(tecnicoService.obtenerRadarEquipo(desde, hasta, tenantId)));
        respuesta.put("tareasPorTecnico",
                convertirChartData(tecnicoService.obtenerCorrectivosPorTecnico(desde, hasta, tenantId)));
        respuesta.put("preventivosPorTecnico",
                convertirChartData(tecnicoService.obtenerPreventivosPorTecnico(desde, hasta, tenantId)));

        return ResponseEntity.ok(respuesta);
    }

    private Map<String, Object> construirRadarData(List<Object[]> lista) {
        List<String> labels = List.of(
            "Eficiencia", "Calidad", "Comunicación", "Trabajo en equipo",
            "Resolución", "Iniciativa", "Seguridad", "Puntualidad", "Conocimiento"
        );
        List<Object> values = new ArrayList<>();
        if (lista != null && !lista.isEmpty() && lista.get(0) != null) {
            Object[] row = lista.get(0);
            for (Object col : row) {
                values.add(col != null ? col : 0);
            }
        } else {
            for (int i = 0; i < 9; i++) values.add(0);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("labels", labels);
        map.put("values", values);
        return map;
    }

    private Map<String, Object> convertirChartData(List<Object[]> lista) {
        List<String> labels = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        if (lista != null) {
            for (Object[] fila : lista) {
                labels.add(fila[0] != null ? fila[0].toString() : "N/A");
                values.add(fila[1] != null ? fila[1] : 0);
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("labels", labels);
        map.put("values", values);
        return map;
    }
}
