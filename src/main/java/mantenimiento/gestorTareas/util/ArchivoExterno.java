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
    private static final String CONFIG_PATH_LOCAL = "recursos/configuracion.properties";
    private static final String CONFIG_PATH_CLOUD = "/media/sf_personal/sigmaweb/recursos/configuracion.properties";
    private static final String CONFIG_PATH_DOCKER = "/app/recursos/configuracion.properties";
    
    private static final Properties properties = new Properties();

    static {
        loadProperties();
    }

    private static void loadProperties() {
        String[] paths = {CONFIG_PATH_LOCAL, CONFIG_PATH_CLOUD, CONFIG_PATH_DOCKER};
        boolean loaded = false;
        for (String path : paths) {
            try (FileInputStream fis = new FileInputStream(path)) {
                properties.load(fis);
                log.info("Archivo de configuración cargado desde: " + path);
                loaded = true;
                break;
            } catch (IOException e) {
                // Sigue al siguiente path
            }
        }
        if (!loaded) {
            log.warn("No se pudo cargar ningún archivo de configuración externa. Usando valores por defecto.");
            // Valores por defecto para local
            properties.setProperty("nube", "no");
            properties.setProperty("editorLayout", "si");
            properties.setProperty("editarUsuarios", "si");
            properties.setProperty("tiempoRefresco", "5000");
        }
    }
    //DMS prueba CI  
    public static String getString(String key) {
        return properties.getProperty(key, "");
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
        properties.clear();
        loadProperties();
    }

    public static String getBasePath() {
        if (getString("nube").equals("si")) {
            return "/media/sf_personal/sigmaweb/recursos/";
        } else if (Files.exists(Paths.get("recursos"))) {
            return "recursos/";
        } else {
            return "/app/recursos/";
        }
    }

    public static String getLayoutPath() {
        return getBasePath() + "layouts/";
    }

    public static String getImagenesPath() {
        return getBasePath() + "imagenes/";
    }

    public static List<String> nombresLayouts() {
        String direccion = getLayoutPath();
        Path layoutDir = Paths.get(direccion);

        if (!Files.exists(layoutDir)) {
            try {
                Files.createDirectories(layoutDir);
            } catch (IOException e) {
                log.error("Error creando directorio de layouts: " + e.getMessage());
                return new ArrayList<>();
            }
        }

        List<String> nombresLayouts = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(layoutDir, "*.svg")) {
            for (Path entry : stream) {
                String fileName = entry.getFileName().toString();
                String suffix = "Tenant" + TenantContext.getTenantId() + ".svg";

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