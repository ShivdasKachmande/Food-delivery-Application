
const API_BASE_URL = "http://localhost:8080/api";

const state = {
  afterLogin: null,
  restaurants: [],
  foodItems: [],
  selectedRestaurantId: null,
  activeCategory: "All",
  searchTerm: "",
  cart: {}
};

const $ = selector => document.querySelector(selector);

function escapeHTML(value = "") {
  return String(value).replace(/[&<>"']/g, char => ({
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    '"': "&quot;",
    "'": "&#39;"
  })[char]);
}

function showNotice(message) {
  const el = $("#notice");
  el.textContent = message;
  el.hidden = false;

  clearTimeout(showNotice.timer);
  showNotice.timer = setTimeout(() => {
    el.hidden = true;
  }, 3200);
}

async function apiRequest(url, options = {}) {
  const response = await fetch(url, {
    ...options,
    headers: Auth.headers(options.headers || {})
  });

  if (response.status === 401 && Auth.isLoggedIn()) {
    Auth.clear();
    renderUserArea();
  }

  if (!response.ok) {
    throw new Error(await readError(response));
  }

  return response.status === 204 ? null : response.json();
}

// ---------------- Restaurants ----------------

function restaurantEmoji(restaurant) {
  const text = `${restaurant.name || ""} ${restaurant.cuisine || ""}`
    .toLowerCase();

  if (text.includes("pizza")) return "🍕";
  if (text.includes("burger")) return "🍔";
  if (text.includes("indian") || text.includes("spice") ||
      text.includes("biryani")) return "🍛";
  if (text.includes("cafe") || text.includes("coffee")) return "☕";
  if (text.includes("chinese")) return "🥡";

  return "🍽️";
}

async function loadRestaurants() {
  try {
    state.restaurants = await apiRequest(`${API_BASE_URL}/restaurants`);
    renderRestaurants();

    if (state.restaurants.length) {
      const selected = state.restaurants.find(
        restaurant =>
          String(restaurant.id) === String(state.selectedRestaurantId)
      ) || state.restaurants[0];

      await loadMenu(selected.id, false);
    } else {
      $("#resultsCount").textContent = "0 restaurants";
      $("#restaurantEmpty").hidden = false;
      $("#restaurantHeading").textContent = "No restaurants yet";
      $("#menuGrid").innerHTML =
        '<p class="placeholder">Add restaurants and food items to your database to see a menu.</p>';
    }
  } catch (error) {
    console.error("Restaurant API error:", error);

    $("#restaurantGrid").innerHTML =
      '<p class="loading-message">Could not connect. Make sure Spring Boot is running on port 8080.</p>';

    $("#resultsCount").textContent = "Connection error";
    showNotice("Could not load restaurants. Check the backend.");
  }
}

function renderRestaurants() {
  const grid = $("#restaurantGrid");
  const query = state.searchTerm.toLowerCase();

  const list = state.restaurants.filter(restaurant =>
    `${restaurant.name || ""} ${restaurant.cuisine || ""} ${restaurant.address || ""}`
      .toLowerCase()
      .includes(query)
  );

  grid.innerHTML = "";

  list.forEach(restaurant => {
    const card = document.createElement("button");
    card.type = "button";
    card.className =
      `restaurant-card${String(restaurant.id) === String(state.selectedRestaurantId) ? " selected" : ""}`;

    card.setAttribute(
      "aria-pressed",
      String(String(restaurant.id) === String(state.selectedRestaurantId))
    );

    card.innerHTML = `
      <div class="restaurant-art" aria-hidden="true">
        ${restaurantEmoji(restaurant)}
      </div>
      <div class="restaurant-info">
        <h3>${escapeHTML(restaurant.name || "Restaurant")}</h3>
        <p>${escapeHTML(restaurant.cuisine || "Food")} · ${escapeHTML(restaurant.address || "")}</p>
        <div class="restaurant-meta">
          <span class="rating">★ ${Number(restaurant.rating || 0).toFixed(1)}</span>
          · View menu ↗
        </div>
      </div>`;

    card.addEventListener("click", () => loadMenu(restaurant.id, true));
    grid.appendChild(card);
  });

  $("#resultsCount").textContent =
    `${list.length} restaurant${list.length === 1 ? "" : "s"}`;

  $("#restaurantEmpty").hidden = list.length !== 0;
}

// ---------------- Food Menu ----------------

