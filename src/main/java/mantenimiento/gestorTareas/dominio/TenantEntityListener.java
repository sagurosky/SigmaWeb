package mantenimiento.gestorTareas.dominio;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

public class TenantEntityListener {
    @PrePersist
    @PreUpdate
    public void setTenant(Object entity) {
        if (entity instanceof TenantSupport) {
            TenantSupport tenantEntity = (TenantSupport) entity;
            if (tenantEntity.getTenant() == null && TenantContext.getTenantId() != null) {
                Tenant t = new Tenant();
                t.setId(TenantContext.getTenantId());
                tenantEntity.setTenant(t);
            }
        }
    }
}
