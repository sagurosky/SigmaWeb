package mantenimiento.gestorTareas.infraestructura.multitenant;

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

    public TenantUserDetails(String username, String password, boolean enabled,
                             Collection<? extends GrantedAuthority> authorities,
                             Long tenantId) {
        super(username, password, enabled, true, true, true, authorities);
        this.tenantId = tenantId;
    }

    public Long getTenantId() {
        return tenantId;
    }
}

