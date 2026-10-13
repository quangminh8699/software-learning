## WHAT IS BEAN SCOPE ? (Spring)

Trong Spring, **bean scope** quyết định **container tạo BAO NHIÊU instance của một bean và mỗi instance sống BAO LÂU**. Đây là "scope" ở tầng **framework** — khác hẳn [Variable Scope] (phạm vi biến trong Java) và [Access Modifiers] (phạm vi truy cập). Ở đây "scope" = **vòng đời & số lượng object do Spring quản lý**.

```
Các scope chính:
1. singleton  (mặc định) — 1 instance duy nhất / container
2. prototype             — MỖI lần yêu cầu → 1 instance mới
   Web scope (chỉ trong web app):
3. request               — 1 instance / 1 HTTP request
4. session               — 1 instance / 1 HTTP session (user)
5. application           — 1 instance / ServletContext
6. websocket             — 1 instance / 1 phiên WebSocket
```

> ⚠️ **HIỂU NHẦM THƯỜNG GẶP:** "Spring singleton = Singleton Pattern (GoF), một instance cho cả JVM." **SAI.** Spring singleton là **một instance mỗi ApplicationContext (container)**, không phải per-JVM. Chạy 2 container → 2 instance. Cơ chế cũng khác: Spring quản lý trong registry, không dùng `private static` + `getInstance()`.

---

## 🍽️ VÍ DỤ ĐỜI THƯỜNG — Menu chung vs giấy gọi món riêng

Coi Spring container là **cái quán ăn**:

- **`singleton`** = **tấm bảng menu treo tường**: cả quán **dùng chung 1 cái**. Khách nào cũng nhìn cùng tấm đó. Tạo 1 lần, xài mãi.
- **`prototype`** = **tờ giấy gọi món**: **mỗi khách được phát 1 tờ mới**, viết riêng, không đụng nhau. Cần là in tờ mới.
- **`request`** = tờ giấy **chỉ dùng cho một lượt phục vụ** (1 HTTP request), xong lượt là bỏ.
- **`session`** = **thẻ tích điểm của một khách quen** (1 user), theo suốt nhiều lần ghé (nhiều request) tới khi hết phiên.

👉 Chọn scope = trả lời "**cái này nên dùng chung, hay mỗi người/mỗi lượt một cái riêng?**"

---

## 1. singleton — Mặc định

> "Một instance duy nhất trong container, chia sẻ cho mọi nơi inject nó."

```java
@Component                       // mặc định là singleton — không cần ghi @Scope
class OrderService {
    // Spring tạo ĐÚNG 1 OrderService, mọi chỗ @Autowired đều nhận cùng object này
}
```

- ✅ Tiết kiệm: tạo 1 lần, tái dùng → nhẹ, nhanh.
- ⚠️ **Phải stateless** (không giữ dữ liệu riêng theo user/request). Nếu singleton mang state thay đổi → nhiều thread đụng nhau → [Race Condition].

---

## 2. prototype — Mỗi lần một instance mới

> "Cứ mỗi lần yêu cầu bean từ container là tạo MỚI một instance."

```java
@Component
@Scope("prototype")              // hoặc @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class ShoppingCart {
    private List<String> items = new ArrayList<>();  // state riêng từng cái → cần prototype
}
```

> 📌 Với prototype, Spring **chỉ chịu trách nhiệm tạo**, **KHÔNG** gọi hủy (`@PreDestroy` không chạy). Việc dọn dẹp thuộc về người dùng bean.

---

## 3. Bẫy kinh điển — Inject prototype vào singleton

> "Singleton chỉ được tạo 1 lần → dependency prototype của nó cũng chỉ được inject 1 lần → hóa ra dùng lại mãi một instance."

```java
@Component // singleton
class OrderService {
    @Autowired
    private ShoppingCart cart;   // ❌ prototype nhưng chỉ inject LÚC tạo OrderService (1 lần)
                                 //    → mọi request dùng CHUNG một cart → sai ý đồ prototype
}
```

**Vì sao?** OrderService là singleton nên chỉ khởi tạo một lần; lúc đó Spring inject **một** ShoppingCart rồi thôi. Prototype "mất tác dụng".

