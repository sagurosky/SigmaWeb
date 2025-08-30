package mantenimiento.gestorTareas.dominio;

import lombok.Data;

import javax.persistence.*;
@Data
@Entity
@Table(name = "tenant")
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;   // tenant_id

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tenant_id", nullable = true)
    private Tenant tenant;

    @Column(nullable = false, unique = true)
    private String nombre;   // Nombre de la empresa/cliente


}

