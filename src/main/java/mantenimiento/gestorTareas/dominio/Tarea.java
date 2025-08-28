package mantenimiento.gestorTareas.dominio;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tareas")
@EntityListeners(TenantEntityListener.class)
public class Tarea implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = true)
    private Tenant tenant;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "activo")
    private Activo activo;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "informe")
    private Informe informe;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "evaluacion")
    private Evaluacion evaluacion;
    // DMS el JsonIgnore evita el error de recursividad infinita (si lo saco falla al cargar activo). Considerar si falla con preventivos o informes
    @JsonIgnore
    @OneToMany( cascade=CascadeType.ALL, mappedBy = "tarea")
    private List<Asignacion> asignaciones;
    
    
    private String categoriaTecnica;
    private String motivoDemoraAsignacion;
    private String motivoDemoraCierre;
    
    private String descripcion;
    private String departamentoResponsable;
    
    private String solicita;

    private String estado;
    private String afectaProduccion;
    
    private String imagen;
    
    private LocalDateTime momentoDetencion;
    private LocalDateTime momentoAsignacion;
    private LocalDateTime momentoLiberacion;
    private LocalDateTime momentoCierre;
    
    

}
