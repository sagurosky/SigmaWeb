package mantenimiento.gestorTareas.servicio;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import mantenimiento.gestorTareas.datos.UsuarioDao;
import mantenimiento.gestorTareas.dominio.Rol;
import mantenimiento.gestorTareas.dominio.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("userDetailsService")//tiene que ser ese nombre para que lo reconozca spring
//con esto indicamos que UsuarioService va a ser un bean de servicio y se va a llamar userDetailsService
@Slf4j
public class UsuarioService implements UserDetailsService{

    @Autowired 
    UsuarioDao  usuarioDao;
    
    @Override
    @Transactional(readOnly=true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Usuario usuario=usuarioDao.findByUsername(username);
    //toda esta parafernalia es lo que se necesita para cargar los usuarios y roles
    //analizando un poco: tiene que creear un userDetails con los datos de nombre,password y
    //roles, y los roles tienen que estar en el formato que necesita (GrantedAuthority)
    if(usuario==null){
        throw new UsernameNotFoundException(username);
    }
    
    var roles=new ArrayList<GrantedAuthority>();
        
    for(Rol rol:usuario.getRoles()){
        roles.add(new SimpleGrantedAuthority(rol.getNombre()));
    }
    
    return new User(usuario.getUsername(),usuario.getPassword(),roles);
    }
    public void guardar(Usuario usuario){
        
        if(usuario.getIdUsuario()!=null)usuario=usuarioDao.findById(usuario.getIdUsuario()).orElse(null);
        usuarioDao.save(usuario);
    }
    
    public List<Usuario> listarUsuarios(){
        return usuarioDao.findAll();
    }
    
}
