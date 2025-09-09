package mantenimiento.gestorTareas.servicio;

import com.google.api.client.util.Value;
import mantenimiento.gestorTareas.util.ArchivoExterno;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
@Service
public class CaptchaService {

    private static final String GOOGLE_RECAPTCHA_VERIFY_URL =
            "https://www.google.com/recaptcha/api/siteverify";

    private String recaptchaSecret = ArchivoExterno.getString("captcha_secret");

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean validarCaptcha(String responseToken) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("secret", recaptchaSecret);
        params.add("response", responseToken);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(params, headers);

        ResponseEntity<Map> recaptchaResponse =
                restTemplate.postForEntity(GOOGLE_RECAPTCHA_VERIFY_URL, request, Map.class);

        System.out.println("Respuesta CAPTCHA: " + recaptchaResponse.getBody());

        if (recaptchaResponse.getBody() == null) return false;

        return (Boolean) recaptchaResponse.getBody().get("success");
    }
}