package uz.app.projectv1;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import uz.app.projectv1.user.UserRepository;
import uz.app.projectv1.user.entity.UserEntity;

@SpringBootApplication
public class ProjectV1Application {

    public static void main(String[] args) {
        SpringApplication.run(ProjectV1Application.class, args);
    }
//
//    @Bean
//    CommandLineRunner test(UserRepository repo) {
//        return args -> {
//            UserEntity u = new UserEntity();
//            u.setEmail("test@app.uz");
//            u.setPassword("vaqtinchalik");
//            repo.save(u);
//            System.out.println("createdDate = " + u.getCreatedDate());
//        };
//    }

}
