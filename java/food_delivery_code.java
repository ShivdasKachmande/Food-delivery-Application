import java.util.*;

// =========================
// FoodItem Class
// =========================
class FoodItem {

    private int id;
    private String name;
    private double price;

    public FoodItem(int id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "FoodItem{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                '}';
    }
}


// =========================
// User Base Class
// =========================
class User {

    private int userId;
    private String username;
    private long contactNo;

    public User(int userId, String username, long contactNo) {
        this.userId = userId;
        this.username = username;
        this.contactNo = contactNo;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public long getContactNo() {
        return contactNo;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", contactNo=" + contactNo +
                '}';
    }
}


// =========================
// Cart Class
// =========================
class Cart {

    private Map<FoodItem, Integer> items;

    public Cart() {
        items = new LinkedHashMap<>();
    }

    // Add or update food quantity
    public void addItem(FoodItem foodItem, int quantity) {

        if (quantity <= 0) {
            System.out.println("Quantity must be greater than 0.");
            return;
        }

        items.put(
                foodItem,
                items.getOrDefault(foodItem, 0) + quantity
        );
    }

    // Remove food item
    public void removeItem(FoodItem foodItem) {
        if (items.remove(foodItem) != null) {
            System.out.println("Food item removed from cart.");
        } else {
            System.out.println("Food item not found in cart.");
        }
    }

    public Map<FoodItem, Integer> getItems() {
        return items;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public double getTotalCost() {

        double total = 0;

        for (Map.Entry<FoodItem, Integer> entry : items.entrySet()) {
            total += entry.getKey().getPrice() * entry.getValue();
        }

        return total;
    }

    public void clear() {
        items.clear();
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append("Cart:\n");

        for (Map.Entry<FoodItem, Integer> entry : items.entrySet()) {

            FoodItem food = entry.getKey();
            int quantity = entry.getValue();

            double cost = food.getPrice() * quantity;

            sb.append("Food Item: ")
                    .append(food.getName())
                    .append(", Quantity: ")
                    .append(quantity)
                    .append(", Cost: Rs. ")
                    .append(cost)
                    .append("\n");
        }

        sb.append("Total Cost: Rs. ")
                .append(getTotalCost());

        return sb.toString();
    }
}


// =========================
// Customer Class
// =========================
class Customer extends User {

    private Cart cart;

    public Customer(int userId, String username, long contactNo) {

        super(userId, username, contactNo);

        cart = new Cart();
    }

    public Cart getCart() {
        return cart;
    }
}


// =========================
// DeliveryPerson Class
// =========================
class DeliveryPerson {

    private int deliveryPersonId;
    private String name;
    private long contactNo;

    public DeliveryPerson(
            int deliveryPersonId,
            String name,
            long contactNo) {

        this.deliveryPersonId = deliveryPersonId;
        this.name = name;
        this.contactNo = contactNo;
    }

    public int getDeliveryPersonId() {
        return deliveryPersonId;
    }

    public String getName() {
        return name;
    }

    public long getContactNo() {
        return contactNo;
    }

    @Override
    public String toString() {

        return "DeliveryPerson{" +
                "id=" + deliveryPersonId +
                ", name='" + name + '\'' +
                ", contactNo=" + contactNo +
                '}';
    }
}


// =========================
// Restaurant Class
// =========================
class Restaurant {

    private int id;
    private String name;

    private List<FoodItem> menu;

    public Restaurant(int id, String name) {

        this.id = id;
        this.name = name;

        menu = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<FoodItem> getMenu() {
        return menu;
    }

    // Add food item
    public void addFoodItem(FoodItem foodItem) {

        menu.add(foodItem);

        System.out.println("Food item added successfully!");
    }

    // Remove food item using ID
    public void removeFoodItem(int foodId) {

        Iterator<FoodItem> iterator = menu.iterator();

        while (iterator.hasNext()) {

            FoodItem food = iterator.next();

            if (food.getId() == foodId) {

                iterator.remove();

                System.out.println(
                        "Food item removed successfully!"
                );

                return;
            }
        }

        System.out.println("Food item not found.");
    }

    // Find food item
    public FoodItem findFoodItem(int foodId) {

        for (FoodItem food : menu) {

            if (food.getId() == foodId) {
                return food;
            }
        }

        return null;
    }

    @Override
    public String toString() {

        return "Restaurant ID: " +
                id +
                ", Name: " +
                name;
    }
}


// =========================
// Order Class
// =========================
class Order {

    private int orderId;

    private Customer customer;

    private Map<FoodItem, Integer> items;

    private String status;

    private DeliveryPerson deliveryPerson;

    private String deliveryAddress;

    public Order(int orderId, Customer customer) {

        this.orderId = orderId;
        this.customer = customer;

        items = new LinkedHashMap<>();

        status = "Pending";
    }

