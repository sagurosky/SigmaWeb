package mantenimiento.gestorTareas.servicio;

import java.util.List;
import mantenimiento.gestorTareas.dominio.Tarea;
import mantenimiento.gestorTareas.dominio.Usuario;

public interface Servicio  {
    
    public List<Tarea> listar();
    public List<Tarea> filtrar(String palabraClave);
    public Tarea encontrar(Tarea tarea);
    public Usuario encontrarUsuario(Usuario usuario);
    public void guardar(Tarea tarea);
    public void eliminar(Tarea tarea);
    
}
