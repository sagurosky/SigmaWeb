package mantenimiento.gestorTareas.dominio;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "producto")


public class Producto implements Serializable {

    
    
    
    
    
    
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
   
    
    private String codigo;
    private String nombre;
    private String descripcion;
    private String cadenciaAdulto2;
    private String cadenciaAdulto3;
    private String cadenciaAdulto4;
    private String cadenciaAdulto5;
    private String cadenciaAposito;
    
    
    

}