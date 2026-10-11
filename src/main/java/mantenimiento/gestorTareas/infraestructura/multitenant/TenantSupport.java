package mantenimiento.gestorTareas.infraestructura.multitenant;

public interface TenantSupport {
    Tenant getTenant();
    void setTenant(Tenant tenant);
}
