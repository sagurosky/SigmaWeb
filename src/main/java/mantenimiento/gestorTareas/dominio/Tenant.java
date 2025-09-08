package mantenimiento.gestorTareas.dominio;

import lombok.Data;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tenant")
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;   // tenant_id

    @NotBlank
    @Column(name = "empresa", nullable = false)
    private String empresa;   // nombre de la empresa/cliente (no obligatorio único si querés)

    @NotBlank
    @Column(name = "email_contacto", nullable = false, unique = true)
    private String emailContacto; // email de quien registró (usar para validación)

    @Column(name = "fecha_alta")
    private LocalDateTime fechaAlta;

    @NotBlank
    @Column(name="nombre", nullable = false)
    private String nombre;
}


