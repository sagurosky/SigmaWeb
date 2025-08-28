package mantenimiento.gestorTareas.dominio;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import lombok.Data;

@Data
@Entity
@Table(name = "rol")
@EntityListeners(TenantEntityListener.class)
public class Rol implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = true)
    private Tenant tenant;

    private String nombre;

    @ManyToOne
//    @ManyToOne(targetEntity = Usuario.class )
    @JoinColumn(name = "id_usuario")
    
    private Usuario usuario;

}
