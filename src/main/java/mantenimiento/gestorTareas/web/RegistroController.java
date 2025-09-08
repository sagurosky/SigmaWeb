package mantenimiento.gestorTareas.web;

import mantenimiento.gestorTareas.datos.TenantDao;
import mantenimiento.gestorTareas.datos.UsuarioDao;
import mantenimiento.gestorTareas.dominio.Rol;
import mantenimiento.gestorTareas.dominio.Tenant;
import mantenimiento.gestorTareas.dominio.Usuario;
import mantenimiento.gestorTareas.servicio.CaptchaService;
import mantenimiento.gestorTareas.servicio.UsuarioService;
import mantenimiento.gestorTareas.util.EncriptarPassword;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

//@CrossOrigin(origins = "*")
@CrossOrigin(origins = {"https://sigmawebapp.com", "https://www.sigmawebapp.com"})
@RestController
@RequestMapping("/api")
public class RegistroController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private TenantDao tenantRepository;

    @Autowired
    private UsuarioDao usuarioDao;
    @Autowired
    private CaptchaService captchaService;

    @PostMapping("/registro")
    public ResponseEntity<Map<String, Object>> registrarUsuario(
            @RequestParam String nombre,
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String empresa,
            @RequestParam String password,
            @RequestParam("g-recaptcha-response") String captchaResponse
    ) {
        Map<String, Object> response = new HashMap<>();

        if (!captchaService.validarCaptcha(captchaResponse)) {
            response.put("success", false);
            response.put("message", "Captcha inválido, intente nuevamente.");
            return ResponseEntity.badRequest().body(response);
        }


        if (tenantRepository.existsByEmailContacto(email)) {
            response.put("success", false);
            response.put("message", "El email ya está registrado.");
            return ResponseEntity.badRequest().body(response);
        }
        // validar username en usuario
        if (usuarioDao.existsByUsername(username)) {
            response.put("success", false);
            response.put("message", "El nombre de usuario ya está registrado.");
            return ResponseEntity.badRequest().body(response);
        }

        Tenant tenant = new Tenant();
        tenant.setEmpresa(empresa);
        tenant.setNombre(nombre);
        tenant.setFechaAlta(LocalDateTime.now());
        tenant.setEmailContacto(email);
        tenantRepository.save(tenant);

        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(EncriptarPassword.encriptarPassword(password));
        usuario.setPasswordClaro(password);
        usuario.setTenant(tenant);
        usuario.setRoles(crearRoles(usuario));
        usuarioService.guardar(usuario);

        response.put("success", true);
        response.put("message", "Cuenta creada con éxito.");
        response.put("tenantId", tenant.getId());

        return ResponseEntity.ok(response);
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

