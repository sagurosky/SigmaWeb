package mantenimiento.gestorTareas.dominio;


import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import javax.persistence.*;
import lombok.Data;

@Data
@Entity
public class Asignacion implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(name = "tarea_id",referencedColumnName = "id")
    private Tarea tarea;

    @ManyToOne
    
    @JoinColumn(name = "tecnico_id",referencedColumnName = "id")
    private Tecnico tecnico;

    // Otros atributos adicionales, si es necesario, como fecha de asignación, etc.

    // Getters y setters
   

}