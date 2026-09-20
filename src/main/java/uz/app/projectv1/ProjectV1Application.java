package uz.app.projectv1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ProjectV1Application {

    public static void main(String[] args) {
        SpringApplication.run(ProjectV1Application.class, args);
    }
}
