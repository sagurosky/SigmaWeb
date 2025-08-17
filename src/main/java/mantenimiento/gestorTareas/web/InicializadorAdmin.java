/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mantenimiento.gestorTareas.web;

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

    @Override
    public void run(String... args) {
        if (usuarioDao.count() == 0) {

            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword(EncriptarPassword.encriptarPassword("admin"));

            List<Rol> roles=new ArrayList<>();
            Rol rol=new Rol();
            rol.setNombre("ROLE_ADMIN");
            rol.setUsuario(admin);
            roles.add(rol);

            Rol rolMant = new Rol();
            rolMant.setUsuario(rol.getUsuario());
            rolMant.setNombre("ROLE_MANT");
            roles.add(rolMant);

            Rol rolProd = new Rol();
            rolProd.setUsuario(rol.getUsuario());
            rolProd.setNombre("ROLE_PROD");
            roles.add(rolProd);

            admin.setRoles(roles);


            usuarioDao.save(admin);
        }
    }
}

