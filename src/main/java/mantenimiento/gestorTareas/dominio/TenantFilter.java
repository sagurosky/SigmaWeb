package mantenimiento.gestorTareas.dominio;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mantenimiento.gestorTareas.datos.UsuarioDao;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class TenantFilter extends OncePerRequestFilter {

    private final UsuarioDao usuarioDao;

    public TenantFilter(UsuarioDao usuarioDao) {
        this.usuarioDao = usuarioDao;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {

                Object principal = auth.getPrincipal();
                Long tenantId = null;

                if (principal instanceof TenantUserDetails) {
                    tenantId = ((TenantUserDetails) principal).getTenantId();
                }

                // Seteamos en TenantContext
                if (tenantId != null) {
                    TenantContext.setTenantId(tenantId);
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear(); // limpiamos ThreadLocal
        }
    }
}

