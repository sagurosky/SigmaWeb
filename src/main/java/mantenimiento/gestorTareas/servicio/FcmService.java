package mantenimiento.gestorTareas.servicio;

import com.google.auth.oauth2.GoogleCredentials;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import org.apache.hc.core5.http.ParseException;

@Service
public class FcmService {

    private final String MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    private final String FCM_SEND_ENDPOINT = "https://fcm.googleapis.com/v1/projects/notificacionessigmawebapp/messages:send";

    // Reemplazá esto con el token que ves en el Logcat de tu app móvil
    private final String targetDeviceToken = "eHr0yemkSKOANR2Uf4g3rP:APA91bHGe-KlCqiBw0qjHm8uh_R4KR0xmhDfR622JEUJnwUmfxHOITbNjYk4LjRtKnCW6k7mVbGF-OljhD20lQwb3kiRcCRaEBBYz7kvh6GUbIyu7BBzJEM";

    public void enviarNotificacionBasica() throws IOException, ParseException {
        
        String accessToken = obtenerAccessToken();
        String mensajeJson = construirMensajeJson();

        HttpPost post = new HttpPost(FCM_SEND_ENDPOINT);
        post.setHeader("Authorization", "Bearer " + accessToken);
        post.setHeader("Content-Type", "application/json");
        post.setEntity(new StringEntity(mensajeJson));

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            var response = client.execute(post);
            String responseBody = EntityUtils.toString(response.getEntity());
            System.out.println("FCM respuesta: " + responseBody);
        }
    }

    private String obtenerAccessToken() throws IOException {
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new FileInputStream("src/main/resources/notificacionessigmawebapp-firebase-adminsdk-fbsvc-c34bd9003a.json"))
                .createScoped(Collections.singleton(MESSAGING_SCOPE));
        googleCredentials.refreshIfExpired();
        return googleCredentials.getAccessToken().getTokenValue();
    }

   private String construirMensajeJson() {
    return "{\n" +
           "  \"message\": {\n" +
           "    \"token\": \"" + targetDeviceToken + "\",\n" +
           "    \"data\": {\n" +
           "      \"title\": \"titulo notificacion\",\n" +
           "      \"body\": \"Cuerpo notificacion\"\n" +
           "    }\n" +
           "  }\n" +
           "}";
}
}

