## WHAT IS SOLID ?
SOLID principles are a set of five object-oriented design guidelines to make software designs more **understandable**, **flexible** and **maintainable**. 
Adhering to these principles prevents code rot, reduces tight coupling, and minimizes bugs when a codebase scales.
```
SOLID consists of five principles:
S - Single Responsibility Principle
O - Open Closed Principle
L - Liskov Substitution Principle
I - Interface Segregation Principle
D - Dependency Inversion Principle
```
---

### 1. Single Responsibility Principle (SRP)
> "A class should have one, and only one, reason to change."

A Java class must focus on a single, well-defined purpose. If a class handles multiple concerns, a change in one concern may break the other — increasing fragility and coupling.

**Violation:**
```java
class UserService {
    public void saveUser(User user) { /* DB logic */ }
    public void sendWelcomeEmail(User user) { /* Email logic */ }
    public String generateReport(User user) { /* Report logic */ }
}
```
This class changes whenever DB logic, email logic, or report format changes.

**Correct:**
```java
class UserRepository {
    public void save(User user) { /* DB logic only */ }
}

class EmailService {
    public void sendWelcomeEmail(User user) { /* Email logic only */ }
}

class UserReportService {
    public String generateReport(User user) { /* Report logic only */ }
}
```
Each class has exactly one reason to change.

---

### 2. Open/Closed Principle (OCP)
> "Software entities should be open for extension, but closed for modification."

You should be able to add new behavior without changing existing, tested code. Achieve this via abstraction (interfaces/abstract classes).

**Violation:**
```java
class DiscountCalculator {
    public double calculate(String customerType, double price) {
        if (customerType.equals("VIP")) return price * 0.8;
        if (customerType.equals("MEMBER")) return price * 0.9;
        return price; // Adding new type requires modifying this class
    }
}
```

**Correct:**
```java
interface DiscountPolicy {
    double apply(double price);
}

class VipDiscount implements DiscountPolicy {
    public double apply(double price) { return price * 0.8; }
}

class MemberDiscount implements DiscountPolicy {
    public double apply(double price) { return price * 0.9; }
}

class NoDiscount implements DiscountPolicy {
    public double apply(double price) { return price; }
}

class DiscountCalculator {
    public double calculate(DiscountPolicy policy, double price) {
        return policy.apply(price); // Never needs modification
    }
}
```
Adding a new discount type means adding a new class — no existing code changes.

---

### 3. Liskov Substitution Principle (LSP)
> "Objects of a subclass should be replaceable with objects of its superclass without altering the correctness of the program."

A subclass must honor the contract (behavior) of its parent. If substituting a child breaks the program, the hierarchy is wrong.

**Violation:**
```java
class Rectangle {
    protected int width, height;
    public void setWidth(int w) { this.width = w; }
    public void setHeight(int h) { this.height = h; }
    public int area() { return width * height; }
}

class Square extends Rectangle {
    @Override
    public void setWidth(int w) { this.width = w; this.height = w; } // Breaks parent contract
    @Override
    public void setHeight(int h) { this.width = h; this.height = h; }
}

// Caller expects Rectangle behavior — Square silently breaks it
Rectangle r = new Square();
r.setWidth(5);
r.setHeight(3);
System.out.println(r.area()); // Expected 15, got 9 — LSP violated
```

**Correct:**
```java
interface Shape {
    int area();
}

class Rectangle implements Shape {
    private int width, height;
    public Rectangle(int w, int h) { this.width = w; this.height = h; }
    public int area() { return width * height; }
}

class Square implements Shape {
    private int side;
    public Square(int side) { this.side = side; }
    public int area() { return side * side; }
}
```
Neither class pretends to be the other — substitution is safe.

---

### 4. Interface Segregation Principle (ISP)
> "Clients should not be forced to depend on interfaces they do not use."

Large, fat interfaces force implementing classes to provide methods they don't need (often as empty stubs or `UnsupportedOperationException`). Split them into focused interfaces.

**Violation:**
```java
interface Worker {
    void work();
    void eat();
    void sleep();
}

class Robot implements Worker {
    public void work() { /* OK */ }
    public void eat() { throw new UnsupportedOperationException(); } // Forced, meaningless
    public void sleep() { throw new UnsupportedOperationException(); }
}
```

**Correct:**
```java
interface Workable {
    void work();
}

interface Restable {
    void eat();
    void sleep();
}

class Human implements Workable, Restable {
    public void work() { /* works */ }
    public void eat() { /* eats */ }
    public void sleep() { /* sleeps */ }
}

class Robot implements Workable {
    public void work() { /* works */ }
    // No forced methods
}
```
Each class only implements what is relevant to it.

---

### 5. Dependency Inversion Principle (DIP)
> "High-level modules should not depend on low-level modules. Both should depend on abstractions."
> "Abstractions should not depend on details. Details should depend on abstractions."

Instead of high-level business logic depending on concrete implementations, both sides depend on an interface. This makes the system easy to test and swap implementations.

**Violation:**
```java
class MySQLDatabase {
    public void save(String data) { /* MySQL-specific logic */ }
}

class UserService {
    private MySQLDatabase db = new MySQLDatabase(); // Hard dependency on concrete class

    public void registerUser(String name) {
        db.save(name); // Impossible to swap DB without changing UserService
    }
}
```

**Correct:**
```java
interface Database {
    void save(String data);
}

class MySQLDatabase implements Database {
    public void save(String data) { /* MySQL logic */ }
}

class MongoDatabase implements Database {
    public void save(String data) { /* MongoDB logic */ }
}

class UserService {
    private final Database db; // Depends on abstraction

    public UserService(Database db) { // Injected — high-level module is decoupled
        this.db = db;
    }

    public void registerUser(String name) {
        db.save(name);
    }
}

// Usage
UserService service = new UserService(new MySQLDatabase());
// Swap: new UserService(new MongoDatabase()) — no UserService change needed
```

---

## Summary Table

| Principle | Core Idea | Key Mechanism |
|-----------|-----------|---------------|
| **SRP** | One class, one reason to change | Split classes by concern |
| **OCP** | Extend without modifying | Interfaces / abstract classes |
| **LSP** | Subtypes are safe substitutes | Respect parent contracts |
| **ISP** | No forced unused methods | Small, focused interfaces |
| **DIP** | Depend on abstractions, not concretions | Constructor injection / interfaces |

> SOLID is not a checklist — it is a set of trade-offs. Apply them where they reduce real coupling and fragility, not mechanically everywhere.
