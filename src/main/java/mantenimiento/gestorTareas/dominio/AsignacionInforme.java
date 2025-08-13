package mantenimiento.gestorTareas.dominio;


import java.io.Serializable;
import javax.persistence.*;
import lombok.Data;

@Data
@Entity
public class AsignacionInforme implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "informe_id",referencedColumnName = "id")
    private Informe informe;

    @ManyToOne
    @JoinColumn(name = "tecnico_id",referencedColumnName = "id")
    private Tecnico tecnico;

    
}