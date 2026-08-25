package mantenimiento.gestorTareas.modulo.usuarios;
import mantenimiento.gestorTareas.infraestructura.multitenant.Tenant;
import mantenimiento.gestorTareas.infraestructura.multitenant.TenantEntityListener;
import mantenimiento.gestorTareas.infraestructura.multitenant.TenantSupport;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.*;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@Entity
@Table(name = "rol")
@EntityListeners(TenantEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Rol implements Serializable, TenantSupport {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tenant_id", nullable = true)
    private Tenant tenant;

    private String nombre;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    private Usuario usuario;

}
