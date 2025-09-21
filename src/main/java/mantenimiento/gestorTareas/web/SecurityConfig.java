package mantenimiento.gestorTareas.web;

import mantenimiento.gestorTareas.dominio.TenantFilter;
import mantenimiento.gestorTareas.servicio.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UsuarioService usuarioService;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(usuarioService).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf()

                .ignoringAntMatchers("/notificaciones/accionDeDispositivo","/api/registro")
                .ignoringAntMatchers("/api/usuario/pagoWebhook") // permitir POST sin CSRF
                .and()
                .authorizeRequests()
                .antMatchers("/accionDeDispositivo", "/notificaciones/**").permitAll()
                .antMatchers("/editar/**", "/modificar/**", "/eliminar", "/crearUsuario/**", "/gestionUsuarios/**", "/gestionar/").hasRole("ADMIN")
                .antMatchers("/", "/editar/**", "/modificar/**").hasAnyRole("MANT", "ADMIN", "PROD")
                .antMatchers("/", "/index/**").hasAnyRole("MANT", "ADMIN", "PROD", "TECNICO", "MONITOR")
                .antMatchers("/api/registro").permitAll()
                .antMatchers("/api/usuario/pagoWebhook").permitAll()
                .and()
                .formLogin()
                .loginPage("/login")
                .and()
                .exceptionHandling().accessDeniedPage("/errores/403");

        // 👇 Registramos el TenantFilter antes de UsernamePasswordAuthenticationFilter
        http.addFilterBefore(new TenantFilter(usuarioService.getUsuarioDao()), UsernamePasswordAuthenticationFilter.class);
    }

    // necesario si luego queremos inyectar AuthenticationManager
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
