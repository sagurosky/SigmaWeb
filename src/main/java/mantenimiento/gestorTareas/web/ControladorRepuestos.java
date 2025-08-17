package mantenimiento.gestorTareas.web;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.sql.Array;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import mantenimiento.gestorTareas.datos.ActivoDao;
import mantenimiento.gestorTareas.datos.RolDao;
import mantenimiento.gestorTareas.datos.UsuarioDao;
import mantenimiento.gestorTareas.dominio.Activo;
import mantenimiento.gestorTareas.dominio.Asignacion;
import mantenimiento.gestorTareas.dominio.Evaluacion;
import mantenimiento.gestorTareas.dominio.Informe;
import mantenimiento.gestorTareas.dominio.Produccion;
import mantenimiento.gestorTareas.dominio.Tarea;
import mantenimiento.gestorTareas.dominio.Tecnico;
import mantenimiento.gestorTareas.dominio.Usuario;
import mantenimiento.gestorTareas.servicio.ActivoService;
import mantenimiento.gestorTareas.servicio.AsignacionService;
import mantenimiento.gestorTareas.servicio.ProduccionService;
import mantenimiento.gestorTareas.servicio.RepuestoService;
import mantenimiento.gestorTareas.servicio.TareaService;
import mantenimiento.gestorTareas.servicio.Servicio;
import mantenimiento.gestorTareas.servicio.TecnicoService;
import mantenimiento.gestorTareas.servicio.UsuarioService;
import mantenimiento.gestorTareas.util.ArchivoExterno;
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
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import mantenimiento.gestorTareas.dominio.Repuesto;

@Controller
@Slf4j
public class ControladorRepuestos {

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
    AsignacionService asignacionService;
    @Autowired
    ProduccionService produccionService;

    @Autowired
     RepuestoService repuestoService;

    // Muestra la vista para cargar el archivo
    @GetMapping("/repuestos")
    public String showUploadForm() {
//        DMS cuando siga con esto cargar el model
//        model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());
        return "repuestos"; // Nombre de la vista (por ejemplo, uploadForm.html)
    }

    // Procesa el archivo enviado desde el formulario
    @PostMapping("/cargarExcel")
    public String cargarExcel(@RequestParam("file") MultipartFile file, Model model) {
       
        
        
        
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            // Se toma la primera hoja
            Sheet sheet = workbook.getSheetAt(0);
            // Formateador para fechas con patrón "dd/MM/yyyy"
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            // Empezamos a leer desde la tercera fila (índice 2, pues las filas son 0-based)
            for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                Repuesto repuesto = new Repuesto();

                // Mapeo de celdas en orden:
                // Columna 0: codigo
                repuesto.setCodigo(getStringCellValue(row.getCell(0)));

                // Columna 1: descripcion
                repuesto.setDescripcion(getStringCellValue(row.getCell(1)));

                // Columna 2: familia
                repuesto.setFamilia(getStringCellValue(row.getCell(2)));

                // Columna 3: cantidad (se espera valor numérico)
                if(row.getCell(3) != null && row.getCell(3).getCellType() == CellType.NUMERIC) {
                    repuesto.setCantidad((int) row.getCell(3).getNumericCellValue());
                } else {
                    repuesto.setCantidad(0);
                }

                // Columna 4: ubicacion
                repuesto.setUbicacion(getStringCellValue(row.getCell(4)));

                // Columna 5: fecha (formato dd/MM/yyyy)
                String fechaStr = getStringCellValue(row.getCell(5));
                if(!fechaStr.isEmpty()){
                    LocalDateTime fecha = LocalDateTime.parse(fechaStr, formatter);
                    repuesto.setFecha(fecha);
                }

                // Persistir el objeto
                repuestoService.save(repuesto);
            }
        } catch (Exception e) {
            // Manejo básico de excepciones, se puede mejorar el log o notificar de otra forma
            e.printStackTrace();
        }
        
        
        
        
        
      
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        model.addAttribute("mensaje", "Archivo procesado y datos persistidos correctamente.");










        model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());
        
        return "uploadForm";
    }
    
    
    
    
    
    
       // Método auxiliar para extraer el valor String de una celda
    private String getStringCellValue(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            // Si la celda es numérica, lo convertimos a String
            // Esto puede ser útil para códigos o campos que sean numéricos pero se quieren como texto.
            return String.valueOf((int)cell.getNumericCellValue());
        } else if (cell.getCellType() == CellType.BLANK) {
            return "";
        }
        return "";
    }
        
    
    
    
    
    
    
}
