
const API_BASE_URL = "http://localhost:8080/api";

const ORDER_STATUSES = [
    "PENDING",
    "CONFIRMED",
    "PREPARING",
    "OUT_FOR_DELIVERY",
    "DELIVERED",
    "CANCELLED"
];

const $ = (selector) => document.querySelector(selector);

let restaurantsCache = [];
let ordersCache = [];

// ==================================================
// 1. COMMON HELPER FUNCTIONS
// ==================================================

function escapeHTML(value = "") {
    return String(value).replace(/[&<>"']/g, (character) => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    })[character]);
}

function showAdminNotice(message, isError = false) {
    const notice = $("#adminNotice");

    if (!notice) {
        console.log(message);
        return;
    }

    notice.textContent = message;
    notice.classList.toggle("error", isError);
    notice.hidden = false;

    clearTimeout(showAdminNotice.timer);

    showAdminNotice.timer = setTimeout(() => {
        notice.hidden = true;
    }, 4000);
}

async function apiRequest(path, options = {}) {
    const controller = new AbortController();

    const timeoutId = setTimeout(() => {
        controller.abort();
    }, 15000);

    try {
        const response = await fetch(`${API_BASE_URL}${path}`, {
            ...options,
            headers: Auth.headers(options.headers || {}),
            signal: controller.signal
        });

        if (response.status === 401) {
            // Session expired: return to the login screen
            Auth.clear();
            showAdminView();
            throw new Error("Your session expired. Please sign in again.");
        }

        if (!response.ok) {
            throw new Error(await readError(response));
        }

        if (response.status === 204) {
            return null;
        }

        return await response.json();

    } catch (error) {
        if (error.name === "AbortError") {
            throw new Error(`Request timed out: ${path}`);
        }

        throw error;

    } finally {
        clearTimeout(timeoutId);
    }
}

// ==================================================
// 2. RESTAURANT MANAGEMENT
// ==================================================

async function loadRestaurants() {
    const list = $("#restaurantList");

    if (list) {
        list.textContent = "Loading restaurants...";
    }

    try {
        const restaurants = await apiRequest("/restaurants");

        if (!Array.isArray(restaurants)) {
            throw new Error("Invalid restaurant API response.");
        }

        restaurantsCache = restaurants;

        $("#restaurantCount").textContent =
            restaurantsCache.length;

        renderRestaurants();
        populateRestaurantDropdown();

        // Refresh the food item list using the latest restaurant names.
        await loadFoodItems();

    } catch (error) {
        console.error("Restaurant loading error:", error);

        if (list) {
            list.innerHTML =
                '<div class="list-empty">Could not load restaurants. Check that Spring Boot is running.</div>';
        }

        showAdminNotice(
            `Could not load restaurants: ${error.message}`,
            true
        );
    }
}

function renderRestaurants() {
    const list = $("#restaurantList");

    if (!list) return;

    list.innerHTML = "";

    if (restaurantsCache.length === 0) {
        list.innerHTML =
            '<div class="list-empty">No restaurants yet. Add one using the form above.</div>';

        return;
    }

    restaurantsCache.forEach((restaurant) => {
        const row = document.createElement("article");

        row.className = "data-row";

        row.innerHTML = `
            <div class="row-main">
                <strong>
                    ${escapeHTML(restaurant.name || "Restaurant")}
                    <span>#${restaurant.id}</span>
                </strong>

                <small>
                    ${escapeHTML(restaurant.cuisine || "Cuisine not specified")}
                    · ${escapeHTML(restaurant.address || "")}
                </small>

                <small>
                    Rating:
                    ${Number(restaurant.rating || 0).toFixed(1)} / 5
                </small>
            </div>

            <div class="row-actions">
                <button type="button"
                        class="button secondary edit-restaurant">
                    Edit
                </button>

                <button type="button"
                        class="button danger delete-restaurant">
                    Delete
                </button>
            </div>
        `;

        row.querySelector(".edit-restaurant").addEventListener(
            "click",
            () => editRestaurant(restaurant)
        );

        row.querySelector(".delete-restaurant").addEventListener(
            "click",
            () => deleteRestaurant(restaurant)
        );

        list.appendChild(row);
    });
}

function populateRestaurantDropdown() {
    const dropdown = $("#foodRestaurantId");

    if (!dropdown) return;

    const previousValue = dropdown.value;

    dropdown.innerHTML =
        '<option value="">Choose a restaurant</option>';

    restaurantsCache.forEach((restaurant) => {
        const option = document.createElement("option");

        option.value = restaurant.id;
        option.textContent =
            `${restaurant.name} (ID ${restaurant.id})`;

        dropdown.appendChild(option);
    });

    if (
        restaurantsCache.some(
            (restaurant) => String(restaurant.id) === previousValue
        )
    ) {
        dropdown.value = previousValue;
    }
}

async function addRestaurant(event) {
    event.preventDefault();

    const restaurant = {
        name: $("#restaurantName").value.trim(),
        cuisine: $("#restaurantCuisine").value.trim(),
        address: $("#restaurantAddress").value.trim(),
        rating: Number($("#restaurantRating").value)
    };

    if (
        !restaurant.name ||
        !restaurant.cuisine ||
        !restaurant.address ||
        !Number.isFinite(restaurant.rating) ||
        restaurant.rating < 0 ||
        restaurant.rating > 5
    ) {
        showAdminNotice(
            "Enter valid restaurant details and a rating from 0 to 5.",
            true
        );

        return;
    }

    try {
        await apiRequest("/restaurants", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(restaurant)
        });

        $("#restaurantForm").reset();
        $("#restaurantRating").value = "4.0";

        await loadRestaurants();

        showAdminNotice("Restaurant added successfully.");

    } catch (error) {
        console.error("Add restaurant error:", error);

        showAdminNotice(
            `Could not add restaurant: ${error.message}`,
            true
        );
    }
}

async function editRestaurant(restaurant) {
    const name = prompt(
        "Restaurant name:",
        restaurant.name || ""
    );

    if (name === null) return;

    const cuisine = prompt(
        "Cuisine:",
        restaurant.cuisine || ""
    );

    if (cuisine === null) return;

    const address = prompt(
        "Address:",
        restaurant.address || ""
    );

    if (address === null) return;

    const ratingText = prompt(
        "Rating from 0 to 5:",
        restaurant.rating ?? 4
    );

    if (ratingText === null) return;

    const rating = Number(ratingText);

    if (
        !name.trim() ||
        !cuisine.trim() ||
        !address.trim() ||
        !Number.isFinite(rating) ||
        rating < 0 ||
        rating > 5
    ) {
        showAdminNotice(
            "Please enter valid restaurant details.",
            true
        );

        return;
    }

    try {
        await apiRequest(`/restaurants/${restaurant.id}`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                name: name.trim(),
                cuisine: cuisine.trim(),
                address: address.trim(),
                rating
            })
        });

        await loadRestaurants();

        showAdminNotice("Restaurant updated successfully.");

    } catch (error) {
        console.error("Update restaurant error:", error);

        showAdminNotice(
            `Could not update restaurant: ${error.message}`,
            true
        );
    }
}

