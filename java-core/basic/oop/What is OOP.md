## WHAT IS OOP?
OOP stands for Object-Oriented Programming. OOP in Java is a programming model that organizes software
design around data, or objects, rather than functions and logic.
An object is a real-world entity that bundles both **state** (data fields) and **behavior** (methods) together.
Java leverages OOP to make code more modular, scalable, reusable, and easier to maintain.

---

## Core Building Blocks

- **Class**: A blueprint or template used to create individual objects. It defines the structure and behavior that every object of that type will have.
- **Object**: A concrete instance created from a class blueprint. Every object occupies space in heap memory and contains its own unique data.

```java
class Dog {              // Class — the blueprint
    String name;
    int age;

    void bark() {
        System.out.println(name + " says: Woof!");
    }
}

Dog d = new Dog();       // Object — an instance in memory
d.name = "Rex";
d.bark();                // Rex says: Woof!
```

> In Java, every class implicitly extends `java.lang.Object` — the root of the entire class hierarchy.

---

## The 4 Pillars of Java OOP

### 1. Encapsulation

**What it is:** Hiding the internal state of an object and restricting direct access from the outside. The object controls how its own data is read or modified.

**How Java does it:** Fields are declared `private`. Controlled access is provided via `public` getter and setter methods. This lets you add validation, logging, or computed logic without changing the caller.

```java
class BankAccount {
    private double balance; // hidden from outside

    public double getBalance() {
        return balance;
    }

    public void deposit(double amount) {
        if (amount > 0) balance += amount; // validation inside setter
    }

    public void withdraw(double amount) {
        if (amount > 0 && amount <= balance) balance -= amount;
    }
}

BankAccount acc = new BankAccount();
acc.deposit(1000);
// acc.balance = -999; // compile error — private field protected
System.out.println(acc.getBalance()); // 1000.0
```

**Key benefit:** Internal implementation can change freely as long as the public API stays the same.

---

### 2. Inheritance

**What it is:** The mechanism by which one class acquires the properties and behaviors of another class. It models an **"is-a" relationship**.

**How Java does it:** A subclass uses the `extends` keyword to inherit from a superclass. Java supports **single inheritance** for classes (one parent only), but a class can implement multiple interfaces.

```java
class Animal {
    String name;

    Animal(String name) {
        this.name = name;
    }

    void eat() {
        System.out.println(name + " is eating.");
    }
}

class Dog extends Animal {
    Dog(String name) {
        super(name); // calls parent constructor
    }

    void bark() {
        System.out.println(name + " barks!");
    }
}

Dog d = new Dog("Rex");
d.eat();  // inherited from Animal — Rex is eating.
d.bark(); // own method — Rex barks!
```

**Key keywords:**
| Keyword | Purpose |
|---------|---------|
| `extends` | Inherit from a superclass |
| `super()` | Call parent constructor |
| `super.method()` | Call parent method from child |
| `final class` | Prevent a class from being subclassed |
| `instanceof` | Check type at runtime |

```java
System.out.println(d instanceof Dog);    // true
System.out.println(d instanceof Animal); // true — is-a relationship
```

---

### 3. Polymorphism

**What it is:** The ability of a single interface or method name to take on multiple forms depending on context.

**How Java does it:** Two mechanisms:

#### Method Overriding (Runtime Polymorphism)
A subclass provides its own implementation of a method inherited from the parent. The JVM decides which version to call at **runtime** based on the actual object type.

```java
class Animal {
    void sound() {
        System.out.println("Some generic sound");
    }
}

class Dog extends Animal {
    @Override
    void sound() { System.out.println("Woof"); }
}

class Cat extends Animal {
    @Override
    void sound() { System.out.println("Meow"); }
}

Animal a1 = new Dog(); // reference type = Animal, actual type = Dog
Animal a2 = new Cat();

a1.sound(); // Woof  — decided at runtime
a2.sound(); // Meow  — decided at runtime
```

> `@Override` annotation is optional but strongly recommended — it makes the compiler catch mistakes if the method signature doesn't actually match a parent method.

#### Method Overloading (Compile-time Polymorphism)
Same method name, different parameter lists in the **same class**. The compiler picks the correct version at **compile time**.

