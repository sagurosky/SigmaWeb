package mantenimiento.gestorTareas.servicio;

import com.google.api.client.util.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class CaptchaService {

    private static final String GOOGLE_RECAPTCHA_VERIFY_URL =
            "https://www.google.com/recaptcha/api/siteverify";

    @Value("${google.recaptcha.secret}")
    private String recaptchaSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean validarCaptcha(String responseToken) {
        Map<String, String> body = new HashMap<>();
        body.put("secret", recaptchaSecret);
        body.put("response", responseToken);

        ResponseEntity<Map> recaptchaResponse =
                restTemplate.postForEntity(GOOGLE_RECAPTCHA_VERIFY_URL, body, Map.class);

        if (recaptchaResponse.getBody() == null) return false;

        return (Boolean) recaptchaResponse.getBody().get("success");
    }
}

