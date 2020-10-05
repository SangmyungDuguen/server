package smu.capstone.heartsignal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;

@SpringBootApplication
public class HeartsignalApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(HeartsignalApplication.class);
        app.addListeners((ApplicationStartedEvent event)->{System.out.println("App Started !");});
        app.run(args);
    }

}
