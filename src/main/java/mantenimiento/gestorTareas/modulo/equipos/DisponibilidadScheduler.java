package mantenimiento.gestorTareas.modulo.equipos;

import mantenimiento.gestorTareas.infraestructura.util.TiempoUtils;
import mantenimiento.gestorTareas.modulo.tareas.Tarea;
import mantenimiento.gestorTareas.modulo.tareas.TareaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class DisponibilidadScheduler {

    @Autowired
    private ActivoDao activoDao;

    @Autowired
    private ActivoService activoService;

    @Autowired
    private TareaService tareaService;

    @Scheduled(cron = "0 * * * * *") // Se ejecuta cada minuto
    public void gestionarDisponibilidadesProgramadas() {
        LocalDateTime ahora = TiempoUtils.ahora();

        // 1. Activar disponibilidades cuya fecha inicio llegó
        List<Activo> pendientes = activoDao.findActivosPendientesDisponibilidad(ahora);
        for (Activo a : pendientes) {
            Long tenantId = a.getTenant() != null ? a.getTenant().getId() : null;
            List<Tarea> tareasAbiertas = tareaService.traerNoCerradaPorActivo(a, tenantId);

            // Si no hay tareas de reparación o fallas abiertas en el activo
            if (tareasAbiertas == null || tareasAbiertas.isEmpty()) {
                a.setEstado("disponible");

                Tarea tarea = new Tarea();
                tarea.setActivo(a);
                tarea.setEstado("disponible");
                LocalDateTime momentoInicio = (a.getDisponibilidadDesde() != null && !a.getDisponibilidadDesde().isBefore(ahora)) ? a.getDisponibilidadDesde() : ahora;
                tarea.setMomentoDetencion(momentoInicio);
                tarea.setMomentoLiberacion(a.getDisponibilidadHasta());
                if (a.getTenant() != null) {
                    tarea.setTenant(a.getTenant());
                }
                tareaService.save(tarea);
                activoService.save(a);

                log.info("🟢 Disponibilidad activada automáticamente para el activo: {}", a.getNombre());
            } else {
                log.info("⚠️ No se activó disponibilidad para {} por existir tareas no cerradas.", a.getNombre());
            }
        }

        // 2. Finalizar disponibilidades cuya fecha fin ya venció
        List<Activo> vencidos = activoDao.findActivosVencidosDisponibilidad(ahora);
        for (Activo a : vencidos) {
            Long tenantId = a.getTenant() != null ? a.getTenant().getId() : null;
            List<Tarea> tareasDisponibles = tareaService.traerDisponiblePorActivo(a, tenantId);

            if (tareasDisponibles != null && !tareasDisponibles.isEmpty()) {
                Tarea tDisp = tareasDisponibles.get(0);
                tDisp.setEstado("finDisponible");
                tDisp.setMomentoLiberacion(ahora);
                tareaService.save(tDisp);
            }

            a.setEstado("operativa");
            a.setDisponibilidadDesde(null);
            a.setDisponibilidadHasta(null);
            activoService.save(a);

            log.info("🏁 Disponibilidad finalizada automáticamente para el activo: {}", a.getNombre());
        }

        // 3. Limpiar disponibilidades cuya fecha fin ya venció pero no llegaron a activarse
        List<Activo> expirados = activoDao.findActivosProgramacionExpirada(ahora);
        for (Activo a : expirados) {
            a.setDisponibilidadDesde(null);
            a.setDisponibilidadHasta(null);
            activoService.save(a);

            log.info("🧹 Limpieza de programación expirada no activada para el activo: {}", a.getNombre());
        }
    }
}
