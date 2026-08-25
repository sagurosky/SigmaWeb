package mantenimiento.gestorTareas.modulo.tareas;

import mantenimiento.gestorTareas.infraestructura.multitenant.TenantContext;
import mantenimiento.gestorTareas.infraestructura.util.ArchivoExterno;
import mantenimiento.gestorTareas.modulo.tecnicos.Tecnico;
import mantenimiento.gestorTareas.modulo.tecnicos.TecnicoService;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador del Dashboard de Mantenimiento.
 *
 * FRONTEND: la plantilla dashboard.html ya está completa con datos mock y
 * Chart.js. Los gráficos se renderizan con datos estáticos hasta que se
 * implemente el backend de datos.
 */
@Controller
public class ControladorDashboard {

    @Autowired
    private TecnicoService tecnicoService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());

        List<Tecnico> tecnicosFiltrados = tecnicoService.traerHabilitados(TenantContext.getTenantId()).stream()
                .filter(t -> t.getUsuario() != null && t.getUsuario().getRoles() != null && !t.getUsuario().getRoles().isEmpty()
                        && "ROLE_TECNICO".equals(t.getUsuario().getRoles().get(0).getNombre()))
                .collect(Collectors.toList());
        model.addAttribute("tecnicos", tecnicosFiltrados);

        model.addAttribute("habilitarGestionUsuarios", ArchivoExterno.getString("editarUsuarios"));
        model.addAttribute("habilitarEditorLayout", ArchivoExterno.getString("editorLayout"));
        model.addAttribute("tiempoRefresco", ArchivoExterno.getString("tiempoRefresco"));

        return "dashboard";
    }
}
