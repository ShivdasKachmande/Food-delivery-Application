package com.fooddelivery.controller;

import com.fooddelivery.entity.FoodItem;
import com.fooddelivery.service.FoodItemService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/food-items")
public class FoodItemController {

    private final FoodItemService foodItemService;

    public FoodItemController(FoodItemService foodItemService) {
        this.foodItemService = foodItemService;
    }

    @GetMapping
    public List<FoodItem> getAllFoodItems() {
        return foodItemService.getAllFoodItems();
    }

    @GetMapping("/restaurant/{restaurantId}")
    public List<FoodItem> getFoodItemsByRestaurant(
            @PathVariable Long restaurantId) {
        return foodItemService.getFoodItemsByRestaurant(restaurantId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FoodItem createFoodItem(@RequestBody FoodItem foodItem) {
        return foodItemService.saveFoodItem(foodItem);
    }

    @PutMapping("/{id}")
    public FoodItem updateFoodItem(
            @PathVariable Long id,
            @RequestBody FoodItem foodItem) {
        return foodItemService.updateFoodItem(id, foodItem);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFoodItem(@PathVariable Long id) {
        foodItemService.deleteFoodItem(id);
    }
}
