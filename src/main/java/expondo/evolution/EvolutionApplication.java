package expondo.evolution;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EvolutionApplication {

    public static void main(String[] args) {
        SpringApplication.run(EvolutionApplication.class, args);
    }
}
