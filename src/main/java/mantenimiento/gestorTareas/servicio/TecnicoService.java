package mantenimiento.gestorTareas.servicio;

import java.util.List;
import mantenimiento.gestorTareas.dominio.Activo;
import mantenimiento.gestorTareas.dominio.Tarea;
import mantenimiento.gestorTareas.dominio.Tecnico;
import mantenimiento.gestorTareas.dominio.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface TecnicoService extends JpaRepository<Tecnico,Long> {
    
 
    
    @Query("SELECT t FROM Tecnico t  WHERE "
        + "t.usuario =?1 ")
    public Tecnico traerPorUsuario(Usuario usuario );

    @Query("SELECT t FROM Tecnico t  WHERE "
        + "t.nombre !=null ")
    public List<Tecnico> traerHabilitados(  );
    
    //trae los tecnicos que estan interviniendo en el activo enviado por parametro
      @Query("SELECT t FROM Tecnico t JOIN t.asignaciones a WHERE a.tarea.estado='enProceso' AND a.tarea.activo= ?1")
    public List<Tecnico> traerPorTareaEnActivo(Activo activo );

//    @Transactional
//    @Modifying
//    @Query(value = "UPDATE tecnico " +
//               "SET cantidad_preventivos = CAST(IFNULL(cantidad_preventivos, '0') AS UNSIGNED) + 1 " +
//               "WHERE id = ?1", 
//       nativeQuery = true)
//    public void incrementarPreventivo(Long  tecnicoId );
    
    
    
   
//    
//    @Query("SELECT t FROM Tarea t  WHERE "
//        + "t.estado ='cerrada' ")
//    public List<Tarea> traerCerradas( );
    
//    @Query("SELECT t FROM Tarea t  WHERE "
//        + "t.estado ='cerrada' and "
//            + "t.activo=?1")
//    public List<Tarea> traerCerradasPorActivo(Activo activo );
  
    
    
    
    
}
