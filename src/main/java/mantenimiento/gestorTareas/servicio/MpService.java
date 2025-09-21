package mantenimiento.gestorTareas.servicio;
import com.mercadopago.client.preapproval.PreapprovalClient;
import com.mercadopago.client.preapproval.PreapprovalCreateRequest;
import com.mercadopago.resources.preapproval.Preapproval;
import com.mercadopago.MercadoPagoConfig;
import lombok.extern.slf4j.Slf4j;
import mantenimiento.gestorTareas.datos.RolDao;
import mantenimiento.gestorTareas.datos.UsuarioDao;
import mantenimiento.gestorTareas.dominio.Rol;
import mantenimiento.gestorTareas.dominio.Usuario;
import mantenimiento.gestorTareas.util.ArchivoExterno;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class MpService {

    @Autowired
    private UsuarioDao usuarioDao;
    @Autowired
    private UsuarioService usuarioService;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String accessToken = ArchivoExterno.getString("mp_access_token");

    public String crearPreapproval(Usuario usuario, String email, Double monto) {
        try {
            String url = "https://api.mercadopago.com/preapproval";
            log.info("Usando token MP: {}", accessToken);
            log.info("token mp: "+ArchivoExterno.getString("mp_access_token"));
            Map<String, Object> json = new HashMap<>();
            json.put("reason", "Usuario agregado - " + usuario.getUsername());
            json.put("payer_email", email); // MP valida este email
            Map<String, Object> autoRecurring = new HashMap<>();
            autoRecurring.put("frequency", 1);
            autoRecurring.put("frequency_type", "months");
//            autoRecurring.put("repetitions", 12);
            autoRecurring.put("transaction_amount", monto);
            autoRecurring.put("currency_id", "ARS");
//            autoRecurring.put("free_trial", Map.of(
//                    "frequency", 1,
//                    "frequency_type", "months"
//            ));
            json.put("auto_recurring", autoRecurring);

            json.put("payment_methods_allowed", Map.of(
                    "payment_types", List.of(Map.of("id", "credit_card")),
                    "payment_methods", List.of(Map.of("id", "bolbradesco"))
            ));

//            json.put("back_url", "https://sigmawebapp.com/api/usuario/pagoExitoso");
            json.put("back_url", "https://30c719d7b2a1.ngrok-free.app/api/usuario/pagoExitoso");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(json, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            Map<String, Object> body = response.getBody();
            log.info("Preapproval response: {}", body);

            if (body != null && body.containsKey("init_point")) {
                usuario.setPreapprovalId(body.get("id").toString());
                usuarioService.guardar(usuario);
                log.info("respuesta de MP: "+body.get("init_point").toString());
                return body.get("init_point").toString();
            }

            throw new RuntimeException("No se pudo crear la preaprobación");

        } catch (Exception e) {
            log.error("Error creando preapproval", e);
            throw new RuntimeException("No se pudo iniciar la suscripción en MP");
        }
    }

    public boolean validarPreapproval(String preapprovalId) {
        // Aquí podrías llamar a GET /preapproval_plan/{id} si quieres validar estado real
        return true; // temporal
    }

    public Usuario obtenerUsuarioTemporal(String preapprovalId) {
        return usuarioDao.findByPreapprovalId(preapprovalId);
    }
}

