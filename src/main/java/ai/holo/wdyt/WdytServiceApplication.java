package ai.holo.wdyt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class WdytServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(WdytServiceApplication.class, args);
	}

}