async function loadMenu(id, scrollToMenu = false) {
  state.selectedRestaurantId = id;
  state.activeCategory = "All";
  renderRestaurants();

  const restaurant = state.restaurants.find(
    item => String(item.id) === String(id)
  );

  $("#restaurantHeading").textContent =
    restaurant ? `${restaurant.name} Menu` : "Restaurant Menu";

  $("#menuCount").textContent = "Loading menu…";
  $("#categories").innerHTML = "";
  $("#menuGrid").innerHTML =
    '<p class="placeholder">Loading food items…</p>';
  $("#menuEmpty").hidden = true;

  try {
    state.foodItems = await apiRequest(
      `${API_BASE_URL}/food-items/restaurant/${encodeURIComponent(id)}`
    );

    renderCategories();
    renderMenu();

    if (scrollToMenu) {
      $("#foodMenuSection").scrollIntoView({
        behavior: "smooth",
        block: "start"
      });
    }
  } catch (error) {
    console.error("Food menu API error:", error);

    $("#menuCount").textContent = "Could not load menu";
    $("#menuGrid").innerHTML =
      '<p class="placeholder">Could not load this menu. Check the food-items API and restaurant ID.</p>';

    showNotice("Could not load this restaurant's menu.");
  }
}

function renderCategories() {
  const element = $("#categories");
  const categories = [
    "All",
    ...new Set(state.foodItems.map(item => item.category).filter(Boolean))
  ];

  element.innerHTML = "";

  categories.forEach(category => {
    const button = document.createElement("button");
    button.type = "button";
    button.className =
      `category-button${category === state.activeCategory ? " active" : ""}`;
    button.textContent = category;

    button.addEventListener("click", () => {
      state.activeCategory = category;
      renderCategories();
      renderMenu();
    });

    element.appendChild(button);
  });
}

function foodEmoji(item) {
  const text = `${item.name || ""} ${item.category || ""}`.toLowerCase();

  if (text.includes("burger")) return "🍔";
  if (text.includes("pizza")) return "🍕";
  if (text.includes("biryani") || text.includes("rice")) return "🍚";
  if (text.includes("pasta") || text.includes("noodle")) return "🍝";
  if (text.includes("sandwich")) return "🥪";
  if (text.includes("salad")) return "🥗";
  if (text.includes("cake") || text.includes("dessert")) return "🍰";

  return "🍽️";
}

function imagePlaceholder(item) {
  const element = document.createElement("div");
  element.className = "food-image-placeholder";
  element.textContent = foodEmoji(item);
  return element;
}

function renderMenu() {
  const grid = $("#menuGrid");
  const query = state.searchTerm.toLowerCase();

  const list = state.foodItems.filter(item =>
    (state.activeCategory === "All" ||
      item.category === state.activeCategory) &&
    `${item.name || ""} ${item.description || ""} ${item.category || ""}`
      .toLowerCase()
      .includes(query)
  );

  grid.innerHTML = "";

  list.forEach(item => {
    const card = document.createElement("article");
    card.className = "food-card";

    const imageWrap = document.createElement("div");
    imageWrap.className = "food-image-wrap";

    if (item.imageUrl) {
      const image = document.createElement("img");
      image.src = item.imageUrl;
      image.alt = item.name || "Food";
      image.loading = "lazy";

      image.onerror = () => image.replaceWith(imagePlaceholder(item));
      imageWrap.appendChild(image);
    } else {
      imageWrap.appendChild(imagePlaceholder(item));
    }

    const details = document.createElement("div");
    details.className = "food-details";

    details.innerHTML = `
      <div class="food-topline">
        <h3>${escapeHTML(item.name || "Food item")}</h3>
        <span class="food-price">₹${Number(item.price || 0).toFixed(2)}</span>
      </div>
      <p>${escapeHTML(item.description || "Freshly prepared and ready to enjoy.")}</p>
      <div class="food-bottom">
        <span class="food-category">${escapeHTML(item.category || "Popular")}</span>
        <button class="add-button" type="button">+ Add to cart</button>
      </div>`;

    details.querySelector("button").addEventListener("click", () => {
      addToCart(item);
    });

    card.append(imageWrap, details);
    grid.appendChild(card);
  });

  $("#menuCount").textContent =
    `${list.length} item${list.length === 1 ? "" : "s"}`;

  $("#menuEmpty").hidden = list.length !== 0;

  if (!state.foodItems.length) {
    grid.innerHTML =
      '<p class="placeholder">No food items are saved for this restaurant yet.</p>';
  }
}

