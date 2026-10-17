package com.fooddelivery.service;

import com.fooddelivery.entity.FoodItem;
import com.fooddelivery.repository.FoodItemRepository;
import com.fooddelivery.repository.RestaurantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class FoodItemService {

    private final FoodItemRepository foodItemRepository;
    private final RestaurantRepository restaurantRepository;

    public FoodItemService(
            FoodItemRepository foodItemRepository,
            RestaurantRepository restaurantRepository) {
        this.foodItemRepository = foodItemRepository;
        this.restaurantRepository = restaurantRepository;
    }

    public List<FoodItem> getAllFoodItems() {
        return foodItemRepository.findAll();
    }

    public List<FoodItem> getFoodItemsByRestaurant(Long restaurantId) {
        return foodItemRepository.findByRestaurantId(restaurantId);
    }

    public FoodItem saveFoodItem(FoodItem foodItem) {
        validate(foodItem);
        return foodItemRepository.save(foodItem);
    }

    public FoodItem updateFoodItem(Long id, FoodItem updated) {

        FoodItem existing = foodItemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Food item not found"));

        validate(updated);

        existing.setName(updated.getName().trim());
        existing.setDescription(updated.getDescription());
        existing.setPrice(updated.getPrice());
        existing.setCategory(updated.getCategory().trim());
        existing.setImageUrl(updated.getImageUrl());
        existing.setRestaurantId(updated.getRestaurantId());

        return foodItemRepository.save(existing);
    }

    public void deleteFoodItem(Long id) {

        if (!foodItemRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Food item not found");
        }

        foodItemRepository.deleteById(id);
    }

    private void validate(FoodItem item) {

        if (item.getName() == null || item.getName().isBlank()
                || item.getName().length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Food name is required (max 100 characters)");
        }

        if (item.getCategory() == null || item.getCategory().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Category is required");
        }

        if (item.getPrice() < 0 || Double.isNaN(item.getPrice())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Price must be zero or more");
        }

        if (item.getRestaurantId() == null
                || !restaurantRepository.existsById(item.getRestaurantId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Choose an existing restaurant");
        }
    }
}
