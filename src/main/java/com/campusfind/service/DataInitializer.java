package com.campusfind.service;

import com.campusfind.model.Item;
import com.campusfind.model.User;
import com.campusfind.repository.ItemRepository;
import com.campusfind.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           ItemRepository itemRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsersAndItems();
    }

    private void seedUsersAndItems() {
        // 1. Seed Administrator if not present
        User admin = userRepository.findByEmail("admin@campusfind.edu").orElseGet(() -> {
            User newAdmin = new User();
            newAdmin.setFullName("Campus Security Administrator");
            newAdmin.setEmail("admin@campusfind.edu");
            newAdmin.setPasswordHash(passwordEncoder.encode("Admin@123"));
            newAdmin.setRole("ROLE_ADMIN");
            newAdmin.setPhone("+1-555-0199");
            User saved = userRepository.save(newAdmin);
            logger.info("Initialized default Administrator: admin@campusfind.edu / Admin@123");
            return saved;
        });

        // 2. Seed Sample Student if not present
        User student = userRepository.findByEmail("student@campusfind.edu").orElseGet(() -> {
            User newStudent = new User();
            newStudent.setFullName("Alex Morgan (Student)");
            newStudent.setEmail("student@campusfind.edu");
            newStudent.setPasswordHash(passwordEncoder.encode("Student@123"));
            newStudent.setRole("ROLE_USER");
            newStudent.setPhone("+1-555-0142");
            User saved = userRepository.save(newStudent);
            logger.info("Initialized sample Student: student@campusfind.edu / Student@123");
            return saved;
        });

        // 3. Seed sample items if items table is empty
        if (itemRepository.countTotal() == 0) {
            Item item1 = new Item();
            item1.setUserId(student.getId());
            item1.setTitle("Apple AirPods Pro (2nd Gen)");
            item1.setType("LOST");
            item1.setCategory("Electronics");
            item1.setDescription("White case with a small scratch on the front hinge. Lost during evening study session on the 2nd floor.");
            item1.setItemDate(LocalDate.now().minusDays(1));
            item1.setLocation("Central Library, 2nd Floor Quiet Zone");
            item1.setContactInfo("alex.m@campus.edu or WhatsApp 555-0142");
            item1.setImageUrl(null);
            item1.setStatus("OPEN");
            itemRepository.save(item1);

            Item item2 = new Item();
            item2.setUserId(admin.getId());
            item2.setTitle("Student ID Card — Sanket Rao");
            item2.setType("FOUND");
            item2.setCategory("ID & Cards");
            item2.setDescription("Found student ID card belonging to Computer Science Dept. Deposited at Security Desk.");
            item2.setItemDate(LocalDate.now());
            item2.setLocation("Main Cafeteria, Table #14");
            item2.setContactInfo("Campus Security Office (Admin Bldg, Rm 101)");
            item2.setImageUrl(null);
            item2.setStatus("OPEN");
            itemRepository.save(item2);

            Item item3 = new Item();
            item3.setUserId(student.getId());
            item3.setTitle("Stewart Calculus Textbook (9th Edition)");
            item3.setType("LOST");
            item3.setCategory("Books & Notes");
            item3.setDescription("Hardcover textbook with yellow sticky notes on chapters 7 through 10. Left on back bench.");
            item3.setItemDate(LocalDate.now().minusDays(2));
            item3.setLocation("Mathematics Block, Room 204");
            item3.setContactInfo("alex.m@campus.edu");
            item3.setImageUrl(null);
            item3.setStatus("OPEN");
            itemRepository.save(item3);

            Item item4 = new Item();
            item4.setUserId(admin.getId());
            item4.setTitle("Set of Dorm Keys with Blue Lanyard");
            item4.setType("FOUND");
            item4.setCategory("Keys & Wallets");
            item4.setDescription("Three silver keys attached to a blue Spiderman keychain lanyard. Found near the bleachers.");
            item4.setItemDate(LocalDate.now().minusDays(1));
            item4.setLocation("Sports Complex, Badminton Court");
            item4.setContactInfo("Campus Security Office / admin@campusfind.edu");
            item4.setImageUrl(null);
            item4.setStatus("OPEN");
            itemRepository.save(item4);

            Item item5 = new Item();
            item5.setUserId(student.getId());
            item5.setTitle("Navy Blue North Face Zip Hoodie (Size M)");
            item5.setType("RESOLVED");
            item5.setCategory("Clothing & Accessories");
            item5.setDescription("Dark navy jacket left on the couch. Already claimed and returned!");
            item5.setItemDate(LocalDate.now().minusDays(5));
            item5.setLocation("Student Center Lounge");
            item5.setContactInfo("alex.m@campus.edu");
            item5.setImageUrl(null);
            item5.setStatus("RESOLVED");
            itemRepository.save(item5);

            logger.info("Initialized sample Lost & Found reports.");
        }
    }
}