// ---------------- Cart ----------------

function addToCart(item) {
  const key = String(item.id);

  if (state.cart[key]) {
    state.cart[key].quantity++;
  } else {
    state.cart[key] = {
      id: item.id,
      name: item.name,
      price: Number(item.price),
      quantity: 1
    };
  }

  renderCart();
  showNotice(`${item.name} added to your cart.`);
}

function cartLines() {
  return Object.values(state.cart);
}

function totals() {
  const subtotal = cartLines().reduce(
    (sum, item) => sum + item.price * item.quantity,
    0
  );

  const deliveryFee = subtotal > 0 && subtotal < 499 ? 35 : 0;

  return {
    subtotal,
    deliveryFee,
    total: subtotal + deliveryFee
  };
}

function changeQuantity(id, delta) {
  const key = String(id);
  if (!state.cart[key]) return;

  state.cart[key].quantity += delta;

  if (state.cart[key].quantity <= 0) {
    delete state.cart[key];
  }

  renderCart();
}

function renderCart() {
  const lines = cartLines();
  const quantity = lines.reduce((sum, item) => sum + item.quantity, 0);
  const content = $("#cartContent");

  $("#cartCount").textContent = quantity;
  $("#drawerCount").textContent = `(${quantity})`;
  content.innerHTML = "";

  if (!lines.length) {
    content.innerHTML =
      '<p class="cart-empty">Your cart is empty. Add something delicious!</p>';
  }

  lines.forEach(item => {
    const row = document.createElement("div");
    row.className = "cart-item";

    row.innerHTML = `
      <div>
        <div class="cart-item-name">${escapeHTML(item.name)}</div>
        <div class="cart-item-price">₹${item.price.toFixed(2)} each</div>
      </div>
      <strong>₹${(item.price * item.quantity).toFixed(2)}</strong>
      <div class="quantity-controls">
        <button type="button" aria-label="Remove one">−</button>
        <span>${item.quantity}</span>
        <button type="button" aria-label="Add one">+</button>
      </div>`;

    const buttons = row.querySelectorAll("button");

    buttons[0].addEventListener("click", () => {
      changeQuantity(item.id, -1);
    });

    buttons[1].addEventListener("click", () => {
      changeQuantity(item.id, 1);
    });

    content.appendChild(row);
  });

  const amount = totals();

  $("#cartSubtotal").textContent = `₹${amount.subtotal.toFixed(2)}`;
  $("#deliveryFee").textContent =
    amount.subtotal >= 499 ? "Free" : `₹${amount.deliveryFee.toFixed(2)}`;

  $("#checkoutTotal").textContent = `₹${amount.total.toFixed(2)}`;
  $("#checkoutFormTotal").textContent = `₹${amount.total.toFixed(2)}`;
  $("#proceedCheckout").disabled = !lines.length;
}

function openCart() {
  $("#cartOverlay").hidden = false;
  document.body.style.overflow = "hidden";
}

function closeCart() {
  $("#cartOverlay").hidden = true;
  document.body.style.overflow = "";
}

// ---------------- Checkout ----------------

function openCheckout() {
  if (!cartLines().length) {
    return showNotice("Your cart is empty.");
  }

  if (!Auth.isLoggedIn()) {
    closeCart();
    state.afterLogin = openCheckout;
    showNotice("Please log in to place your order.");
    return openAuth("login");
  }

  const user = Auth.get();

  if (user && !$("#customerName").value) {
    $("#customerName").value = user.name || "";
  }

  closeCart();
  $("#checkoutOverlay").hidden = false;
  $("#checkoutFormTotal").textContent = `₹${totals().total.toFixed(2)}`;
  document.body.style.overflow = "hidden";
}

function closeCheckout() {
  $("#checkoutOverlay").hidden = true;
  document.body.style.overflow = "";
}

async function submitOrder(event) {
  event.preventDefault();

  if (!cartLines().length) {
    return showNotice("Your cart is empty.");
  }

  if (!$("#checkoutForm").reportValidity()) return;

  const payload = {
    customerName: $("#customerName").value.trim(),
    phone: $("#phone").value.trim(),
    deliveryAddress: $("#deliveryAddress").value.trim(),
    totalAmount: totals().total,
    items: cartLines().map(item => ({
      foodItemId: Number(item.id),
      quantity: Number(item.quantity)
    }))
  };

  const button = $("#placeOrderButton");
  button.disabled = true;
  button.textContent = "Placing order…";

  try {
    const saved = await apiRequest(`${API_BASE_URL}/orders`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });

    state.cart = {};
    renderCart();
    $("#checkoutForm").reset();
    closeCheckout();

    showNotice(
      `Order #${saved.id} placed! Total ₹${Number(saved.totalAmount).toFixed(2)}`
    );

    console.log("Saved order:", saved);
  } catch (error) {
    console.error("Checkout error:", error);
    showNotice(`Order failed: ${error.message}`);
  } finally {
    button.disabled = false;
    button.textContent = "Place order";
  }
}

