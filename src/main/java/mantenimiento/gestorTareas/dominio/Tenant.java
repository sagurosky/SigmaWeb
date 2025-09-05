package mantenimiento.gestorTareas.dominio;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tenant",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"empresa"})})
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;   // tenant_id

    @Column(name = "empresa", nullable = false)
    private String empresa;   // nombre de la empresa/cliente (no obligatorio único si querés)

    @Column(name = "email_contacto", nullable = false, unique = true)
    private String emailContacto; // email de quien registró (usar para validación)

    @Column(name = "fecha_alta")
    private LocalDateTime fechaAlta;

    @Column(name="nombre_completo")
    private String nombre;
}


