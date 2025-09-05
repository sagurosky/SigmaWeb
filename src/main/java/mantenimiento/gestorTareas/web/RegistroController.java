package mantenimiento.gestorTareas.web;

import mantenimiento.gestorTareas.datos.TenantDao;
import mantenimiento.gestorTareas.dominio.Rol;
import mantenimiento.gestorTareas.dominio.Tenant;
import mantenimiento.gestorTareas.dominio.Usuario;
import mantenimiento.gestorTareas.servicio.UsuarioService;
import mantenimiento.gestorTareas.util.EncriptarPassword;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class RegistroController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private TenantDao tenantRepository;

    @PostMapping("/registro")
    public ResponseEntity<String> registrarUsuario(
            @RequestParam String nombre,
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String empresa,
            @RequestParam String password
    ) {
        // validar email en tenant
        if (tenantRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body("El email ya está registrado.");
        }

        // crear tenant
        Tenant tenant = new Tenant();
        tenant.setEmpresa(empresa);
        tenant.setNombre(nombre);
        tenant.setFechaAlta(LocalDateTime.now());
        tenant.setEmailContacto(email);
        tenantRepository.save(tenant);

        // crear usuario admin
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(EncriptarPassword.encriptarPassword(password));
        usuario.setTenant(tenant);

        usuario.setRoles(crearRoles(usuario));

        usuarioService.guardar(usuario);

        return ResponseEntity.ok("Cuenta creada con éxito.");
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

