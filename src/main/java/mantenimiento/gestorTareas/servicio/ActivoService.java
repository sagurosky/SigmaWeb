package mantenimiento.gestorTareas.servicio;

import java.util.List;

import mantenimiento.gestorTareas.datos.ActivoDao;
import mantenimiento.gestorTareas.dominio.Activo;
import mantenimiento.gestorTareas.dominio.Tarea;
import mantenimiento.gestorTareas.dominio.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

@Service
public class ActivoService {

    @Autowired
    private ActivoDao activoDao;

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
}
