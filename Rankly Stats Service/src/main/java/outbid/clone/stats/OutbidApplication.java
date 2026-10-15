package outbid.clone.stats;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OutbidApplication {

	public static void main(String[] args) {
		SpringApplication.run(OutbidApplication.class, args);
	}

}
