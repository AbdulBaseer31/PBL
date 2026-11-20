package com.vrs.config;

import com.vrs.dao.CustomerDao;
import com.vrs.dao.VehicleDao;
import com.vrs.model.Customer;
import com.vrs.model.Vehicle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CustomerDao customerDao;
    private final VehicleDao vehicleDao;

    @Value("${vrs.admin.contact}")
    private String adminContact;

    @Value("${vrs.admin.password}")
    private String adminPassword;

    public DataSeeder(CustomerDao customerDao, VehicleDao vehicleDao) {
        this.customerDao = customerDao;
        this.vehicleDao = vehicleDao;
    }

    @Override
    public void run(String... args) throws Exception {
        if (customerDao.countByRole("ADMIN") == 0) {
            String hashedPassword = new BCryptPasswordEncoder().encode(adminPassword);
            Customer admin = new Customer(
                    "C000001",
                    "Admin",
                    30,
                    "Headquarters",
                    adminContact,
                    hashedPassword,
                    "ADMIN"
            );
            customerDao.insert(admin);
        }

        seedVehiclesIfEmpty();
    }

    private void seedVehiclesIfEmpty() throws Exception {
        // If vehicles exist with old simple names, refresh to realistic models & years
        boolean needsRefresh = false;
        try {
            List<Vehicle> sample = vehicleDao.search(null, null, null, false);
            if (!sample.isEmpty() && !sample.get(0).getBrand().matches(".*\\d{4}$")) {
                needsRefresh = true;
            }
        } catch (Exception ignored) {
        }

        if (!needsRefresh && vehicleDao.countAll() >= 300) {
            return;
        }

        // Wipe old placeholder vehicles if needed and safe
        if (needsRefresh) {
            try {
                for (Vehicle v : vehicleDao.search(null, null, null, false)) {
                    if (!vehicleDao.isUsed(v.getNumberplate())) {
                        vehicleDao.delete(v.getNumberplate());
                    }
                }
            } catch (Exception ignored) {
            }
        }

        record ModelSpec(String brandName, String type, int minPrice, int maxPrice) {}

        ModelSpec[] catalog = new ModelSpec[] {
            // Sedans
            new ModelSpec("Honda Civic", "Sedan", 70, 110),
            new ModelSpec("Honda City", "Sedan", 60, 95),
            new ModelSpec("Toyota Camry", "Sedan", 80, 130),
            new ModelSpec("Toyota Corolla", "Sedan", 65, 100),
            new ModelSpec("Hyundai Elantra", "Sedan", 70, 105),
            new ModelSpec("Hyundai Verna", "Sedan", 60, 90),
            new ModelSpec("BMW 3 Series", "Sedan", 140, 220),
            new ModelSpec("BMW 5 Series", "Sedan", 180, 280),
            new ModelSpec("Mercedes C-Class", "Sedan", 150, 230),
            new ModelSpec("Mercedes E-Class", "Sedan", 190, 290),
            new ModelSpec("Audi A4", "Sedan", 130, 210),
            new ModelSpec("Audi A6", "Sedan", 170, 260),
            new ModelSpec("Skoda Octavia", "Sedan", 75, 115),
            new ModelSpec("Tesla Model 3", "Sedan", 110, 170),

            // SUVs
            new ModelSpec("Ford EcoSport", "SUV", 65, 95),
            new ModelSpec("Ford Endeavour", "SUV", 130, 190),
            new ModelSpec("Toyota Fortuner", "SUV", 120, 180),
            new ModelSpec("Toyota RAV4", "SUV", 90, 140),
            new ModelSpec("Hyundai Creta", "SUV", 75, 110),
            new ModelSpec("Hyundai Tucson", "SUV", 95, 145),
            new ModelSpec("Tata Harrier", "SUV", 85, 125),
            new ModelSpec("Tata Safari", "SUV", 95, 140),
            new ModelSpec("Mahindra Thar", "SUV", 90, 135),
            new ModelSpec("Mahindra Scorpio-N", "SUV", 85, 130),
            new ModelSpec("Mahindra XUV700", "SUV", 95, 145),
            new ModelSpec("Kia Seltos", "SUV", 75, 115),
            new ModelSpec("BMW X5", "SUV", 200, 320),
            new ModelSpec("Audi Q7", "SUV", 210, 330),
            new ModelSpec("Mercedes GLE", "SUV", 220, 340),

            // Hatchbacks
            new ModelSpec("Hyundai i20", "Hatchback", 45, 70),
            new ModelSpec("Maruti Suzuki Swift", "Hatchback", 40, 65),
            new ModelSpec("Maruti Suzuki Baleno", "Hatchback", 45, 70),
            new ModelSpec("Tata Altroz", "Hatchback", 45, 68),
            new ModelSpec("Volkswagen Polo", "Hatchback", 50, 80),
            new ModelSpec("Toyota Yaris", "Hatchback", 50, 75),
            new ModelSpec("Mini Cooper", "Hatchback", 110, 160),

            // Bikes
            new ModelSpec("Royal Enfield Classic 350", "Bike", 35, 55),
            new ModelSpec("Royal Enfield Himalayan", "Bike", 45, 70),
            new ModelSpec("Yamaha R15", "Bike", 30, 50),
            new ModelSpec("Yamaha MT-15", "Bike", 30, 48),
            new ModelSpec("KTM Duke 390", "Bike", 45, 65),
            new ModelSpec("Bajaj Pulsar NS200", "Bike", 25, 40),
            new ModelSpec("Hero Splendor Plus", "Bike", 18, 28),
            new ModelSpec("Hero Xpulse 200", "Bike", 28, 42),

            // Scooters
            new ModelSpec("Honda Activa 6G", "Scooter", 20, 30),
            new ModelSpec("Suzuki Access 125", "Scooter", 22, 32),
            new ModelSpec("TVS Jupiter", "Scooter", 20, 30),
            new ModelSpec("TVS Ntorq 125", "Scooter", 25, 35),
            new ModelSpec("Vespa SXL 150", "Scooter", 32, 45),
            new ModelSpec("Ather 450X", "Scooter", 30, 42)
        };

        int[] years = {2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024};
        List<Vehicle> list = new ArrayList<>();
        java.util.Random rnd = new java.util.Random(42);

        int plateCounter = 1000;
        // Generate 5-6 instances per model with distinct years
        for (ModelSpec spec : catalog) {
            int copies = 6;
            List<Integer> yearChoices = new ArrayList<>();
            for (int y : years) yearChoices.add(y);
            java.util.Collections.shuffle(yearChoices, rnd);

            for (int c = 0; c < copies; c++) {
                int year = yearChoices.get(c % yearChoices.size());
                String brand = spec.brandName + " " + year;
                int price = spec.minPrice + rnd.nextInt(spec.maxPrice - spec.minPrice + 1);

                char l1 = (char) ('A' + rnd.nextInt(26));
                char l2 = (char) ('A' + rnd.nextInt(26));
                String state = rnd.nextBoolean() ? "TS09" : "TS08";
                String plate = String.format("%s%c%c%04d", state, l1, l2, ++plateCounter);

                list.add(new Vehicle(plate, spec.type, brand, new BigDecimal(price)));
            }
        }

        // Shuffle the list so they appear randomized rather than grouped
        java.util.Collections.shuffle(list, rnd);

        for (Vehicle v : list) {
            try {
                vehicleDao.insert(v);
            } catch (Exception ignored) {
            }
        }
    }
}


