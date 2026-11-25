# A2Z – Smart E-Commerce Order & Inventory Management System

**Everything from A to Z**

A comprehensive desktop-based e-commerce management system developed using Java, JavaFX, JDBC and MySQL. A2Z integrates online shopping, inventory management, order processing, supplier management, product recommendations and sales analytics into a unified application.

## Project Overview

A2Z is designed to simplify e-commerce operations by providing two separate interfaces for administrators and customers.

Administrators can manage products, inventory, suppliers, customers and orders, while customers can browse products, receive personalized recommendations, manage their shopping carts, place orders, track purchases and generate invoices.

## Project Objectives

- Develop a professional and user-friendly e-commerce application.
- Implement object-oriented programming concepts using Java.
- Provide role-based authentication and access control.
- Integrate JavaFX graphical interfaces with MySQL using JDBC.
- Automate inventory updates and order processing.
- Provide personalized product recommendations.
- Generate meaningful sales and inventory reports.
- Maintain database consistency using transactions.

## Technologies Used

Technology        | Purpose 
Java              | Application development and business logic 
JavaFX            | Graphical user interface 
CSS               | Interface styling 
JDBC              | Database connectivity 
MySQL             | Relational database management 
Maven             | Dependency management and application execution 
BCrypt            | Password hashing 
VS Code           | Development environment 
Git and GitHub    | Version control 

## Key Features

### Admin Module

- Secure administrator login
- Interactive dashboard with statistics and charts
- Product addition, editing and activation management
- Product image URL management
- Category-based product organization
- Inventory stock-in and stock-out operations
- Low-stock and out-of-stock alerts
- Inventory transaction history
- Supplier management and product assignments
- Customer management and purchase history
- Order confirmation, shipment and delivery management
- Order cancellation and automatic stock restoration
- Sales analytics and revenue reports
- Best-selling product analysis

### Customer Module

- Customer registration and secure login
- Professional shopping dashboard
- Product search, filtering and sorting
- Category-based product browsing
- Personalized product recommendations
- Detailed product information
- Shopping cart management
- Real-time stock validation during cart operations
- Delivery address validation
- Checkout and order placement
- Simulated UPI and card payments
- Cash on delivery
- Order confirmation
- Order history and status tracking
- Invoice generation
- HTML and TXT invoice export
- Invoice printing

## Smart Recommendation System

The application includes a rule-based product recommendation system that considers:

- Customer purchase history
- Preferred product categories
- Product popularity
- Previously purchased products
- Current product availability

Recommendations are generated using SQL queries based on completed purchases and available inventory.

This approach provides personalized shopping suggestions without requiring an external machine learning model.

## System Architecture

```text
             ADMIN / CUSTOMER
                     |
                     v
            JavaFX User Interface
                     |
                     v
              Java Service Layer
                     |
                     v
                   JDBC
                     |
                     v
               MySQL Database
```

The application follows a layered structure that separates the user interface, business logic, data models and database operations.

## Database Design

The application uses a relational MySQL database named `a2z_db`.

 Table                   | Description

 users                   | Administrator and customer account information
 categories              | Product categories
 products                | Product details, prices and stock
 suppliers               | Supplier information
 product_suppliers       | Product and supplier relationships
 cart_items              | Customer shopping cart items
 orders                  | Order and payment information
 order_items             | Individual products purchased in each order
 inventory_transactions  | Stock movement and transaction history

## Application Workflow

```text
Customer Registration / Login
             |
             v
       Browse Products
             |
             v
        Add to Cart
             |
             v
     Delivery Information
             |
             v
      Payment Selection
             |
             v
       Order Placement
             |
             v
   Automatic Stock Deduction
             |
             v
     Admin Order Processing
             |
             v
       Order Tracking
             |
             v
       Invoice Generation
```

## Order Management Lifecycle

```text
PLACED --> CONFIRMED --> SHIPPED --> DELIVERED
   |           |
   +-----------+
         |
         v
     CANCELLED
```

Orders can be cancelled before shipment. When an eligible order is cancelled, the purchased quantities are restored to inventory and the corresponding inventory transactions are recorded.

## Inventory Management

The inventory module maintains product stock and records stock movements.

- Stock IN increases the available quantity.
- Stock OUT decreases the available quantity.
- Order placement automatically deducts purchased quantities.
- Eligible order cancellation restores stock.
- Minimum stock thresholds identify low-stock products.
- Inventory transactions maintain a history of stock changes.
- Database transactions help prevent inconsistent stock updates.

## Java Concepts Implemented

- Classes and objects
- Encapsulation
- Constructors
- Methods
- Collections
- Exception handling
- Event handling
- Lambda expressions
- JavaFX controls and layouts
- JDBC connectivity
- Prepared statements
- Database transactions
- File handling
- Object-oriented programming

