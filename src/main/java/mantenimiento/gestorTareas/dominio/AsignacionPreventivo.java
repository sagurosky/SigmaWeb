package mantenimiento.gestorTareas.dominio;


import java.io.Serializable;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@Entity
@EntityListeners(TenantEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AsignacionPreventivo implements Serializable, TenantSupport{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tenant_id", nullable = true)
    private Tenant tenant;

    @ManyToOne
    @JoinColumn(name = "preventivo_id",referencedColumnName = "id")
    private Preventivo preventivo;

    @ManyToOne
    @JoinColumn(name = "tecnico_id",referencedColumnName = "id")
    private Tecnico tecnico;

    
}