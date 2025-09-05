package mantenimiento.gestorTareas.dominio;


import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@Entity
@EntityListeners(TenantEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Asignacion implements Serializable, TenantSupport{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tenant_id", nullable = true)
    private Tenant tenant;

    @ManyToOne
    @JoinColumn(name = "tarea_id",referencedColumnName = "id")
    private Tarea tarea;

    @ManyToOne
    
    @JoinColumn(name = "tecnico_id",referencedColumnName = "id")
    private Tecnico tecnico;

    // Otros atributos adicionales, si es necesario, como fecha de asignación, etc.

    // Getters y setters
   

}