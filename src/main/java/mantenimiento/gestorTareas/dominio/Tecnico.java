package mantenimiento.gestorTareas.dominio;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Set;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import mantenimiento.gestorTareas.util.TiempoUtils;

@Data
@Entity
@Table(name = "tecnico")
@EntityListeners(TenantEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Tecnico implements Serializable , TenantSupport{

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tenant_id", nullable = true)
    private Tenant tenant;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "usuario")
    private Usuario usuario;
    
 
    @OneToMany( cascade=CascadeType.ALL,mappedBy = "tecnico")
    private List<Asignacion> asignaciones;

    @OneToMany( cascade=CascadeType.ALL,mappedBy = "tecnico")
    private List<AsignacionPreventivo> asignacionesPreventivos;
    
    

    @OneToMany( cascade=CascadeType.ALL,mappedBy = "tecnico")
    private List<AsignacionInforme> asignacionesInformes;
    
    //datos personales
    @Column(length = 1000)
    private String acercaDeMi;
    private String nombre;
    private String apellido;
    private LocalDate fechaNacimiento;
    private String telefono;
    private String email;
    private String direccion;
    @Column(length = 1000)
    private String formacionAcademica;
    @Column(length = 1000)
    private String conocimientosVarios;
    @Column(length = 1000)
    private String experienciaLaboral;
    @Column(length = 1000)
    private String pasatiempos;
    
   
    //datos profesionales y empresariales
    private String estado;
    private String legajo;
    private String especialidad;
    private LocalDate fechaIngresoPlanta;
    private String habilidades;
    private String proyectosDestacados;
    private String promedioEvaluaciones;
    private String notasAdicionales;
    private String cantidadPreventivos;
    private String cantidadInformes;

    //datos desempeño 
    private String satisfaccionClienteInterno;
    private String predisposicionParaLaTarea;
    private String responsabilidad;
    private String cumplimientoNormasSeguridad;
    private String nivelDeConocimiento;
    private String tratoRecibidoPorCliente;
    private String prolijidad;
    private String puntualidad;
    private String eficiencia;
    private String calidadDelTrabajo;
    private String comunicacion;
    private String trabajoEnEquipo;
    private String resolucionDeProblemas;
    private String creatividadEInnovacion;
    private String iniciativa;
    private String autogestion;
    private String formacionContinua;
    
    
    
    private LocalDateTime ultimaActualizacion;
    
    
    
    
    
    
    
    // Getter calculado para la edad
    public int getEdad() {
        if (this.fechaNacimiento == null) {
            return 0; // o lanzar una excepción, dependiendo de tus necesidades
        }
        return Period.between(this.fechaNacimiento, LocalDate.now()).getYears();
    }
    
    // Método para actualizar la fecha y hora de la última actualización
    @PreUpdate
    @PrePersist
    public void actualizarUltimaActualizacion() {
        this.ultimaActualizacion = TiempoUtils.ahora();
    }
    
}
