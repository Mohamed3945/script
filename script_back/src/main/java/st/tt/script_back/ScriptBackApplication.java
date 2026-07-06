package st.tt.script_back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ScriptBackApplication class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
@SpringBootApplication
public class ScriptBackApplication {

	/**
	 * Executes main.
	 *
	 * @param args input argument consumed by main.
	 */
	public static void main(String[] args) {
		SpringApplication.run(ScriptBackApplication.class, args);
	}

}
