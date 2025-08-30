/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mantenimiento.gestorTareas.web;

import mantenimiento.gestorTareas.datos.TenantDao;
import mantenimiento.gestorTareas.dominio.Tenant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import mantenimiento.gestorTareas.datos.UsuarioDao;
import mantenimiento.gestorTareas.dominio.Rol;
import mantenimiento.gestorTareas.dominio.Usuario;
import mantenimiento.gestorTareas.util.EncriptarPassword;

import java.util.ArrayList;
import java.util.List;

@Component
public class InicializadorAdmin implements CommandLineRunner {

    @Autowired
    private UsuarioDao usuarioDao;

    @Autowired
    private TenantDao tenantDao;

    @Override
    public void run(String... args) {
        if (usuarioDao.count() == 0) {

            // Crear tenants temporales
            Tenant tenant1 = new Tenant();
            tenant1.setNombre("Empresa1");
            tenantDao.save(tenant1);

            Tenant tenant2 = new Tenant();
            tenant2.setNombre("Empresa2");
            tenantDao.save(tenant2);

            // Crear admin1
            Usuario admin1 = new Usuario();
            admin1.setUsername("admin1");
            admin1.setPasswordClaro("admin1");
            admin1.setPassword(EncriptarPassword.encriptarPassword("admin1"));
            admin1.setTenant(tenant1);
            admin1.setRoles(crearRoles(admin1));
            usuarioDao.save(admin1);

            // Crear admin2
            Usuario admin2 = new Usuario();
            admin2.setUsername("admin2");
            admin2.setPasswordClaro("admin2");
            admin2.setPassword(EncriptarPassword.encriptarPassword("admin2"));
            admin2.setTenant(tenant2);
            admin2.setRoles(crearRoles(admin2));
            usuarioDao.save(admin2);
        }
    }

    private List<Rol> crearRoles(Usuario usuario) {
        List<Rol> roles = new ArrayList<>();

        Rol rolAdmin = new Rol();
        rolAdmin.setNombre("ROLE_ADMIN");
        rolAdmin.setUsuario(usuario);
        roles.add(rolAdmin);

        Rol rolMant = new Rol();
        rolMant.setNombre("ROLE_MANT");
        rolMant.setUsuario(usuario);
        roles.add(rolMant);

        Rol rolProd = new Rol();
        rolProd.setNombre("ROLE_PROD");
        rolProd.setUsuario(usuario);
        roles.add(rolProd);

        return roles;
    }
}