```java
class Printer {
    void print(int x)    { System.out.println("int: " + x); }
    void print(String s) { System.out.println("String: " + s); }
    void print(int x, int y) { System.out.println("two ints: " + x + ", " + y); }
}

Printer p = new Printer();
p.print(5);        // int: 5
p.print("Hello");  // String: Hello
p.print(3, 4);     // two ints: 3, 4
```

| | Overriding | Overloading |
|---|---|---|
| Where | Between parent and child class | Within the same class |
| Decided at | Runtime (JVM) | Compile time (compiler) |
| Signature | Must match parent exactly | Must differ in parameters |
| Return type | Must be same (or covariant) | Can differ |
| `@Override` | Applicable | Not applicable |

---

### 4. Abstraction

**What it is:** Hiding complex implementation details and exposing only the essential features. Users interact with a simplified interface without needing to know how it works internally.

**How Java does it:** Two mechanisms — `abstract` classes and `interfaces`.

#### Abstract Class
Cannot be instantiated. Can have both abstract methods (no body) and concrete methods (with body). Used when subclasses share common state or partial implementation.

```java
abstract class Shape {
    String color;

    Shape(String color) { this.color = color; }

    abstract double area(); // must be implemented by subclass

    void describe() { // shared concrete method
        System.out.println(color + " shape with area: " + area());
    }
}

class Circle extends Shape {
    double radius;

    Circle(String color, double radius) {
        super(color);
        this.radius = radius;
    }

    @Override
    double area() { return Math.PI * radius * radius; }
}

Shape s = new Circle("Red", 5);
s.describe(); // Red shape with area: 78.53...
```

#### Interface
A pure contract — defines what a class must do, not how. A class can implement multiple interfaces (Java's answer to multiple inheritance).

```java
interface Flyable {
    void fly(); // implicitly public abstract
}

interface Swimmable {
    void swim();
}

class Duck implements Flyable, Swimmable {
    public void fly()  { System.out.println("Duck is flying"); }
    public void swim() { System.out.println("Duck is swimming"); }
}
```

> Since Java 8, interfaces can have `default` and `static` methods with implementations. Since Java 9, they can also have `private` methods.

#### Abstract Class vs Interface

| | Abstract Class | Interface |
|---|---|---|
| Instantiation | No | No |
| Multiple inheritance | No (single `extends`) | Yes (multiple `implements`) |
| Fields | Any (state allowed) | `public static final` only |
| Methods | Abstract + concrete | Abstract + `default`/`static` (Java 8+) |
| Constructor | Yes | No |
| Use when | Shared base implementation | Defining a capability/contract |

---

## Key Java OOP Concepts

### `this` vs `super`
```java
class Animal {
    String name = "Animal";
    Animal(String name) { this.name = name; }
}

class Dog extends Animal {
    String name = "Dog";

    Dog(String name) {
        super(name);          // calls Animal(String)
    }

    void printNames() {
        System.out.println(this.name);  // Dog's own field
        System.out.println(super.name); // Animal's field
    }
}
```

### Constructor Chaining
```java
class Person {
    String name;
    int age;

    Person(String name) {
        this(name, 0); // calls Person(String, int)
    }

    Person(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
```

### Object Class Methods (inherited by all classes)
Every Java object inherits these from `java.lang.Object`:
| Method | Purpose |
|--------|---------|
| `toString()` | String representation |
| `equals(Object o)` | Logical equality check |
| `hashCode()` | Hash for collections |
| `getClass()` | Runtime class info |

Override `equals()` and `hashCode()` together when objects are used in collections like `HashMap` or `HashSet`.

---

## Summary

```
OOP in Java
├── Building Blocks
│   ├── Class     → blueprint
│   └── Object    → instance in memory
│
└── 4 Pillars
    ├── Encapsulation  → private fields + getters/setters
    ├── Inheritance    → extends (single) / implements (multiple via interface)
    ├── Polymorphism   → Overriding (runtime) + Overloading (compile-time)
    └── Abstraction    → abstract class (partial impl) + interface (pure contract)
```
