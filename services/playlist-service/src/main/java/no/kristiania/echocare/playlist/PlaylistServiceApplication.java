package no.kristiania.echocare.playlist;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRabbit
public class PlaylistServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlaylistServiceApplication.class, args);
    }

}
