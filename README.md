# B2LShoes – Secure E-commerce Web Application

B2LShoes is a secure e-commerce web application for selling sports shoes online.  
The project focuses on building a complete shopping experience for customers and an admin management system, while applying web security practices to reduce common risks such as XSS and CSRF attacks.

## Overview

This project was developed as a team project for the **Internet and Web Technology** course at the University of Information Technology, VNU-HCM.

The system supports core e-commerce workflows such as product browsing, product filtering, cart management, voucher application, checkout, order tracking, and admin management. In addition, the project applies security mechanisms including input validation, output encoding, CSRF protection, email OTP, and backend-side checkout total recalculation.

## Key Features

### Customer Features

- Register, log in, remember login, and reset password via email OTP
- Browse products and view product details
- Search and filter products by brand, sport type, size, color, and price range
- Add products to cart, update quantity, and remove items
- Apply vouchers during checkout
- Place orders using COD or online bank transfer/VNPay flow
- View order history and order status
- Manage personal information and shipping address
- Add products to wishlist
- Submit product reviews

### Admin Features

- Admin dashboard with business overview
- Manage products, product images, product variants, sizes, colors, brands, and categories
- Manage orders and update delivery status
- Manage vouchers and promotion programs
- Manage customers and membership levels
- Receive system notifications for new orders or customer requests

### Security Features

- Spring Security authentication and authorization
- Role-based access control for customer and admin pages
- CSRF protection for state-changing requests
- XSS prevention using input validation and output-safe rendering
- Email OTP for sensitive actions such as password recovery
- Backend-side recalculation of checkout totals to reduce client-side price manipulation
- Password hashing before storing user credentials
- Remember-me token mechanism for improved user experience

## Tech Stack

### Backend

- Java
- Spring Boot
- Spring MVC
- Spring Security
- Spring Data JPA
- Hibernate
- Maven

### Frontend

- Thymeleaf
- Tailwind CSS
- HTML
- CSS
- JavaScript
- AJAX

### Database and Storage

- MySQL
- Redis
- Database Triggers
- Indexing

### Tools

- Git / GitHub
- IntelliJ IDEA
- VS Code
- Postman
- MySQL Workbench

## Architecture

The project follows an MVC and layered architecture to separate responsibilities clearly:

```text
Controller  ->  Service  ->  DAO / Repository  ->  Model / Entity  ->  Database
View        ->  Thymeleaf templates + Tailwind CSS + JavaScript / AJAX
```

### Main Layers

- **Controller:** Handles HTTP requests, page routing, and API/AJAX interactions.
- **Service:** Contains business logic such as checkout calculation, voucher validation, order handling, and authentication flow.
- **DAO / Repository:** Handles database access and query logic.
- **Model / Entity:** Represents database entities such as users, products, product variants, carts, orders, vouchers, reviews, and notifications.
- **View:** Uses Thymeleaf and Tailwind CSS to render dynamic web pages.

## My Contributions

As a Java Web Developer Contributor, I was responsible for:

- Designing and implementing the backend system using Spring Boot
- Developing customer-facing pages such as home page, product list, product detail, support pages, login, register, account information, order history, voucher wallet, and checkout
- Developing admin pages such as dashboard, product management, and order management
- Integrating API/AJAX calls to fetch and process backend data
- Handling user interactions and client-side validation
- Building system analysis artifacts including requirements, use cases, sequence diagrams, and ERD
- Supporting secure e-commerce logic such as OTP, checkout validation, CSRF protection, and XSS-safe handling

## Main Modules

```text
Authentication & Authorization
Product Management
Product Variant Management
Cart Management
Checkout & Order Processing
Voucher Management
Wishlist
Product Reviews
Customer Account Management
Admin Dashboard
Notification System
Security Modules
```

## Database Design

The database is designed around the main e-commerce entities:

- User
- Product
- Brand
- Category
- Color
- Sport
- Master Size
- Product Group
- Product Variant
- Product Image
- Cart Item
- Order
- Order Detail
- Voucher
- Wishlist
- Product Review
- Notification
- Remember-me Token

The system also uses database triggers for business automation, such as stock synchronization and stock restoration when an order is cancelled.

## Screenshots

### Customer Pages

| Home Page | Product List |
|---|---|
| <img src="docs/screenshots/home-page.png" alt="B2LShoes Home Page" width="420"> | <img src="docs/screenshots/product-list.png" alt="B2LShoes Product List" width="420"> |

| Product Detail | Add to Cart |
|---|---|
| <img src="docs/screenshots/product-detail.png" alt="B2LShoes Product Detail" width="420"> | <img src="docs/screenshots/add-to-cart.png" alt="B2LShoes Add to Cart" width="420"> |

| Cart Page | Checkout Page |
|---|---|
| <img src="docs/screenshots/cart-page.png" alt="B2LShoes Cart Page" width="420"> | <img src="docs/screenshots/checkout-page.png" alt="B2LShoes Checkout Page" width="420"> |

| Account Information | Order History |
|---|---|
| <img src="docs/screenshots/account-info.png" alt="B2LShoes Account Information" width="420"> | <img src="docs/screenshots/order-history.png" alt="B2LShoes Order History" width="420"> |

| Voucher Wallet | Wishlist |
|---|---|
| <img src="docs/screenshots/voucher-wallet.png" alt="B2LShoes Voucher Wallet" width="420"> | <img src="docs/screenshots/wishlist-page.png" alt="B2LShoes Wishlist" width="420"> |

