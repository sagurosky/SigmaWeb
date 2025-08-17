package mantenimiento.gestorTareas.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

public class TiempoUtils {

    // Cambiá esta línea según el país de despliegue
    private static final ZoneId ZONA_SISTEMA = ZoneId.of("America/Argentina/Buenos_Aires");
    // private static final ZoneId ZONA_SISTEMA = ZoneId.of("America/La_Paz");


    public static LocalDateTime ahora() {
        return LocalDateTime.now(ZONA_SISTEMA);
    }

    public static LocalDateTime haceAnios(int cantidad) {
        return ahora().minusYears(cantidad);
    }

    public static LocalDateTime haceDias(int cantidad) {
        return ahora().minusDays(cantidad);
    }



}