async function deleteRestaurant(restaurant) {
    const confirmed = confirm(
        `Delete "${restaurant.name}"?\n\n` +
        "All food items of this restaurant will be deleted too."
    );

    if (!confirmed) return;

    try {
        await apiRequest(`/restaurants/${restaurant.id}`, {
            method: "DELETE"
        });

        await loadRestaurants();

        showAdminNotice("Restaurant deleted successfully.");

    } catch (error) {
        console.error("Delete restaurant error:", error);

        showAdminNotice(
            `Could not delete restaurant: ${error.message}`,
            true
        );
    }
}

// ==================================================
// 3. FOOD ITEM MANAGEMENT
// ==================================================

async function loadFoodItems() {
    const list = $("#foodList");

    if (!list) return;

    list.textContent = "Loading food items...";

    try {
        const foodItems = await apiRequest("/food-items");

        if (!Array.isArray(foodItems)) {
            throw new Error("Invalid food items API response.");
        }

        $("#foodCount").textContent = foodItems.length;

        const restaurantNames = new Map(
            restaurantsCache.map((restaurant) => [
                String(restaurant.id),
                restaurant.name
            ])
        );

        list.innerHTML = "";

        if (foodItems.length === 0) {
            list.innerHTML =
                '<div class="list-empty">No food items yet. Add one using the form above.</div>';

            return;
        }

        foodItems.forEach((item) => {
            const row = document.createElement("article");

            row.className = "data-row";

            const restaurantName =
                restaurantNames.get(String(item.restaurantId)) ||
                `Restaurant ID ${item.restaurantId}`;

            row.innerHTML = `
                <div class="row-main">
                    <strong>
                        ${escapeHTML(item.name || "Food item")}
                        <span>#${item.id}</span>
                    </strong>

                    <small>
                        ${escapeHTML(item.category || "Uncategorised")}
                        · ₹${Number(item.price || 0).toFixed(2)}
                    </small>

                    <small>
                        Restaurant: ${escapeHTML(restaurantName)}
                    </small>

                    <small>
                        ${escapeHTML(item.description || "")}
                    </small>
                </div>

                <div class="row-actions">
                    <button type="button"
                            class="button secondary edit-food">
                        Edit
                    </button>

                    <button type="button"
                            class="button danger delete-food">
                        Delete
                    </button>
                </div>
            `;

            row.querySelector(".edit-food").addEventListener(
                "click",
                () => editFoodItem(item)
            );

            row.querySelector(".delete-food").addEventListener(
                "click",
                () => deleteFoodItem(item)
            );

            list.appendChild(row);
        });

    } catch (error) {
        console.error("Food item loading error:", error);

        list.innerHTML =
            '<div class="list-empty">Could not load food items. Check the backend.</div>';

        showAdminNotice(
            `Could not load food items: ${error.message}`,
            true
        );
    }
}

