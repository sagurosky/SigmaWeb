package mantenimiento.gestorTareas.modulo.suscripcion;
import mantenimiento.gestorTareas.modulo.usuarios.UsuarioService;
import mantenimiento.gestorTareas.infraestructura.util.ArchivoExterno;
import mantenimiento.gestorTareas.modulo.usuarios.Rol;
import mantenimiento.gestorTareas.modulo.usuarios.RolDao;
import mantenimiento.gestorTareas.modulo.usuarios.Usuario;
import mantenimiento.gestorTareas.modulo.usuarios.UsuarioDao;
import com.mercadopago.client.preapproval.PreapprovalClient;
import com.mercadopago.client.preapproval.PreapprovalCreateRequest;
import com.mercadopago.resources.preapproval.Preapproval;
import com.mercadopago.MercadoPagoConfig;
import lombok.extern.slf4j.Slf4j;
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

    public String getAccessToken() {
        return ArchivoExterno.getString("mp_access_token");
    }

    public String crearPreapproval(Usuario usuario, String email, Double monto) {
        try {
            String token = getAccessToken();
            if (token == null || token.trim().isEmpty() || token.contains("TU-TOKEN")) {
                throw new IllegalStateException("Token de Mercado Pago no configurado en recursos/configuracion.properties");
            }

            String url = "https://api.mercadopago.com/preapproval";
            String payerEmail = email;
            String testPayer = ArchivoExterno.getString("mp_test_payer_email");
            if (testPayer != null && !testPayer.trim().isEmpty()) {
                payerEmail = testPayer.trim();
            } else if (token.contains("2684416608")) {
                // Fallback automático para la cuenta de prueba de Mercado Pago
                payerEmail = "test_user_4798818165579077916@testuser.com";
                log.info("Usando email de pagador de prueba automático: {}", payerEmail);
            }

            log.info("Iniciando creación de preapproval para usuario: {}, email pagador: {}, monto: {}", usuario.getUsername(), payerEmail, monto);

            Map<String, Object> json = new HashMap<>();
            json.put("reason", "Suscripción Usuario - " + usuario.getUsername());
            json.put("payer_email", payerEmail);
            Map<String, Object> autoRecurring = new HashMap<>();
            autoRecurring.put("frequency", 1);
            autoRecurring.put("frequency_type", "months");
            autoRecurring.put("transaction_amount", monto);
            autoRecurring.put("currency_id", "ARS");
            json.put("auto_recurring", autoRecurring);

            String backUrl = ArchivoExterno.getString("mp_back_url");
            if (backUrl == null || backUrl.trim().isEmpty()) {
                backUrl = "https://sigmawebapp.com/inicio/api/usuario/pagoExitoso";
            }
            json.put("back_url", backUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(json, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            Map<String, Object> body = response.getBody();
            log.info("Respuesta de Mercado Pago preapproval: {}", body);

            if (body != null && body.containsKey("init_point")) {
                usuario.setPreapprovalId(body.get("id").toString());
                usuarioService.guardar(usuario);
                String initPoint = body.get("init_point").toString();
                log.info("Preapproval creado exitosamente. init_point: {}", initPoint);
                return initPoint;
            }

            throw new RuntimeException("No se recibió init_point en la respuesta de Mercado Pago");

        } catch (Exception e) {
            log.error("Error creando preapproval en Mercado Pago", e);
            throw new RuntimeException("No se pudo iniciar la suscripción en MP: " + e.getMessage(), e);
        }
    }

    public boolean validarPreapproval(String preapprovalId) {
        if (preapprovalId == null || preapprovalId.trim().isEmpty()) {
            return false;
        }
        try {
            String url = "https://api.mercadopago.com/preapproval/" + preapprovalId;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(getAccessToken());
            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, request, Map.class);
            Map<String, Object> body = response.getBody();
            if (body != null) {
                String status = (String) body.get("status");
                log.info("Validación preapproval {} - estado MP: {}", preapprovalId, status);
                return "authorized".equalsIgnoreCase(status);
            }
        } catch (Exception e) {
            log.error("Error al validar preapproval " + preapprovalId + " con MP", e);
        }
        return false;
    }

    public Usuario obtenerUsuarioTemporal(String preapprovalId) {
        return usuarioDao.findByPreapprovalId(preapprovalId);
    }

    public boolean cancelarPreapproval(String preapprovalId) {
        if (preapprovalId == null || preapprovalId.trim().isEmpty()) {
            return false;
        }
        try {
            String url = "https://api.mercadopago.com/preapproval/" + preapprovalId;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(getAccessToken());

            Map<String, Object> body = Map.of("status", "cancelled");
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.PUT, request, Map.class);
            log.info("Cancelación de suscripción MP para {}: status {}", preapprovalId, response.getStatusCode());
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Error al cancelar suscripción en Mercado Pago para " + preapprovalId, e);
            return false;
        }
    }
}

