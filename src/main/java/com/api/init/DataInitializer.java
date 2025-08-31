package com.api.init;


import com.api.dto.GradeRequest;
import com.api.dto.RegisterRequest;
import com.api.dto.SubjectRequest;
import com.api.entity.Grade;
import com.api.entity.Role;
import com.api.entity.User;
import com.api.repository.RoleRepository;
import com.api.repository.UserRepository;
import com.api.service.AuthService;
import com.api.service.GradeService;
import com.api.service.SubjectService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class DataInitializer implements ApplicationListener<ApplicationReadyEvent> {

    @Value("${datasource.seed.enabled:false}")
    private boolean seedEnabled;

    @Bean
    CommandLineRunner initRoles(
            RoleRepository roleRepository,
            UserRepository userRepository,
            AuthService authService,
            GradeService gradeService,
            SubjectService subjectService
    ) {
        return args -> {

            if(!seedEnabled) {
                return;
            }

            List<String> roles = List.of("MASTER", "ADMIN", "TEACHER", "STUDENT", "USER");

            List<User> users = new ArrayList<>(List.of(
                    new User("Master", "", "master@master.com", "master@master.com", "master@master.com"),
                    new User("Admin", "", "admin@admin.com", "admin@admin.com", "admin@admin.com"),
                    new User("Seng", "Heat", "heatblack009@gmail.com", "heatblack009@gmail.com", "heatblack009@gmail.com")
            ));

            List<GradeRequest> grades = new ArrayList<>();

            for (int i = 1; i <= 12; i++) {
                grades.add(new GradeRequest(String.valueOf(i), "Grade " + i, "ថ្នាក់ទី " + i,   ""));
            }

            List<SubjectRequest> subjects = new ArrayList<>(List.of(
                    new SubjectRequest("Khmer", "ភាសាខ្មែរ", null),
                    new SubjectRequest("Mathematics", "គណិតវិទ្យា", null),
                    new SubjectRequest("Physics", "រូបវិទ្យា", null),
                    new SubjectRequest("Chemistry", "គីមីវិទ្យា", null),
                    new SubjectRequest("Biology", "ជីវវិទ្យា", null),
                    new SubjectRequest("Geography", "ភូមិវិទ្យា", null),
                    new SubjectRequest("History", "ប្រវត្តិវិទ្យា", null),
                    new SubjectRequest("Civic Education", "អប់រំពលរដ្ឋ", null),
                    new SubjectRequest("English", "ភាសាអង់គ្លេស", null),
                    new SubjectRequest("French", "ភាសាបារាំង", null),
                    new SubjectRequest("Economics", "សេដ្ឋកិច្ច", null),
                    new SubjectRequest("Philosophy", "ទស្សនវិជ្ជា", null),
                    new SubjectRequest("ICT", "វិទ្យាសាស្ត្រកុំព្យូទ័រ", null),
                    new SubjectRequest("Physical Education", "កាយវឌ្ឍនសាស្ត្រ និងកីឡា", null),
                    new SubjectRequest("Morality & Life Skills", "គុណធម៌ និងជីវិត", null)
            ));



            for (String r : roles) {
                roleRepository.findByName(r).orElseGet(() -> {
                    Role role = new Role();
                    role.setName(r);
                    role.setGroup(r);
                    return roleRepository.save(role);
                });
            }

            for (User user : users) {

                RegisterRequest request = new RegisterRequest(user.getFirstName(),
                        user.getLastName(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getPasswordHash()
                );

                if (user.getUsername().contains("master")) {

                    Role role = roleRepository.findByName("MASTER")
                            .orElseThrow(() -> new RuntimeException("MASTER role not found"));

                    User u = authService.register(request);

                    u.getRoles().add(role);
                    userRepository.save(u);
                    continue;
                }

                if(user.getUsername().contains("admin")) {
                    Role role = roleRepository.findByName("ADMIN")
                            .orElseThrow(() -> new RuntimeException("ADMIN role not found"));

                    User u = authService.register(request);

                    u.getRoles().add(role);
                    userRepository.save(u);
                    continue;
                }

                Role role = roleRepository.findByName("STUDENT")
                        .orElseThrow(() -> new RuntimeException("STUDENT role not found"));

                User u = authService.register(request);

                u.getRoles().add(role);
                userRepository.save(u);
            }

            for (GradeRequest grade : grades) {
                gradeService.create(grade);
            }

            for (SubjectRequest subject : subjects) {
                subjectService.create(subject);
            }

        };
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {

    }
}