// ---------------- Search ----------------

function handleSearch(event) {
  state.searchTerm = event.target.value.trim();
  renderRestaurants();

  if (state.selectedRestaurantId !== null) {
    renderMenu();
  }
}

function clearSearch() {
  state.searchTerm = "";
  $("#searchInput").value = "";
  renderRestaurants();
  renderMenu();
}

// ---------------- Initialisation ----------------

document.addEventListener("DOMContentLoaded", () => {
  renderCart();
  renderUserArea();
  loadRestaurants();
  initAuthUI();

  $("#searchInput").addEventListener("input", handleSearch);
  $("#clearSearch").addEventListener("click", clearSearch);

  $("#openCart").addEventListener("click", openCart);
  $("#closeCart").addEventListener("click", closeCart);
  $("#proceedCheckout").addEventListener("click", openCheckout);
  $("#closeCheckout").addEventListener("click", closeCheckout);
  $("#checkoutForm").addEventListener("submit", submitOrder);

  $("#cartOverlay").addEventListener("click", event => {
    if (event.target === $("#cartOverlay")) closeCart();
  });

  $("#checkoutOverlay").addEventListener("click", event => {
    if (event.target === $("#checkoutOverlay")) closeCheckout();
  });

  $("#exploreLink").addEventListener("click", () => {
    $("#restaurants").scrollIntoView({ behavior: "smooth" });
  });

  $("#popularLink").addEventListener("click", () => {
    $("#popular").scrollIntoView({ behavior: "smooth" });
  });

  $("#homeLink").addEventListener("click", event => {
    event.preventDefault();
    window.scrollTo({ top: 0, behavior: "smooth" });
  });

  $("#menuToggle").addEventListener("click", () => {
    $("#nav").classList.toggle("open");
  });

  document.addEventListener("keydown", event => {
    if (event.key === "Escape") {
      closeCart();
      closeCheckout();
      closeAuth();
      closeOrders();
    }
  });
});

// ---------------- Authentication ----------------

let authMode = "login";

function renderUserArea() {
  const area = $("#userArea");
  const user = Auth.get();

  if (!user) {
    area.innerHTML =
      '<button id="openLogin" class="link-btn" type="button">Log in</button>';

    $("#openLogin").addEventListener("click", () => openAuth("login"));
    return;
  }

  area.innerHTML =
    `<span class="user-name">Hi, ${escapeHTML((user.name || "").split(" ")[0])}</span>` +
    '<button id="myOrdersBtn" class="link-btn" type="button">My orders</button>' +
    (user.role === "ADMIN" ? '<a class="link-btn" href="admin.html">Admin</a>' : "") +
    '<button id="logoutBtn" class="link-btn" type="button">Log out</button>';

  $("#myOrdersBtn").addEventListener("click", openOrders);

  $("#logoutBtn").addEventListener("click", () => {
    Auth.clear();
    renderUserArea();
    closeOrders();
    showNotice("You have been logged out.");
  });
}

function setAuthMode(mode) {
  authMode = mode;
  const register = mode === "register";

  $("#authTitle").textContent = register ? "Create account" : "Log in";
  $("#authSubmit").textContent = register ? "Create account" : "Log in";
  $("#nameField").hidden = !register;

  $("#tabLogin").classList.toggle("active", !register);
  $("#tabRegister").classList.toggle("active", register);

  $("#authPassword").autocomplete =
    register ? "new-password" : "current-password";

  $("#authError").hidden = true;
}

function openAuth(mode = "login") {
  setAuthMode(mode);
  $("#authOverlay").hidden = false;
  document.body.style.overflow = "hidden";
  $("#authEmail").focus();
}

function closeAuth() {
  $("#authOverlay").hidden = true;
  document.body.style.overflow = "";
}

