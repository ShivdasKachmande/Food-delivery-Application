package com.fooddelivery.config;

import com.fooddelivery.entity.Customer;
import com.fooddelivery.entity.FoodItem;
import com.fooddelivery.entity.Restaurant;
import com.fooddelivery.repository.CustomerRepository;
import com.fooddelivery.repository.FoodItemRepository;
import com.fooddelivery.repository.RestaurantRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;

// Creates the first admin account and optional demo data on startup
@Component
public class DataSeeder implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final RestaurantRepository restaurantRepository;
    private final FoodItemRepository foodItemRepository;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.name:Admin}")
    private String adminName;

    @Value("${app.seed.demo-data:false}")
    private boolean seedDemoData;

    public DataSeeder(
            CustomerRepository customerRepository,
            RestaurantRepository restaurantRepository,
            FoodItemRepository foodItemRepository) {
        this.customerRepository = customerRepository;
        this.restaurantRepository = restaurantRepository;
        this.foodItemRepository = foodItemRepository;
    }

    @Override
    public void run(String... args) {
        seedAdmin();
        if (seedDemoData && restaurantRepository.count() == 0) {
            seedDemoMenu();
        }
    }

    private void seedAdmin() {

        String email = adminEmail.trim().toLowerCase(Locale.ROOT);

        if (customerRepository.existsByEmailIgnoreCase(email)) {
            return;
        }

        Customer admin = new Customer(
                adminName,
                email,
                new BCryptPasswordEncoder().encode(adminPassword));
        admin.setRole("ADMIN");
        customerRepository.save(admin);

        System.out.println("Admin account created: " + email);
    }

    private void seedDemoMenu() {

        Restaurant spice = restaurant("Spice Garden", "Indian",
                "Andheri, Mumbai", 4.5);
        Restaurant pizza = restaurant("Pizza Palace", "Italian",
                "Bandra, Mumbai", 4.3);
        Restaurant burger = restaurant("Burger Barn", "Burgers",
                "Powai, Mumbai", 4.1);

        food("Chicken Biryani", "Fragrant basmati rice with spiced chicken",
                249, "Biryani", spice);
        food("Paneer Butter Masala", "Creamy tomato gravy with soft paneer",
                219, "Curries", spice);
        food("Butter Naan", "Soft tandoor-baked bread", 49, "Breads", spice);

        food("Margherita Pizza", "Classic tomato, mozzarella and basil",
                299, "Pizza", pizza);
        food("Farmhouse Pizza", "Loaded with fresh vegetables and cheese",
                379, "Pizza", pizza);
        food("Penne Arrabbiata", "Penne tossed in spicy tomato sauce",
                229, "Pasta", pizza);

        food("Classic Burger", "Juicy patty, lettuce, tomato and sauce",
                149, "Burgers", burger);
        food("Cheese Burger", "Double cheese with a crispy patty",
                179, "Burgers", burger);
        food("French Fries", "Golden and lightly salted", 99, "Sides", burger);
    }

    private Restaurant restaurant(
            String name, String cuisine, String address, double rating) {
        Restaurant r = new Restaurant();
        r.setName(name);
        r.setCuisine(cuisine);
        r.setAddress(address);
        r.setRating(rating);
        return restaurantRepository.save(r);
    }

    private void food(String name, String description, double price,
                      String category, Restaurant restaurant) {
        FoodItem item = new FoodItem();
        item.setName(name);
        item.setDescription(description);
        item.setPrice(price);
        item.setCategory(category);
        item.setRestaurantId(restaurant.getId());
        foodItemRepository.save(item);
    }
}
