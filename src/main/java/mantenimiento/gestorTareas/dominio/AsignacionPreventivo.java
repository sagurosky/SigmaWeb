package mantenimiento.gestorTareas.dominio;


import java.io.Serializable;
import javax.persistence.*;
import lombok.Data;

@Data
@Entity
public class AsignacionPreventivo implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "preventivo_id",referencedColumnName = "id")
    private Preventivo preventivo;

    @ManyToOne
    @JoinColumn(name = "tecnico_id",referencedColumnName = "id")
    private Tecnico tecnico;

    
}