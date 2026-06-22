# Bài 39: Reflection — Soi & thao tác class lúc runtime

Reflection là "siêu năng lực" cho phép framework làm điều kỳ diệu — và là con dao hai lưỡi về hiệu năng + an toàn.

## 📖 Mô tả
Reflection cho phép **kiểm tra** (đọc cấu trúc) và **thao tác** (tạo object, gọi method, đọc/ghi field — kể cả `private`) một class **lúc runtime**, dựa trên object `Class<?>` mô tả nó.

## 🔧 Kỹ thuật
| Việc | API |
|------|-----|
| Lấy Class | `X.class`, `obj.getClass()`, `Class.forName("X")` |
| Soi cấu trúc | `getDeclaredFields/Methods/Constructors` |
| Tạo object | `Constructor.newInstance(...)` |
| Gọi method | `Method.invoke(obj, args)` |
| Truy cập private | `setAccessible(true)` |

## ⚙️ Dưới nắp capo (Under the hood)
- Khi class được nạp, JVM tạo **một** object `Class<?>` mô tả nó (tên, field, method, annotation...) trong **Metaspace**. Mọi `X.class`/`getClass()`/`forName` trả về **cùng** object đó → so `==` được.
- `Method.invoke` không gọi thẳng — nó qua tầng kiểm tra (access, kiểu tham số, **autobox** đối số thành `Object[]`) rồi dispatch → **chậm hơn** lời gọi tĩnh. JIT tối ưu dần (inflation: sau nhiều lần, JVM sinh accessor bytecode chuyên biệt) nhưng vẫn có overhead + chặn nhiều tối ưu (không inline tốt).
- **`setAccessible(true)`** tắt kiểm tra truy cập của JVM → đọc/ghi được `private`. Đây là cách Hibernate gán field, Jackson deserialize, Spring inject **không cần** setter. Nhưng **phá đóng gói** + có thể bị **Module System (JPMS)** chặn (`InaccessibleObjectException`) nếu module không `opens`.
- Mất **an toàn kiểu compile-time**: lỗi tên method/sai kiểu chỉ lộ lúc **runtime** (`NoSuchMethodException`, `ClassCastException`).

## ▶️ Cách dùng
```bash
javac Main.java && java Main
```
Output mẫu:
```
Cùng một Class object? true
Tên: Person, cha: Object
Fields: name age
Methods: greet toString
Tạo động: Person{Alice, 30}
invoke greet: Alice chào Bob
age private hiện tại = 30
Sau khi sửa private: Person{Alice, 99}
1tr lời gọi: trực tiếp=2ms, reflection=18ms (reflection chậm hơn ~7.x)
```
*(Tỉ lệ chậm thay đổi tuỳ máy/JIT; điểm chính: reflection có overhead rõ rệt.)*

## 🎨 Bản vẽ — Class metadata
```
   Person.class ──nạp──▶ [Class<Person>] trong Metaspace
                          ├ fields:  name, age
                          ├ methods: greet, toString
                          └ ctors:   Person(String,int)
   Reflection đọc bảng này + (setAccessible) bỏ kiểm tra → thao tác động.
```

## ⚠️ Cạm bẫy & lưu ý senior
- **Đắt** → cache `Method`/`Field` đã `setAccessible`, đừng tra cứu lại trong vòng lặp; framework làm vậy lúc khởi động.
- **Mất an toàn kiểu** → lỗi runtime thay vì compile. Hạn chế dùng trong code nghiệp vụ; ưu tiên interface/đa hình.
- **JPMS**: module không `opens package` → `setAccessible` ném `InaccessibleObjectException`. Cấu hình `--add-opens` hoặc `opens` trong module-info.
- **Bảo mật**: reflection có thể bị lạm dụng để truy cập nội bộ; mã không tin cậy + reflection = rủi ro.
- Khi cần "động" nhưng nhanh: cân nhắc `MethodHandle`/`VarHandle` (nhanh hơn, JIT-friendly hơn reflection), hoặc **annotation processor** (compile-time) thay reflection runtime.

## 🔗 Bài tiếp theo
👉 [40 — JVM Architecture](../../07-jvm-internals/40-jvm-architecture) (bắt đầu Module 07 — JVM Internals)
