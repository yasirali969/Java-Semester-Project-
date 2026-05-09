# Java-Semester-Project-
A Java-based semester project developed to demonstrate core programming concepts, GUI design, and problem-solving skills using Java. This project showcases object-oriented programming, event handling, and user interaction features.


#  Smart Shop Management System

##  Group Members

* Yasir Ali (CMS: 023-25-0188) 
* Muhammad Zeeshan (CMS: 023-25-0190)
* Section C

---

##  Project Description

Smart Shop is a **Java Swing-based desktop application** that simulates a complete shopping system.
It provides two main roles:

* **Admin Panel** → Manage products (Add, Update, Delete)
* **User Panel** → Browse products, add to cart, and checkout

The system uses **MySQL database** for storing users, products, and order history.

---

##  Features

###  Authentication System

* User Login
* User Registration
* Admin Login

###  Admin Panel

* Add new products
* Update existing products
* Delete products
* View all products

###  User Panel

* View products
* Add items to cart
* Checkout system

###  Checkout System

* Generates receipt
* Updates stock in database
* Saves order history

---

##  OOP Concepts Used

* **Classes & Objects**
* **Encapsulation**
* **Inheritance** (`Cloth extends Product`)
* **Abstraction** (Abstract class `Product`)
* **Interface** (`Purchasable`)
* **Composition** (`ProductDetail`)
* **Arrays for data handling**

---

##  Technologies Used

* Java (JDK 17+)
* Java Swing (GUI)
* MySQL Database
* JDBC (MySQL Connector)

---

##  How to Run the Project

###  1. Requirements

* Install Java JDK (17 or above)
* Install MySQL Server

---

###  2. Database Setup

Open MySQL and run:

```sql
CREATE DATABASE SmartShop;
```

Then create tables:

```sql
CREATE TABLE Users (
    username VARCHAR(50),
    password VARCHAR(50)
);

CREATE TABLE Products (
    id INT PRIMARY KEY,
    name VARCHAR(100),
    type VARCHAR(50),
    price DOUBLE,
    qty INT
);

CREATE TABLE ProductDetails (
    product_id INT,
    brand VARCHAR(50),
    size VARCHAR(20),
    color VARCHAR(20),
    material VARCHAR(50)
);

CREATE TABLE OrderHistory (
    username VARCHAR(50),
    total_amount DOUBLE
);
```

---

###  3. Update Database Credentials

Open `ShoppingHall.java` and update:

```java
"root", "YOUR_PASSWORD"
```

---

###  4. Compile

```bash
javac ShoppingHall.java
```

---

### 🔹 5. Run

```bash
java SmartShopGUI
```

##  Default Admin Login

Username: `Admin`
Password: `123`

---

##  Demo Video

 **Watch our project demo here:**
https://youtu.be/2R1uzbotPL0
### Video Includes:

* Project overview
* Explanation of OOP concepts
* Admin panel demo
* User shopping and checkout
* All group members presenting

---

##  Notes

* Make sure MySQL server is running
* Update DB username/password before running
* If database does not connect, check credentials and MySQL service

---

##  Conclusion

This project demonstrates a **complete Object-Oriented Java application** using GUI and database integration, simulating a real-world shopping system with admin and user functionalities.

---
