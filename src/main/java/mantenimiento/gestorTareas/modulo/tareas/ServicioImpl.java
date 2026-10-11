package mantenimiento.gestorTareas.modulo.tareas;
import mantenimiento.gestorTareas.modulo.equipos.ActivoService;
import mantenimiento.gestorTareas.modulo.tecnicos.TecnicoService;
import mantenimiento.gestorTareas.infraestructura.multitenant.Tenant;
import mantenimiento.gestorTareas.infraestructura.multitenant.TenantContext;
import mantenimiento.gestorTareas.infraestructura.util.TiempoUtils;
import mantenimiento.gestorTareas.modulo.informes.Evaluacion;
import mantenimiento.gestorTareas.modulo.tecnicos.Tecnico;
import mantenimiento.gestorTareas.modulo.usuarios.Usuario;
import mantenimiento.gestorTareas.modulo.usuarios.UsuarioDao;

import java.util.List;

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
    @Autowired
    ActivoService activoService;

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
        if (tarea == null || tarea.getId() == null) return null;
        Long tenantId = TenantContext.getTenantId();
        Tarea t = tareaDao.findById(tarea.getId()).orElse(null);
        if (t == null) return null;

        // Guardia: que la tarea pertenezca a este tenant
        if (t.getTenant() == null || !t.getTenant().getId().equals(tenantId)) {
            return null; // o lanzar AccessDeniedException
        }

        // Inicializamos la colección sin reemplazar la referencia de la colección gestionada por Hibernate
        if (t.getAsignaciones() != null) {
            t.getAsignaciones().size();
        }
        return t;
    }

    @Transactional
    @Override
    public void desasignarTecnico(Long tareaId, Long tecnicoId) {
        Tarea temp = new Tarea();
        temp.setId(tareaId);
        Tarea t = encontrar(temp);
        if (t != null && t.getAsignaciones() != null) {
            t.getAsignaciones().removeIf(a -> a.getTecnico() != null && a.getTecnico().getId().equals(tecnicoId));
            guardar(t);
            if (t.getActivo() != null) {
                activoService.save(t.getActivo()); // Desencadenar notificaciones WebSocket
            }
        }
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

    @Transactional
    @Override
    public void cerrarSolicitud(Tarea tarea, Evaluacion evaluacion) {
        Tarea t = encontrar(tarea);
        t.getActivo().setEstado("operativa");
        t.setEstado("cerrada");
        
        t.setEvaluacion(evaluacion);
        t.setMomentoCierre(TiempoUtils.ahora());

        if (t.getDepartamentoResponsable() != null && !t.getDepartamentoResponsable().equals("mantenimiento")) {
            t.setMomentoLiberacion(TiempoUtils.ahora());
        }
        guardar(t);
        activoService.save(t.getActivo()); // Desencadenar notificaciones WebSocket
    }

    @Autowired
    TecnicoService tecnicoService;
    
    @Autowired
    AsignacionService asignacionService;

    @Transactional
    @Override
    public void asignarSolicitud(Tarea tarea, java.util.List<Long> tecnicosIds, String motivoDemoraAsignacion) {
        Tarea t = encontrar(tarea);
        if (t == null) return;

        if (t.getAsignaciones() == null) {
            t.setAsignaciones(new java.util.ArrayList<>());
        } else {
            t.getAsignaciones().clear();
        }

        if (tecnicosIds != null) {
            java.util.List<Long> distinctIds = tecnicosIds.stream()
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .collect(java.util.stream.Collectors.toList());

            for (Long idTecnico : distinctIds) {
                Tecnico tecnico = tecnicoService.getById(idTecnico);
                if (tecnico != null) {
                    Asignacion asignacion = new Asignacion();
                    asignacion.setTecnico(tecnico);
                    asignacion.setTarea(t);
                    t.getAsignaciones().add(asignacion);
                }
            }
        }

        t.setEstado("enProceso");
        if (t.getMomentoAsignacion() == null) {
            t.setMomentoAsignacion(TiempoUtils.ahora());
        }
        t.setMotivoDemoraAsignacion(motivoDemoraAsignacion);
        guardar(t);
        if (t.getActivo() != null) {
            activoService.save(t.getActivo()); // Desencadenar notificaciones WebSocket
        }
    }

    @Transactional
    @Override
    public void liberarSolicitud(Tarea tarea) {
        Tarea t = encontrar(tarea);
        if (t.getAsignaciones() == null || t.getAsignaciones().isEmpty()) {
            throw new IllegalStateException("Debe haber al menos un interventor asignado a la tarea para poder liberarla.");
        }
        t.setEstado("liberada");
        t.getActivo().setEstado("liberada");
        t.setMomentoLiberacion(TiempoUtils.ahora());
        guardar(t);
        activoService.save(t.getActivo()); // Desencadenar notificaciones WebSocket
    }

}
