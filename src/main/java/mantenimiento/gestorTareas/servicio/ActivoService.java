package mantenimiento.gestorTareas.servicio;

import java.util.List;

import mantenimiento.gestorTareas.datos.ActivoDao;
import mantenimiento.gestorTareas.datos.TenantDao;
import mantenimiento.gestorTareas.dominio.Activo;
import mantenimiento.gestorTareas.dominio.Tarea;
import mantenimiento.gestorTareas.dominio.Tenant;
import mantenimiento.gestorTareas.dominio.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivoService {

    @Autowired
    private ActivoDao activoDao;

    @Autowired
    private TenantDao tenantDao;

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
        return activoDao.save(activo);
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
