package mantenimiento.gestorTareas.web;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import mantenimiento.gestorTareas.datos.ActivoDao;
import mantenimiento.gestorTareas.datos.RolDao;
import mantenimiento.gestorTareas.datos.UsuarioDao;
import mantenimiento.gestorTareas.dominio.*;
import mantenimiento.gestorTareas.servicio.ActivoService;
import mantenimiento.gestorTareas.servicio.AsignacionService;
import mantenimiento.gestorTareas.servicio.ProduccionService;
import mantenimiento.gestorTareas.servicio.ProductoService;
import mantenimiento.gestorTareas.servicio.TareaService;
import mantenimiento.gestorTareas.servicio.Servicio;
import mantenimiento.gestorTareas.servicio.TecnicoService;
import mantenimiento.gestorTareas.servicio.UsuarioService;
import mantenimiento.gestorTareas.util.ArchivoExterno;
import mantenimiento.gestorTareas.util.Convertidor;
import mantenimiento.gestorTareas.util.TiempoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Slf4j
public class ControladorProduccion {

    @Autowired
    Servicio servicio;
    @Autowired
    UsuarioService usuarioService;
    @Autowired
    UsuarioDao usuarioDao;
    @Autowired
    RolDao rolDao;
    @Autowired
    ActivoDao activo;
    @Autowired
    ActivoService activoService;
    @Autowired
    TareaService tareaService;
    @Autowired
    TecnicoService tecnicoService;
    @Autowired
    AsignacionService asignacionService;
    @Autowired
    ProduccionService produccionService;
    @Autowired
    ProductoService productoService;

