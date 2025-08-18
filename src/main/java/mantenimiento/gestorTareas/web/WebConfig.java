package mantenimiento.gestorTareas.web;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import mantenimiento.gestorTareas.util.ArchivoExterno;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//i18n significa internacionalization

@Configuration//sin esto no funciona nada
public class WebConfig implements WebMvcConfigurer{
    @Bean//al declarar un bean lo agrega al contenedor de Spring
    public LocaleResolver localeResolver(){
       //estas son clases del API de Spring, las utiliza para
       //poder configurar la internacionalizacion
        var slr=new SessionLocaleResolver();
        slr.setDefaultLocale(new Locale("es"));
        return slr;
    }
    //interceptor para cambiar de idioma de manera dinamica
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor(){
        //parametro que vamos a utilizar para cambiar de idioma
        var lci=new LocaleChangeInterceptor();
        lci.setParamName("lang");//buscar una lista de elementos de i18n
   return lci;
    }
//registramos el interceptor
    
    @Override
    public void addInterceptors(InterceptorRegistry registro){
        registro.addInterceptor(localeChangeInterceptor());
    }
    
    @Override
    public void addViewControllers(ViewControllerRegistry registro){
        registro.addViewController("/").setViewName("index");
        registro.addViewController("/login");
        registro.addViewController("/crearUsuario");
        registro.addViewController("/gestionUsuarios");
        registro.addViewController("/errores/403").setViewName("/errores/403");
                
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        WebMvcConfigurer.super.addResourceHandlers(registry); //To change body of generated methods, choose Tools | Templates.

            //DMS para AWS
        registry.addResourceHandler("/recursos/**").addResourceLocations("file:/media/sf_personal/sigmaweb/recursos/");

            //DMS docker
//            registry.addResourceHandler("/recursos/**").addResourceLocations("file:/app/recursos/");







    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addFormatterForFieldType(LocalDate.class, new LocalDateFormatter());
    }
    
    
      private static class LocalDateFormatter implements org.springframework.format.Formatter<LocalDate> {

        @Override
        public LocalDate parse(String text, java.util.Locale locale) {
            return LocalDate.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }

        @Override
        public String print(LocalDate object, java.util.Locale locale) {
            return DateTimeFormatter.ofPattern("yyyy-MM-dd").format(object);
        }
    }
      
      
      
     @Bean
    public FilterRegistrationBean<MDCFilter> loggingFilter() {
        FilterRegistrationBean<MDCFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new MDCFilter());
        registrationBean.addUrlPatterns("/*"); // Aplica el filtro a todas las URL
        return registrationBean;
    }

    
}
