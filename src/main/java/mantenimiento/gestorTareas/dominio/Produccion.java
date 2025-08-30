package mantenimiento.gestorTareas.dominio;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@Entity
@Table(name = "produccion")
@EntityListeners(TenantEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Produccion implements Serializable, TenantSupport {


    public static final String LINEA_1="adulto2";
    public static final String LINEA_2="adulto3";
    public static final String LINEA_3="adulto4";
    public static final String LINEA_4="adulto5";
    public static final String LINEA_5="aposito";
    
    
    
    
    
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tenant_id", nullable = true)
    private Tenant tenant;

    private LocalDateTime inicio;
    private LocalDateTime fin;
    
    private String ordenDeTrabajo;
    private String estado;
    @OneToOne
    @JoinColumn(name = "producto")
    private Producto producto;
    private String cantidad;
    private String linea;
    
    
    

}