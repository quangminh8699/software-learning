## WHAT ARE ACCESS MODIFIERS ?

**Access modifiers** kiểm soát **AI được phép nhìn thấy / dùng tới** một class, field, method hay constructor. Đây là một dạng "scope" khác với [Variable Scope]: không phải "biến sống ở đâu", mà là "**từ đâu được phép truy cập vào**".

Java có **4 mức**, từ mở nhất tới kín nhất:

```
public     → mọi nơi, mọi package đều thấy
protected  → cùng package + các subclass (kể cả khác package)
(default)  → CHỈ cùng package  ← khi KHÔNG ghi modifier nào
private    → CHỈ trong chính class đó
```

> ⚠️ **HIỂU NHẦM THƯỜNG GẶP:** "Không ghi modifier thì mặc định là `public`." **SAI.** Không ghi gì = **package-private (default)** — chỉ code **cùng package** thấy được. Đây là lỗi rất phổ biến khiến class tưởng "mở" hóa ra "kín trong package".

---

## 🏢 VÍ DỤ ĐỜI THƯỜNG — Các vòng an ninh trong tòa nhà công ty

Coi class như một **công ty** trong tòa nhà:

| Mức | Đời thường | Ai vào được |
|---|---|---|
| `public` | **Sảnh tiếp khách** | Bất kỳ ai từ ngoài đường |
| `protected` | **Khu vực nhân viên + công ty con** | Người trong tòa nhà (package) + chi nhánh thừa kế (subclass) |
| *default* | **Chỉ nội bộ tầng công ty** | Chỉ người cùng tầng (cùng package) |
| `private` | **Két sắt phòng giám đốc** | Chỉ chính phòng đó (class đó) |

👉 Nguyên tắc bảo mật đời thường cũng đúng với code: **mặc định để kín nhất, chỉ mở đúng cái cần cho đúng người.** Càng mở rộng vô tội vạ, càng khó kiểm soát ai đụng vào cái gì.

---

## 1. `private` — Kín trong class

> "Chỉ chính class đó truy cập được. Nền tảng của Encapsulation."

```java
class BankAccount {
    private double balance;              // không ai ngoài class đụng thẳng được

    public void deposit(double amount) { // cổng public có kiểm soát
        if (amount > 0) balance += amount;
    }
}

BankAccount acc = new BankAccount();
// acc.balance = -999;   // ❌ LỖI compile — private, chặn thao tác bậy
acc.deposit(1000);       // ✅ phải đi qua cổng có validate
```

Liên hệ [What is OOP]: `private` field + `public` getter/setter chính là **Encapsulation** — giấu dữ liệu, chỉ mở API có kiểm soát.

---

## 2. *default* (package-private) — Kín trong package

> "Không ghi modifier → chỉ cùng package thấy. Dùng cho chi tiết nội bộ của một module."

```java
// file: com/shop/OrderValidator.java
package com.shop;

class OrderValidator {          // default class — chỉ package com.shop dùng
    boolean isValid(String o) { // default method
        return o != null;
    }
}
```

```java
// file: com/other/Test.java
package com.other;
// import com.shop.OrderValidator; // ❌ không dùng được — khác package
```

> 📌 Hữu ích khi muốn một class/method là "helper nội bộ" của package, **không lộ ra API công khai**.

---

## 3. `protected` — Cùng package + subclass

> "Như default, nhưng NỚI THÊM cho subclass ở package khác — dành cho kế thừa."

```java
// file: com/animal/Animal.java
package com.animal;
public class Animal {
    protected String name;             // subclass (kể cả khác package) dùng được
    protected void breathe() { }
}
```

```java
// file: com/pet/Dog.java
package com.pet;
import com.animal.Animal;

public class Dog extends Animal {
    void bark() {
        System.out.println(name + " gâu gâu");  // ✅ protected → subclass thấy
        breathe();                               // ✅
    }
}
```

> ⚠️ `protected` cho phép subclass **khác package** truy cập, nhưng **không** cho class thường (không kế thừa) ở package khác. Nó "rộng hơn default một chút, hẹp hơn public".

---

## 4. `public` — Mọi nơi

> "Ai cũng thấy. Dành cho API bạn cố ý phơi ra cho thế giới."

```java
public class StringUtils {
    public static boolean isEmpty(String s) {   // mọi package đều gọi được
        return s == null || s.isEmpty();
    }
}
```

> Chỉ để `public` những gì **thật sự là hợp đồng công khai**. Public càng nhiều → càng khó đổi sau này (đổi là làm gãy code người dùng).

---

## ⚙️ CÁCH HOẠT ĐỘNG BÊN DƯỚI

### Modifier được kiểm tra lúc COMPILE

Access modifier là **luật do compiler thực thi**, không phải cơ chế runtime. Vi phạm (`acc.balance` từ ngoài) → **lỗi compile**, chưa cần chạy. Vào bytecode, mức truy cập được ghi thành **access flags**:

```
public    → ACC_PUBLIC
protected → ACC_PROTECTED
private   → ACC_PRIVATE
(default) → không cờ nào trong nhóm này
```

### Bảng ma trận — ai truy cập được gì

| Vị trí truy cập | `private` | *default* | `protected` | `public` |
|---|:---:|:---:|:---:|:---:|
| Cùng class | ✅ | ✅ | ✅ | ✅ |
| Cùng package (khác class) | ❌ | ✅ | ✅ | ✅ |
| Subclass — **khác** package | ❌ | ❌ | ✅ | ✅ |
| Class thường — khác package | ❌ | ❌ | ❌ | ✅ |

### Reflection có thể "lách" — nhưng đừng lạm dụng

Access modifier **không phải rào bảo mật tuyệt đối**. Qua Reflection, code có thể ép truy cập cả `private`:

```java
Field f = BankAccount.class.getDeclaredField("balance");
f.setAccessible(true);     // ⚠️ vô hiệu hóa kiểm tra access
f.set(acc, -999.0);        // đặt thẳng vào private field
```

> 📌 Nghĩa là: modifier bảo vệ khỏi **truy cập vô tình / sai thiết kế**, chứ không chống được kẻ cố tình dùng reflection. (Từ Java 9+, module system `--illegal-access` siết chặt hơn.)

### Lưu ý phạm vi modifier trên top-level class

Class ngoài cùng (top-level) **chỉ được** `public` hoặc *default* — **không** được `private`/`protected`. Chỉ **nested class** (class con bên trong) mới dùng đủ 4 mức.

---

## Summary

```
Access Modifiers = kiểm soát AI được truy cập class/field/method

Từ KÍN → MỞ:
private   → chỉ trong class            (Encapsulation)
(default) → chỉ cùng package           ← KHÔNG ghi gì = mức này, KHÔNG phải public
protected → cùng package + subclass    (dành cho kế thừa)
public    → mọi nơi                    (API công khai)
```

| Modifier | Cùng class | Cùng package | Subclass khác pkg | Mọi nơi |
|---|:---:|:---:|:---:|:---:|
| `private` | ✅ | ❌ | ❌ | ❌ |
| *default* | ✅ | ✅ | ❌ | ❌ |
| `protected` | ✅ | ✅ | ✅ | ❌ |
| `public` | ✅ | ✅ | ✅ | ✅ |

> Quy tắc vàng: **mặc định chọn mức HẸP nhất, chỉ nới rộng khi thật sự cần.** Bắt đầu bằng `private`, chỉ nâng lên khi có lý do rõ ràng. Đây là cách hiện thực Encapsulation (xem [What is OOP]) — và là dạng "scope truy cập", song song với "scope biến" ở [Variable Scope].
