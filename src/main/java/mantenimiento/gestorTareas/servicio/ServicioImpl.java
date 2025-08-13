package mantenimiento.gestorTareas.servicio;

import java.util.List;
import mantenimiento.gestorTareas.datos.TareaDao;
import mantenimiento.gestorTareas.datos.UsuarioDao;
import mantenimiento.gestorTareas.dominio.Tarea;
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
    AsignacionService asignacionService;

    @Transactional(readOnly = true)
    @Override
    public List<Tarea> listar() {
        return (List<Tarea>) tareaDao.findAll();
    }

    @Transactional(readOnly = true)

    @Override
    public List<Tarea> filtrar(String palabraClave) {
        
        return (List<Tarea>) tareaDao.filtrar(palabraClave);
    }

    @Transactional
    @Override
    public void guardar(Tarea tarea) {
        tareaDao.save(tarea);
    }

    @Transactional(readOnly = true)
    @Override
    public Tarea encontrar(Tarea tarea) {
        
        Tarea t=tareaDao.findById(tarea.getId()).orElse(null);
        t.setAsignaciones(asignacionService.traerPorTarea(tarea));
        return t;
    }

    
    
    @Override
    @Transactional(readOnly = true)
    public Usuario encontrarUsuario(Usuario usuario) {
        return usuario = usuarioDao.findById(usuario.getIdUsuario()).orElse(null);
    }

    @Transactional
    @Override
    public void eliminar(Tarea tarea) {
        tareaDao.delete(tarea);
    }

  

}