## Security and Validation

- Role-based authentication
- BCrypt password hashing
- Parameterized SQL queries using PreparedStatement
- Customer-specific order and invoice access
- Input validation
- Email and phone number validation
- Password confirmation
- Delivery address format validation
- Product stock validation
- Transaction commit and rollback
- Database password configuration through an environment variable

## User Interface

The application features a consistent JavaFX interface with:

- Dark charcoal navigation panels
- Deep teal primary actions
- Professional dashboard cards
- Interactive charts
- Product image previews
- Search and filtering controls
- Order status indicators
- Confirmation dialogs
- Success, warning and error messages
- Empty states and image fallbacks
- Professional invoice layouts

## Project Structure

```text
A2Z/
|
|-- database/
|   |-- a2z.sql
|
|-- src/
|   |-- main/
|       |-- java/
|       |   |-- Main.java
|       |   |-- DBConnection.java
|       |   |-- UIComponents.java
|       |   |-- LoginPage.java
|       |   |-- RegisterPage.java
|       |   |-- AuthService.java
|       |   |-- AdminDashboard.java
|       |   |-- CustomerDashboard.java
|       |   |-- ProductManagement.java
|       |   |-- InventoryManagement.java
|       |   |-- OrderManagement.java
|       |   |-- CustomerManagement.java
|       |   |-- SupplierManagement.java
|       |   |-- ShoppingCart.java
|       |   |-- CheckoutPage.java
|       |   |-- PaymentPage.java
|       |   |-- RecommendationService.java
|       |   |-- OrderTracking.java
|       |   |-- InvoicePage.java
|       |   |-- Reports.java
|       |   |-- Other models and services
|       |
|       |-- resources/
|           |-- styles/
|               |-- a2z-javafx.css
|
|-- .gitignore
|-- pom.xml
|-- README.md
```

## Installation and Setup

### Prerequisites

Ensure the following software is installed:

- Java Development Kit compatible with the Maven configuration
- Apache Maven
- MySQL Server
- MySQL Workbench or MySQL command-line client
- Git
- Visual Studio Code or another Java IDE

### Step 1: Clone the Repository

```bash
git clone https://github.com/jahnavi-153/A2Z-Ecommerce-Management-System.git
```

```bash
cd A2Z-Ecommerce-Management-System
```

### Step 2: Configure the Database

1. Open MySQL Workbench.
2. Connect to your local MySQL server.
3. Open the `database/a2z.sql` file.
4. Execute the SQL script to initialize the database.
5. Verify the database connection settings in `DBConnection.java`.

### Step 3: Configure the Database Password

In Windows Command Prompt, execute:

```cmd
set "A2Z_DB_PASSWORD=your_mysql_password"
```

Replace `your_mysql_password` with your local MySQL password.

Never commit actual database credentials to GitHub.

### Step 4: Compile the Project

```bash
mvn clean compile
```

### Step 5: Run the Application

```bash
mvn clean javafx:run
```

### Step 6: Access the Application

- Use an administrator account configured in the local database to access the Admin module.
- Create a new customer account using the registration page to access the Customer module.

## Real-World Applications

The concepts implemented in A2Z can be applied to:

- E-commerce platforms
- Retail management systems
- Warehouse inventory systems
- Supermarket management
- Electronics stores
- Supplier management platforms
- Order processing systems

## Advantages

- Centralized product and inventory management
- Reduced manual stock management
- Improved order processing
- Personalized shopping experience
- Organized customer and supplier information
- Better sales and inventory visibility
- Consistent and reusable user interface components

## Current Limitations

- Payments are simulated for academic demonstration.
- Delivery tracking is based on order status rather than live GPS tracking.
- Address validation checks input format rather than geographic deliverability.
- Product recommendations are rule-based.
- The application is desktop-based.
- Online product images require internet connectivity.
- Generated invoices are intended for academic demonstration.

## Future Enhancements

- Integration with real payment gateways
- Live shipment and delivery tracking
- Machine learning-based recommendations
- Cloud database deployment
- Mobile and web application support
- Email and SMS notifications
- Advanced sales forecasting
- Customer reviews and ratings
- Discount and coupon management
- Automated supplier purchase orders

## Project Purpose

This project demonstrates the practical implementation of Java programming, object-oriented concepts, JavaFX graphical interfaces, event-driven programming, JDBC connectivity and relational database management in a real-world e-commerce scenario.

## Conclusion

A2Z combines essential e-commerce and inventory management operations into a single desktop application. It provides separate administrator and customer interfaces, structured database operations, personalized product recommendations, order management and sales analytics.

The project demonstrates how Java and MySQL can be integrated to develop a comprehensive business management application.

---

**A2Z – Everything from A to Z**

Developed as an academic Java application project.