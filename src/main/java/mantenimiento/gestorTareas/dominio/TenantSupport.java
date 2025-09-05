package mantenimiento.gestorTareas.dominio;

public interface TenantSupport {
    Tenant getTenant();
    void setTenant(Tenant tenant);
}
