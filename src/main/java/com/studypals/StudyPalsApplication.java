package com.studypals;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class StudyPalsApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudyPalsApplication.class, args);
    }

}