async function submitAuth(event) {
  event.preventDefault();

  const name = $("#authName").value.trim();
  const email = $("#authEmail").value.trim();
  const password = $("#authPassword").value;
  const errorElement = $("#authError");

  errorElement.hidden = true;

  if (authMode === "register" && !name) {
    return showAuthError("Enter your name.");
  }

  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    return showAuthError("Enter a valid email address.");
  }

  if (password.length < 8) {
    return showAuthError("Password must be at least 8 characters.");
  }

  const button = $("#authSubmit");
  button.disabled = true;

  try {
    if (authMode === "register") {
      await registerRequest(API_BASE_URL, name, email, password);
    }

    const user = await loginRequest(API_BASE_URL, email, password);

    $("#authForm").reset();
    closeAuth();
    renderUserArea();
    showNotice(`Welcome, ${user.name}!`);

    if (state.afterLogin) {
      const next = state.afterLogin;
      state.afterLogin = null;
      next();
    }
  } catch (error) {
    showAuthError(error.message);
  } finally {
    button.disabled = false;
  }
}

function showAuthError(message) {
  const errorElement = $("#authError");
  errorElement.textContent = message;
  errorElement.hidden = false;
}

function initAuthUI() {
  $("#authForm").addEventListener("submit", submitAuth);
  $("#tabLogin").addEventListener("click", () => setAuthMode("login"));
  $("#tabRegister").addEventListener("click", () => setAuthMode("register"));
  $("#closeAuth").addEventListener("click", closeAuth);

  $("#authOverlay").addEventListener("click", event => {
    if (event.target === $("#authOverlay")) closeAuth();
  });

  $("#closeOrders").addEventListener("click", closeOrders);

  $("#ordersOverlay").addEventListener("click", event => {
    if (event.target === $("#ordersOverlay")) closeOrders();
  });
}

// ---------------- My Orders ----------------

const STATUS_FLOW = [
  "PENDING",
  "CONFIRMED",
  "PREPARING",
  "OUT_FOR_DELIVERY",
  "DELIVERED"
];

async function openOrders() {
  $("#ordersOverlay").hidden = false;
  document.body.style.overflow = "hidden";

  const box = $("#ordersContent");
  box.innerHTML = '<p class="cart-empty">Loading your orders…</p>';

  try {
    // Correct customer order-history endpoint
    const orders = await apiRequest(
      `${API_BASE_URL}/orders/my-orders`
    );

    if (!orders.length) {
      box.innerHTML =
        '<p class="cart-empty">You have not placed any orders yet.</p>';
      return;
    }

    box.innerHTML = "";

    orders.forEach(order => {
      const step = STATUS_FLOW.indexOf(order.status);
      const card = document.createElement("div");
      card.className = "order-card";

      card.innerHTML = `
        <div class="order-top">
          <b>Order #${order.id}</b>
          <span class="status-pill ${escapeHTML(order.status)}">
            ${escapeHTML(String(order.status).replaceAll("_", " "))}
          </span>
        </div>
        ${
          order.status === "CANCELLED"
            ? ""
            : `<div class="progress">${STATUS_FLOW.map((_, index) =>
                `<i class="${index <= step ? "on" : ""}"></i>`
              ).join("")}</div>`
        }
        <div class="order-lines">
          ${(order.items || []).map(item =>
            `${escapeHTML(item.foodName || "Food item")} × ${Number(item.quantity || 0)}`
          ).join("<br>")}
        </div>
        <div class="order-meta">
          <span>${new Date(order.orderDate).toLocaleString()}</span>
          <b>₹${Number(order.totalAmount).toFixed(2)}</b>
        </div>
        <p class="order-address">
          Delivery address: ${escapeHTML(order.deliveryAddress || "")}
        </p>`;

      if (order.status === "PENDING") {
        const button = document.createElement("button");
        button.className = "cancel-btn";
        button.type = "button";
        button.textContent = "Cancel order";

        button.addEventListener("click", async () => {
          if (!confirm(`Cancel order #${order.id}?`)) return;

          try {
            await apiRequest(
              `${API_BASE_URL}/orders/my/${order.id}/cancel`,
              { method: "PUT" }
            );

            showNotice(`Order #${order.id} cancelled.`);
            openOrders();
          } catch (error) {
            showNotice(error.message);
          }
        });

        card.appendChild(button);
      }

      box.appendChild(card);
    });
  } catch (error) {
    box.innerHTML =
      `<p class="cart-empty">Could not load orders: ${escapeHTML(error.message)}</p>`;
  }
}

function closeOrders() {
  $("#ordersOverlay").hidden = true;
  document.body.style.overflow = "";
}