**Cách xử lý đúng — lấy instance mới mỗi lần cần:**

```java
@Component
class OrderService {
    @Autowired
    private ObjectProvider<ShoppingCart> cartProvider;  // ✅ cách khuyến nghị

    void checkout() {
        ShoppingCart cart = cartProvider.getObject();   // mỗi lần gọi → prototype MỚI
        // ...
    }
}
```

Các cách khác: `@Lookup` method injection, hoặc **scoped proxy** `@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)`.

---

## 4. Web Scopes — request / session / application

> "Vòng đời gắn với vòng đời của HTTP request / session / ứng dụng web."

```java
@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST,
       proxyMode = ScopedProxyMode.TARGET_CLASS)   // proxy để inject vào singleton an toàn
class RequestContext {
    // 1 instance mới cho MỖI HTTP request, tự hủy khi request xong
}
```

| Scope | Một instance cho... | Ví dụ dùng |
|---|---|---|
| `request` | 1 HTTP request | Dữ liệu tạm của một lượt gọi API |
| `session` | 1 HTTP session (1 user) | Giỏ hàng, thông tin đăng nhập theo user |
| `application` | 1 ServletContext (cả app) | Cấu hình dùng chung toàn web app |
| `websocket` | 1 phiên WebSocket | Trạng thái của một kết nối realtime |

> ⚠️ Inject bean scope hẹp (request/session) vào một singleton phải dùng **scoped proxy** (`proxyMode`), nếu không Spring không biết lấy instance nào tại thời điểm chạy.

---

## ⚙️ CÁCH HOẠT ĐỘNG BÊN DƯỚI

### Singleton nằm trong registry của container

Khi context khởi động, Spring tạo sẵn các bean singleton (eager) và **cache chúng trong một `Map` (singleton registry)**. Mỗi lần `getBean()` hay `@Autowired`, container **trả lại object đã cache** — không tạo mới.

```
ApplicationContext
 └── singletonObjects (Map<tên bean, instance>)
       "orderService" → (một OrderService duy nhất)  ← mọi nơi nhận cùng cái này
```

### Prototype không được cache

Với prototype, container **tạo mới → tiêm dependency → trả về → "quên" luôn** (không giữ tham chiếu). Vì thế Spring không quản lý phần hủy của nó.

### Scoped proxy hoạt động thế nào?

Khi cần inject bean scope hẹp vào bean scope rộng, Spring inject **một proxy** thay cho bean thật. Mỗi lần gọi method trên proxy, nó **tra cứu instance đúng của scope hiện tại** (request/session đang chạy) rồi ủy quyền. Nhờ vậy một singleton vẫn "thấy đúng" request-bean của từng request.

---

## Summary

```
Bean Scope (Spring) = container tạo BAO NHIÊU instance & sống BAO LÂU
(khác Variable Scope / Access Modifiers — đây là scope tầng FRAMEWORK)

singleton  → 1 / container (mặc định)  → phải STATELESS
prototype  → mỗi lần lấy = 1 mới        → Spring không hủy giúp
request    → 1 / HTTP request
session    → 1 / HTTP session (user)
application→ 1 / cả web app
websocket  → 1 / phiên WebSocket

Bẫy: prototype inject vào singleton → chỉ tạo 1 lần
     → fix: ObjectProvider / @Lookup / scoped proxy
```

| Scope | Số instance | Vòng đời | Dùng khi |
|---|---|---|---|
| **singleton** | 1 / container | Suốt đời container | Service/stateless (đa số) |
| **prototype** | Mỗi lần lấy 1 mới | Do người dùng quản lý | Bean có state riêng |
| **request** | 1 / request | 1 HTTP request | Dữ liệu tạm của 1 lượt gọi |
| **session** | 1 / session | 1 phiên user | Giỏ hàng, trạng thái user |
| **application** | 1 / app | Suốt đời app | Config dùng chung web app |

> Quy tắc: **mặc định để singleton và giữ nó stateless**; chỉ chuyển prototype/web-scope khi bean thật sự cần state riêng theo lần dùng hoặc theo user. Nếu singleton buộc phải giữ state chia sẻ → coi chừng [Race Condition], cần đồng bộ hóa.