async function addFoodItem(event) {
    event.preventDefault();

    const item = {
        name: $("#foodName").value.trim(),
        restaurantId: Number($("#foodRestaurantId").value),
        category: $("#foodCategory").value.trim(),
        price: Number($("#foodPrice").value),
        description: $("#foodDescription").value.trim(),
        imageUrl: $("#foodImageUrl").value.trim()
    };

    if (
        !item.name ||
        !item.restaurantId ||
        !item.category ||
        !Number.isFinite(item.price) ||
        item.price < 0
    ) {
        showAdminNotice(
            "Enter a food name, choose a restaurant, provide a category, and enter a valid price.",
            true
        );

        return;
    }

    try {
        await apiRequest("/food-items", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(item)
        });

        $("#foodForm").reset();

        await loadFoodItems();

        showAdminNotice("Food item added successfully.");

    } catch (error) {
        console.error("Add food item error:", error);

        showAdminNotice(
            `Could not add food item: ${error.message}`,
            true
        );
    }
}

async function editFoodItem(item) {
    const name = prompt("Food name:", item.name || "");

    if (name === null) return;

    const category = prompt(
        "Category:",
        item.category || ""
    );

    if (category === null) return;

    const priceText = prompt(
        "Price in rupees:",
        item.price
    );

    if (priceText === null) return;

    const restaurantIdText = prompt(
        "Restaurant ID:",
        item.restaurantId
    );

    if (restaurantIdText === null) return;

    const description = prompt(
        "Description:",
        item.description || ""
    );

    if (description === null) return;

    const imageUrl = prompt(
        "Image URL (optional):",
        item.imageUrl || ""
    );

    if (imageUrl === null) return;

    const price = Number(priceText);
    const restaurantId = Number(restaurantIdText);

    if (
        !name.trim() ||
        !category.trim() ||
        !Number.isFinite(price) ||
        price < 0 ||
        !Number.isInteger(restaurantId) ||
        !restaurantsCache.some(
            (restaurant) => String(restaurant.id) === String(restaurantId)
        )
    ) {
        showAdminNotice(
            "Enter valid details and an existing restaurant ID.",
            true
        );

        return;
    }

    try {
        await apiRequest(`/food-items/${item.id}`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                name: name.trim(),
                category: category.trim(),
                price,
                restaurantId,
                description: description.trim(),
                imageUrl: imageUrl.trim()
            })
        });

        await loadFoodItems();

        showAdminNotice("Food item updated successfully.");

    } catch (error) {
        console.error("Update food item error:", error);

        showAdminNotice(
            `Could not update food item: ${error.message}`,
            true
        );
    }
}

