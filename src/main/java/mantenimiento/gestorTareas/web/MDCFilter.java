package mantenimiento.gestorTareas.web;

import org.slf4j.MDC;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;
import org.springframework.security.core.context.SecurityContextHolder;

@WebFilter("/*")  // Aplica el filtro para todas las rutas
public class MDCFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No es necesario implementar
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            // Aquí obtienes el usuario de alguna forma, por ejemplo de la sesión
            String nombreUsuario = obtenerUsuarioDeLaSesion();  // Método que debes implementar

            // Agregar el usuario al contexto de MDC
            MDC.put("usuario", nombreUsuario);

            // Continuar con el ciclo de vida de la solicitud
            chain.doFilter(request, response);
        } finally {
            // Limpiar el MDC al final de la solicitud
            MDC.clear();
        }
    }

    @Override
    public void destroy() {
        // No es necesario implementar
    }

    private String obtenerUsuarioDeLaSesion() {
        // Ejemplo: obtener el usuario de la sesión o de un contexto
        return SecurityContextHolder.getContext().getAuthentication().getName(); // Implementa esto según tu lógica
    }
}
