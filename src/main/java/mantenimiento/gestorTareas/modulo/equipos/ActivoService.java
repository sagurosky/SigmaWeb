package mantenimiento.gestorTareas.modulo.equipos;
import mantenimiento.gestorTareas.infraestructura.multitenant.Tenant;
import mantenimiento.gestorTareas.infraestructura.multitenant.TenantContext;
import mantenimiento.gestorTareas.infraestructura.multitenant.TenantDao;
import mantenimiento.gestorTareas.modulo.tareas.Tarea;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivoService {

    @Autowired
    private ActivoDao activoDao;

    @Autowired
    private TenantDao tenantDao;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public List<Activo> listar() {
        Long tenantId = TenantContext.getTenantId();
        return activoDao.findByTenantId(tenantId);
    }

    public Activo findByName(String nombre) {
        Long tenantId = TenantContext.getTenantId();
        return activoDao.findByNameAndTenantId(nombre, tenantId);
    }

    public List<Activo> findByStatus(String estado) {
        Long tenantId = TenantContext.getTenantId();
        return activoDao.findByStatusAndTenantId(estado, tenantId);
    }

    @Transactional
    public Activo save(Activo activo) {
        if (activo.getTenant() == null && TenantContext.getTenantId() != null) {
            Tenant tenant = tenantDao.findById(TenantContext.getTenantId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Tenant con id " + TenantContext.getTenantId() + " no encontrado"));
            activo.setTenant(tenant);
        }
        Activo savedActivo = activoDao.save(activo);
        
        // Push notification of the asset state via WebSockets
        Map<String, String> payload = new HashMap<>();
        payload.put("nombre", savedActivo.getNombre());
        payload.put("estado", savedActivo.getEstado());
        payload.put("layout", savedActivo.getLayout());
        messagingTemplate.convertAndSend("/topic/activos", payload);
        
        return savedActivo;
    }

    @Transactional
    public void delete(Long id) {
        Long tenantId = TenantContext.getTenantId();
        Activo activo = activoDao.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalStateException(
                        "Activo con id " + id + " no encontrado para tenant " + tenantId));
        activoDao.delete(activo);
    }
}