async function deleteFoodItem(item) {
    const confirmed = confirm(
        `Delete "${item.name}" (ID ${item.id})?\n\n` +
        "Past orders keep their saved item names and prices."
    );

    if (!confirmed) return;

    try {
        await apiRequest(`/food-items/${item.id}`, {
            method: "DELETE"
        });

        await loadFoodItems();

        showAdminNotice("Food item deleted successfully.");

    } catch (error) {
        console.error("Delete food item error:", error);

        showAdminNotice(
            `Could not delete the food item: ${error.message}`,
            true
        );
    }
}

// ==================================================
// 4. CUSTOMER ORDER MANAGEMENT
// ==================================================

function normalizeStatus(status) {
    const value = String(status || "PENDING")
        .trim()
        .toUpperCase();

    return ORDER_STATUSES.includes(value)
        ? value
        : "PENDING";
}

async function loadOrders() {
    const list = $("#orderList");

    if (!list) {
        console.error(
            'Could not find #orderList. Check your admin.html.'
        );

        return;
    }

    list.textContent = "Loading customer orders...";

    try {
        // Load orders first. Do not wait for item details.
        const orders = await apiRequest("/orders");

        if (!Array.isArray(orders)) {
            throw new Error("The orders API did not return a list.");
        }

        list.innerHTML = "";

        if (orders.length === 0) {
            list.innerHTML =
                '<div class="list-empty">No customer orders yet.</div>';

            return;
        }

        orders.sort((a, b) =>
            new Date(b.orderDate || 0) -
            new Date(a.orderDate || 0)
        );

        ordersCache = orders;
        updateOrderStats(orders);

        const filter = $("#orderFilter")?.value || "ALL";
        const visibleOrders = filter === "ALL"
            ? orders
            : orders.filter((o) => normalizeStatus(o.status) === filter);

        if (visibleOrders.length === 0) {
            list.innerHTML =
                '<div class="list-empty">No orders with this status.</div>';
            return;
        }

        visibleOrders.forEach((order) => {
            const status = normalizeStatus(order.status);

            const row = document.createElement("article");
            row.className = "data-row";

            row.innerHTML = `
                <div class="row-main">
                    <strong>
                        Order #${order.id} —
                        ${escapeHTML(order.customerName || "Customer")}
                    </strong>

                    <small>
                        Phone: ${escapeHTML(order.phone || "Not provided")}
                    </small>

                    <small>
                        Address:
                        ${escapeHTML(order.deliveryAddress || "Not provided")}
                    </small>

                    <small class="order-items">
                        Items: Loading item details...
                    </small>

                    <small>
                        Total:
                        ₹${Number(order.totalAmount || 0).toFixed(2)}
                    </small>

                    <small>
                        Date:
                        ${escapeHTML(order.orderDate || "Unknown")}
                    </small>

                    <small>
                        Status:
                        <strong class="display-status">
                            ${escapeHTML(status.replaceAll("_", " "))}
                        </strong>
                    </small>
                </div>

                <div class="row-actions">
                    <select class="order-status-select"
                            aria-label="Update status for order ${order.id}">
                        ${ORDER_STATUSES.map((optionStatus) => `
                            <option value="${optionStatus}"
                                ${status === optionStatus ? "selected" : ""}>
                                ${optionStatus.replaceAll("_", " ")}
                            </option>
                        `).join("")}
                    </select>
                </div>
            `;

            const itemsElement = row.querySelector(".order-items");
            const select = row.querySelector(".order-status-select");
            const displayStatus = row.querySelector(".display-status");

            select.addEventListener("change", async () => {
                const previousStatus = normalizeStatus(order.status);
                const newStatus = select.value;

                select.disabled = true;

                try {
                    const updatedOrder = await apiRequest(
                        `/orders/${order.id}/status`,
                        {
                            method: "PUT",
                            headers: {
                                "Content-Type": "application/json"
                            },
                            body: JSON.stringify({
                                status: newStatus
                            })
                        }
                    );

                    order.status = normalizeStatus(updatedOrder.status);
                    updateOrderStats(ordersCache);

                    select.value = order.status;

                    displayStatus.textContent =
                        order.status.replaceAll("_", " ");

                    showAdminNotice(
                        `Order #${order.id} updated to ${order.status}.`
                    );

                } catch (error) {
                    console.error(
                        `Order #${order.id} status update failed:`,
                        error
                    );

                    select.value = previousStatus;

                    showAdminNotice(
                        `Could not update order #${order.id}: ${error.message}`,
                        true
                    );

                } finally {
                    select.disabled = false;
                }
            });

            list.appendChild(row);

            // Fetch order items after this order is visible.
            loadOrderItems(order.id, itemsElement);
        });

    } catch (error) {
        console.error("Order loading error:", error);

        list.innerHTML =
            '<div class="list-empty">Could not load orders. Check the backend and browser console.</div>';

        showAdminNotice(
            `Could not load customer orders: ${error.message}`,
            true
        );
    }
}

