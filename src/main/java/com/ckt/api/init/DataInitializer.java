package com.ckt.api.init;

import com.ckt.api.contentManagement.model.entity.ComponentText;
import com.ckt.api.contentManagement.model.entity.Content;
import com.ckt.api.contentManagement.model.entity.DataStructure;
import com.ckt.api.contentManagement.repository.ComponentRepository;
import com.ckt.api.contentManagement.repository.ComponentTextRepository;
import com.ckt.api.contentManagement.repository.ContentRepository;
import com.ckt.api.contentManagement.repository.DataStructureRepository;
import com.ckt.api.enums.DataStructureType;
import com.ckt.api.enums.SystemPermission;
import com.ckt.api.enums.SystemRole;
import com.ckt.api.user.model.entity.Permission;
import com.ckt.api.user.model.entity.Role;
import com.ckt.api.user.model.entity.User;
import com.ckt.api.user.repository.PermissionRepository;
import com.ckt.api.user.repository.RoleRepository;
import com.ckt.api.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DataInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final ContentRepository contentRepository;
    private final ComponentRepository componentRepository;
    private final ComponentTextRepository componentTextRepository;

    @Value("${datasource.seed.enabled:false}")
    private boolean seedEnabled;

    public DataInitializer(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PermissionRepository permissionRepository,
                           PasswordEncoder passwordEncoder,
                           ContentRepository contentRepository,
                           ComponentRepository componentRepository,
                           ComponentTextRepository componentTextRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
        this.contentRepository = contentRepository;
        this.componentRepository = componentRepository;
        this.componentTextRepository = componentTextRepository;
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
        DataStructureRepository dsRepo = event.getApplicationContext().getBean(DataStructureRepository.class);
        initGradesAndSubjects(masterUser, dsRepo);

        // 5️⃣ Seed Grade 12 Lesson Contents
        initGrade12LessonContents(masterUser, dsRepo);
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

                    Role adminRole = roleRepository.findFirstByName(SystemRole.MASTER.toString())
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
                        Role role = roleRepository.findFirstByName(roleName)
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
                roleRepository.findFirstByName(roleName)
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
                permissionRepository.findFirstByName(permissionName)
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

            // Subject: Physics (រូបវិទ្យា)
            chapters = new LinkedHashMap<>();
            chapters.put("មេកានិកនិងថាមពល", Arrays.asList("មេរៀនទី១៖ ចលនាលីនេអ៊ែរ", "មេរៀនទី២៖ ជំហររបស់ Newton", "មេរៀនទី៣៖ ថាមពលចីស្ថា и ចីតី"));
            chapters.put("រលកនិងសំឡេង", Arrays.asList("មេរៀនទី១៖ លក្ខណៈរលក", "មេរៀនទី២៖ ល្បឿនសំឡេង", "មេរៀនទី៣៖ Doppler Effect"));
            chapters.put("កំដៅ", Arrays.asList("មេរៀនទី១៖ សីតុណ្ហភាព និងកំដៅ", "មេរៀនទី២៖ ការបញ្ជូនកំដៅ", "មេរៀនទី៣៖ ច្បាប់ Thermodynamics ទីមួយ"));
            chapters.put("អគ្គិសនី", Arrays.asList("មេរៀនទី១៖ ចរន្តអគ្គិសនី", "មេរៀនទី២៖ ច្បាប់ Ohm", "មេរៀនទី៣៖ ថាមពល​ and ​ P=VI"));
            grade8Subjects.put("រូបវិទ្យា", chapters);

            // Subject: Chemistry (គីមីវិទ្យា)
            chapters = new LinkedHashMap<>();
            chapters.put("តារាង Periodic", Arrays.asList("មេរៀនទី១៖ ការរៀបចំតារាង Periodic", "មេរៀនទី២៖ លក្ខណៈ Periodic", "មេរៀនទី៣៖ ឆ្លងទ្រង់ទ្រាយ Group & Period"));
            chapters.put("ចំណងគីមី", Arrays.asList("មេរៀនទី១៖ Ionic Bond", "មេរៀនទី២៖ Covalent Bond", "មេរៀនទី៣៖ Metallic Bond"));
            chapters.put("ប្រតិកម្មគីមី", Arrays.asList("មេរៀនទី១៖ ប្រភេទប្រតិកម្ម", "មេរៀនទី២៖ ការ Balance សមីការ", "មេរៀនទី៣៖ Stoichiometry"));
            chapters.put("ឧស្ម័ន", Arrays.asList("មេរៀនទី១៖ ច្បាប់ Boyle", "មេរៀនទី២៖ ច្បាប់ Charles", "មេរៀនទី៣៖ Ideal Gas Law"));
            grade8Subjects.put("គីមីវិទ្យា", chapters);

            // Subject: Biology (ជីវវិទ្យា)
            chapters = new LinkedHashMap<>();
            chapters.put("ចំណីអាហារ និងការរំលាយ", Arrays.asList("មេរៀនទី១៖ ប្រភេទចំណីអាហារ", "មេរៀនទី២៖ ដំណើរការរំលាយ", "មេរៀនទី៣៖ ការស្រូបយកអាហារ"));
            chapters.put("ប្រព័ន្ធឈាម", Arrays.asList("មេរៀនទី១៖ ផ្នែករបស់ឈាម", "មេរៀនទី២៖ បេះដូង", "មេរៀនទី៣៖ ការចរចរឈាម"));
            chapters.put("ការដកដង្ហើម", Arrays.asList("មេរៀនទី១៖ ដំណើរការដកដង្ហើម", "មេរៀនទី២៖ ការ Exchange Gas", "មេរៀនទី៣៖ Aerobic vs Anaerobic"));
            chapters.put("ការបន្តពូជ", Arrays.asList("មេរៀនទី១៖ ការបន្តពូជ Asexual", "មេរៀនទី២៖ ការបន្តពូជ Sexual", "មេរៀនទី៣៖ Life Cycles"));
            grade8Subjects.put("ជីវវិទ្យា", chapters);

            // Subject: History (ប្រវត្តិសាស្ត្រ)
            chapters = new LinkedHashMap<>();
            chapters.put("ប្រវត្តិនៃអរិយធម៌ពិភពលោក", Arrays.asList("មេរៀនទី១៖ អរិយធម៌អេហ្ស៊ីប", "មេរៀនទី២៖ អរិយធម៌ក្រិក", "មេរៀនទី៣៖ អរិយធម៌រ៉ូម"));
            chapters.put("ប្រវត្តិអាស៊ី", Arrays.asList("មេរៀនទី១៖ ចក្រភពចិន", "មេរៀនទី២៖ ចក្រភពម៉ុងហ្គោល", "មេរៀនទី៣៖ ចក្រភពជប៉ុន"));
            chapters.put("ប្រវត្តិខ្មែរបុរាណ", Arrays.asList("មេរៀនទី១៖ ព្រះរាជាណាចក្រ Funan", "មេរៀនទី២៖ ព្រះរាជាណាចក្រ Chenla", "មេរៀនទី៣៖ អាណាចក្រអង្គរ"));
            grade8Subjects.put("ប្រវត្តិសាស្ត្រ", chapters);

            // Subject: English (ភាសាអង់គ្លេស)
            chapters = new LinkedHashMap<>();
            chapters.put("Grammar Fundamentals", Arrays.asList("មេរៀនទី១៖ Parts of Speech", "មេរៀនទី២៖ Sentence Structure", "មេរៀនទី៣៖ Tenses (Past, Present, Future)"));
            chapters.put("Reading & Vocabulary", Arrays.asList("មេរៀនទី១៖ Reading Strategies", "មេរៀនទី២៖ Context Clues", "មេរៀនទី៣៖ Word Formation"));
            chapters.put("Writing Skills", Arrays.asList("មេរៀនទី១៖ Paragraph Writing", "មេរៀនទី២៖ Descriptive Writing", "មេរៀនទី៣៖ Narrative Writing"));
        }
        curriculum.put("ថ្នាក់ទី៨", grade8Subjects);

        // Grade 9 (ថ្នាក់ទី៩)
        Map<String, Map<String, List<String>>> grade9Subjects = new LinkedHashMap<>();
        {
            // Subject: Mathematics (គណិតវិទ្យា)
            Map<String, List<String>> chapters = new LinkedHashMap<>();
            chapters.put("សមីការដឺក្រេទីពីរ", Arrays.asList("មេរៀនទី១៖ ការដោះស្រាយដោយការแยกជាកត្តា", "មេរៀនទី២៖ ការដោះស្រាយដោយរូបមន្ត", "មេរៀនទី៣៖ ការប្រើប្រាស់ Discriminant"));
            chapters.put("អនុគមន៍ និងក្រាហ្វ", Arrays.asList("មេរៀនទី១៖ និយមន័យនៃអនុគមន៍", "មេរៀនទី២៖ ការគូសក្រាបនៃអនុគមន៍ដឺក្រេទីពីរ (Parabola)", "មេរៀនទី៣៖ Domain & Range"));
            chapters.put("សេចក្តីផ្តើមអំពីត្រីកោណមាត្រ", Arrays.asList("មេរៀនទី១៖ ស៊ីនុស កូស៊ីនុស និងតង់សង់", "មេរៀនទី២៖ ការដោះស្រាយត្រីកោណកែង", "មេរៀនទី៣៖ ការអនុវត្តន៍ក្នុងជីវិត"));
            chapters.put("អថេរ និងប្រូបាប", Arrays.asList("មេរៀនទី១៖ Variables & Expressions", "មេរៀនទី២៖ ការគណនា Probability", "មេរៀនទី៣៖ ស្ថិតិ Descriptive"));
            grade9Subjects.put("គណិតវិទ្យា", chapters);

            // Subject: Khmer Language (ភាសាខ្មែរ)
            chapters = new LinkedHashMap<>();
            chapters.put("ការអានស្រង់", Arrays.asList("មេរៀនទី១៖ ការអានស្រង់កំណាព្យ", "មេរៀនទី២៖ ការអានស្រង់អត្ថបទ", "មេរៀនទី៣៖ ការវិភាគអត្ថន័យ"));
            chapters.put("វចនានុក្រម", Arrays.asList("មេរៀនទី១៖ ការប្រើពាក្យ", "មេរៀនទី២៖ ពាក្យ Synonym & Antonym", "មេរៀនទី៣៖ Idioms ខ្មែរ"));
            chapters.put("ការសរសេរ", Arrays.asList("មេរៀនទី១៖ ការសរសេររបាយការណ៍", "មេរៀនទី២៖ ការសរសេរអត្ថបទ​ Argumentative", "មេរៀនទី៣៖ ការសរសេរសំបុត្រ"));
            grade9Subjects.put("ភាសាខ្មែរ", chapters);

            // Subject: Physics (រូបវិទ្យា)
            chapters = new LinkedHashMap<>();
            chapters.put("ថាមពលចលន", Arrays.asList("មេរៀនទី១៖ ថាមពលចីស្ថា", "មេរៀនទី២៖ ថាមពលចីតី", "មេរៀនទី៣៖ ការអភិរក្សថាមពល"));
            chapters.put("ម៉ាញ៉េទិច", Arrays.asList("មេរៀនទី១៖ Magnetic Field", "មេរៀនទី២៖ ការជះឥទ្ធិពល Electromagnetic", "មេរៀនទី៣៖ ការចាំង Induction"));
            chapters.put("អុបទិចបឋម", Arrays.asList("មេរៀនទី១៖ ការបង្ហើបពន្លឺ", "មេរៀនទី២៖ ការបន្ទុចពន្លឺ", "មេរៀនទី៣៖ ប្រអប់ Lens"));
            grade9Subjects.put("រូបវិទ្យា", chapters);

            // Subject: Chemistry (គីមីវិទ្យា)
            chapters = new LinkedHashMap<>();
            chapters.put("ជ្រុះ Acid & Base", Arrays.asList("មេរៀនទី១៖ Arrhenius Theory", "មេរៀនទី២៖ pH Scale", "មេរៀនទី៣៖ ការ Titration"));
            chapters.put("អុកស៊ីដ-Reduction", Arrays.asList("មេរៀនទី១៖ Oxidation States", "មេរៀនទី២៖ Redox Reactions", "មេរៀនទី៣៖ ការ Balance Redox"));
            chapters.put("គីមីសរីរាង្គបឋម", Arrays.asList("មេរៀនទី១៖ Hydrocarbons", "មេរៀនទី២៖ Alcohol & Acids", "មេរៀនទី៣៖ Polymers"));
            grade9Subjects.put("គីមីវិទ្យា", chapters);

            // Subject: Biology (ជីវវិទ្យា)
            chapters = new LinkedHashMap<>();
            chapters.put("ប្រព័ន្ធប្រសាទ", Arrays.asList("មេរៀនទី១៖ Neuron Structure", "មេរៀនទី២៖ CNS & PNS", "មេរៀនទី៣៖ Reflex Arc"));
            chapters.put("ហ័រម៉ូន", Arrays.asList("មេរៀនទី១៖ Endocrine System", "មេរៀនទី២៖ ហ័រម៉ូនចម្បង", "មេរៀនទី៣៖ Feedback Mechanism"));
            chapters.put("ប្រព័ន្ធភាពស៊ាំ", Arrays.asList("មេរៀនទី១៖ Innate Immunity", "មេរៀនទី២៖ Adaptive Immunity", "មេរៀនទី៣៖ Vaccines"));
            grade9Subjects.put("ជីវវិទ្យា", chapters);

            // Subject: History (ប្រវត្តិសាស្ត្រ)
            chapters = new LinkedHashMap<>();
            chapters.put("ការដួលរលំអាណានិគម", Arrays.asList("មេរៀនទី១៖ Colonialism in Asia", "មេរៀនទី២៖ Independence Movements", "មេរៀនទី៣៖ Post-Colonial States"));
            chapters.put("សង្គ្រាមលោក", Arrays.asList("មេរៀនទី១៖ Causes of WWI", "មេរៀនទី២៖ Causes of WWII", "មេរៀនទី៣៖ Aftermath & UN"));
            grade9Subjects.put("ប្រវត្តិសាស្ត្រ", chapters);

            // Subject: English (ភាសាអង់គ្លេស)
            chapters = new LinkedHashMap<>();
            chapters.put("Grammar Intermediate", Arrays.asList("មេរៀនទី១៖ Perfect Tenses", "មេរៀនទី២៖ Modal Verbs", "មេរៀនទី៣៖ Passive Voice Basics"));
            chapters.put("Reading Comprehension", Arrays.asList("មេរៀនទី១៖ Main Idea & Details", "មេរៀនទី២៖ Cause & Effect", "មេរៀនទី៣៖ Compare & Contrast"));
            chapters.put("Writing", Arrays.asList("មេរៀនទី១៖ Opinion Essay", "មេរៀនទី២៖ Compare & Contrast Essay", "មេរៀនទី៣៖ Letter Writing"));
            grade9Subjects.put("ភាសាអង់គ្លេស", chapters);
        }
        curriculum.put("ថ្នាក់ទី៩", grade9Subjects);

        // Grade 10 (ថ្នាក់ទី១០)
        Map<String, Map<String, List<String>>> grade10Subjects = new LinkedHashMap<>();
        {
            // Subject: Mathematics (គណិតវិទ្យា)
            Map<String, List<String>> chapters = new LinkedHashMap<>();
            chapters.put("អនុគមន៍ (លីនេអ៊ែរ, ដឺក្រេទីពីរ, ពហុធា)", Arrays.asList("មេរៀនទី១៖ ការពិនិត្យឡើងវិញលើអនុគមន៍", "មេរៀនទី២៖ លក្ខណៈនៃក្រាបពហុធា", "មេរៀនទី៣៖ Transformation of Graphs"));
            chapters.put("អនុគមន៍អិចស្ប៉ូណង់ស្យែល និងអនុគមន៍លោការីត", Arrays.asList("មេរៀនទី១៖ ក្រាបនៃអនុគមន៍អិចស្ប៉ូណង់ស្យែល", "មេរៀនទី២៖ លក្ខណៈនៃលោការីត", "មេរៀនទី៣៖ ការដោះស្រាយសមីការលោការីត"));
            chapters.put("វ៉ិចទ័រក្នុងប្លង់", Arrays.asList("មេរៀនទី១៖ ប្រមាណវិធីលើវ៉ិចទ័រ", "មេរៀនទី២៖ ផលគុណស្កាលែរ", "មេរៀនទី៣៖ Vector Applications"));
            chapters.put("ត្រីកោណមាត្រ", Arrays.asList("មេរៀនទី១៖ Trigonometric Identities", "មេរៀនទី២៖ Sin/Cos/Tan Graphs", "មេរៀនទី៣៖ ការដោះស្រាយ Trig Equations"));
            chapters.put("ស្ថិតិ", Arrays.asList("មេរៀនទី១៖ ការប្រមូលទិន្នន័យ", "មេរៀនទី២៖ Frequency Distribution", "មេរៀនទី៣៖ Normal Distribution"));
            grade10Subjects.put("គណិតវិទ្យា", chapters);

            // Subject: Khmer Language (ភាសាខ្មែរ)
            chapters = new LinkedHashMap<>();
            chapters.put("អក្សរសិល្ប៍ Prose", Arrays.asList("មេរៀនទី១៖ Prose Analysis", "មេរៀនទី២៖ Character & Theme", "មេរៀនទី៣៖ Narrative Techniques"));
            chapters.put("វេយ្យាករណ៍ Advanced", Arrays.asList("មេរៀនទី១៖ Complex Sentences", "មេរៀនទី២៖ Subordinate Clauses", "មេរៀនទី៣៖ Punctuation"));
            chapters.put("ការសរសេរ Research", Arrays.asList("មេរៀនទី១៖ Research Methods", "មេរៀនទី២៖ Citation & References", "មេរៀនទី៣៖ Academic Writing"));
            grade10Subjects.put("ភាសាខ្មែរ", chapters);

            // Subject: Physics (រូបវិទ្យា)
            chapters = new LinkedHashMap<>();
            chapters.put("ចលនា 2D", Arrays.asList("មេរៀនទី១៖ Projectile Motion", "មេរៀនទី២៖ Circular Motion", "មេរៀនទី៣៖ Relative Motion"));
            chapters.put("Electricity", Arrays.asList("មេរៀនទី១៖ Coulomb's Law", "មេរៀនទី២៖ Electric Field & Potential", "មេរៀនទី៣៖ Capacitors"));
            chapters.put("Electromagnetism", Arrays.asList("មេរៀនទី១៖ Magnetic Force", "មេរៀនទី២៖ Electromagnetic Induction", "មេរៀនទី៣៖ Faraday's Law"));
            grade10Subjects.put("រូបវិទ្យា", chapters);

            // Subject: Chemistry (គីមីវិទ្យា)
            chapters = new LinkedHashMap<>();
            chapters.put("Thermochemistry", Arrays.asList("មេរៀនទី១៖ Enthalpy", "មេរៀនទី២៖ Hess's Law", "មេរៀនទី៣៖ Entropy"));
            chapters.put("Kinetics", Arrays.asList("មេរៀនទី១៖ Rate of Reaction", "មេរៀនទី២៖ Activation Energy", "មេរៀនទី៣៖ Catalysis"));
            chapters.put("Equilibrium", Arrays.asList("មេរៀនទី១៖ Le Chatelier", "មេរៀនទី២៖ Kc & Kp", "មេរៀនទី៣៖ Industrial Applications"));
            grade10Subjects.put("គីមីវិទ្យា", chapters);

            // Subject: Biology (ជីវវិទ្យា)
            chapters = new LinkedHashMap<>();
            chapters.put("Cell Biology", Arrays.asList("មេរៀនទី១៖ Cell Cycle", "មេរៀនទី២៖ Mitosis", "មេរៀនទី៣៖ Cancer Cells"));
            chapters.put("Genetics Intro", Arrays.asList("មេរៀនទី១៖ DNA Structure", "មេរៀនទី២៖ Replication", "មេរៀនទី៣៖ Mutations"));
            chapters.put("Ecology", Arrays.asList("មេរៀនទី១៖ Ecosystems", "មេរៀនទី២៖ Food Chains", "មេរៀនទី៣៖ Environmental Issues"));
            grade10Subjects.put("ជីវវិទ្យា", chapters);

            // Subject: History (ប្រវត្តិសាស្ត្រ)
            chapters = new LinkedHashMap<>();
            chapters.put("ប្រវត្តិខ្មែរ", Arrays.asList("មេរៀនទី១៖ ការស្ថាបនាអង្គរ", "មេរៀនទី២៖ ប្រព័ន្ធទឹក", "មេរៀនទី៣៖ ការធ្លាក់ចុះអង្គរ"));
            chapters.put("អាណានិគម", Arrays.asList("មេរៀនទី១៖ ការចូលមកថ្មី​ France", "មេរៀនទី២៖ Resistance Movements", "មេរៀនទី៣៖ Independence 1953"));
            grade10Subjects.put("ប្រវត្តិសាស្ត្រ", chapters);

            // Subject: Geography (ភូមិវិទ្យា)
            chapters = new LinkedHashMap<>();
            chapters.put("ភូមិវិទ្យាកម្ពុជា", Arrays.asList("មេរៀនទី១៖ Landforms", "មេរៀនទី២៖ Rivers & Lakes", "មេរៀនទី៣៖ Natural Resources"));
            chapters.put("ភូមិវិទ្យាអាស៊ី", Arrays.asList("មេរៀនទី១៖ Southeast Asia Overview", "មេរៀនទី២៖ ASEAN Countries", "មេរៀនទី៣៖ Climate Zones"));
            grade10Subjects.put("ភូមិវិទ្យា", chapters);

            // Subject: English (ភាសាអង់គ្លេស)
            chapters = new LinkedHashMap<>();
            chapters.put("Advanced Grammar", Arrays.asList("មេរៀនទី១៖ Conditionals (1,2,3)", "មេរៀនទី២៖ Relative Clauses", "មេរៀនទី៣៖ Reported Speech"));
            chapters.put("Academic Reading", Arrays.asList("មេរៀនទី១៖ Skimming & Scanning", "មេរៀនទី២៖ Critical Reading", "មេរៀនទី៣៖ Summarizing"));
            chapters.put("Essay Writing", Arrays.asList("មេរៀនទី១៖ Essay Structure", "មេរៀនទី២៖ Argumentative Essay", "មេរៀនទី៣៖ Cause & Effect Essay"));
            grade10Subjects.put("ភាសាអង់គ្លេស", chapters);
        }
        curriculum.put("ថ្នាក់ទី១០", grade10Subjects);

        // Grade 11 (ថ្នាក់ទី១១)
        Map<String, Map<String, List<String>>> grade11Subjects = new LinkedHashMap<>();
        {
            // Subject: Mathematics (គណិតវិទ្យា)
            Map<String, List<String>> chapters = new LinkedHashMap<>();
            chapters.put("ម៉ាទ្រីស និងដេទែមីណង់", Arrays.asList("មេរៀនទី១៖ ប្រមាណវិធីលើម៉ាទ្រីស", "មេរៀនទី២៖ ការគណនាដេទែមីណង់", "មេរៀនទី៣៖ ការដោះស្រាយប្រព័ន្ធសមីការដោយប្រើម៉ាទ្រីស"));
            chapters.put("សេចក្តីផ្តើមអំពីលីមីត", Arrays.asList("មេរៀនទី១៖ គំនិតនៃលីមីត", "មេរៀនទី២៖ ការគណនាលីមីតនៃអនុគមន៍", "មេរៀនទី៣៖ Continuity"));
            chapters.put("ដេរីវេបឋម", Arrays.asList("មេរៀនទី១៖ Definition of Derivative", "មេរៀនទី២៖ Differentiation Rules", "មេរៀនទី៣៖ Applications of Derivatives"));
            chapters.put("លំដាប់ & ស៊េរី Intro", Arrays.asList("មេរៀនទី១៖ Arithmetic Sequences", "មេរៀនទី២៖ Geometric Sequences", "មេរៀនទី៣៖ Partial Sums"));
            grade11Subjects.put("គណិតវិទ្យា", chapters);

            // Subject: Khmer Language (ភាសាខ្មែរ)
            chapters = new LinkedHashMap<>();
            chapters.put("អក្សរសិល្ប៍ Contemporary", Arrays.asList("មេរៀនទី១៖ Modern Khmer Literature", "មេរៀនទី២៖ Literary Criticism", "មេរៀនទី៣៖ Author's Intent"));
            chapters.put("ការសរសេរ Advanced", Arrays.asList("មេរៀនទី១៖ Analytical Writing", "មេរៀនទី២៖ Academic Essay", "មេរៀនទី៣៖ Thesis Statement"));
            grade11Subjects.put("ភាសាខ្មែរ", chapters);

            // Subject: Physics (រូបវិទ្យា)
            chapters = new LinkedHashMap<>();
            chapters.put("DC Circuits", Arrays.asList("មេរៀនទី១៖ Kirchhoff's Laws", "មេរៀនទី២៖ Series & Parallel Circuits", "មេរៀនទី៣៖ Power in Circuits"));
            chapters.put("Electrostatics", Arrays.asList("មេរៀនទី១៖ Electric Charge", "មេរៀនទី២៖ Electric Field Lines", "មេរៀនទី៣៖ Gauss's Law"));
            chapters.put("Waves & Sound", Arrays.asList("មេរៀនទី១៖ Wave Properties", "មេរៀនទី២៖ Standing Waves", "មេរៀនទី៣៖ Resonance"));
            grade11Subjects.put("រូបវិទ្យា", chapters);

            // Subject: Chemistry (គីមីវិទ្យា)
            chapters = new LinkedHashMap<>();
            chapters.put("Electrochemistry Intro", Arrays.asList("មេរៀនទី១៖ Galvanic Cell", "មេរៀនទី២៖ Standard Potentials", "មេរៀនទី៣៖ Electrolysis"));
            chapters.put("Organic Chemistry", Arrays.asList("មេរៀនទី១៖ Functional Groups", "មេរៀនទី២៖ IUPAC Naming", "មេរៀនទី៣៖ Reaction Types"));
            chapters.put("Nuclear Chemistry Intro", Arrays.asList("មេរៀនទី១៖ Radioactivity", "មេរៀនទី២៖ Half-life", "មេរៀនទី៣៖ Nuclear Reactions"));
            grade11Subjects.put("គីមីវិទ្យា", chapters);

            // Subject: Biology (ជីវវិទ្យា)
            chapters = new LinkedHashMap<>();
            chapters.put("Genetics Advanced", Arrays.asList("មេរៀនទី១៖ Mendelian Genetics", "មេរៀនទី២៖ Non-Mendelian", "មេរៀនទី៣៖ Genetic Disorders"));
            chapters.put("Evolution", Arrays.asList("មេរៀនទី១៖ Natural Selection", "មេរៀនទី២៖ Speciation", "មេរៀនទី៣៖ Evidence for Evolution"));
            chapters.put("Biotechnology", Arrays.asList("មេរៀនទី១៖ PCR", "មេរៀនទី២៖ Gene Cloning", "មេរៀនទី៣៖ GMO & Applications"));
            grade11Subjects.put("ជីវវិទ្យា", chapters);

            // Subject: History (ប្រវត្តិសាស្ត្រ)
            chapters = new LinkedHashMap<>();
            chapters.put("ស.ស ទី២០", Arrays.asList("មេរៀនទី១៖ The Cold War", "មេរៀនទី២៖ Decolonization", "មេរៀនទី៣៖ Globalization"));
            chapters.put("ប្រវត្តិខ្មែរ ១៩៥៣-២០០០", Arrays.asList("មេរៀនទី១៖ Sihanouk Era", "មេរៀនទី២៖ Khmer Republic", "មេរៀនទី៣៖ Post-1979 Reconstruction"));
            grade11Subjects.put("ប្រវត្តិសាស្ត្រ", chapters);

            // Subject: Geography (ភូមិវិទ្យា)
            chapters = new LinkedHashMap<>();
            chapters.put("Physical Geography", Arrays.asList("មេរៀនទី១៖ Plate Tectonics", "មេរៀនទី២៖ Climate Systems", "មេរៀនទី៣៖ Ocean Currents"));
            chapters.put("Human Geography", Arrays.asList("មេរៀនទី១៖ Population Distribution", "មេរៀនទី២៖ Urbanization", "មេរៀនទី៣៖ Migration"));
            grade11Subjects.put("ភូមិវិទ្យា", chapters);

            // Subject: English (ភាសាអង់គ្លេស)
            chapters = new LinkedHashMap<>();
            chapters.put("Advanced Grammar", Arrays.asList("មេរៀនទី១៖ Mixed Conditionals", "មេរៀនទី២៖ Inversion", "មេរៀនទី៣៖ Nominalization"));
            chapters.put("Literature", Arrays.asList("មេរៀនទី១៖ Short Stories", "មេរៀនទី២៖ Poetry Analysis", "មេរៀនទី៣៖ Drama"));
            chapters.put("Academic Writing", Arrays.asList("មេរៀនទី១៖ Research Paper", "មេរៀនទី២៖ Abstract & Introduction", "មេរៀនទី៣៖ Conclusion & References"));
            grade11Subjects.put("ភាសាអង់គ្លេស", chapters);
        }
        curriculum.put("ថ្នាក់ទី១១", grade11Subjects);

        // Grade 12 (ថ្នាក់ទី១២)
        Map<String, Map<String, List<String>>> grade12Subjects = new LinkedHashMap<>();
        {
            // Subject: Mathematics (គណិតវិទ្យា)
            Map<String, List<String>> chapters = new LinkedHashMap<>();
            chapters.put("លីមីត និងភាពជាប់", Arrays.asList(
                    "មេរៀនទី១៖ លក្ខណៈនៃលីមីត",
                    "មេរៀនទី២៖ ច្បាប់នៃលីមីត",
                    "មេរៀនទី៣៖ លីមីតទៅកន់អណ្តើក",
                    "មេរៀនទី៤៖ ភាពជាប់នៃអនុគមន៍"));
            chapters.put("ដេរីវេ និងការអនុវត្ត", Arrays.asList(
                    "មេរៀនទី១៖ និយមន័យនៃដេរីវេ",
                    "មេរៀនទី២៖ ច្បាប់នៃដេរីវេ (ផលបូក ផលគុណ ផលចែក)",
                    "មេរៀនទី៣៖ ដេរីវេនៃអនុគមន៍ស្មុគស្មាញ",
                    "មេរៀនទី៤៖ ដេរីវេអនុគមន៍ត្រីកោណមាត្រ",
                    "មេរៀនទី៥៖ ការរកតម្លៃអតិបរមា និងអប្បបរមា",
                    "មេរៀនទី៦៖ ការសិក្សាការប្រែប្រួលរបស់អនុគមន៍"));
            chapters.put("អាំងតេក្រាល និងការអនុវត្ត", Arrays.asList(
                    "មេរៀនទី១៖ អាំងតេក្រាលមិនកំណត់",
                    "មេរៀនទី២៖ វិធីសាស្ត្រអាំងតេក្រាលដោយការជំនួស",
                    "មេរៀនទី៣៖ វិធីសាស្ត្រអាំងតេក្រាលដោយការបំបែកផ្នែក",
                    "មេរៀនទី៤៖ អាំងតេក្រាលកំណត់",
                    "មេរៀនទី៥៖ ការគណនាផ្ទៃក្រឡាដោយប្រើអាំងតេក្រាល",
                    "មេរៀនទី៦៖ ការគណនាមាឌដោយប្រើអាំងតេក្រាល"));
            chapters.put("ចំនួនកុំផ្លិច", Arrays.asList(
                    "មេរៀនទី១៖ សេចក្តីផ្តើមអំពីចំនួនកុំផ្លិច",
                    "មេរៀនទី២៖ ប្រមាណវិធីបូក ដក គុណ ចែក",
                    "មេរៀនទី៣៖ ទម្រង់ធរណីមាត្រ និងម៉ូឌុល",
                    "មេរៀនទី៤៖ ទម្រង់ត្រីកោណមាត្រ និងទ្រឹស្តីបទ De Moivre"));
            chapters.put("លំដាប់ និងស៊េរី", Arrays.asList(
                    "មេរៀនទី១៖ លំដាប់នព្វន្ធ",
                    "មេរៀនទី២៖ លំដាប់ធរណីមាត្រ",
                    "មេរៀនទី៣៖ ស៊េរីនព្វន្ធ",
                    "មេរៀនទី៤៖ ស៊េរីធរណីមាត្រ និងស៊េរីឥតទីបញ្ចប់"));
            chapters.put("ប្រូបាប៊ីលីតេ និងស្ថិតិ", Arrays.asList(
                    "មេរៀនទី១៖ ការរៀបចំ និងការបន្សំ",
                    "មេរៀនទី២៖ ការគណនាប្រូបាប៊ីលីតេ",
                    "មេរៀនទី៣៖ ការបែងចែកប្រូបាប៊ីលីតេ",
                    "មេរៀនទី៤៖ ស្ថិតិពណ៌នា មធ្យម ភាគមធ្យម និងគម្លាតស្តង់ដារ"));
            grade12Subjects.put("គណិតវិទ្យា", chapters);

            // Subject: Physics (រូបវិទ្យា)
            chapters = new LinkedHashMap<>();
            chapters.put("សៀគ្វីចរន្តស្លាប់ (AC)", Arrays.asList(
                    "មេរៀនទី១៖ ភាពធន់ក្នុងសៀគ្វី AC",
                    "មេរៀនទី២៖ កាប៉ាស៊ីតង់ក្នុងសៀគ្វី AC",
                    "មេរៀនទី៣៖ អាំងឌុចតង់ក្នុងសៀគ្វី AC",
                    "មេរៀនទី៤៖ សៀគ្វី RLC និងរ៉េសូណង់",
                    "មេរៀនទី៥៖ ថាមពលក្នុងសៀគ្វី AC"));
            chapters.put("រលកអេឡិចត្រូម៉ាញ៉េទិច", Arrays.asList(
                    "មេរៀនទី១៖ លក្ខណៈ និងការបង្កើតរលកអេឡិចត្រូម៉ាញ៉េទិច",
                    "មេរៀនទី២៖ វិសាលភាពនៃរលកអេឡិចត្រូម៉ាញ៉េទិច",
                    "មេរៀនទី៣៖ ការប្រើប្រាស់រលកអេឡិចត្រូម៉ាញ៉េទិចក្នុងជីវិត"));
            chapters.put("អុបទិច", Arrays.asList(
                    "មេរៀនទី១៖ ការបង្ហើបពន្លឺ និងការបន្ទុចពន្លឺ",
                    "មេរៀនទី២៖ ឡែនស៍ស្តើង និងការប្រើប្រាស់",
                    "មេរៀនទី៣៖ ការប្រឡោះ និងការ diffraction",
                    "មេរៀនទី៤៖ ថ្នាំភ្នែក និងប្រដាប់ប្រើប្រាស់អុបទិច"));
            chapters.put("រូបវិទ្យាបរមាណូ", Arrays.asList(
                    "មេរៀនទី១៖ ប្រូតុង ន្យូត្រុង និងអេឡិចត្រុង",
                    "មេរៀនទី២៖ អ៊ីយ៉ូណីសសាស្យុង និងពន្លឺ",
                    "មេរៀនទី៣៖ ទ្រឹស្តីរបស់ Bohr",
                    "មេរៀនទី៤៖ ទ្រឹស្តីលេខយ៉ន្ត (Quantum)"));
            chapters.put("រូបវិទ្យានុយក្លេអ៊ែរ", Arrays.asList(
                    "មេរៀនទី១៖ រចនាសម្ព័ន្ធនុយក្លេអ៊ែរ",
                    "មេរៀនទី២៖ ការបំបែករ៉ាឌីយូអាក់ទីវ",
                    "មេរៀនទី៣៖ ប្រតិកម្មនុយក្លេអ៊ែរ",
                    "មេរៀនទី៤៖ ការប្រើប្រាស់ថាមពលនុយក្លេអ៊ែរ"));
            grade12Subjects.put("រូបវិទ្យា", chapters);

            // Subject: Chemistry (គីមីវិទ្យា)
            chapters = new LinkedHashMap<>();
            chapters.put("ស្ថិតភាពគីមី", Arrays.asList(
                    "មេរៀនទី១៖ ស្ថិតភាពឌីណាមិក",
                    "មេរៀនទី២៖ ថេរស្ថិតភាព Kc និង Kp",
                    "មេរៀនទី៣៖ គោលការណ៍ Le Chatelier",
                    "មេរៀនទី៤៖ ការអនុវត្តស្ថិតភាពក្នុងឧស្សាហកម្ម"));
            chapters.put("ជ្រុះអ៊ីដ្រូហ្សែន អ៊ីយ៉ូន និង pH", Arrays.asList(
                    "មេរៀនទី១៖ ទ្រឹស្តី Brønsted-Lowry",
                    "មេរៀនទី២៖ ការគណនា pH",
                    "មេរៀនទី៣៖ ជ្រុះ Buffer",
                    "មេរៀនទី៤៖ ការ Titration"));
            chapters.put("អេឡិចត្រូគីមី", Arrays.asList(
                    "មេរៀនទី១៖ ប្រតិកម្ម Redox",
                    "មេរៀនទី២៖ ស៊ែល Galvanic",
                    "មេរៀនទី៣៖ ស្ថានប័ន (Electrolysis)",
                    "មេរៀនទី៤៖ ការអនុវត្ត Corrosion និងការការពារ"));
            chapters.put("គីមីសរីរាង្គ", Arrays.asList(
                    "មេរៀនទី១៖ ហាយដ្រូកាបូនអាំង",
                    "មេរៀនទី២៖ ហាយដ្រូកាបូនអ្លីន និងអ្លាឈីន",
                    "មេរៀនទី៣៖ ក្រុមមុខងារ (Functional Groups)",
                    "មេរៀនទី៤៖ ប្រតិកម្មជំនួស ការបន្ថែម និងការ Elimination",
                    "មេរៀនទី៥៖ ស្ករ ប្រូតេអ៊ីន និងខ្លាញ់"));
            chapters.put("ប៉ូលីម៉ែរ និងសម្ភារៈ", Arrays.asList(
                    "មេរៀនទី១៖ ប្រភេទប៉ូលីម៉ែរ",
                    "មេរៀនទី២៖ ប្លាស្ទិច និងអំពែ",
                    "មេរៀនទី៣៖ ប៉ូលីម៉ែរធម្មជាតិ (ស្ករ DNA)",
                    "មេរៀនទី៤៖ ឥទ្ធិពលប្លាស្ទិចទៅលើបរិស្ថាន"));
            grade12Subjects.put("គីមីវិទ្យា", chapters);

            // Subject: Biology (ជីវវិទ្យា)
            chapters = new LinkedHashMap<>();
            chapters.put("ហ្សែន និងក្រូម៉ូសូម", Arrays.asList(
                    "មេរៀនទី១៖ DNA រចនាសម្ព័ន្ធ និងការចម្លង",
                    "មេរៀនទី២៖ ហ្សែន ទីតាំង និងអ្លែល",
                    "មេរៀនទី៣៖ ការបែងចែកកោសិកា Mitosis និង Meiosis",
                    "មេរៀនទី៤៖ ក្រូម៉ូសូមមនុស្ស និងភេទ"));
            chapters.put("ព័ន្ធតំណពូជ", Arrays.asList(
                    "មេរៀនទី១៖ ទ្រឹស្តីបទ Mendel ទី១ និង ទី២",
                    "មេរៀនទី២៖ ការតំណពូជតាមភេទ",
                    "មេរៀនទី៣៖ ការ Linkage និង Crossing Over",
                    "មេរៀនទី៤៖ ជំងឺតំណពូជ"));
            chapters.put("ជីវវិទ្យាម៉ូឡេគុល", Arrays.asList(
                    "មេរៀនទី១៖ RNA និងការបំលែង Transcription",
                    "មេរៀនទី២៖ ការបកប្រែ Translation",
                    "មេរៀនទី៣៖ ការបញ្ចេញ Gene (Gene Expression)",
                    "មេរៀនទី៤៖ បច្ចេកវិទ្យា DNA រីគំដារ"));
            chapters.put("វិវត្តន៍", Arrays.asList(
                    "មេរៀនទី១៖ ទ្រឹស្តី Darwin និង Wallace",
                    "មេរៀនទី២៖ ភ័ស្ដុតាងនៃវិវត្តន៍",
                    "មេរៀនទី៣៖ ការជ្រើសរើសសំណព្វ",
                    "មេរៀនទី៤៖ ប្រភពដើមនៃអាយុជីវិត"));
            chapters.put("អេកូឡូស៊ី", Arrays.asList(
                    "មេរៀនទី១៖ ប្រព័ន្ធអេកូ និងច្រវ៉ាក់អាហារ",
                    "មេរៀនទី២៖ ដំណើរការចរចរសារធាតុ",
                    "មេរៀនទី៣៖ ប្រជាជនសត្វ និងការគ្រប់គ្រង",
                    "មេរៀនទី៤៖ ការអភិរក្សសត្វព្រៃ និងបរិស្ថាន"));
            grade12Subjects.put("ជីវវិទ្យា", chapters);

            // Subject: Khmer Language (ភាសាខ្មែរ)
            chapters = new LinkedHashMap<>();
            chapters.put("អក្សរសិល្ប៍ខ្មែរ", Arrays.asList(
                    "មេរៀនទី១៖ អក្សរសិល្ប៍បុរាណ ( រាមកេរ្តិ៍ ចបក)",
                    "មេរៀនទី២៖ អក្សរសិល្ប៍សម័យទំនើប",
                    "មេរៀនទី៣៖ ការវិភាគ និងបកស្រាយអក្សរសិល្ប៍",
                    "មេរៀនទី៤៖ ស្ថានភាពអក្សរសិល្ប៍ខ្មែរបច្ចុប្បន្ន"));
            chapters.put("វេយ្យាករណ៍ខ្មែរកម្រិតខ្ពស់", Arrays.asList(
                    "មេរៀនទី១៖ ប្រភេទល្បះស្មុគស្មាញ",
                    "មេរៀនទី២៖ ឃ្លា និងរចនាសម្ព័ន្ធ",
                    "មេរៀនទី៣៖ ឧបករណ៍ភ្ជាប់ល្បះ",
                    "មេរៀនទី៤៖ ការប្រើប្រាស់ភាសាផ្លូវការ"));
            chapters.put("កំណាព្យ និងច្បាប់ភ្លេង", Arrays.asList(
                    "មេរៀនទី១៖ ប្រភេទកំណាព្យខ្មែរ",
                    "មេរៀនទី២៖ ច្បាប់គ្រប់គ្រង",
                    "មេរៀនទី៣៖ ការសរសេរ និងការវិភាគកំណាព្យ",
                    "មេរៀនទី៤៖ ការប្រៀបធៀបកំណាព្យបុរាណ-ទំនើប"));
            chapters.put("ការតែងសេចក្តីកម្រិតខ្ពស់", Arrays.asList(
                    "មេរៀនទី១៖ ការតែងប្រភេទបកស្រាយ",
                    "មេរៀនទី២៖ ការតែងប្រភេទបញ្ចុះបញ្ចូល",
                    "មេរៀនទី៣៖ ការសរសេររបាយការណ៍",
                    "មេរៀនទី៤៖ ការសរសេរសំបុត្រផ្លូវការ"));
            grade12Subjects.put("ភាសាខ្មែរ", chapters);

            // Subject: History (ប្រវត្តិសាស្ត្រ)
            chapters = new LinkedHashMap<>();
            chapters.put("ប្រវត្តិសាស្ត្រខ្មែរ", Arrays.asList(
                    "មេរៀនទី១៖ សម័យបុព្វប្រវត្តិខ្មែរ",
                    "មេរៀនទី២៖ អាណាចក្រអង្គរ",
                    "មេរៀនទី៣៖ សម័យក្រោយអង្គរ (ឧទ្ទុងបុរី)",
                    "មេរៀនទី៤៖ ខ្មែរក្រោមការត្រួតត្រារបស់បារាំង",
                    "មេរៀនទី៥៖ ឯករាជ្យភាព ១៩៥៣ និងសម័យសីហនុ"));
            chapters.put("ប្រវត្តិសាស្ត្រទំនើបកម្ពុជា", Arrays.asList(
                    "មេរៀនទី១៖ សម័យសាធារណរដ្ឋខ្មែរ",
                    "មេរៀនទី២៖ របបខ្មែរក្រហម",
                    "មេរៀនទី៣៖ ការស្តារប្រទេស ១៩៧៩-១៩៩១",
                    "មេរៀនទី៤៖ ការបោះឆ្នោតក្រោម UNTAC ១៩៩៣",
                    "មេរៀនទី៥៖ កម្ពុជាក្នុងសតវត្សទី២១"));
            chapters.put("ប្រវត្តិសាស្ត្រអាស៊ី-ប៉ាស៊ីហ្វិច", Arrays.asList(
                    "មេរៀនទី១៖ ការរីកចម្រើនរបស់ចិន ជប៉ុន អ៊ីនដ្រូណេស៊ី",
                    "មេរៀនទី២៖ សហគមន៍ ASEAN",
                    "មេរៀនទី៣៖ ប្រវត្តិសង្គ្រាមកូរ៉េ និងវៀតណាម"));
            chapters.put("ប្រវត្តិសាស្ត្រពិភពលោក", Arrays.asList(
                    "មេរៀនទី១៖ ការដួលរលំរបប Colonialism",
                    "មេរៀនទី២៖ សង្គ្រាមលោកលើកទី១ និងទី២",
                    "មេរៀនទី៣៖ សង្គ្រាមត្រជាក់",
                    "មេរៀនទី៤៖ ពិភពលោកក្រោយ Cold War"));
            grade12Subjects.put("ប្រវត្តិសាស្ត្រ", chapters);

            // Subject: Geography (ភូមិវិទ្យា)
            chapters = new LinkedHashMap<>();
            chapters.put("ភូមិវិទ្យាកម្ពុជា", Arrays.asList(
                    "មេរៀនទី១៖ ភូមិសាស្ត្រ ទីតាំង និងព្រំដែន",
                    "មេរៀនទី២៖ ទន្លេ ភ្នំ និងដែនលិច",
                    "មេរៀនទី៣៖ ធនធានធម្មជាតិ (ព្រៃ ទឹក រ៉ែ)",
                    "មេរៀនទី៤៖ ប្រជាជន ការចែករំលែក និងទីក្រុង",
                    "មេរៀនទី៥៖ វិស័យកសិកម្ម ឧស្សាហកម្ម ទេសចរណ៍"));
            chapters.put("ភូមិវិទ្យាអាស៊ីអាគ្នេយ៍", Arrays.asList(
                    "មេរៀនទី១៖ លក្ខណៈភូមិសាស្ត្ររបស់ ASEAN",
                    "មេរៀនទី២៖ ធនធានធម្មជាតិ និងការប្រើប្រាស់",
                    "មេរៀនទី៣៖ ការអភិវឌ្ឍសេដ្ឋកិច្ចអាស៊ីអាគ្នេយ៍"));
            chapters.put("ភូមិវិទ្យាពិភពលោក", Arrays.asList(
                    "មេរៀនទី១៖ ទ្វីប មហាសមុទ្រ",
                    "មេរៀនទី២៖ ការប្រែប្រួលអាកាសធាតុ",
                    "មេរៀនទី៣៖ ប្រជាជនពិភពលោក និងការអភិវឌ្ឍ",
                    "មេរៀនទី៤៖ បញ្ហាបរិស្ថានពិភពលោក"));
            grade12Subjects.put("ភូមិវិទ្យា", chapters);

            // Subject: Economics (សេដ្ឋកិច្ច)
            chapters = new LinkedHashMap<>();
            chapters.put("មូលដ្ឋានសេដ្ឋកិច្ច", Arrays.asList(
                    "មេរៀនទី១៖ ប្រព័ន្ធសេដ្ឋកិច្ចស្វ័យប្រវត្តិ",
                    "មេរៀនទី២៖ ការផ្គត់ផ្គង់ និងតម្រូវការ",
                    "មេរៀនទី៣៖ ការប្រកួតប្រជែង និងទីផ្សារ",
                    "មេរៀនទី៤៖ ប្រព័ន្ធប្រាក់ and ការធ្វើអន្តរាគមន៍រដ្ឋ"));
            chapters.put("សេដ្ឋកិច្ចម៉ាក្រូ", Arrays.asList(
                    "មេរៀនទី១៖ GDP និងការគណនា",
                    "មេរៀនទី២៖ អតិផរណា និងការគ្រប់គ្រង",
                    "មេរៀនទី៣៖ ការបើកការងារ និងអត្រាការងារ",
                    "មេរៀនទី៤៖ គោលនយោបាយ Fiscal និង Monetary"));
            chapters.put("ពាណិជ្ជកម្មអន្តរជាតិ", Arrays.asList(
                    "មេរៀនទី១៖ ការនាំចូល-នាំចេញ",
                    "មេរៀនទី២៖ ជំងឺខ្សែក្រវ៉ាត់ (Balance of Trade)",
                    "មេរៀនទី៣៖ ការវិនិយោគ FDI",
                    "មេរៀនទី៤៖ ស្ថាប័នហិរញ្ញវត្ថុ IMF ADB WB"));
            chapters.put("សេដ្ឋកិច្ចកម្ពុជា", Arrays.asList(
                    "មេរៀនទី១៖ រចនាសម្ព័ន្ធសេដ្ឋកិច្ចកម្ពុជា",
                    "មេរៀនទី២៖ វិស័យដែកនាំ (កសិកម្ម វាយនភណ្ឌ ទេសចរណ៍ សំណង់)",
                    "មេរៀនទី៣៖ គោលនយោបាយអភិវឌ្ឍន៍ ២០៣០",
                    "មេរៀនទី៤៖ ប្រទេសកម្ពុជានៅក្នុង ASEAN"));
            grade12Subjects.put("សេដ្ឋកិច្ច", chapters);

            // Subject: English (ភាសាអង់គ្លេស)
            chapters = new LinkedHashMap<>();
            chapters.put("Grammar & Structures", Arrays.asList(
                    "មេរៀនទី១៖ Tenses Review (Simple, Progressive, Perfect)",
                    "មេរៀនទី២៖ Conditional Sentences (Type 1, 2, 3)",
                    "មេរៀនទី៣៖ Passive Voice",
                    "មេរៀនទី៤៖ Reported Speech",
                    "មេរៀនទី៥៖ Relative Clauses & Conjunctions"));
            chapters.put("Reading Comprehension", Arrays.asList(
                    "មេរៀនទី១៖ Main Idea & Supporting Details",
                    "មេរៀនទី២៖ Inference & Author's Purpose",
                    "មេរៀនទី៣៖ Vocabulary in Context",
                    "មេរៀនទី៤៖ Academic Reading Strategies"));
            chapters.put("Writing Skills", Arrays.asList(
                    "មេរៀនទី១៖ Essay Structure (Introduction, Body, Conclusion)",
                    "មេរៀនទី២៖ Argumentative Essay",
                    "មេរៀនទី៣៖ Formal Letter & Email Writing",
                    "មេរៀនទី៤៖ Summary & Paraphrasing"));
            chapters.put("Speaking & Listening", Arrays.asList(
                    "មេរៀនទី១៖ Presentation Skills",
                    "មេរៀនទី២៖ Debate & Discussion",
                    "មេរៀនទី៣៖ Listening for Specific Information",
                    "មេរៀនទី៤៖ Academic Vocabulary Building"));
            grade12Subjects.put("ភាសាអង់គ្លេស", chapters);
        }
        curriculum.put("ថ្នាក់ទី១២", grade12Subjects);


        // Iterate through the structured data and save it to the repository.
        curriculum.forEach((gradeName, subjectsMap) -> {

            DataStructure grade = dataStructureRepository.findFirstByNameAndParentIsNull(gradeName)
                    .orElseGet(() -> {
                        DataStructure g = new DataStructure();
                        g.setName(gradeName);
                        g.setType(DataStructureType.GRADE.name());
                        g.setCreatedBy(masterUser);
                        return dataStructureRepository.save(g);
                    });

            subjectsMap.forEach((subjectName, chaptersMap) -> {
                DataStructure subject = dataStructureRepository.findFirstByNameAndParent(subjectName, grade)
                        .orElseGet(() -> {
                            DataStructure s = new DataStructure();
                            s.setName(subjectName);
                            s.setType(DataStructureType.SUBJECT.name());
                            s.setParent(grade);
                            s.setCreatedBy(masterUser);
                            return dataStructureRepository.save(s);
                        });

                chaptersMap.forEach((chapterName, lessonsList) -> {
                    DataStructure chapter = dataStructureRepository.findFirstByNameAndParent(chapterName, subject)
                            .orElseGet(() -> {
                                DataStructure c = new DataStructure();
                                c.setName(chapterName);
                                c.setType(DataStructureType.CHAPTER.name());
                                c.setParent(subject);
                                c.setCreatedBy(masterUser);
                                return dataStructureRepository.save(c);
                            });

                    lessonsList.forEach(lessonName -> {
                        dataStructureRepository.findFirstByNameAndParent(lessonName, chapter)
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

    private void initGrade12LessonContents(@SuppressWarnings("unused") User masterUser, DataStructureRepository dsRepo) {
        // Map lesson name -> body text
        Map<String, String> contentMap = buildLessonContentMap();

        List<DataStructure> lessons = dsRepo.findByType(DataStructureType.LESSON.name());
        for (DataStructure lesson : lessons) {
            if (contentRepository.findFirstByDataStructure(lesson).isPresent()) continue;

            String body = contentMap.getOrDefault(lesson.getName(),
                    "ខ្លឹមសារនៃ" + lesson.getName() + " នឹងត្រូវបានបញ្ចូលក្នុងពេលឆាប់ៗនេះ។");

            Content content = new Content();
            content.setTitle(lesson.getName());
            content.setDescription(body.length() > 200 ? body.substring(0, 200) : body);
            content.setDataStructure(lesson);
            contentRepository.save(content);

            // component must stay transient here so cascade PERSIST in ComponentText works
            com.ckt.api.contentManagement.model.entity.Component component =
                    new com.ckt.api.contentManagement.model.entity.Component();
            component.setPosition(1L);
            component.setContent(content);

            ComponentText ct = new ComponentText();
            ct.setDataType("TEXT");
            ct.setData(body);
            ct.setComponent(component);   // cascade = ALL → persists component too
            componentTextRepository.save(ct);
        }
    }

    private Map<String, String> buildLessonContentMap() {
        Map<String, String> m = new LinkedHashMap<>();

        // ===== GRADE 12 — MATHEMATICS =====
        m.put("មេរៀនទី១៖ លក្ខណៈនៃលីមីត",
            "លីមីត (Limit) គឺជាតម្លៃដែលអនុគមន៍ f(x) ចូលជិតនៅពេល x ចូលជិតតម្លៃណាមួយ។\n\n" +
            "**និយមន័យ:** lim(x→a) f(x) = L មានន័យថា f(x) ចូលជិត L នៅពេល x ចូលជិត a\n\n" +
            "**លក្ខណៈ:**\n" +
            "- lim[f(x) + g(x)] = lim f(x) + lim g(x)\n" +
            "- lim[f(x) · g(x)] = lim f(x) · lim g(x)\n" +
            "- lim[f(x)/g(x)] = lim f(x) / lim g(x) (លុះត្រាtែ lim g(x) ≠ 0)\n\n" +
            "**ឧទាហរណ៍:** lim(x→2) (x²-4)/(x-2) = lim(x→2)(x+2) = 4");

        m.put("មេរៀនទី២៖ ច្បាប់នៃលីមីត",
            "**ច្បាប់នៃលីមីត**\n\n" +
            "1. lim(x→a) c = c (លីមីតនៃថេរ)\n" +
            "2. lim(x→a) x = a\n" +
            "3. lim(x→a) [c·f(x)] = c · lim(x→a) f(x)\n" +
            "4. lim(x→a) [f(x)]ⁿ = [lim(x→a) f(x)]ⁿ\n" +
            "5. lim(x→a) √f(x) = √[lim(x→a) f(x)]\n\n" +
            "**វិធីដោះស្រាយ Indeterminate Forms (0/0):**\n" +
            "- ដោះស្រាយដោយการแยកជាកត្តា\n" +
            "- គុណដោយ Conjugate\n" +
            "- ប្រើ L'Hôpital's Rule: lim f(x)/g(x) = lim f'(x)/g'(x)");

        m.put("មេរៀនទី៣៖ លីមីតទៅកន់អណ្តើក",
            "**លីមីតទៅកន់អណ្តើក (Limits at Infinity)**\n\n" +
            "នៅពេល x→+∞ ឬ x→-∞:\n\n" +
            "- lim(x→∞) 1/x = 0\n" +
            "- lim(x→∞) 1/xⁿ = 0 (n > 0)\n" +
            "- lim(x→∞) (axⁿ+...)/(bxⁿ+...) = a/b\n\n" +
            "**ឧទាហរណ៍:** lim(x→∞) (3x²+2x)/(x²-1) = 3\n\n" +
            "**Horizontal Asymptote:** y = L នៅពេល lim(x→±∞) f(x) = L\n" +
            "**Vertical Asymptote:** x = a នៅពេល lim(x→a) |f(x)| = ∞");

        m.put("មេរៀនទី៤៖ ភាពជាប់នៃអនុគមន៍",
            "**ភាពជាប់ (Continuity)**\n\n" +
            "អនុគមន៍ f ជាប់ (continuous) នៅ x=a បើ:\n" +
            "1. f(a) ត្រូវបានកំណត់\n" +
            "2. lim(x→a) f(x) មាន\n" +
            "3. lim(x→a) f(x) = f(a)\n\n" +
            "**ប្រភេទភាពមិនជាប់:**\n" +
            "- Removable Discontinuity: lim មាន ប៉ុន្តែ ≠ f(a)\n" +
            "- Jump Discontinuity: lim ខាងឆ្វេង ≠ lim ខាងស្តាំ\n" +
            "- Infinite Discontinuity: lim = ∞\n\n" +
            "**Intermediate Value Theorem:** បើ f ជាប់នៅ [a,b] ហើយ f(a)≠f(b) នោះ f យក​តម្លៃ​ទាំងអស់​រវាង f(a) និង f(b)");

        // Derivatives
        m.put("មេរៀនទី១៖ និយមន័យនៃដេរីវេ",
            "**ដេរីវេ (Derivative)**\n\n" +
            "ដេរីវេ f'(x) = lim(h→0) [f(x+h) - f(x)] / h\n\n" +
            "**អត្ថន័យ:** ដេរីវេគឺជាអត្រានៃការប្រែប្រួល (rate of change) ឬជ្រុងបន្ទាត់ប៉ះ (slope of tangent)\n\n" +
            "**ឧទាហរណ៍:**\n" +
            "f(x) = x² → f'(x) = lim(h→0)(2x+h) = 2x\n\n" +
            "**សញ្ញាសម្គាល់:** f'(x), dy/dx, Df(x)");

        m.put("មេរៀនទី២៖ ច្បាប់នៃដេរីវេ (ផលបូក ផលគុណ ផលចែក)",
            "**ច្បាប់ដេរីវេ:**\n\n" +
            "- (c)' = 0\n" +
            "- (xⁿ)' = nxⁿ⁻¹\n" +
            "- (cf)' = cf'\n" +
            "- (f+g)' = f' + g'\n\n" +
            "**ច្បាប់ផលគុណ (Product Rule):**\n" +
            "(fg)' = f'g + fg'\n\n" +
            "**ច្បាប់ផលចែក (Quotient Rule):**\n" +
            "(f/g)' = (f'g - fg') / g²\n\n" +
            "**ឧទាហរណ៍:**\n" +
            "y = x²·sin(x) → y' = 2x·sin(x) + x²·cos(x)");

        m.put("មេរៀនទី៣៖ ដេរីវេនៃអនុគមន៍ស្មុគស្មាញ",
            "**Chain Rule (ច្បាប់ខ្សែច្រវ៉ាក់):**\n\n" +
            "បើ y = f(g(x)) នោះ dy/dx = f'(g(x)) · g'(x)\n\n" +
            "**ឧទាហរណ៍:**\n" +
            "y = (x²+1)⁵ → y' = 5(x²+1)⁴ · 2x = 10x(x²+1)⁴\n\n" +
            "y = sin(3x²) → y' = cos(3x²) · 6x = 6x·cos(3x²)\n\n" +
            "**Implicit Differentiation:**\n" +
            "x² + y² = 25 → 2x + 2y·(dy/dx) = 0 → dy/dx = -x/y");

        m.put("មេរៀនទី៤៖ ដេរីវេអនុគមន៍ត្រីកោណមាត្រ",
            "**ដេរីវេអនុគមន៍ត្រីកោណមាត្រ:**\n\n" +
            "- (sin x)' = cos x\n" +
            "- (cos x)' = -sin x\n" +
            "- (tan x)' = 1/cos²x = sec²x\n" +
            "- (cot x)' = -1/sin²x = -csc²x\n" +
            "- (sec x)' = sec x · tan x\n" +
            "- (csc x)' = -csc x · cot x\n\n" +
            "**ឧទាហរណ៍:**\n" +
            "y = sin(x²) → y' = cos(x²) · 2x\n" +
            "y = x·cos(x) → y' = cos(x) - x·sin(x)");

        m.put("មេរៀនទី៥៖ ការរកតម្លៃអតិបរមា និងអប្បបរមា",
            "**Extrema (តម្លៃអតិបរមា/អប្បបរមា):**\n\n" +
            "**Critical Points:** x ដែល f'(x) = 0 ឬ f'(x) undefined\n\n" +
            "**First Derivative Test:**\n" +
            "- f' ផ្លាស់ + → - នៅ x=c: Local Maximum\n" +
            "- f' ផ្លាស់ - → + នៅ x=c: Local Minimum\n\n" +
            "**Second Derivative Test:**\n" +
            "- f''(c) < 0: Local Maximum\n" +
            "- f''(c) > 0: Local Minimum\n\n" +
            "**Absolute Extrema នៅ [a,b]:**\n" +
            "ប្រៀបធៀប f(a), f(b), និង f នៅ critical points");

        m.put("មេរៀនទី៦៖ ការសិក្សាការប្រែប្រួលរបស់អនុគមន៍",
            "**ការសិក្សាការប្រែប្រួល:**\n\n" +
            "**Increasing/Decreasing:**\n" +
            "- f'(x) > 0: f កើន\n" +
            "- f'(x) < 0: f ថយ\n\n" +
            "**Concavity:**\n" +
            "- f''(x) > 0: Concave Up (ប្រហោងឡើង)\n" +
            "- f''(x) < 0: Concave Down (ប្រហោងចុះ)\n\n" +
            "**Inflection Point:** ទីកន្លែងដែល concavity ផ្លាស់ (f''=0)\n\n" +
            "**ជំហានដោះស្រាយ:**\n" +
            "1. រក domain\n2. រក f', f''\n3. critical points\n4. sign charts\n5. គូស sketch");

        // Integrals
        m.put("មេរៀនទី១៖ អាំងតេក្រាលមិនកំណត់",
            "**អាំងតេក្រាលមិនកំណត់ (Indefinite Integral):**\n\n" +
            "∫f(x)dx = F(x) + C បើ F'(x) = f(x)\n\n" +
            "**រូបមន្តជាមូលដ្ឋាន:**\n" +
            "- ∫xⁿdx = xⁿ⁺¹/(n+1) + C (n ≠ -1)\n" +
            "- ∫1/x dx = ln|x| + C\n" +
            "- ∫eˣdx = eˣ + C\n" +
            "- ∫sin(x)dx = -cos(x) + C\n" +
            "- ∫cos(x)dx = sin(x) + C\n\n" +
            "**ឧទាហរណ៍:**\n" +
            "∫(3x²+2x-1)dx = x³+x²-x+C");

        m.put("មេរៀនទី២៖ វិធីសាស្ត្រអាំងតេក្រាលដោយការជំនួស",
            "**Integration by Substitution (u-substitution):**\n\n" +
            "ដំណើរការ: ដាក់ u = g(x), du = g'(x)dx\n\n" +
            "**ឧទាហរណ៍:**\n" +
            "∫2x(x²+1)⁵dx\n" +
            "→ u = x²+1, du = 2x dx\n" +
            "→ ∫u⁵du = u⁶/6 + C = (x²+1)⁶/6 + C\n\n" +
            "∫sin(3x)dx\n" +
            "→ u = 3x, du = 3dx → dx = du/3\n" +
            "→ (1/3)∫sin(u)du = -cos(3x)/3 + C");

        m.put("មេរៀនទី៣៖ វិធីសាស្ត្រអាំងតេក្រាលដោយការបំបែកផ្នែក",
            "**Integration by Parts:**\n\n" +
            "∫u dv = uv - ∫v du\n\n" +
            "**ការជ្រើសរើស u (LIATE):**\n" +
            "L: Logarithm, I: Inverse trig, A: Algebraic, T: Trig, E: Exponential\n\n" +
            "**ឧទាហរណ៍:**\n" +
            "∫x·eˣdx\n" +
            "→ u = x, dv = eˣdx\n" +
            "→ du = dx, v = eˣ\n" +
            "→ x·eˣ - ∫eˣdx = xeˣ - eˣ + C = eˣ(x-1) + C");

        m.put("មេរៀនទី៤៖ អាំងតេក្រាលកំណត់",
            "**Definite Integral:**\n\n" +
            "∫[a to b] f(x)dx = F(b) - F(a)\n\n" +
            "**Fundamental Theorem of Calculus:**\n" +
            "d/dx[∫[a to x] f(t)dt] = f(x)\n\n" +
            "**លក្ខណៈ:**\n" +
            "- ∫[a to b] f dx = -∫[b to a] f dx\n" +
            "- ∫[a to a] f dx = 0\n" +
            "- ∫[a to b] f dx = ∫[a to c] f dx + ∫[c to b] f dx\n\n" +
            "**ឧទាហរណ៍:**\n" +
            "∫[0 to 2] x²dx = [x³/3]₀² = 8/3 - 0 = 8/3");

        m.put("មេរៀនទី៥៖ ការគណនាផ្ទៃក្រឡាដោយប្រើអាំងតេក្រាល",
            "**ផ្ទៃក្រឡាតាមអាំងតេក្រាល:**\n\n" +
            "**ផ្ទៃក្រឡារវាង f(x) និងអ័ក្ស x:**\n" +
            "A = ∫[a to b] |f(x)| dx\n\n" +
            "**ផ្ទៃក្រឡារវាង f(x) និង g(x):**\n" +
            "A = ∫[a to b] |f(x) - g(x)| dx\n\n" +
            "**ឧទាហរណ៍:**\n" +
            "ផ្ទៃក្រឡារវាង y=x² និង y=x:\n" +
            "ចំណុចប្រសព្វ: x²=x → x=0,1\n" +
            "A = ∫[0 to 1](x-x²)dx = [x²/2 - x³/3]₀¹ = 1/6");

        m.put("មេរៀនទី៦៖ ការគណនាមាឌដោយប្រើអាំងតេក្រាល",
            "**មាឌដោយ Disk/Washer Method:**\n\n" +
            "**Disk Method** (បង្វិលជុំអ័ក្ស x):\n" +
            "V = π ∫[a to b] [f(x)]² dx\n\n" +
            "**Washer Method:**\n" +
            "V = π ∫[a to b] ([f(x)]² - [g(x)]²) dx\n\n" +
            "**Shell Method** (ស៊ីឡាំង):\n" +
            "V = 2π ∫[a to b] x·f(x) dx\n\n" +
            "**ឧទាហរណ៍:**\n" +
            "y=√x, x=0,x=4 បង្វិលជុំអ័ក្ស x:\n" +
            "V = π∫[0 to 4]x dx = π[x²/2]₀⁴ = 8π");

        // Complex Numbers
        m.put("មេរៀនទី១៖ សេចក្តីផ្តើមអំពីចំនួនកុំផ្លិច",
            "**ចំនួនកុំផ្លិច (Complex Numbers):**\n\n" +
            "i = √(-1), i² = -1\n\n" +
            "ទម្រង់: z = a + bi (a: ផ្នែកពិត, b: ផ្នែកស្រម័យ)\n\n" +
            "**ចំនួន Conjugate:** z̄ = a - bi\n\n" +
            "**ម៉ូឌុល:** |z| = √(a²+b²)\n\n" +
            "**Argand Diagram:** គំនូសចំនួនកុំផ្លិចលើ plane ពីរ​ខ្នង");

        m.put("មេរៀនទី២៖ ប្រមាណវិធីបូក ដក គុណ ចែក",
            "**ប្រមាណវិធីលើចំនួនកុំផ្លិច:**\n\n" +
            "**បូក/ដក:** (a+bi)±(c+di) = (a±c) + (b±d)i\n\n" +
            "**គុណ:** (a+bi)(c+di) = (ac-bd) + (ad+bc)i\n\n" +
            "**ចែក:** (a+bi)/(c+di) = [(a+bi)(c-di)] / (c²+d²)\n\n" +
            "**ឧទាហរណ៍:**\n" +
            "(2+3i)(1-i) = 2-2i+3i-3i² = 2+i+3 = 5+i\n\n" +
            "(1+2i)/(1-i) = (1+2i)(1+i)/2 = (-1+3i)/2");

        m.put("មេរៀនទី៣៖ ទម្រង់ធរណីមាត្រ និងម៉ូឌុល",
            "**ទម្រង់ Polar:**\n\n" +
            "z = r(cosθ + i·sinθ) = r·e^(iθ)\n\n" +
            "r = |z| = √(a²+b²)\n" +
            "θ = arg(z) = arctan(b/a)\n\n" +
            "**ការបំប្លែង:**\n" +
            "a = r·cosθ, b = r·sinθ\n\n" +
            "**ការគុណក្នុងទម្រង់ Polar:**\n" +
            "z₁·z₂ = r₁r₂ · [cos(θ₁+θ₂) + i·sin(θ₁+θ₂)]");

        m.put("មេរៀនទី៤៖ ទម្រង់ត្រីកោណមាត្រ និងទ្រឹស្តីបទ De Moivre",
            "**De Moivre's Theorem:**\n\n" +
            "[r(cosθ + i·sinθ)]ⁿ = rⁿ(cos(nθ) + i·sin(nθ))\n\n" +
            "**ឧទាហរណ៍:**\n" +
            "(1+i)⁸: r=√2, θ=π/4\n" +
            "→ (√2)⁸ · (cos(2π) + i·sin(2π)) = 16\n\n" +
            "**nth Roots:** ដំណោះស្រាយ n ចំនួននៃ zⁿ = w\n" +
            "z_k = r^(1/n) · [cos((θ+2kπ)/n) + i·sin((θ+2kπ)/n)]\n" +
            "k = 0, 1, ..., n-1");

        // Sequences and Series
        m.put("មេរៀនទី១៖ លំដាប់នព្វន្ធ",
            "**លំដាប់នព្វន្ធ (Arithmetic Sequence):**\n\n" +
            "aₙ = a₁ + (n-1)d\n\n" +
            "d = aₙ - aₙ₋₁ (ភាពខុសគ្នាទូទៅ)\n\n" +
            "**ឧទាហរណ៍:** 2, 5, 8, 11, ... (d=3)\n" +
            "a₁₀ = 2 + 9·3 = 29\n\n" +
            "**Arithmetic Mean:** (a+b)/2 = m → a,m,b ជា AP");

        m.put("មេរៀនទី២៖ លំដាប់ធរណីមាត្រ",
            "**លំដាប់ធរណីមាត្រ (Geometric Sequence):**\n\n" +
            "aₙ = a₁ · rⁿ⁻¹\n\n" +
            "r = aₙ/aₙ₋₁ (អនុបាតទូទៅ)\n\n" +
            "**ឧទាហរណ៍:** 3, 6, 12, 24, ... (r=2)\n" +
            "a₈ = 3 · 2⁷ = 384\n\n" +
            "**Geometric Mean:** √(ab) = m → a,m,b ជា GP");

        m.put("មេរៀនទី៣៖ ស៊េរីនព្វន្ធ",
            "**ស៊េរីនព្វន្ធ (Arithmetic Series):**\n\n" +
            "Sₙ = n/2 · (a₁ + aₙ) = n/2 · [2a₁ + (n-1)d]\n\n" +
            "**ឧទាហរណ៍:**\n" +
            "ផលបូក 1+2+3+...+100:\n" +
            "S₁₀₀ = 100/2 · (1+100) = 5050\n\n" +
            "ផលបូក 3+7+11+...+47:\n" +
            "n = (47-3)/4 + 1 = 12\n" +
            "S₁₂ = 12/2 · (3+47) = 300");

        m.put("មេរៀនទី៤៖ ស៊េរីធរណីមាត្រ និងស៊េរីឥតទីបញ្ចប់",
            "**ស៊េរីធរណីមាត្រ (Geometric Series):**\n\n" +
            "Sₙ = a₁(1-rⁿ)/(1-r), r ≠ 1\n\n" +
            "**ស៊េរីឥតទីបញ្ចប់ (|r| < 1):**\n" +
            "S∞ = a₁/(1-r)\n\n" +
            "**ឧទាហរណ៍:**\n" +
            "S∞ ស៊េរី 1 + 1/2 + 1/4 + ...:\n" +
            "a₁=1, r=1/2 → S∞ = 1/(1-1/2) = 2\n\n" +
            "**ការប្រើប្រាស់:** ទសភាគនិច្ចកាល 0.333... = 1/3");

        // Probability & Statistics
        m.put("មេរៀនទី១៖ ការរៀបចំ និងការបន្សំ",
            "**Permutation (P) – ពាក់ព័ន្ធ​នឹង​លំដាប់:**\n\n" +
            "P(n,r) = n!/(n-r)!\n\n" +
            "**Combination (C) – មិន​ពាក់ព័ន្ធ​លំដាប់:**\n\n" +
            "C(n,r) = n!/[r!(n-r)!] = C(n,n-r)\n\n" +
            "**ឧទាហរណ៍:**\n" +
            "P(5,3) = 5!/2! = 60\n" +
            "C(5,3) = 5!/(3!·2!) = 10\n\n" +
            "**Binomial Theorem:**\n" +
            "(a+b)ⁿ = Σ C(n,k)·aⁿ⁻ᵏ·bᵏ");

        m.put("មេរៀនទី២៖ ការគណនាប្រូបាប៊ីលីតេ",
            "**ប្រូបាប៊ីលីតេ (Probability):**\n\n" +
            "P(A) = n(A)/n(S), 0 ≤ P(A) ≤ 1\n\n" +
            "**ច្បាប់:**\n" +
            "- P(A') = 1 - P(A)\n" +
            "- P(A∪B) = P(A) + P(B) - P(A∩B)\n" +
            "- P(A∩B) = P(A)·P(B) (A,B ឯករាជ្យ)\n\n" +
            "**Conditional Probability:**\n" +
            "P(A|B) = P(A∩B)/P(B)\n\n" +
            "**Bayes' Theorem:**\n" +
            "P(A|B) = P(B|A)·P(A)/P(B)");

        m.put("មេរៀនទី៣៖ ការបែងចែកប្រូបាប៊ីលីតេ",
            "**Binomial Distribution:**\n\n" +
            "P(X=k) = C(n,k)·pᵏ·(1-p)ⁿ⁻ᵏ\n" +
            "E(X) = np, Var(X) = np(1-p)\n\n" +
            "**Normal Distribution:**\n" +
            "Bell curve, symmetric, μ±σ contains 68%\n" +
            "Z-score: Z = (X-μ)/σ\n\n" +
            "**ឧទាហរណ៍:**\n" +
            "ប្រូបាប៊ីលីតេ 3 ក្បាលក្នុង 5 ត្រា (p=0.5):\n" +
            "P(X=3) = C(5,3)·(0.5)³·(0.5)² = 10/32 = 5/16");

        m.put("មេរៀនទី៤៖ ស្ថិតិពណ៌នា មធ្យម ភាគមធ្យម និងគម្លាតស្តង់ដារ",
            "**វិធានស្ថិតិ:**\n\n" +
            "**Mean (មធ្យម):** x̄ = Σxᵢ/n\n\n" +
            "**Median (ភាគមធ្យម):** តម្លៃ​កណ្តាល​ក្រោយ​តម្រៀប\n\n" +
            "**Mode (ប្រេកង់):** តម្លៃ​ដែល​លេចឡើង​ច្រើន​ជាងគេ\n\n" +
            "**Standard Deviation:**\n" +
            "σ = √[Σ(xᵢ-x̄)²/n]\n\n" +
            "**Variance:** σ² = Σ(xᵢ-x̄)²/n\n\n" +
            "**IQR:** Q3 - Q1 (Interquartile Range)");

        // ===== GRADE 12 — PHYSICS =====
        m.put("មេរៀនទី១៖ ភាពធន់ក្នុងសៀគ្វី AC",
            "**ភាពធន់ (Resistance) ក្នុង AC:**\n\n" +
            "V = IR (Ohm's Law)\n" +
            "ភាពធន់ R មិន​ផ្លាស់ប្ដូរ​ទៅ​តាម​ frequency\n\n" +
            "**ថាមពល:** P = I²R = V²/R\n\n" +
            "**RMS Values:**\n" +
            "V_rms = V_max/√2\n" +
            "I_rms = I_max/√2\n\n" +
            "**Phase:** V និង I ស្ថិតក្នុង phase តែមួយ");

        m.put("មេរៀនទី២៖ កាប៉ាស៊ីតង់ក្នុងសៀគ្វី AC",
            "**Capacitor ក្នុង AC:**\n\n" +
            "Capacitive Reactance: Xc = 1/(2πfC)\n\n" +
            "**Phase:** I នាំ​មុខ V ដោយ 90°\n\n" +
            "**ថាមពល:** P = 0 (capacitor មិន​ប្រើ​ប្រាស់​ energy)\n\n" +
            "**ឧទាហរណ៍:**\n" +
            "C = 100μF, f = 50Hz\n" +
            "Xc = 1/(2π·50·100×10⁻⁶) ≈ 31.8Ω");

        m.put("មេរៀនទី៣៖ អាំងឌុចតង់ក្នុងសៀគ្វី AC",
            "**Inductor ក្នុង AC:**\n\n" +
            "Inductive Reactance: XL = 2πfL\n\n" +
            "**Phase:** V នាំ​មុខ I ដោយ 90°\n\n" +
            "**ថាមពល:** P = 0 (inductor ផ្ទុក​ energy ក្នុង magnetic field)\n\n" +
            "**ឧទាហរណ៍:**\n" +
            "L = 0.1H, f = 50Hz\n" +
            "XL = 2π·50·0.1 ≈ 31.4Ω");

        m.put("មេរៀនទី៤៖ សៀគ្វី RLC និងរ៉េសូណង់",
            "**សៀគ្វី RLC Series:**\n\n" +
            "Impedance: Z = √(R² + (XL-Xc)²)\n" +
            "Phase angle: tanφ = (XL-Xc)/R\n\n" +
            "**Resonance (រ៉េសូណង់):**\n" +
            "XL = Xc → Z = R (ភាពធន់ទាបបំផុត)\n" +
            "f₀ = 1/(2π√(LC))\n\n" +
            "**ការប្រើប្រាស់:**\n" +
            "Radio tuning, filter circuits");

        m.put("មេរៀនទី៥៖ ថាមពលក្នុងសៀគ្វី AC",
            "**ថាមពល AC:**\n\n" +
            "P = V_rms · I_rms · cosφ\n\n" +
            "Power Factor: cosφ = R/Z\n\n" +
            "**Apparent Power:** S = V_rms · I_rms (VA)\n" +
            "**Real Power:** P = S·cosφ (W)\n" +
            "**Reactive Power:** Q = S·sinφ (VAR)\n\n" +
            "**ការបង្កើន Power Factor:**\n" +
            "ប្រើ capacitor bank ជាមួយ inductive load");

        // ===== GRADE 12 — CHEMISTRY =====
        m.put("មេរៀនទី១៖ ស្ថិតភាពឌីណាមិក",
            "**Chemical Equilibrium:**\n\n" +
            "aA + bB ⇌ cC + dD\n\n" +
            "ស្ថិតភាពឌីណាមិក: អត្រាប្រតិកម្មមុខ = អត្រាប្រតិកម្មក្រោយ\n\n" +
            "**លក្ខណៈ:**\n" +
            "- ប្រព័ន្ធ closed\n" +
            "- [reactant] និង [product] ថេរ\n" +
            "- ប្រតិកម្មមុខ-ក្រោយ​ប្រព្រឹត្ត​ទៅ​ជា​ប្រចាំ");

        m.put("មេរៀនទី២៖ ថេរស្ថិតភាព Kc និង Kp",
            "**Equilibrium Constants:**\n\n" +
            "Kc = [C]ᶜ[D]ᵈ / [A]ᵃ[B]ᵇ\n\n" +
            "Kp = Pc^c · Pd^d / Pa^a · Pb^b\n\n" +
            "**Relationship:** Kp = Kc(RT)^Δn\n" +
            "Δn = moles gas products - moles gas reactants\n\n" +
            "**ការបកស្រាយ:**\n" +
            "- Kc >> 1: ផ្នែក product ស្ថិតភាព\n" +
            "- Kc << 1: ផ្នែក reactant ស្ថិតភាព");

        m.put("មេរៀនទី៣៖ គោលការណ៍ Le Chatelier",
            "**Le Chatelier's Principle:**\n\n" +
            "\"ប្រសិន​ប្រព័ន្ធ​ស្ថិតភាព​ទទួល​ការ​ប្រែប្រួល, ប្រព័ន្ធ​នឹង​ \\nតបស្នង​ដើម្បី​បន្ថយ​ការ​ប្រែប្រួល​នោះ\"\n\n" +
            "**ការ​ DisturbanceS:**\n" +
            "- បន្ថែម reactant → shift ទៅ product\n" +
            "- បង្កើន pressure → shift ទៅ​ side ​moles​ gas​ less\n" +
            "- ឡើង​សីតុណ្ហភាព → shift ​endothermic direction");

        m.put("មេរៀនទី១៖ ទ្រឹស្តី Brønsted-Lowry",
            "**Brønsted-Lowry Theory:**\n\n" +
            "**Acid:** ម្ចាស់ proton (H⁺ donor)\n" +
            "**Base:** អ្នកទទួល proton (H⁺ acceptor)\n\n" +
            "**Conjugate Pairs:**\n" +
            "HCl + H₂O → H₃O⁺ + Cl⁻\n" +
            "HCl/Cl⁻ ជា conjugate acid-base pair\n\n" +
            "**Amphoteric Species:**\n" +
            "H₂O អាចជា acid ឬ base");

        m.put("មេរៀនទី២៖ ការគណនា pH",
            "**pH Calculations:**\n\n" +
            "pH = -log[H⁺]\n" +
            "pOH = -log[OH⁻]\n" +
            "pH + pOH = 14 (25°C)\n\n" +
            "**Strong Acid:** [H⁺] = [acid]\n" +
            "**Weak Acid:** Ka = [H⁺][A⁻]/[HA]\n" +
            "[H⁺] = √(Ka·[HA])\n\n" +
            "**ឧទាហរណ៍:**\n" +
            "0.1M HCl: pH = -log(0.1) = 1\n" +
            "0.1M CH₃COOH (Ka=1.8×10⁻⁵): pH ≈ 2.87");

        // ===== GRADE 12 — BIOLOGY =====
        m.put("មេរៀនទី១៖ DNA រចនាសម្ព័ន្ធ និងការចម្លង",
            "**DNA Structure:**\n\n" +
            "Double helix, antiparallel strands\n" +
            "Base pairs: A-T (2 H-bonds), G-C (3 H-bonds)\n\n" +
            "**DNA Replication (Semi-conservative):**\n" +
            "1. Helicase unwinds DNA\n" +
            "2. Primase adds RNA primer\n" +
            "3. DNA Polymerase III synthesizes new strand (5'→3')\n" +
            "4. Leading/Lagging strand (Okazaki fragments)\n" +
            "5. DNA Ligase joins fragments\n\n" +
            "**ទីកន្លែង:** Nucleus (Eukaryotes)");

        m.put("មេរៀនទី១៖ ទ្រឹស្តីបទ Mendel ទី១ និង ទី២",
            "**Mendel's Laws:**\n\n" +
            "**Law 1 (Segregation):**\n" +
            "Alleles segregate during gamete formation\n" +
            "Aa × Aa → 1AA:2Aa:1aa (3:1 phenotype ratio)\n\n" +
            "**Law 2 (Independent Assortment):**\n" +
            "Genes on different chromosomes assort independently\n" +
            "AaBb × AaBb → 9:3:3:1 ratio\n\n" +
            "**Punnett Square:** ឧបករណ៍​ព្យាករ​ phenotype/genotype ratio");

        m.put("មេរៀនទី១៖ RNA និងការបំលែង Transcription",
            "**Transcription:**\n\n" +
            "DNA → RNA (ក្នុង Nucleus)\n\n" +
            "**ជំហាន:**\n" +
            "1. RNA Polymerase ចាប់ Promoter\n" +
            "2. Elongation: សង្កត់​ template strand, A→U\n" +
            "3. Termination: ជួប terminator\n\n" +
            "**Types of RNA:**\n" +
            "- mRNA: នាំ​ genetic code\n" +
            "- tRNA: ដឹក​ amino acid\n" +
            "- rRNA: សមាសភាគ ribosome\n\n" +
            "**RNA Processing (Eukaryotes):**\n" +
            "5' cap, poly-A tail, splicing (remove introns)");

        m.put("មេរៀនទី១៖ ទ្រឹស្តី Darwin និង Wallace",
            "**Darwin's Theory of Natural Selection:**\n\n" +
            "1. **Variation:** Individual differences exist\n" +
            "2. **Heritability:** Traits are inherited\n" +
            "3. **Overproduction:** More offspring than survive\n" +
            "4. **Differential Survival:** Best-adapted survive\n\n" +
            "**Wallace** arrived at same theory independently (1858)\n\n" +
            "**Evidence for Evolution:**\n" +
            "- Fossil record\n- Comparative anatomy (homologous structures)\n" +
            "- Molecular genetics (DNA similarity)\n- Biogeography");

        // ===== GRADE 7 — KEY LESSONS =====
        m.put("មេរៀនទី១៖ សេចក្តីផ្តើមអំពីចំនួនគត់",
            "**ចំនួនគត់ (Integers):**\n\nចំនួនគត់គឺជាចំនួន ..., -3, -2, -1, 0, 1, 2, 3, ...\n\n" +
            "**ប្រភេទ:**\n- ចំនួនគត់វិជ្ជមាន: 1, 2, 3, ...\n- ចំនួនគត់អវិជ្ជមាន: -1, -2, -3, ...\n- សូន្យ: 0\n\n" +
            "**Number Line:** ចំនួនអវិជ្ជមាន ← 0 → ចំនួនវិជ្ជមាន\n\n" +
            "**ឧទាហរណ៍:** -5 < -2 < 0 < 3 < 7");

        m.put("មេរៀនទី១៖ ការស្គាល់រូបវិទ្យា",
            "**រូបវិទ្យា (Physics):**\n\nវិទ្យាសាស្ត្រនៃរូបធាតុ ថាមពល ចលនា និងកម្លាំង\n\n" +
            "**សាខាចម្បង:**\n- Mechanics (ចលន)\n- Thermodynamics (កំដៅ)\n- Electromagnetism (អគ្គិសនី)\n- Optics (ពន្លឺ)\n\n" +
            "**Scientific Method:**\n1. Observation\n2. Hypothesis\n3. Experiment\n4. Analysis\n5. Conclusion");

        m.put("មេរៀនទី១៖ សារៈសំខាន់នៃគីមីវិទ្យា",
            "**គីមីវិទ្យា (Chemistry):**\n\nវិទ្យាសាស្ត្រនៃសារធាតុ ស្ថាបនា ។\n\n" +
            "**ការប្រើប្រាស់:** ថ្នាំ ប្លាស្ទិច ម្ហូប ថ្ម\n\n" +
            "**ប្រភេទ:** Organic / Inorganic / Physical / Analytical\n\n" +
            "**SI Units:** kg, m, s, mol, K, A");

        m.put("មេរៀនទី១៖ លក្ខណៈនៃភាវៈរស់",
            "**ភាវៈរស់ (Living Things):**\n\n**លក្ខណៈ 7:**\n" +
            "1. Movement\n2. Respiration\n3. Sensitivity\n4. Growth\n5. Reproduction\n6. Excretion\n7. Nutrition\n\n" +
            "**MRSGREN** - Mnemonic\n\n**Cells:** Building block of all life");

        // ===== GRADE 8 KEY LESSONS =====
        m.put("មេរៀនទី១៖ ចលនាលីនេអ៊ែរ",
            "**Linear Motion (ចលនាលីនេអ៊ែរ):**\n\n" +
            "**Equations:**\n- v = u + at\n- s = ut + ½at²\n- v² = u² + 2as\n\n" +
            "**Variables:**\n- u = initial velocity\n- v = final velocity\n- a = acceleration\n- t = time\n- s = displacement\n\n" +
            "**ឧទាហរណ៍:** Car starts from rest (u=0), a=2m/s², find v after 5s:\nv = 0 + 2×5 = 10 m/s");

        m.put("មេរៀនទី១៖ Ionic Bond",
            "**Ionic Bonding:**\n\nការផ្ទេរ electron ពី metal ទៅ non-metal\n\n" +
            "**ដំណើរការ:**\n- Metal loses electrons → cation (+)\n- Non-metal gains electrons → anion (-)\n- Electrostatic attraction\n\n" +
            "**ឧទាហរណ៍:** NaCl\n- Na → Na⁺ + e⁻\n- Cl + e⁻ → Cl⁻\n- Na⁺ + Cl⁻ → NaCl");

        m.put("មេរៀនទី១៖ ប្រភេទចំណីអាហារ",
            "**Food Groups (ចំណីអាហារ):**\n\n**Macronutrients:**\n" +
            "- Carbohydrates: ថាមពល (4 kcal/g)\n- Proteins: ការកសាង (4 kcal/g)\n- Fats: ថាមពលខ្ពស់ (9 kcal/g)\n\n" +
            "**Micronutrients:** Vitamins & Minerals\n\n" +
            "**Balanced Diet:** 50% Carbs, 20% Protein, 30% Fat");

        // ===== GRADE 9 KEY LESSONS =====
        m.put("មេរៀនទី១៖ ការដោះស្រាយដោយការแยកជាកត្តា",
            "**Solving Quadratics by Factoring:**\n\nax² + bx + c = 0\n\n" +
            "**ដំណើរការ:**\n1. ដាក់ = 0\n2. ស្វែងរកកត្តា: (x+p)(x+q)=0\n3. x = -p ឬ x = -q\n\n" +
            "**ឧទាហរណ៍:**\nx² - 5x + 6 = 0\n(x-2)(x-3) = 0\nx = 2 ឬ x = 3");

        m.put("មេរៀនទី១៖ ស៊ីនុស កូស៊ីនុស និងតង់សង់",
            "**Trigonometric Ratios:**\n\nIn a right triangle:\n\n" +
            "- sin θ = opposite/hypotenuse\n- cos θ = adjacent/hypotenuse\n- tan θ = opposite/adjacent\n\n" +
            "**Mnemonic:** SOH-CAH-TOA\n\n" +
            "**Special Angles:**\n- sin 30° = 0.5, cos 30° = √3/2\n- sin 45° = cos 45° = √2/2\n- sin 60° = √3/2, cos 60° = 0.5");

        // ===== GRADE 10 KEY LESSONS =====
        m.put("មេរៀនទី១៖ Projectile Motion",
            "**Projectile Motion:**\n\nចលនាក្នុង 2 ជ្រុង (horizontal + vertical)\n\n" +
            "**Equations:**\n- Horizontal: x = v₀cos(θ)·t\n- Vertical: y = v₀sin(θ)·t - ½gt²\n\n" +
            "**Range:** R = v₀²sin(2θ)/g\n" +
            "**Max Height:** H = v₀²sin²(θ)/2g\n\n" +
            "**ឧទាហរណ៍:** Ball at 45° with v₀=20m/s: R = 400×1/10 = 40m");

        m.put("មេរៀនទី១៖ Enthalpy",
            "**Enthalpy (H):**\n\nEnthalpy = internal energy + pressure×volume\n\nΔH = Hproducts - Hreactants\n\n" +
            "**Exothermic:** ΔH < 0 (releases heat)\n" +
            "**Endothermic:** ΔH > 0 (absorbs heat)\n\n" +
            "**Standard Enthalpy:** ΔH° measured at 25°C, 1 atm\n\n" +
            "**ឧទាហរណ៍:** CH₄ + 2O₂ → CO₂ + 2H₂O, ΔH = -890 kJ/mol");

        // ===== GRADE 11 KEY LESSONS =====
        m.put("មេរៀនទី១៖ ប្រមាណវិធីលើម៉ាទ្រីស",
            "**Matrix Operations:**\n\n**Addition:** Add corresponding elements\n\n" +
            "**Multiplication:**\nC[i,j] = Σ A[i,k]·B[k,j]\n\n" +
            "**Identity Matrix (I):** A·I = I·A = A\n\n" +
            "**ឧទាហរណ៍:**\n[1 2][3 4]   [1×3+2×1  1×4+2×2]   [5  8]\n[0 1][1 2] = [0×3+1×1  0×4+1×2] = [1  2]");

        m.put("មេរៀនទី១៖ Kirchhoff's Laws",
            "**Kirchhoff's Laws:**\n\n**KCL (Current Law):**\nΣI_in = ΣI_out at any node\n\n" +
            "**KVL (Voltage Law):**\nΣV = 0 around any closed loop\n\n" +
            "**ការប្រើប្រាស់:**\nដោះស្រាយ circuits ស្មុគ with multiple branches\n\n" +
            "**ឧទាហរណ៍:** Node with 3A in, 1A out → third branch = 2A out");

        m.put("មេរៀនទី១៖ Galvanic Cell",
            "**Galvanic Cell (Voltaic Cell):**\n\nបំប្លែង chemical energy → electrical energy\n\n" +
            "**ផ្នែក:**\n- Anode (-): Oxidation occurs\n- Cathode (+): Reduction occurs\n- Salt Bridge: maintains charge balance\n\n" +
            "**ឧទាហរណ៍ Zn-Cu Cell:**\n- Anode: Zn → Zn²⁺ + 2e⁻\n- Cathode: Cu²⁺ + 2e⁻ → Cu\n- EMF = +1.10V");

        m.put("មេរៀនទី១៖ Natural Selection",
            "**Natural Selection:**\n\nDarwin's mechanism of evolution\n\n" +
            "**4 Conditions:**\n1. Variation in population\n2. Heritable traits\n3. Overproduction\n4. Differential survival\n\n" +
            "**Fitness:** Reproductive success, not strength\n\n" +
            "**Types:** Directional, Stabilizing, Disruptive\n\n" +
            "**ឧទាហរណ៍:** Peppered Moths - industrial melanism");

        return m;
    }
}
