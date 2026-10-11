package mantenimiento.gestorTareas.modulo.equipos;
import mantenimiento.gestorTareas.infraestructura.multitenant.TenantContext;
import mantenimiento.gestorTareas.infraestructura.util.TiempoUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
@Slf4j
@Component
public class PreventivoScheduler {

    @Autowired
    private PreventivoService preventivoService;

    @Scheduled(cron = "0 0 0 * * *") // Todos los días a medianoche
   //@Scheduled(cron = "*/5 * * * * *")     //para debug, se ejecuta cada 5 seg
    public void generarPreventivosPeriodicos() {
        List<Preventivo> cerradosPeriodicos = preventivoService.traerPreventivosCerradosPeriodicos(TenantContext.getTenantId());

        for (Preventivo p : cerradosPeriodicos) {
            if (correspondeGenerarNuevo(p)) {
                Preventivo nuevo = clonarPreventivo(p);
                preventivoService.save(nuevo);
                p.setEstado("cerrado y reiniciado");
                preventivoService.save(p);
            }
        }
    }


    private boolean correspondeGenerarNuevo(Preventivo p) {
        if (p.getFechaRealizado() == null) return false;
        LocalDateTime hoy = TiempoUtils.ahora();
        LocalDateTime realizado = p.getFechaRealizado();

        switch (p.getFrecuencia()) {
//            case "mensual": return ChronoUnit.MONTHS.between(realizado, hoy) >= 1;
            case "mensual": return ChronoUnit.SECONDS.between(realizado, hoy) >= 10;
            case "trimestral": return ChronoUnit.MONTHS.between(realizado, hoy) >= 3;
            case "semestral": return ChronoUnit.MONTHS.between(realizado, hoy) >= 6;
            case "anual": return ChronoUnit.YEARS.between(realizado, hoy) >= 1;
            default: return false;
        }
    }

    private Preventivo clonarPreventivo(Preventivo original) {
        Preventivo nuevo = new Preventivo();
        nuevo.setActivo(original.getActivo());
        nuevo.setSolicita(original.getSolicita());
        nuevo.setDescripcion(original.getDescripcion());
        nuevo.setDetalle(original.getDetalle());
        nuevo.setCategoria(original.getCategoria());
        nuevo.setEstado("pendiente");
        nuevo.setFrecuencia(original.getFrecuencia());
        nuevo.setFechaDeCreacion(TiempoUtils.ahora());
        nuevo.setImagen(original.getImagen());
        nuevo.setFechaRealizado(null); // aún no se realizó
        log.info("🛠️ Nuevo preventivo generado automáticamente desde el ID original {} con frecuencia '{}'", original.getId(), original.getFrecuencia());

        return nuevo;
    }


}
