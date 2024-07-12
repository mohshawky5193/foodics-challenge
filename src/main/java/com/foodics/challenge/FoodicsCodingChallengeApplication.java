package com.foodics.challenge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FoodicsCodingChallengeApplication {

	public static void main(String[] args) {
		SpringApplication.run(FoodicsCodingChallengeApplication.class, args);
	}

}
