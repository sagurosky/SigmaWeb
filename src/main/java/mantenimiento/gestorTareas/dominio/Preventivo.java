package mantenimiento.gestorTareas.dominio;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@Entity


public class Preventivo implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    private String descripcion;
    private String detalle;
    private String categoria;

    private String estado;
    private String solicita;
    private String validacionMantenimiento;
    private String frecuencia;
     private String imagen;
    @OneToOne
    @JoinColumn(name = "activo")
    private Activo activo;
    
    
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime fechaDeCreacion;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime fechaRealizado;
    
    
    @OneToMany( cascade=CascadeType.ALL, mappedBy = "preventivo")
    private List<AsignacionPreventivo> asignaciones;
     

}