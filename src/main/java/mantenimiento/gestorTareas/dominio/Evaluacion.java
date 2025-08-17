/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mantenimiento.gestorTareas.dominio;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

/**
 *
 * @author daniel
 */

@Data
@Entity
@Table(name = "evaluacion")
public class Evaluacion implements Serializable {

 @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
 
private String  satisfaccion;
private String predisposicion;
private String responsabilidad;
private String seguridad;
private String conocimiento;
private String trato;
private String prolijidad;
private String puntualidad;
private String eficiencia;
private String calidad;
private String comunicacion;
private String trabajoEnEquipo;
private String resolucion;
private String creatividad;
private String iniciativa;
private String autogestion;
private String formacionContinua;
 
 
 
 
 
}
