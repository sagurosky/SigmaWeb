package mantenimiento.gestorTareas.dominio;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "repuestos")
@EntityListeners(TenantEntityListener.class)
public class Repuesto implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = true)
    private Tenant tenant;

    private String codigo;
    private String descripcion;
    private String familia;
    private Integer cantidad;
    private String ubicacion;
    private LocalDateTime fecha;
    
    

}
