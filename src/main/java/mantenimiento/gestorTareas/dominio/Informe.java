/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mantenimiento.gestorTareas.dominio;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 *
 * @author daniel
 */
@Data
@Entity
@Table(name = "informe")
@EntityListeners(TenantEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Informe implements Serializable, TenantSupport {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tenant_id", nullable = true)
    private Tenant tenant;

    @OneToMany( cascade=CascadeType.ALL, mappedBy = "informe")
    private List<AsignacionInforme> asignaciones;
  
     @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime fechaDeCreacion;
    
    private String descripcion;
    private String acciones;
    private String materiales;
    private String estadoFinal;
    private String pruebas;
    private String causaRaiz;
    private String categoriaCausaRaiz;
    private String pregunta1;
    private String pregunta2;
    private String pregunta3;
    private String pregunta4;
    private String pregunta5;
    private String seguimiento;
    private String recomendaciones;
    private String imagen;
    private String estadoInforme;
    private String revision;
    
    
    
}
