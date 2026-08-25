package mantenimiento.gestorTareas.modulo.tareas;
import mantenimiento.gestorTareas.modulo.informes.Evaluacion;
import mantenimiento.gestorTareas.modulo.usuarios.Usuario;

import java.util.List;

public interface Servicio  {
    
    public List<Tarea> listar();
    public List<Tarea> filtrar(String palabraClave);
    public Tarea encontrar(Tarea tarea);
    public Usuario encontrarUsuario(Usuario usuario);
    public void guardar(Tarea tarea);
    public void eliminar(Tarea tarea);
    public void cerrarSolicitud(Tarea tarea, Evaluacion evaluacion);
    public void asignarSolicitud(Tarea tarea, List<Long> tecnicosIds, String motivoDemoraAsignacion);
    public void liberarSolicitud(Tarea tarea);
    
}