### Payment Flow

| Payment Method | QR Payment |
|---|---|
| <img src="docs/screenshots/payment-method.png" alt="B2LShoes Payment Method" width="420"> | <img src="docs/screenshots/qr-payment.png" alt="B2LShoes QR Payment" width="420"> |

| Bank Transfer |
|---|
| <img src="docs/screenshots/bank-transfer.png" alt="B2LShoes Bank Transfer" width="500"> |

### Admin Pages

| Admin Login | Admin Dashboard |
|---|---|
| <img src="docs/screenshots/admin-login.png" alt="B2LShoes Admin Login" width="420"> | <img src="docs/screenshots/admin-dashboard.png" alt="B2LShoes Admin Dashboard" width="420"> |

| Product Management | Add Product |
|---|---|
| <img src="docs/screenshots/admin-product-list.png" alt="B2LShoes Admin Product List" width="420"> | <img src="docs/screenshots/admin-add-product.png" alt="B2LShoes Admin Add Product" width="420"> |

| Order Management | Order Detail |
|---|---|
| <img src="docs/screenshots/admin-order-list.png" alt="B2LShoes Admin Order List" width="420"> | <img src="docs/screenshots/admin-order-detail.png" alt="B2LShoes Admin Order Detail" width="420"> |

| Voucher Management | Customer Management |
|---|---|
| <img src="docs/screenshots/admin-voucher-list.png" alt="B2LShoes Admin Voucher List" width="420"> | <img src="docs/screenshots/admin-customer-list.png" alt="B2LShoes Admin Customer List" width="420"> |

<details>
<summary><strong>More Screenshots</strong></summary>

### Authentication

| Register | Login |
|---|---|
| <img src="docs/screenshots/register-page.png" alt="B2LShoes Register Page" width="420"> | <img src="docs/screenshots/login-page.png" alt="B2LShoes Login Page" width="420"> |

### Support Pages

| Buying Guide | Size Guide |
|---|---|
| <img src="docs/screenshots/buying-guide.png" alt="B2LShoes Buying Guide" width="420"> | <img src="docs/screenshots/size-guide.png" alt="B2LShoes Size Guide" width="420"> |

| Return Policy | Terms of Use |
|---|---|
| <img src="docs/screenshots/return-policy.png" alt="B2LShoes Return Policy" width="420"> | <img src="docs/screenshots/terms-of-use.png" alt="B2LShoes Terms of Use" width="420"> |

| Privacy Policy | About Us |
|---|---|
| <img src="docs/screenshots/privacy-policy.png" alt="B2LShoes Privacy Policy" width="420"> | <img src="docs/screenshots/about-us.png" alt="B2LShoes About Us" width="420"> |

### Analysis and Design

| Use Case Diagram | ERD |
|---|---|
| <img src="docs/screenshots/use-case-diagram.png" alt="B2LShoes Use Case Diagram" width="420"> | <img src="docs/screenshots/erd.png" alt="B2LShoes ERD" width="420"> |

| Order & Payment Sequence | OTP Sequence |
|---|---|
| <img src="docs/screenshots/sequence-order-payment.png" alt="B2LShoes Order Payment Sequence" width="420"> | <img src="docs/screenshots/sequence-register-otp.png" alt="B2LShoes Register OTP Sequence" width="420"> |

</details>

## Getting Started

### Prerequisites

Make sure you have the following installed:

- Java 17 or later
- Maven
- MySQL
- Redis
- Git

### Installation

Clone the repository:

```bash
git clone https://github.com/ngoclan-nguyen/B2LShoesProject.git
cd B2LShoesProject
```

Create a MySQL database:

```sql
CREATE DATABASE b2lshoes CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Configure database connection in `application.properties` or `application.yml`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/b2lshoes
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

Configure Redis if the project uses Redis locally:

```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

Configure email settings for OTP:

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

Run the application:

```bash
mvn spring-boot:run
```

Open the application in your browser:

```text
http://localhost:8080
```

## Suggested Test Accounts

> Update this section with your actual seeded accounts if available.

```text
Customer account:
Email: customer@example.com
Password: 12345678

Admin account:
Email: admin@example.com
Password: 12345678
```

## Project Highlights

- Built a complete e-commerce workflow from product browsing to checkout and order tracking
- Implemented both customer and admin features
- Applied Spring Security for authentication and authorization
- Integrated secure handling for OTP, CSRF, XSS, and checkout validation
- Used MVC and layered architecture to improve maintainability
- Designed database structure with product variants, vouchers, orders, reviews, wishlist, and notifications
- Connected dynamic backend data with Thymeleaf views using AJAX

## Limitations

- Product search is still rule-based and does not include AI-powered recommendation.
- Admin reports are basic and can be expanded with more detailed analytics.
- The system does not yet include live chat or chatbot support.
- Some advanced security risks such as SQL Injection hardening and broken access control testing can be improved further.

## Future Improvements

- Add AI-based product recommendation based on user behavior
- Add chatbot or live chat support for customers
- Improve admin analytics and exportable reports
- Add advanced product search and sorting
- Add automated testing for services, controllers, and security flows
- Improve deployment using Docker and CI/CD pipeline

## Team Members

- Phan Duc Chi Bao
- Nguyen Hung Tuan Lam
- Nguyen Thi Ngoc Lan

## Author

**Nguyen Thi Ngoc Lan**  
GitHub: [ngoclan-nguyen](https://github.com/ngoclan-nguyen)

## License

This project is developed for educational purposes.