    @GetMapping("/produccion/{url}")
    public String cargaProduccion(@PathVariable("url") String url, Model model) {
        model.addAttribute("produccion", new Produccion());
         model.addAttribute("productos",productoService.findAllByTenant());
//        log.info("Productos: " + productos);
        List<String> lineas=Arrays.asList(Produccion.LINEA_1,
                                                  Produccion.LINEA_2,
                                                  Produccion.LINEA_3,
                                                  Produccion.LINEA_4,
                                                  Produccion.LINEA_5);
        model.addAttribute("lineas",lineas);
//        log.info("lineas: " + lineas);
        model.addAttribute("ordenesAbiertas", produccionService.traerAbiertas(TenantContext.getTenantId()));
        model.addAttribute("ordenesCerradas", produccionService.traerCerradas(TenantContext.getTenantId()));

        model.addAttribute("url", url);
        model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());
        return "produccion";
    }
    
    
     @PostMapping("/cargaOrdenDeTrabajo")
    public String cargaOrdenDeTrabajo(@RequestParam("url")String url, Model model,  Produccion produccion) {
         List<String> lineas=Arrays.asList(Produccion.LINEA_1,
                                                  Produccion.LINEA_2,
                                                  Produccion.LINEA_3,
                                                  Produccion.LINEA_4,
                                                  Produccion.LINEA_5);
        model.addAttribute("lineas",lineas);
         model.addAttribute("productos",productoService.findAllByTenant());
        produccion.setEstado("abierta");
        produccion.setInicio(TiempoUtils.ahora());
        produccionService.save(produccion);
         model.addAttribute("ordenesAbiertas", produccionService.traerAbiertas(TenantContext.getTenantId()));
        model.addAttribute("url",url);
        return "redirect:/produccion/"+url;
    }
    
     @GetMapping("/cerrarOrdenDeTrabajo/{id}")
    public String cerrarOrdenDeTrabajo(  @PathVariable("id") Long id,@RequestParam("url")String url, Model model,  Produccion produccion) {
        
        Produccion prod=produccionService.findById(produccion.getId()).orElse(null);
        prod.setFin(TiempoUtils.ahora());
        prod.setEstado("cerrada");
        produccionService.save(prod);
         model.addAttribute("ordenesAbiertas", produccionService.traerAbiertas(TenantContext.getTenantId()));
          List<String> lineas=Arrays.asList(Produccion.LINEA_1,
                                                  Produccion.LINEA_2,
                                                  Produccion.LINEA_3,
                                                  Produccion.LINEA_4,
                                                  Produccion.LINEA_5);
        model.addAttribute("lineas",lineas);
         model.addAttribute("productos",productoService.findAllByTenant());
//        log.info("id: "+produccion.getId());
        
        model.addAttribute("url",url);
         model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());
        return "produccion";
    }
     @PostMapping("/modificarCantidad/{id}")
    public String modificarCantidad(  @RequestParam("url")String url,@RequestParam("cantidad")String cantidad, Model model,  Produccion produccion) {
        
        
        Produccion prod=produccionService.findById(produccion.getId()).orElse(null);
        prod.setCantidad(cantidad);
        produccionService.save(prod);
        
         model.addAttribute("ordenesAbiertas", produccionService.traerAbiertas(TenantContext.getTenantId()));
          List<String> lineas=Arrays.asList(Produccion.LINEA_1,
                                                  Produccion.LINEA_2,
                                                  Produccion.LINEA_3,
                                                  Produccion.LINEA_4,
                                                  Produccion.LINEA_5);
        model.addAttribute("lineas",lineas);
         model.addAttribute("productos",productoService.findAllByTenant());
//        log.info("id: "+produccion.getId());
        
        model.addAttribute("url",url);
         model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());
        return "produccion";
    }
    
     @GetMapping("/historialOrdenes/{url}")
    public String cerrarOrdenDeTrabajo(  @PathVariable("url") String url, Model model) {
        
         model.addAttribute("ordenesCerradas", produccionService.traerCerradas(TenantContext.getTenantId()));
        model.addAttribute("url",url);
         model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());
        return "historialProduccion";
    }
     @GetMapping("/configuracionOrdenes/{url}")
    public String configuracionOrdenes(  @PathVariable("url") String url, Model model) {
        model.addAttribute("productos",productoService.findAllByTenant());
       List<String> lineas=Arrays.asList(Produccion.LINEA_1,
                                                  Produccion.LINEA_2,
                                                  Produccion.LINEA_3,
                                                  Produccion.LINEA_4,
                                                  Produccion.LINEA_5);
        model.addAttribute("lineas",lineas);
        model.addAttribute("producto",new Producto());
        
        model.addAttribute("url",url);
         model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());
        return "configuracionOrdenes";
    }
     @GetMapping("/eliminarOrden/{id}")
    public String eliminarOrden(  @PathVariable("id") Long id,@RequestParam("url")String url, Model model ) {
        model.addAttribute("produccion", new Produccion());
        Produccion prod=produccionService.findById(id).orElse(null);
        produccionService.delete(prod);
         model.addAttribute("ordenesAbiertas", produccionService.traerAbiertas(TenantContext.getTenantId()));
         List<String> lineas=Arrays.asList(Produccion.LINEA_1,
                                                  Produccion.LINEA_2,
                                                  Produccion.LINEA_3,
                                                  Produccion.LINEA_4,
                                                  Produccion.LINEA_5);
        model.addAttribute("lineas",lineas);
         model.addAttribute("productos",productoService.findAllByTenant());
//        produccionService.save(produccion);
        model.addAttribute("url",url);
         model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());
        return "produccion";
    }
    
    
  @PostMapping("/nuevoProducto")
    public String nuevoProducto(@RequestParam("url")String url, Model model,  Producto producto) {
        
        
        productoService.save(producto);
       
         model.addAttribute("produccion", new Produccion());
          model.addAttribute("ordenesAbiertas", produccionService.traerAbiertas(TenantContext.getTenantId()));
        model.addAttribute("url",url);
         List<String> lineas=Arrays.asList(Produccion.LINEA_1,
                                                  Produccion.LINEA_2,
                                                  Produccion.LINEA_3,
                                                  Produccion.LINEA_4,
                                                  Produccion.LINEA_5);
        model.addAttribute("lineas",lineas);
               model.addAttribute("producto",new Producto());
         model.addAttribute("productos",productoService.findAllByTenant());
        return "redirect:/configuracionOrdenes/"+url;
    }
      @GetMapping("/eliminarProducto/{id}")
    public String eliminarProducto(  @PathVariable("id") Long id,@RequestParam("url")String url, Model model ) {
        
        
        model.addAttribute("produccion", new Produccion());
        Producto prod=productoService.findById(id).orElse(null);
        productoService.delete(prod);
         model.addAttribute("ordenesAbiertas", produccionService.traerAbiertas(TenantContext.getTenantId()));
         List<String> lineas=Arrays.asList(Produccion.LINEA_1,
                                                  Produccion.LINEA_2,
                                                  Produccion.LINEA_3,
                                                  Produccion.LINEA_4,
                                                  Produccion.LINEA_5);
        model.addAttribute("lineas",lineas);
         model.addAttribute("productos",productoService.findAllByTenant());
         model.addAttribute("producto",new Producto());
        
//        produccionService.save(produccion);
        model.addAttribute("url",url);
          model.addAttribute("nombresLayouts", ArchivoExterno.nombresLayouts());
        return "configuracionOrdenes";
    }
}
