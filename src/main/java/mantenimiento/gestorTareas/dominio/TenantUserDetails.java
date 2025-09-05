package mantenimiento.gestorTareas.dominio;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;

public class TenantUserDetails extends org.springframework.security.core.userdetails.User {

    private final Long tenantId;

    public TenantUserDetails(String username, String password,
                             Collection<? extends GrantedAuthority> authorities,
                             Long tenantId) {
        super(username, password, authorities);
        this.tenantId = tenantId;
    }

    public Long getTenantId() {
        return tenantId;
    }
}