    public int getOrderId() {
        return orderId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Map<FoodItem, Integer> getItems() {
        return items;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public DeliveryPerson getDeliveryPerson() {
        return deliveryPerson;
    }

    public void setDeliveryPerson(
            DeliveryPerson deliveryPerson) {

        this.deliveryPerson = deliveryPerson;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(
            String deliveryAddress) {

        this.deliveryAddress = deliveryAddress;
    }

    // Add food item to order
    public void addItem(
            FoodItem foodItem,
            int quantity) {

        items.put(
                foodItem,
                items.getOrDefault(foodItem, 0) + quantity
        );
    }

    @Override
    public String toString() {

        String deliveryName =
                deliveryPerson == null
                        ? "Not Assigned"
                        : deliveryPerson.getName();

        return "Order{" +
                "orderId=" + orderId +
                ", customer=" +
                customer.getUsername() +
                ", items=" +
                items +
                ", status='" +
                status +
                '\'' +
                ", deliveryPerson=" +
                deliveryName +
                '}';
    }
}


// =========================
// Main Class
// =========================
public class FoodDeliverySystem {

    // Collections
    static List<Restaurant> restaurants =
            new ArrayList<>();

    static List<Customer> customers =
            new ArrayList<>();

    static List<Order> orders =
            new ArrayList<>();

    static List<DeliveryPerson> deliveryPersons =
            new ArrayList<>();

    static int nextOrderId = 1;

    static Scanner sc =
            new Scanner(System.in);


    // =========================================
    // MAIN MENU
    // =========================================
    public static void main(String[] args) {

        while (true) {

            System.out.println("\n==============================");
            System.out.println("     FOOD DELIVERY SYSTEM");
            System.out.println("==============================");

            System.out.println("1. Admin Menu");
            System.out.println("2. Customer Menu");
            System.out.println("3. Exit");

            System.out.print("Choose an option: ");

            int choice = readInt();

            switch (choice) {

                case 1:
                    adminMenu();
                    break;

                case 2:
                    customerMenu();
                    break;

                case 3:
                    System.out.println(
                            "Thank you for using Food Delivery System!"
                    );
                    return;

                default:
                    System.out.println(
                            "Invalid option!"
                    );
            }
        }
    }


    // =========================================
    // ADMIN MENU
    // =========================================
    static void adminMenu() {

        while (true) {

            System.out.println("\n------------------------------");
            System.out.println("         ADMIN MENU");
            System.out.println("------------------------------");

            System.out.println("1. Add Restaurant");
            System.out.println(
                    "2. Add Food Item to Restaurant"
            );
            System.out.println(
                    "3. Remove Food Item from Restaurant"
            );
            System.out.println(
                    "4. View Restaurants and Menus"
            );
            System.out.println("5. View Orders");
            System.out.println("6. Add Delivery Person");
            System.out.println(
                    "7. Assign Delivery Person to Order"
            );
            System.out.println("8. Exit");

            System.out.print("Choose an option: ");

            int choice = readInt();

            switch (choice) {

                case 1:
                    addRestaurant();
                    break;

                case 2:
                    addFoodItem();
                    break;

                case 3:
                    removeFoodItem();
                    break;

                case 4:
                    viewRestaurants();
                    break;

                case 5:
                    viewAllOrders();
                    break;

                case 6:
                    addDeliveryPerson();
                    break;

                case 7:
                    assignDeliveryPerson();
                    break;

                case 8:
                    System.out.println(
                            "Exiting Admin Module"
                    );
                    return;

                default:
                    System.out.println(
                            "Invalid option!"
                    );
            }
        }
    }


    // =========================================
    // ADD RESTAURANT
    // =========================================
    static void addRestaurant() {

        System.out.print("Enter Restaurant ID: ");

        int id = readInt();

        System.out.print("Enter Restaurant Name: ");

        String name = sc.nextLine();

        Restaurant restaurant =
                new Restaurant(id, name);

        restaurants.add(restaurant);

        System.out.println(
                "Restaurant added successfully!"
        );
    }


    // =========================================
    // ADD FOOD ITEM
    // =========================================
    static void addFoodItem() {

        System.out.print(
                "Enter Restaurant ID: "
        );

        int restaurantId = readInt();

        Restaurant restaurant =
                findRestaurant(restaurantId);

        if (restaurant == null) {

            System.out.println(
                    "Restaurant not found!"
            );

            return;
        }

        System.out.print(
                "Enter Food Item ID: "
        );

        int id = readInt();

        System.out.print(
                "Enter Food Item Name: "
        );

        String name = sc.nextLine();

        System.out.print(
                "Enter Food Item Price: "
        );

        double price = readDouble();

        FoodItem food =
                new FoodItem(id, name, price);

        restaurant.addFoodItem(food);
    }


    // =========================================
    // REMOVE FOOD ITEM
    // =========================================
    static void removeFoodItem() {

        System.out.print(
                "Enter Restaurant ID: "
        );

        int restaurantId = readInt();

        Restaurant restaurant =
                findRestaurant(restaurantId);

        if (restaurant == null) {

            System.out.println(
                    "Restaurant not found!"
            );

            return;
        }

        System.out.print(
                "Enter Food Item ID: "
        );

        int foodId = readInt();

        restaurant.removeFoodItem(foodId);
    }


    // =========================================
    // VIEW RESTAURANTS
    // =========================================
    static void viewRestaurants() {

        System.out.println(
                "\nRestaurants and Menus:"
        );

        if (restaurants.isEmpty()) {

            System.out.println(
                    "No restaurants available."
            );

            return;
        }

        for (Restaurant restaurant :
                restaurants) {

            System.out.println(
                    "Restaurant ID: " +
                    restaurant.getId() +
                    ", Name: " +
                    restaurant.getName()
            );

            for (FoodItem food :
                    restaurant.getMenu()) {

                System.out.println(
                        "- Food Item ID: " +
                        food.getId() +
                        ", Name: " +
                        food.getName() +
                        ", Price: Rs. " +
                        food.getPrice()
                );
            }
        }
    }


    // =========================================
    // ADD DELIVERY PERSON
    // =========================================
    static void addDeliveryPerson() {

        System.out.print(
                "Enter Delivery Person ID: "
        );

        int id = readInt();

        System.out.print(
                "Enter Delivery Person Name: "
        );

        String name = sc.nextLine();

        System.out.print(
                "Enter Contact No.: "
        );

        long contact = readLong();

        DeliveryPerson person =
                new DeliveryPerson(
                        id,
                        name,
                        contact
                );

        deliveryPersons.add(person);

        System.out.println(
                "Delivery person added successfully!"
        );
    }


    // =========================================
    // ASSIGN DELIVERY PERSON
    // =========================================
    static void assignDeliveryPerson() {

        System.out.print(
                "Enter Order ID: "
        );

        int orderId = readInt();

        Order order = findOrder(orderId);

        if (order == null) {

            System.out.println(
                    "Order not found!"
            );

            return;
        }

        System.out.print(
                "Enter Delivery Person ID: "
        );

        int personId = readInt();

        DeliveryPerson person =
                findDeliveryPerson(personId);

        if (person == null) {

            System.out.println(
                    "Delivery person not found!"
            );

            return;
        }

        order.setDeliveryPerson(person);

        System.out.println(
                "Delivery person assigned to order successfully!"
        );
    }


    // =========================================
    // VIEW ALL ORDERS
    // =========================================
    static void viewAllOrders() {

        System.out.println("\nOrders:");

        if (orders.isEmpty()) {

            System.out.println(
                    "No orders available."
            );

            return;
        }

        for (Order order : orders) {

            System.out.println(order);
        }
    }


    // =========================================
    // CUSTOMER MENU
    // =========================================
    static void customerMenu() {

        while (true) {

            System.out.println("\n------------------------------");
            System.out.println("       CUSTOMER MENU");
            System.out.println("------------------------------");

            System.out.println("1. Add Customer");
            System.out.println("2. View Food Items");
            System.out.println("3. Add Food to Cart");
            System.out.println("4. View Cart");
            System.out.println("5. Place Order");
            System.out.println("6. View Orders");
            System.out.println("7. Exit");

            System.out.print(
                    "Choose an option: "
            );

            int choice = readInt();

            switch (choice) {

                case 1:
                    addCustomer();
                    break;

                case 2:
                    viewRestaurants();
                    break;

                case 3:
                    addFoodToCart();
                    break;

                case 4:
                    viewCart();
                    break;

                case 5:
                    placeOrder();
                    break;

                case 6:
                    viewCustomerOrders();
                    break;

                case 7:
                    System.out.println(
                            "Exiting Customer Module"
                    );
                    return;

                default:
                    System.out.println(
                            "Invalid option!"
                    );
            }
        }
    }


    // =========================================
    // ADD CUSTOMER
    // =========================================
    static void addCustomer() {

        System.out.print(
                "Enter User ID: "
        );

        int id = readInt();

        System.out.print(
                "Enter Username: "
        );

        String username = sc.nextLine();

        System.out.print(
                "Enter Contact No.: "
        );

        long contact = readLong();

        Customer customer =
                new Customer(
                        id,
                        username,
                        contact
                );

        customers.add(customer);

        System.out.println(
                "Customer created successfully!"
        );
    }


    // =========================================
    // ADD FOOD TO CART
    // =========================================
    static void addFoodToCart() {

        System.out.print(
                "Enter Customer ID: "
        );

        int customerId = readInt();

        Customer customer =
                findCustomer(customerId);

        if (customer == null) {

            System.out.println(
                    "Customer not found!"
            );

            return;
        }

        System.out.print(
                "Enter Restaurant ID: "
        );

        int restaurantId = readInt();

        Restaurant restaurant =
                findRestaurant(restaurantId);

        if (restaurant == null) {

            System.out.println(
                    "Restaurant not found!"
            );

            return;
        }

        System.out.print(
                "Enter Food Item ID: "
        );

        int foodId = readInt();

        FoodItem food =
                restaurant.findFoodItem(foodId);

        if (food == null) {

            System.out.println(
                    "Food item not found!"
            );

            return;
        }

        System.out.print(
                "Enter Quantity: "
        );

        int quantity = readInt();

        customer.getCart().addItem(
                food,
                quantity
        );

        System.out.println(
                "Food item added to cart!"
        );
    }


    // =========================================
    // VIEW CART
    // =========================================
    static void viewCart() {

        System.out.print(
                "Enter Customer ID: "
        );

        int customerId = readInt();

        Customer customer =
                findCustomer(customerId);

        if (customer == null) {

            System.out.println(
                    "Customer not found!"
            );

            return;
        }

        System.out.println(
                customer.getCart()
        );
    }


    // =========================================
    // PLACE ORDER
    // =========================================
    static void placeOrder() {

        System.out.print(
                "Enter Customer ID: "
        );

        int customerId = readInt();

        Customer customer =
                findCustomer(customerId);

        if (customer == null) {

            System.out.println(
                    "Customer not found!"
            );

            return;
        }

        Cart cart = customer.getCart();

        if (cart.isEmpty()) {

            System.out.println(
                    "Cart is empty. Add food first."
            );

            return;
        }

        Order order =
                new Order(
                        nextOrderId++,
                        customer
                );

        // Copy cart items to order
        for (Map.Entry<FoodItem, Integer> entry :
                cart.getItems().entrySet()) {

            order.addItem(
                    entry.getKey(),
                    entry.getValue()
            );
        }

        orders.add(order);

        // Clear cart after placing order
        cart.clear();

        System.out.println(
                "Order placed successfully! " +
                "Your order ID is: " +
                order.getOrderId()
        );
    }


    // =========================================
    // VIEW CUSTOMER ORDERS
    // =========================================
    static void viewCustomerOrders() {

        System.out.print(
                "Enter Customer ID: "
        );

        int customerId = readInt();

        Customer customer =
                findCustomer(customerId);

        if (customer == null) {

            System.out.println(
                    "Customer not found!"
            );

            return;
        }

        System.out.println("\nOrders:");

        boolean found = false;

        for (Order order : orders) {

            if (order.getCustomer()
                    .getUserId() == customerId) {

                System.out.println(order);

                found = true;
            }
        }

        if (!found) {

            System.out.println(
                    "No orders found for this customer."
            );
        }
    }


    // =========================================
    // FIND RESTAURANT
    // =========================================
    static Restaurant findRestaurant(int id) {

        for (Restaurant restaurant :
                restaurants) {

            if (restaurant.getId() == id) {

                return restaurant;
            }
        }

        return null;
    }


    // =========================================
    // FIND CUSTOMER
    // =========================================
    static Customer findCustomer(int id) {

        for (Customer customer :
                customers) {

            if (customer.getUserId() == id) {

                return customer;
            }
        }

        return null;
    }


    // =========================================
    // FIND ORDER
    // =========================================
    static Order findOrder(int id) {

        for (Order order : orders) {

            if (order.getOrderId() == id) {

                return order;
            }
        }

        return null;
    }


    // =========================================
    // FIND DELIVERY PERSON
    // =========================================
    static DeliveryPerson findDeliveryPerson(
            int id) {

        for (DeliveryPerson person :
                deliveryPersons) {

            if (person.getDeliveryPersonId() == id) {

                return person;
            }
        }

        return null;
    }


    // =========================================
    // INPUT METHODS
    // =========================================

    static int readInt() {

        while (true) {

            try {

                int value =
                        sc.nextInt();

                sc.nextLine();

                return value;

            } catch (InputMismatchException e) {

                System.out.println(
                        "Please enter a valid number."
                );

                sc.nextLine();
            }
        }
    }


    static long readLong() {

        while (true) {

            try {

                long value =
                        sc.nextLong();

                sc.nextLine();

                return value;

            } catch (InputMismatchException e) {

                System.out.println(
                        "Please enter a valid contact number."
                );

                sc.nextLine();
            }
        }
    }


    static double readDouble() {

        while (true) {

            try {

                double value =
                        sc.nextDouble();

                sc.nextLine();

                return value;

            } catch (InputMismatchException e) {

                System.out.println(
                        "Please enter a valid price."
                );

                sc.nextLine();
            }
        }
    }
}