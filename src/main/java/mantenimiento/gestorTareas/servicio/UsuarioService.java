package mantenimiento.gestorTareas.servicio;

import java.util.ArrayList;
import java.util.List;
import javax.transaction.Transactional;

import mantenimiento.gestorTareas.datos.UsuarioDao;
import mantenimiento.gestorTareas.dominio.Rol;
import mantenimiento.gestorTareas.dominio.TenantContext;
import mantenimiento.gestorTareas.dominio.TenantUserDetails;
import mantenimiento.gestorTareas.dominio.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service("userDetailsService")
@Transactional
public class UsuarioService implements UserDetailsService {

    @Autowired
    private UsuarioDao usuarioDao;

    public UsuarioDao getUsuarioDao() {
        return usuarioDao;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioDao.findByUsername(username);
        if (usuario == null) throw new UsernameNotFoundException(username);

        List<GrantedAuthority> roles = new ArrayList<>();
        for (Rol rol : usuario.getRoles()) roles.add(new SimpleGrantedAuthority(rol.getNombre()));

        return new TenantUserDetails(
                usuario.getUsername(),
                usuario.getPassword(),
                roles,
                usuario.getTenant() != null ? usuario.getTenant().getId() : null
        );
    }

    public void guardar(Usuario usuario) {
        if (usuario.getIdUsuario() != null) {
            usuario = usuarioDao.findById(usuario.getIdUsuario()).orElse(null);
        }
        usuarioDao.save(usuario);
    }

    public List<Usuario> listarUsuarios() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId != null) return usuarioDao.findByTenantId(tenantId);
        else return usuarioDao.findAll();
    }
}
