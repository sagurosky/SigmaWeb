package mantenimiento.gestorTareas;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")  // Esto asegura que use application-test.properties
class GestorTareasApplicationTests {

	@Test
	void contextLoads() {
		// Este test verifica que el contexto de Spring se carga correctamente
		// No necesita código, si llega aquí es que pasó
	}
}