function updateOrderStats(orders) {
    $("#orderCount").textContent = orders.length;

    $("#pendingCount").textContent = orders.filter(
        (o) => normalizeStatus(o.status) === "PENDING"
    ).length;

    const revenue = orders
        .filter((o) => normalizeStatus(o.status) === "DELIVERED")
        .reduce((sum, o) => sum + Number(o.totalAmount || 0), 0);

    $("#revenueTotal").textContent = `₹${revenue.toFixed(2)}`;
}

async function loadOrderItems(orderId, targetElement) {
    try {
        const items = await apiRequest(`/orders/${orderId}/items`);

        if (!Array.isArray(items) || items.length === 0) {
            targetElement.textContent =
                "Items: No item details available";

            return;
        }

        const summary = items.map((item) => {
            const name =
                item.foodName || `Food item ${item.foodItemId}`;

            return `${name} × ${item.quantity}`;
        }).join(", ");

        targetElement.textContent = `Items: ${summary}`;

    } catch (error) {
        console.warn(
            `Could not load items for order ${orderId}:`,
            error
        );

        targetElement.textContent =
            "Items: Item details unavailable";
    }
}

// ==================================================
// 5. DASHBOARD INITIALIZATION
// ==================================================

// ==================================================
// 6. ADMIN LOGIN GATE
// ==================================================

let dashboardStarted = false;

function showAdminView() {
    const allowed = Auth.isLoggedIn() && Auth.isAdmin();

    $("#loginGate").hidden = allowed;
    $("#dashboard").hidden = !allowed;
    $("#logoutBtn").hidden = !allowed;

    if (!allowed) dashboardStarted = false;

    if (allowed && !dashboardStarted) {
        dashboardStarted = true;
        loadRestaurants();
        loadOrders();
    }
}

async function adminLogin(event) {
    event.preventDefault();

    const error = $("#adminLoginError");
    error.hidden = true;

    try {
        const user = await loginRequest(
            API_BASE_URL,
            $("#adminEmail").value.trim(),
            $("#adminPassword").value
        );

        if (user.role !== "ADMIN") {
            Auth.clear();
            throw new Error("This account is not an administrator.");
        }

        $("#adminLoginForm").reset();
        showAdminView();

    } catch (e) {
        error.textContent = e.message;
        error.hidden = false;
    }
}

document.addEventListener("DOMContentLoaded", () => {
    console.log("Crave Admin Dashboard initialized.");

    $("#adminLoginForm")?.addEventListener("submit", adminLogin);

    $("#logoutBtn")?.addEventListener("click", () => {
        Auth.clear();
        window.location.reload();
    });

    $("#restaurantForm")?.addEventListener("submit", addRestaurant);
    $("#refreshRestaurants")?.addEventListener("click", loadRestaurants);
    $("#foodForm")?.addEventListener("submit", addFoodItem);
    $("#refreshFood")?.addEventListener("click", loadFoodItems);
    $("#refreshOrders")?.addEventListener("click", loadOrders);
    $("#orderFilter")?.addEventListener("change", loadOrders);

    showAdminView();
});
