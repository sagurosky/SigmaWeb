package mantenimiento.gestorTareas;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class GestorTareasApplicationTests {

	@Test
	void contextLoads() {
		// Test de carga de contexto básico
		assertTrue(true, "El contexto se carga correctamente");
	}
}