package mantenimiento.gestorTareas.util;

import lombok.extern.slf4j.Slf4j;
import mantenimiento.gestorTareas.dominio.TenantContext;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
@Slf4j
public class ArchivoExterno {
//DMS AWS
    private static final String CONFIG_PATH = "/media/sf_personal/sigmaweb/recursos/configuracion.properties";
//DMS docker
//    private static final String CONFIG_PATH = "/app/recursos/configuracion.properties";
    private static final Properties properties = new Properties();

    static {
        try (FileInputStream fis = new FileInputStream(CONFIG_PATH)) {
            properties.load(fis);
        } catch (IOException e) {
            System.err.println("Error cargando archivo de configuración externa: " + e.getMessage());
        }
    }
    //DMS prueba CI  
    public static String getString(String key) {
        return properties.getProperty(key);
    }

    public static Integer getInt(String key) {
        String val = properties.getProperty(key);
        return val != null ? Integer.parseInt(val) : null;
    }

    public static LocalDateTime getDateTime(String key) {
        String val = properties.getProperty(key);
        return val != null ? LocalDateTime.parse(val) : null;
    }

    public static void recargar() {
        try (FileInputStream fis = new FileInputStream(CONFIG_PATH)) {
            properties.clear();
            properties.load(fis);
            log.info("archivo externo recargado");
        } catch (IOException e) {
            System.err.println("Error recargando archivo de configuración: " + e.getMessage());
        }
    }

    public static List<String> nombresLayouts(){

        String  direccion = "";

        if(ArchivoExterno.getString("nube").equals("si"))
        {
            direccion = "/media/sf_personal/sigmaweb/recursos/layouts/";
        }else
        {
            direccion = "/app/recursos/layouts/";
        }
       Path  layoutDir = Paths.get(direccion);

        List<String> nombresLayouts = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(layoutDir, "*.svg")) {
            for (Path entry : stream) {
                String fileName = entry.getFileName().toString();
                String suffix = TenantContext.getTenantId() + ".svg";

                if (fileName.endsWith(suffix)) {
                    // le saco el sufijo tenantId.svg para dejar el nombre original
                    String originalName = fileName.substring(0, fileName.length() - suffix.length()) + ".svg";
                    nombresLayouts.add(originalName);
                }
            }

            nombresLayouts.sort(Comparator.comparing(name -> {
                try {
                    return Files.getLastModifiedTime(layoutDir.resolve(name)).toMillis();
                } catch (IOException e) {
                    return 0L;
                }
            }));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return nombresLayouts;
    }


}