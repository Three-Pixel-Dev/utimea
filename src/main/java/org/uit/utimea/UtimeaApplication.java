package org.uit.utimea;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class UtimeaApplication {

	public static void main(String[] args) {
		SpringApplication.run(UtimeaApplication.class, args);
	}

}
