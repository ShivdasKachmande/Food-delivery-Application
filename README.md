# Crave — Food Delivery Application

## Overview
Crave is a full-stack food delivery web application that allows customers to explore restaurants, browse food items, manage a cart, place orders, and track order statuses. Administrators can manage restaurants, food items, and customer orders.

## Features

### Customer
- Browse restaurants and food items
- Search for food and restaurants
- Add items to the cart and manage quantities
- Place food orders
- Register and log in
- View personal order history
- Cancel eligible orders
- View order status updates

### Administrator
- Secure administrator login
- Manage restaurants
- Manage food items
- View customer orders
- Update order statuses

### Backend and Security
- REST API built with Spring Boot
- MySQL database persistence
- JWT-based authentication
- Role-based access control
- Server-side order validation and price calculation

## Technology Stack
- Frontend: HTML, CSS, JavaScript
- Backend: Java, Spring Boot, Maven
- Database: MySQL
- Authentication: JWT
- API testing: PowerShell or an API client
- Version control: Git and GitHub

## Project Structure

```text
Food Delivery Application/
├── backend/
│   ├── mvnw.cmd
│   ├── pom.xml
│   └── src/
├── frontend/
│   ├── index.html
│   ├── style.css
│   ├── script.js
│   ├── admin.html
│   ├── admin.css
│   ├── admin.js
│   └── auth.js
└── README.md
```

## Prerequisites
- Java JDK
- Maven wrapper included with the backend
- MySQL Server
- A modern web browser
- Visual Studio Code or another code editor

## Database Setup

1. Start MySQL Server.
2. Create the application database:

```sql
CREATE DATABASE food_delivery_db;
```

3. Configure the database URL, username, and password in the backend configuration.
4. Start the backend and verify that the required tables are created or available.

Never commit database passwords, JWT secrets, or customer credentials to GitHub.

## Run the Backend

Open PowerShell in the backend directory:

```powershell
.\mvnw.cmd spring-boot:run
```

The backend is expected to run at:

```text
http://localhost:8080
```

## Run the Frontend

Open the frontend folder in Visual Studio Code and launch `index.html` using Live Server.

The frontend currently uses:

```text
http://localhost:8080/api
```

as its backend API base URL.

## Main API Routes

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/restaurants` | List restaurants |
| GET | `/api/food-items` | List food items |
| POST | `/api/auth/register` | Register a customer |
| POST | `/api/auth/login` | Log in |
| POST | `/api/orders` | Place an order |
| GET | `/api/orders/my-orders` | View customer orders |
| PUT | `/api/orders/my/{id}/cancel` | Cancel an eligible customer order |
| GET | `/api/orders` | View all orders (admin) |

Restaurant, food item, and order-management endpoints may require administrator authentication.

## Testing
The project has been tested for order persistence, order-item records, customer order history, order cancellation, administrator status updates, server-side price calculation, and invalid input handling.

Complete the final regression and security checks before describing all tests as passed.

## Future Enhancements
- Online payment integration
- Email or SMS notifications
- Restaurant ratings and reviews
- Delivery partner tracking
- Deployment to a cloud platform

## Author
Shivdas Kachmande
