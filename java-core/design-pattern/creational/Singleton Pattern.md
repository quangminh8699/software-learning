## SINGLETON PATTERN LÀ GÌ?
Singleton Pattern là một **creational design pattern** (mẫu thiết kế thuộc nhóm khởi tạo).
Nó đảm bảo một class **chỉ có duy nhất MỘT object (instance)** trong toàn bộ chương trình,
và cung cấp **một điểm truy cập chung (global access point)** đến object đó.

> Hiểu đơn giản: *"Dù bạn gọi bao nhiêu lần, tôi cũng chỉ trả về đúng MỘT object duy nhất."*

---

## Ví dụ đời thường 👑

Một đất nước chỉ có **duy nhất một vị Tổng thống** tại một thời điểm. Dù ai nhắc đến "Tổng thống"
ở bất cứ đâu, tất cả đều đang nói về **cùng một người**. Không thể có 5 vị Tổng thống cùng lúc.

Một ví dụ khác: **công tắc đèn chính của cả căn nhà**. Chỉ có một cái. Mọi người trong nhà đều
dùng chung công tắc đó — không ai tự dựng thêm một cái công tắc chính thứ hai.

Đó chính là Singleton: **cả hệ thống dùng chung đúng một object.**

---

## Khi nào cần đúng một object?

Có những thứ về bản chất chỉ nên tồn tại một bản duy nhất:

- **Configuration** — cấu hình của app, đọc một lần rồi ai cũng dùng chung.
- **Logger** — bộ ghi log, cả app ghi vào cùng một chỗ.
- **Connection Pool** — kho kết nối database, chỉ nên có một để quản lý tài nguyên.
- **Cache** — bộ nhớ đệm dùng chung toàn app.

Nếu mỗi chỗ tự tạo một object riêng, chúng sẽ **không đồng bộ với nhau** và **lãng phí tài nguyên**.

---

## Vấn đề (khi KHÔNG dùng Singleton)

```java
// Mỗi nơi tự tạo một object cấu hình riêng
AppConfig config1 = new AppConfig(); // đọc file config lần 1
AppConfig config2 = new AppConfig(); // đọc file config lần 2 (lãng phí!)

// config1 và config2 là 2 object KHÁC NHAU
// Sửa config1 không ảnh hưởng config2 → dữ liệu không đồng bộ 😩
```

**Các vấn đề:**
- Tạo lại object tốn kém nhiều lần (đọc file, mở kết nối...).
- Các bản sao không đồng bộ → dễ sinh bug khó tìm.
- Không có "một nguồn sự thật" (single source of truth) chung.

---

## Giải pháp (cách tạo Singleton)

Ý tưởng gồm 3 phần:
1. Cho **constructor thành `private`** → bên ngoài không thể `new` được.
2. Giữ một **`static` field** để lưu instance duy nhất bên trong chính class.
3. Cung cấp một **`static` method** để lấy instance đó.

```java
class AppConfig {
    // (2) instance duy nhất, giữ bên trong class
    private static AppConfig instance;

    private String appName = "My App";

    // (1) private constructor → bên ngoài KHÔNG thể new AppConfig()
    private AppConfig() {
        System.out.println("AppConfig được khởi tạo (chỉ chạy 1 lần)");
    }

    // (3) điểm truy cập chung
    public static AppConfig getInstance() {
        if (instance == null) {         // lần đầu tiên → tạo mới
            instance = new AppConfig();
        }
        return instance;                // các lần sau → trả lại cái cũ
    }

    public String getAppName() { return appName; }
}
```

### Cách dùng

```java
AppConfig a = AppConfig.getInstance(); // "AppConfig được khởi tạo (chỉ chạy 1 lần)"
AppConfig b = AppConfig.getInstance(); // không in gì thêm — dùng lại object cũ

System.out.println(a == b); // true → a và b là CÙNG MỘT object 🎉
```

> `a == b` trả về `true` chứng minh cả hai biến trỏ đến đúng một object trong bộ nhớ.

---

## ⚠️ Cẩn thận với Multithreading

Cách viết ở trên gọi là **lazy initialization** (chỉ tạo khi cần lần đầu). Nó **không an toàn** khi
nhiều thread cùng gọi `getInstance()` một lúc — hai thread có thể cùng thấy `instance == null` và
cùng tạo ra hai object khác nhau.

Cách an toàn phổ biến và đơn giản nhất là dùng **`enum`** (cách được Joshua Bloch khuyên dùng):

```java
enum AppConfig {
    INSTANCE;                       // instance duy nhất, JVM đảm bảo thread-safe

    private String appName = "My App";
    public String getAppName() { return appName; }
}

// Cách dùng
String name = AppConfig.INSTANCE.getAppName();
```

Hoặc dùng **`synchronized`** để khóa lúc tạo (nếu cần lazy):

```java
public static synchronized AppConfig getInstance() {
    if (instance == null) {
        instance = new AppConfig();
    }
    return instance;
}
```

> Với người mới: hãy nhớ *"Singleton bản đơn giản không thread-safe"*. Trong dự án thực tế, thường ta để **framework như Spring** quản lý singleton (mỗi `@Bean`/`@Service` mặc định đã là singleton rồi).

---

## Khi nào nên dùng?

| Dùng Singleton khi... | Ví dụ |
|-----------------------|-------|
| Chỉ nên tồn tại đúng một object | Logger, Configuration, Cache |
| Object tốn kém khi tạo, nên dùng lại | Database connection pool |
| Cần một điểm truy cập chung toàn app | Thanh trạng thái, thư mục thiết lập |

> ⚠️ **Đừng lạm dụng.** Singleton bản chất là một **biến toàn cục (global state)**, dễ làm code khó test và khó theo dõi luồng dữ liệu. Chỉ dùng khi thật sự cần một bản duy nhất. Nhiều người xem lạm dụng Singleton là một *anti-pattern*.

---

## Tóm tắt

```
Singleton Pattern
├── Ý tưởng    → Cả app chỉ có DUY NHẤT một object của class
├── Ví dụ      → Một đất nước một Tổng thống; một công tắc đèn chính
├── Cách làm (3 bước)
│   ├── private constructor  → chặn bên ngoài new
│   ├── static instance      → giữ object duy nhất
│   └── static getInstance() → điểm truy cập chung
│
├── Lưu ý     → Bản lazy đơn giản KHÔNG thread-safe → dùng enum / synchronized
└── Lợi ích
    ├── Tiết kiệm tài nguyên (không tạo lại)
    ├── Một nguồn sự thật chung (single source of truth)
    └── Truy cập được từ mọi nơi
```

> **Một câu tóm gọn:** Singleton Pattern khóa việc khởi tạo lại bằng cách giấu constructor và trả về đúng một object dùng chung, đảm bảo cả chương trình luôn nói về cùng một bản duy nhất.
