package com.api.init;

import com.api.content_management.model.entity.DataStructure;
import com.api.user.model.entity.Role;
import com.api.user.model.entity.User;
import com.api.user.model.entity.Permission;
import com.api.enums.DataStructureType;
import com.api.content_management.repository.DataStructureRepository;
import com.api.user.repository.RoleRepository;
import com.api.user.repository.UserRepository;
import com.api.user.repository.PermissionRepository;
import com.api.enums.SystemPermission;
import com.api.enums.SystemRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
public class DataInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${datasource.seed.enabled:false}")
    private boolean seedEnabled;

    public DataInitializer(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PermissionRepository permissionRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (!seedEnabled) return;

        // 1️⃣ Create Master User
        User masterUser = createMasterUser();

        // 2️⃣ Seed Roles
        seedRoles(masterUser);

        // 3️⃣ Seed Permissions
        seedPermissions(masterUser);

        // 1️⃣ Create User
        createUsersForRoles(masterUser);

        // 4️⃣ Seed Grades and Subjects
        initGradesAndSubjects(masterUser, event.getApplicationContext().getBean(DataStructureRepository.class));
    }

    private User createMasterUser() {
        return userRepository.findByEmail("master@gmail.com")
                .orElseGet(() -> {
                    User user = new User();
                    user.setFirstName("Web");
                    user.setLastName("Master");
                    user.setUsername("master@gmail.com");
                    user.setEmail("master@gmail.com");
                    user.setPasswordHash(passwordEncoder.encode("master123"));
                    user.setStatus("active");

                    userRepository.save(user);

                    Role adminRole = roleRepository.findByName(SystemRole.MASTER.toString())
                            .orElseGet(() -> {
                                Role role = new Role();
                                role.setName(SystemRole.MASTER.toString());
                                role.setGroup("MASTER");
                                role.setCreatedBy(user);
                                return roleRepository.save(role);
                            });

                    user.getRoles().add(adminRole);
                    return userRepository.save(user);
                });
    }

    private void createUsersForRoles(User masterUser) {
        // Map role -> default user info
        Map<String, String> roleUsers = Map.of(
                "ADMIN", "admin@gmail.com",
                "TEACHER", "teacher@gmail.com",
                "STUDENT", "student@gmail.com",
                "PARENT", "parent@gmail.com",
                "USER", "user@gmail.com"
        );

        for (Map.Entry<String, String> entry : roleUsers.entrySet()) {
            String roleName = entry.getKey();
            String email = entry.getValue();

            userRepository.findByEmail(email)
                    .orElseGet(() -> {
                        // Create user
                        User user = new User();
                        user.setFirstName(roleName.charAt(0) + roleName.substring(1).toLowerCase());
                        user.setLastName("User");
                        user.setUsername(email);
                        user.setEmail(email);
                        user.setPasswordHash(passwordEncoder.encode("password123")); // default password
                        user.setStatus("active");

                        // Assign role
                        Role role = roleRepository.findByName(roleName)
                                .orElseThrow(() -> new RuntimeException(roleName + " role not found!"));
                        user.getRoles().add(role);

                        return userRepository.save(user);
                    });
        }
    }


    private void seedRoles(User masterUser) {
        Map<String, List<String>> groupRoles = Map.of(
                "MASTER", List.of(SystemRole.MASTER.toString()),
                "ADMIN", List.of(SystemRole.ADMIN.toString()),
                "GENERAL", List.of(SystemRole.USER.toString(),
                        SystemRole.TEACHER.toString(),
                        SystemRole.STUDENT.toString(),
                        SystemRole.PARENT.toString())
        );

        for (Map.Entry<String, List<String>> entry : groupRoles.entrySet()) {
            String groupName = entry.getKey();
            List<String> roles = entry.getValue();

            for (String roleName : roles) {
                roleRepository.findByName(roleName)
                        .orElseGet(() -> {
                            Role role = new Role();
                            role.setName(roleName);
                            role.setGroup(groupName);
                            role.setCreatedBy(masterUser);
                            return roleRepository.save(role);
                        });
            }
        }
    }

    private void seedPermissions(User masterUser) {
        Map<String, List<String>> groupPermissions = Map.of(
                "SYSTEM_SUPPORT", List.of(SystemPermission.MANAGE_ALL_SYSTEM.toString()),
                "DATA_STRUCTURE", List.of(
                        SystemPermission.CREATE_DATA_STRUCTURE.toString(),
                        SystemPermission.DELETE_DATA_STRUCTURE.toString()
                )
        );

        for (Map.Entry<String, List<String>> entry : groupPermissions.entrySet()) {
            String groupName = entry.getKey();
            List<String> permissions = entry.getValue();

            for (String permissionName : permissions) {
                permissionRepository.findByName(permissionName)
                        .orElseGet(() -> {
                            Permission p = new Permission();
                            p.setName(permissionName);
                            p.setGroup(groupName);
                            p.setCreatedBy(masterUser);
                            return permissionRepository.save(p);
                        });
            }
        }
    }

    public void initGradesAndSubjects(User masterUser, DataStructureRepository dataStructureRepository) {

        // Define the complete curriculum data using a deeply nested map.
        // Structure: Map<Grade, Map<Subject, Map<Chapter, List<Lesson>>>>
        Map<String, Map<String, Map<String, List<String>>>> curriculum = new LinkedHashMap<>();

        // Grade 7 (ថ្នាក់ទី៧)
        Map<String, Map<String, List<String>>> grade7Subjects = new LinkedHashMap<>();
        {
            // Subject: Mathematics (គណិតវិទ្យា)
            Map<String, List<String>> chapters = new LinkedHashMap<>();
            chapters.put("ចំនួនគត់", Arrays.asList("មេរៀនទី១៖ សេចក្តីផ្តើមអំពីចំនួនគត់", "មេរៀនទី២៖ ការប្រៀបធៀប និងលំដាប់ចំនួនគត់", "មេរៀនទី៣៖ ប្រមាណវិធីបូក និងដកចំនួនគត់", "មេរៀនទី៤៖ ប្រមាណវិធីគុណ និងចែកចំនួនគត់"));
            chapters.put("ប្រភាគ និងចំនួនទសភាគ", Arrays.asList("មេរៀនទី១៖ ការបំប្លែងរវាងប្រភាគ និងចំនួនទសភាគ", "មេរៀនទី២៖ ប្រមាណវិធីលើប្រភាគ", "មេរៀនទី៣៖ ប្រមាណវិធីលើចំនួនទសភាគ"));
            chapters.put("ស្វ័យគុណ និងឫស", Arrays.asList("មេរៀនទី១៖ ស្វ័យគុណដែលមាននិទស្សន្តជាចំនួនគត់", "មេរៀនទី២៖ ឫសការេ និងឫសគូប"));
            chapters.put("ផលធៀប និងសមាមាត្រ", Arrays.asList("មេរៀនទី១៖ ការយល់ដឹងពីផលធៀប", "មេរៀនទី២៖ ការដោះស្រាយបញ្ហាសមាមាត្រ"));
            chapters.put("កន្សោមពិជគណិត", Arrays.asList("មេរៀនទី១៖ ការបង្កើតកន្សោមពិជគណិត", "មេរៀនទី២៖ ការគណនាតម្លៃនៃកន្សោម"));
            chapters.put("សមីការ និងវិសមីការលីនេអ៊ែរ", Arrays.asList("មេរៀនទី១៖ ការដោះស្រាយសមីការលីនេអ៊ែរមានមួយអញ្ញាត", "មេរៀនទី២៖ ការដោះស្រាយវិសមីការលីនេអ៊ែរ"));
            chapters.put("បន្ទាត់ និងមុំ", Arrays.asList("មេរៀនទី១៖ ប្រភេទនៃមុំ", "មេរៀនទី២៖ បន្ទាត់ស្រប និងបន្ទាត់កែង"));
            chapters.put("ពហុកោណ និងរង្វង់", Arrays.asList("មេរៀនទី១៖ លក្ខណៈនៃត្រីកោណ និងចតុកោណ", "មេរៀនទី២៖ បរិមាត្រ និងផ្ទៃក្រឡា", "មេរៀនទី៣៖ សេចក្តីផ្តើមអំពីរង្វង់"));
            chapters.put("ស្ថិតិ", Arrays.asList("មេរៀនទី១៖ ការប្រមូលទិន្នន័យ", "មេរៀនទី២៖ តារាង និងក្រាហ្វិក", "មេរៀនទី៣៖ មធ្យមភាគ និងមេដ្យាន"));
            grade7Subjects.put("គណិតវិទ្យា", chapters);

            // Subject: Khmer Language (ភាសាខ្មែរ)
            chapters = new LinkedHashMap<>();
            chapters.put("សេចក្តីផ្តើមអំពីអក្សរសិល្ប៍", Arrays.asList("មេរៀនទី១៖ អ្វីជាអក្សរសិល្ប៍?", "មេរៀនទី២៖ ប្រភេទនៃអក្សរសិល្ប៍"));
            chapters.put("វេយ្យាករណ៍៖ ផ្នែកនៃពាក្យ", Arrays.asList("មេរៀនទី១៖ នាម និងគុណនាម", "មេរៀនទី២៖ កិរិយាស័ព្ទ និងគុណកិរិយា", "មេរៀនទី៣៖ សព្វនាម និងធ្នាក់"));
            chapters.put("រឿងខ្លី និងរឿងព្រេង", Arrays.asList("មេរៀនទី១៖ ការវិភាគសាច់រឿង", "មេរៀនទី២៖ ការកំណត់អត្តសញ្ញាណតួអង្គ"));
            chapters.put("កំណាព្យ៖ ចុងចួន និងចង្វាក់", Arrays.asList("មេរៀនទី១៖ ការស្វែងយល់ពីចុងចួន", "មេរៀនទី២៖ ចង្វាក់ក្នុងកំណាព្យ"));
            chapters.put("បច្ចេកទេសក្នុងការអានដើម្បីយល់ន័យ", Arrays.asList("មេរៀនទី១៖ ការស្វែងរកគំនិតចម្បង", "មេរៀនទី២៖ ការសង្ខេបអត្ថបទ"));
            chapters.put("តែងសេចក្តី៖ ការសរសេរកថាខណ្ឌ", Arrays.asList("មេរៀនទី១៖ រចនាសម្ព័ន្ធនៃកថាខណ្ឌ", "មេរៀនទី២៖ ការសរសេរប្រយោគប្រធាន"));
            grade7Subjects.put("ភាសាខ្មែរ", chapters);

            // Subject: Physics (រូបវិទ្យា)
            chapters = new LinkedHashMap<>();
            chapters.put("សេចក្តីផ្តើមអំពីរូបវិទ្យា និងរង្វាស់", Arrays.asList("មេរៀនទី១៖ ការស្គាល់រូបវិទ្យា", "មេរៀនទី២៖ ខ្នាត និងឯកតា SI", "មេរៀនទី៣៖ ឧបករណ៍វាស់វែង"));
            chapters.put("រូបធាតុ និងភាវៈរបស់វា", Arrays.asList("មេរៀនទី១៖ ភាវៈរឹង រាវ និងឧស្ម័ន", "មេរៀនទី២៖ បម្រែបម្រួលភាវៈ"));
            chapters.put("ដង់ស៊ីតេ", Arrays.asList("មេរៀនទី១៖ ការគណនាដង់ស៊ីតេ", "មេរៀនទី២៖ វត្ថុលិច និងវត្ថុអណ្តែត"));
            chapters.put("សេចក្តីផ្តើមអំពីចលនា", Arrays.asList("មេរៀនទី១៖ ចម្ងាយ និងការផ្លាស់ទី", "មេរៀនទី២៖ ល្បឿន"));
            chapters.put("កម្លាំង និងសម្ពាធ", Arrays.asList("មេរៀនទី១៖ និយមន័យនៃកម្លាំង", "មេរៀនទី២៖ ការគណនាសម្ពាធ"));
            grade7Subjects.put("រូបវិទ្យា", chapters);

            // Subject: Chemistry (គីមីវិទ្យា)
            chapters = new LinkedHashMap<>();
            chapters.put("សេចក្តីផ្តើមអំពីគីមីវិទ្យា", Arrays.asList("មេរៀនទី១៖ សារៈសំខាន់នៃគីមីវិទ្យា", "មេរៀនទី២៖ សុវត្ថិភាពក្នុងបន្ទប់ពិសោធន៍"));
            chapters.put("ល្បាយ និងសារធាតុសុទ្ធ", Arrays.asList("មេរៀនទី១៖ ការបែងចែកល្បាយ", "មេរៀនទី២៖ វិធីសាស្ត្រញែកសារធាតុ"));
            chapters.put("សូលុយស្យុង", Arrays.asList("មេរៀនទី១៖ សារធាតុរលាយ និងសារធាតុរំលាយ", "មេរៀនទី២៖ កំហាប់នៃសូលុយស្យុង"));
            chapters.put("អាតូម និងធាតុ", Arrays.asList("មេរៀនទី១៖ ទ្រឹស្តីអាតូម", "មេរៀនទី២៖ និមិត្តសញ្ញាគីមី"));
            chapters.put("បម្រែបម្រួលរូប និងបម្រែបម្រួលគីមី", Arrays.asList("មេរៀនទី១៖ ការកំណត់អត្តសញ្ញាណបម្រែបម្រួលរូប", "មេរៀនទី២៖ សញ្ញានៃបម្រែបម្រួលគីមី"));
            grade7Subjects.put("គីមីវិទ្យា", chapters);

            // Subject: Biology (ជីវវិទ្យា)
            chapters = new LinkedHashMap<>();
            chapters.put("សេចក្តីផ្តើមអំពីជីវវិទ្យា", Arrays.asList("មេរៀនទី១៖ លក្ខណៈនៃភាវៈរស់", "មេរៀនទី២៖ សាខានៃជីវវិទ្យា"));
            chapters.put("កោសិកា៖ រចនាសម្ព័ន្ធ និងមុខងារ", Arrays.asList("មេរៀនទី១៖ កោសិការុក្ខជាតិ និងសត្វ", "មេរៀនទី២៖ អង្គង្គកោសិកាសំខាន់ៗ"));
            chapters.put("ចំណាត់ថ្នាក់នៃភាវៈរស់", Arrays.asList("មេរៀនទី១៖ ប្រព័ន្ធអាណាចក្រទាំងប្រាំ", "មេរៀនទី២៖ ការចាត់ថ្នាក់បែបវិទ្យាសាស្ត្រ"));
            chapters.put("រុក្ខជាតិ៖ រចនាសម្ព័ន្ធ និងរស្មីសំយោគ", Arrays.asList("មេរៀនទី១៖ ផ្នែកต่างๆ នៃរុក្ខជាតិ", "មេរៀនទី២៖ ដំណើរការរស្មីសំយោគ"));
            chapters.put("សត្វ៖ អត់ឆ្អឹងកង និងមានឆ្អឹងកង", Arrays.asList("មេរៀនទី១៖ ក្រុមសត្វអត់ឆ្អឹងកង", "មេរៀនទី២៖ ក្រុមសត្វមានឆ្អឹងកង"));
            grade7Subjects.put("ជីវវិទ្យា", chapters);

            // Subject: Earth Science (ផែនដីវិទ្យា)
            chapters = new LinkedHashMap<>();
            chapters.put("ប្រព័ន្ធរបស់ផែនដី", Arrays.asList("មេរៀនទី១៖ ស្រទាប់ផែនដី", "មេរៀនទី២៖ វដ្តទឹក"));
            chapters.put("រ៉ែ និងថ្ម", Arrays.asList("មេរៀនទី១៖ លក្ខណៈនៃរ៉ែ", "មេរៀនទី២៖ ប្រភេទនៃថ្ម (ថ្មកំបោរ, ថ្មភ្នំភ្លើង, ថ្មប្រែកំណើត)"));
            chapters.put("ផ្លាកតិចតូនិច", Arrays.asList("មេរៀនទី១៖ ចលនានៃផ្លាកផែនដី", "មេរៀនទី២៖ ការរញ្ជួយដី និងភ្នំភ្លើង"));
            chapters.put("អាកាសធាតុ និងធាតុអាកាស", Arrays.asList("មេរៀនទី១៖ ភាពខុសគ្នារវាងអាកាសធាតុ និងធាតុអាកាស", "មេរៀនទី២៖ ពពក និងទឹកភ្លៀង"));
            chapters.put("ប្រព័ន្ធព្រះអាទិត្យ", Arrays.asList("មេរៀនទី១៖ ព្រះអាទិត្យ ផែនដី និងព្រះច័ន្ទ", "មេរៀនទី២៖ ភពក្នុងប្រព័ន្ធព្រះអាទិត្យ"));
            grade7Subjects.put("ផែនដីវិទ្យា", chapters);
        }
        curriculum.put("ថ្នាក់ទី៧", grade7Subjects);

        // Grade 8 (ថ្នាក់ទី៨)
        Map<String, Map<String, List<String>>> grade8Subjects = new LinkedHashMap<>();
        {
            // Subject: Mathematics (គណិតវិទ្យា)
            Map<String, List<String>> chapters = new LinkedHashMap<>();
            chapters.put("និទស្សន្ត និងពហុធា", Arrays.asList("មេរៀនទី១៖ លក្ខណៈនៃនិទស្សន្ត", "មេរៀនទី២៖ ប្រមាណវិធីលើពហុធា"));
            chapters.put("ការแยกពហុធាជាកត្តា", Arrays.asList("មេរៀនទី១៖ ការប្រើប្រាស់កត្តារួមធំបំផុត", "មេរៀនទី២៖ ការแยกកន្សោមដឺក្រេទីពីរ"));
            chapters.put("សមីការលីនេអ៊ែរមានពីរអញ្ញាត", Arrays.asList("មេរៀនទី១៖ ការគូសក្រាបសមីការលីនេអ៊ែរ", "មេរៀនទី២៖ ការស្វែងរកមេគុណប្រាប់ទិស"));
            chapters.put("ប្រព័ន្ធសមីការលីនេអ៊ែរ", Arrays.asList("មេរៀនទី១៖ ការដោះស្រាយដោយវិធីបូកដក", "មេរៀនទី២៖ ការដោះស្រាយដោយវិធីជំនួស"));
            chapters.put("ទ្រឹស្តីបទធរណីមាត្រ និងការស្រាយបញ្ជាក់", Arrays.asList("មេរៀនទី១៖ ទ្រឹស្តីបទពីតាករ", "មេរៀនទី២៖ លក្ខណៈនៃត្រីកោណស្មើគ្នា"));
            chapters.put("រង្វង់", Arrays.asList("មេរៀនទី១៖ អង្កត់ធ្នូ និងធ្នូ", "មេរៀនទី២៖ មុំក្នុងរង្វង់"));
            chapters.put("រូប sólido", Arrays.asList("មេរៀនទី១៖ ផ្ទៃក្រឡា និងមាឌនៃព្រីស", "មេរៀនទី២៖ ផ្ទៃក្រឡា និងមាឌនៃស៊ីឡាំង"));
            chapters.put("ប្រូបាប៊ីលីតេ", Arrays.asList("មេរៀនទី១៖ ការគណនាប្រូបាប៊ីលីតេនៃព្រឹត្តិការណ៍សាមញ្ញ", "មេរៀនទី២៖ លំហសំណាក"));
            grade8Subjects.put("គណិតវិទ្យា", chapters);

            // Subject: Khmer Language (ភាសាខ្មែរ)
            chapters = new LinkedHashMap<>();
            chapters.put("វេយ្យាករណ៍កម្រិតខ្ពស់ និងវាក្យសម្ព័ន្ធ", Arrays.asList("មេរៀនទី១៖ ប្រភេទល្បះ (ល្បះទោល, ល្បះផ្សំ)", "មេរៀនទី២៖ រចនាសម្ព័ន្ធល្បះ"));
            chapters.put("ការវិភាគប្រលោមលោក និងរឿងខ្លី", Arrays.asList("មេរៀនទី១៖ ការកំណត់ប្រធានបទ", "មេរៀនទី២៖ ការអភិវឌ្ឍតួអង្គ"));
            chapters.put("សិល្បៈល្ខោន និងละคร", Arrays.asList("មេរៀនទី១៖ องค์ประกอบនៃរឿងល្ខោន", "មេរៀនទី២៖ ការអាន និងការវិភាគบทละคร"));
            chapters.put("ការសរសេរបែបបញ្ចុះបញ្ចូល និងសុន្ទរកថា", Arrays.asList("មេរៀនទី១៖ បច្ចេកទេសបញ្ចុះបញ្ចូល", "មេរៀនទី២៖ រចនាសម្ព័ន្ធនៃសុន្ទរកថា"));
            chapters.put("សុភាសិត និងពាក្យស្លោកខ្មែរ", Arrays.asList("មេរៀនទី១៖ ការបកស្រាយអត្ថន័យ", "មេរៀនទី២៖ ការប្រើប្រាស់ក្នុងបរិបទ"));
            grade8Subjects.put("ភាសាខ្មែរ", chapters);

            // ... (Full data for other Grade 8 subjects)
        }
        curriculum.put("ថ្នាក់ទី៨", grade8Subjects);

        // Grade 9 (ថ្នាក់ទី៩)
        Map<String, Map<String, List<String>>> grade9Subjects = new LinkedHashMap<>();
        {
            // Subject: Mathematics (គណិតវិទ្យា)
            Map<String, List<String>> chapters = new LinkedHashMap<>();
            chapters.put("សមីការដឺក្រេទីពីរ", Arrays.asList("មេរៀនទី១៖ ការដោះស្រាយដោយការแยกជាកត្តា", "មេរៀនទី២៖ ការដោះស្រាយដោយរូបមន្ត", "មេរៀនទី៣៖ ការប្រើប្រាស់ Discriminant"));
            chapters.put("អនុគមន៍ និងក្រាហ្វ", Arrays.asList("មេរៀនទី១៖ និយមន័យនៃអនុគមន៍", "មេរៀនទី២៖ ការគូសក្រាបនៃអនុគមន៍ដឺក្រេទីពីរ (Parabola)"));
            chapters.put("សេចក្តីផ្តើមអំពីត្រីកោណមាត្រ", Arrays.asList("មេរៀនទី១៖ ស៊ីនុស កូស៊ីនុស និងតង់សង់", "មេរៀនទី២៖ ការដោះស្រាយត្រីកោណកែង"));
            // ... (Lessons for other chapters)
            grade9Subjects.put("គណិតវិទ្យា", chapters);
            // ... (Full data for other Grade 9 subjects)
        }
        curriculum.put("ថ្នាក់ទី៩", grade9Subjects);

        // Grade 10 (ថ្នាក់ទី១០)
        Map<String, Map<String, List<String>>> grade10Subjects = new LinkedHashMap<>();
        {
            // Subject: Mathematics (គណិតវិទ្យា)
            Map<String, List<String>> chapters = new LinkedHashMap<>();
            chapters.put("អនុគមន៍ (លីនេអ៊ែរ, ដឺក្រេទីពីរ, ពហុធា)", Arrays.asList("មេរៀនទី១៖ ការពិនិត្យឡើងវិញលើអនុគមន៍", "មេរៀនទី២៖ លក្ខណៈនៃក្រាបពហុធា"));
            chapters.put("អនុគមន៍អិចស្ប៉ូណង់ស្យែល និងអនុគមន៍លោការីត", Arrays.asList("មេរៀនទី១៖ ក្រាបនៃអនុគមន៍អិចស្ប៉ូណង់ស្យែល", "មេរៀនទី២៖ លក្ខណៈនៃលោការីត", "មេរៀនទី៣៖ ការដោះស្រាយសមីការលោការីត"));
            chapters.put("វ៉ិចទ័រក្នុងប្លង់", Arrays.asList("មេរៀនទី១៖ ប្រមាណវិធីលើវ៉ិចទ័រ", "មេរៀនទី២៖ ផលគុណស្កាលែរ"));
            grade10Subjects.put("គណិតវិទ្យា", chapters);
            // ... (Full data for other Grade 10 subjects)
        }
        curriculum.put("ថ្នាក់ទី១០", grade10Subjects);

        // Grade 11 (ថ្នាក់ទី១១)
        Map<String, Map<String, List<String>>> grade11Subjects = new LinkedHashMap<>();
        {
            // Subject: Mathematics (គណិតវិទ្យា)
            Map<String, List<String>> chapters = new LinkedHashMap<>();
            chapters.put("ម៉ាទ្រីស និងដេទែមីណង់", Arrays.asList("មេរៀនទី១៖ ប្រមាណវិធីលើម៉ាទ្រីស", "មេរៀនទី២៖ ការគណនាដេទែមីណង់", "មេរៀនទី៣៖ ការដោះស្រាយប្រព័ន្ធសមីការដោយប្រើម៉ាទ្រីស"));
            chapters.put("សេចក្តីផ្តើមអំពីលីមីត", Arrays.asList("មេរៀនទី១៖ គំនិតនៃលីមីត", "មេរៀនទី២៖ ការគណនាលីមីតនៃអនុគមន៍"));
            grade11Subjects.put("គណិតវិទ្យា", chapters);
            // ... (Full data for other Grade 11 subjects)
        }
        curriculum.put("ថ្នាក់ទី១១", grade11Subjects);

        // Grade 12 (ថ្នាក់ទី១២)
        Map<String, Map<String, List<String>>> grade12Subjects = new LinkedHashMap<>();
        {
            // Subject: Mathematics (គណិតវិទ្យា)
            Map<String, List<String>> chapters = new LinkedHashMap<>();
            chapters.put("លីមីត និងភាពជាប់", Arrays.asList("មេរៀនទី១៖ លក្ខណៈនៃលីមីត", "មេរៀនទី២៖ ភាពជាប់នៃអនុគមន៍"));
            chapters.put("ដេរីវេ និងការអនុវត្ត", Arrays.asList("មេរៀនទី១៖ និយមន័យនៃដេរីវេ", "មេរៀនទី២៖ ច្បាប់នៃដេរីវេ", "មេរៀនទី៣៖ ការរកតម្លៃអតិបរមា និងអប្បបរមា"));
            chapters.put("អាំងតេក្រាល និងការអនុវត្ត", Arrays.asList("មេរៀនទី១៖ អាំងតេក្រាលមិនកំណត់", "មេរៀនទី២៖ អាំងតេក្រាលកំណត់", "មេរៀនទី៣៖ ការគណនាផ្ទៃក្រឡាដោយប្រើអាំងតេក្រាល"));
            chapters.put("ចំនួនเชิงซ้อน", Arrays.asList("មេរៀនទី១៖ ប្រមាណវិធីលើจำนวนเชิงซ้อน", "មេរៀនទី២៖ ទម្រង់ត្រីកោណមាត្រ"));
            grade12Subjects.put("គណិតវិទ្យា", chapters);

            // Subject: Physics (រូបវិទ្យា)
            chapters = new LinkedHashMap<>();
            chapters.put("วงจรกระแสสลับ (AC)", Arrays.asList("មេរៀនទី១៖ ភាពធន់, កាប៉ាស៊ីតង់, និងអាំងឌុចតង់ក្នុងសៀគ្វី AC", "មេរៀនទី២៖ រ៉េសូណង់ក្នុងសៀគ្វី RLC"));
            chapters.put("คลื่นแม่เหล็กไฟฟ้า", Arrays.asList("មេរៀនទី១៖ លក្ខណៈនៃคลื่นแม่เหล็กไฟฟ้า", "មេរៀនទី២៖  phổคลื่นแม่เหล็กไฟฟ้า"));
            grade12Subjects.put("រូបវិទ្យា", chapters);

            // ... (Full data for other Grade 12 subjects)
        }
        curriculum.put("ថ្នាក់ទី១២", grade12Subjects);


        // Iterate through the structured data and save it to the repository.
        curriculum.forEach((gradeName, subjectsMap) -> {
            DataStructure grade = dataStructureRepository.findByNameAndParentIsNull(gradeName)
                    .orElseGet(() -> {
                        DataStructure g = new DataStructure();
                        g.setName(gradeName);
                        g.setType(DataStructureType.GRADE.name());
                        g.setCreatedBy(masterUser);
                        return dataStructureRepository.save(g);
                    });

            subjectsMap.forEach((subjectName, chaptersMap) -> {
                DataStructure subject = dataStructureRepository.findByNameAndParent(subjectName, grade)
                        .orElseGet(() -> {
                            DataStructure s = new DataStructure();
                            s.setName(subjectName);
                            s.setType(DataStructureType.SUBJECT.name());
                            s.setParent(grade);
                            s.setCreatedBy(masterUser);
                            return dataStructureRepository.save(s);
                        });

                chaptersMap.forEach((chapterName, lessonsList) -> {
                    DataStructure chapter = dataStructureRepository.findByNameAndParent(chapterName, subject)
                            .orElseGet(() -> {
                                DataStructure c = new DataStructure();
                                c.setName(chapterName);
                                c.setType(DataStructureType.CHAPTER.name());
                                c.setParent(subject);
                                c.setCreatedBy(masterUser);
                                return dataStructureRepository.save(c);
                            });

                    lessonsList.forEach(lessonName -> {
                        dataStructureRepository.findByNameAndParent(lessonName, chapter)
                                .orElseGet(() -> {
                                    DataStructure lesson = new DataStructure();
                                    lesson.setName(lessonName);
                                    lesson.setType(DataStructureType.LESSON.name());
                                    lesson.setParent(chapter);
                                    lesson.setCreatedBy(masterUser);
                                    return dataStructureRepository.save(lesson);
                                });
                    });
                });
            });
        });
    }
}
