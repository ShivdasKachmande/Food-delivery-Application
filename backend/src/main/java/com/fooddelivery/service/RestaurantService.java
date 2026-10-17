package com.fooddelivery.service;

import com.fooddelivery.entity.Restaurant;
import com.fooddelivery.repository.FoodItemRepository;
import com.fooddelivery.repository.RestaurantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final FoodItemRepository foodItemRepository;

    public RestaurantService(
            RestaurantRepository restaurantRepository,
            FoodItemRepository foodItemRepository) {
        this.restaurantRepository = restaurantRepository;
        this.foodItemRepository = foodItemRepository;
    }

    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }

    public Restaurant saveRestaurant(Restaurant restaurant) {
        validate(restaurant);
        return restaurantRepository.save(restaurant);
    }

    public Restaurant updateRestaurant(Long id, Restaurant updated) {

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Restaurant not found"));

        validate(updated);

        restaurant.setName(updated.getName().trim());
        restaurant.setCuisine(updated.getCuisine().trim());
        restaurant.setAddress(updated.getAddress().trim());
        restaurant.setRating(updated.getRating());

        return restaurantRepository.save(restaurant);
    }

    // Deleting a restaurant also removes its menu items
    @Transactional
    public void deleteRestaurant(Long id) {

        if (!restaurantRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Restaurant not found");
        }

        foodItemRepository.deleteAll(
                foodItemRepository.findByRestaurantId(id));

        restaurantRepository.deleteById(id);
    }

    private void validate(Restaurant r) {

        if (r.getName() == null || r.getName().isBlank()
                || r.getCuisine() == null || r.getCuisine().isBlank()
                || r.getAddress() == null || r.getAddress().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Name, cuisine and address are required");
        }

        if (r.getRating() < 0 || r.getRating() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Rating must be between 0 and 5");
        }
    }
}
