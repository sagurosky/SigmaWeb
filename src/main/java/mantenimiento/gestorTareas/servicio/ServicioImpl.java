package mantenimiento.gestorTareas.servicio;

import java.util.List;

import mantenimiento.gestorTareas.datos.AsignacionDao;
import mantenimiento.gestorTareas.datos.TareaDao;
import mantenimiento.gestorTareas.datos.UsuarioDao;
import mantenimiento.gestorTareas.dominio.Tarea;
import mantenimiento.gestorTareas.dominio.Tenant;
import mantenimiento.gestorTareas.dominio.TenantContext;
import mantenimiento.gestorTareas.dominio.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicioImpl implements Servicio {

    @Autowired
    TareaDao tareaDao;
    @Autowired
    UsuarioDao usuarioDao;
    @Autowired
    AsignacionDao asignacionDao;

    @Transactional(readOnly = true)
    @Override
    public List<Tarea> listar() {
        Long tenantId = TenantContext.getTenantId();
        return (List<Tarea>) tareaDao.findByTenantId(tenantId);
    }

    @Transactional(readOnly = true)

    @Override
    public List<Tarea> filtrar(String palabraClave) {
        Long tenantId = TenantContext.getTenantId();
        return (List<Tarea>) tareaDao.filtrar(palabraClave,tenantId);
    }

    @Transactional
    @Override
    public void guardar(Tarea tarea) {
        // Si por algún motivo viene sin tenant (no debería), el EntityListener lo completa.
        // Extra: validar que activo/relaciones pertenezcan al mismo tenant para evitar cruce.
        Long tenantId = TenantContext.getTenantId();
        if (tarea.getTenant() == null && tenantId != null) {
            Tenant t = new Tenant(); t.setId(tenantId);
            tarea.setTenant(t);
        }
        tareaDao.save(tarea);
    }



    @Transactional(readOnly = true)
    @Override
    public Tarea encontrar(Tarea tarea) {
        Long tenantId = TenantContext.getTenantId();
        Tarea t = tareaDao.findById(tarea.getId()).orElse(null);
        if (t == null) return null;

        // Guardia: que la tarea pertenezca a este tenant
        if (t.getTenant() == null || !t.getTenant().getId().equals(tenantId)) {
            return null; // o lanzar AccessDeniedException
        }

        // Cargamos asignaciones del mismo tenant
        t.setAsignaciones(asignacionDao.findByTareaIdAndTenantId(t.getId(), tenantId));
        return t;
    }

    @Transactional(readOnly = true)
    @Override
    public Usuario encontrarUsuario(Usuario usuario) {
        Long tenantId = TenantContext.getTenantId();
        Usuario u = usuarioDao.findById(usuario.getIdUsuario()).orElse(null);
        if (u == null) return null;
        if (u.getTenant() == null || !u.getTenant().getId().equals(tenantId)) {
            return null; // o AccessDeniedException
        }
        return u;
    }

    @Transactional
    @Override
    public void eliminar(Tarea tarea) {
        Long tenantId = TenantContext.getTenantId();
        Tarea t = tareaDao.findById(tarea.getId()).orElse(null);
        if (t == null) return;
        if (t.getTenant() == null || !t.getTenant().getId().equals(tenantId)) {
            return; // o lanzar excepción de acceso
        }
        tareaDao.delete(t);
    }

  

}
