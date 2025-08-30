package mantenimiento.gestorTareas.dominio;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@Entity
@Table(name = "producto")
@EntityListeners(TenantEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})

public class Producto implements Serializable, TenantSupport {

    
    
    
    
    
    
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tenant_id", nullable = true)
    private Tenant tenant;
    
    private String codigo;
    private String nombre;
    private String descripcion;
    private String cadenciaAdulto2;
    private String cadenciaAdulto3;
    private String cadenciaAdulto4;
    private String cadenciaAdulto5;
    private String cadenciaAposito;
    
    
    

}