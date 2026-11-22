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

            // Usamos preapproval_plan para no imponer restricciones de payer_email en el checkout
            String url = "https://api.mercadopago.com/preapproval_plan";

            log.info("Iniciando creación de plan de suscripción para usuario: {}, monto: {}", usuario.getUsername(), monto);

            String backUrl = ArchivoExterno.getString("mp_back_url");
            if (backUrl == null || backUrl.trim().isEmpty()) {
                backUrl = "https://sigmawebapp.com/inicio/api/usuario/pagoExitoso";
            }

            Map<String, Object> json = new HashMap<>();
            json.put("reason", "Suscripción Usuario - " + usuario.getUsername());
            json.put("back_url", backUrl);

            Map<String, Object> autoRecurring = new HashMap<>();
            autoRecurring.put("frequency", 1);
            autoRecurring.put("frequency_type", "months");
            autoRecurring.put("transaction_amount", monto);
            autoRecurring.put("currency_id", "ARS");
            json.put("auto_recurring", autoRecurring);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(json, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            Map<String, Object> body = response.getBody();
            log.info("Respuesta de Mercado Pago preapproval_plan: {}", body);

            if (body != null && body.containsKey("init_point")) {
                String planId = body.get("id").toString();
                usuario.setPreapprovalId(planId);
                usuarioService.guardar(usuario);
                String initPoint = body.get("init_point").toString();
                log.info("Plan de suscripción creado exitosamente. plan_id: {}, init_point: {}", planId, initPoint);
                return initPoint;
            }

            throw new RuntimeException("No se recibió init_point en la respuesta de Mercado Pago");

        } catch (Exception e) {
            log.error("Error creando preapproval_plan en Mercado Pago", e);
            throw new RuntimeException("No se pudo iniciar la suscripción en MP: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> obtenerPreapprovalMP(String preapprovalId) {
        if (preapprovalId == null || preapprovalId.trim().isEmpty()) {
            return null;
        }
        try {
            String url = "https://api.mercadopago.com/preapproval/" + preapprovalId;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(getAccessToken());
            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, request, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error al obtener detalle de preapproval " + preapprovalId + " de MP", e);
            return null;
        }
    }

    public boolean validarPreapproval(String preapprovalId) {
        Map<String, Object> body = obtenerPreapprovalMP(preapprovalId);
        if (body != null) {
            String status = (String) body.get("status");
            log.info("Validación preapproval {} - estado MP: {}", preapprovalId, status);
            return "authorized".equalsIgnoreCase(status);
        }
        return false;
    }

    public Usuario obtenerUsuarioPorSuscripcion(String preapprovalId, String planId) {
        // 1. Si vino el plan_id directamente desde el callback
        if (planId != null && !planId.trim().isEmpty()) {
            Usuario u = usuarioDao.findByPreapprovalId(planId.trim());
            if (u != null) {
                return u;
            }
        }

        // 2. Intentar buscar por el preapproval_id directo
        if (preapprovalId != null && !preapprovalId.trim().isEmpty()) {
            Usuario u = usuarioDao.findByPreapprovalId(preapprovalId.trim());
            if (u != null) {
                return u;
            }

            // 3. Consultar a Mercado Pago para extraer el preapproval_plan_id asociado
            Map<String, Object> mpData = obtenerPreapprovalMP(preapprovalId.trim());
            if (mpData != null && mpData.containsKey("preapproval_plan_id")) {
                Object mpPlanId = mpData.get("preapproval_plan_id");
                if (mpPlanId != null) {
                    u = usuarioDao.findByPreapprovalId(mpPlanId.toString().trim());
                    if (u != null) {
                        return u;
                    }
                }
            }
        }

        return null;
    }

    public Usuario obtenerUsuarioTemporal(String preapprovalId) {
        return obtenerUsuarioPorSuscripcion(preapprovalId, null);
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

