package mantenimiento.gestorTareas.modulo.tareas;
import mantenimiento.gestorTareas.infraestructura.multitenant.Tenant;
import mantenimiento.gestorTareas.infraestructura.multitenant.TenantEntityListener;
import mantenimiento.gestorTareas.infraestructura.multitenant.TenantSupport;
import mantenimiento.gestorTareas.modulo.equipos.Activo;
import mantenimiento.gestorTareas.modulo.informes.Evaluacion;
import mantenimiento.gestorTareas.modulo.informes.Informe;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "tareas")
@EntityListeners(TenantEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Tarea implements Serializable, TenantSupport {

    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
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
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany( cascade=CascadeType.ALL, mappedBy = "tarea")
    private List<Asignacion> asignaciones;
    
    
    private String categoriaTecnica;
    private String motivoDemoraAsignacion;
    private String motivoDemoraCierre;
    
    @Column(columnDefinition = "TEXT")
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
