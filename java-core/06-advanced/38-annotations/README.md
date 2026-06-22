# Bài 38: Annotations — Metadata & nền tảng của mọi framework

Spring, JPA, JUnit, Lombok đều chạy trên annotation. Hiểu cơ chế = tự viết được "mini framework".

## 📖 Mô tả
Annotation là **metadata** gắn vào code. Bản thân nó không đổi logic — nhưng compiler/công cụ/runtime **đọc** nó để sinh hành vi (validate, mapping, inject, route...).

## 🔧 Kỹ thuật
- **Built-in**: `@Override`, `@Deprecated`, `@SuppressWarnings`, `@FunctionalInterface`.
- **Meta-annotation**: `@Retention`, `@Target`, `@Documented`, `@Inherited`, `@Repeatable`.
- **Định nghĩa**: `@interface Name { Type element() default ...; }`
- **Đọc runtime**: reflection (`isAnnotationPresent`, `getAnnotation`).

## ⚙️ Dưới nắp capo (Under the hood)
- **`@Retention` quyết annotation sống tới đâu** — điểm mấu chốt:
```
   SOURCE   → chỉ trong .java, compiler dùng rồi VỨT (vd @Override, Lombok)
   CLASS    → vào .class nhưng KHÔNG nạp lúc runtime (mặc định)
   RUNTIME  → vào .class VÀ đọc được bằng reflection lúc chạy (Spring, JPA, JUnit)
```
  → muốn đọc bằng reflection thì **bắt buộc** `@Retention(RUNTIME)`.
- **`@Target`** giới hạn nơi gắn (`METHOD`, `FIELD`, `TYPE`...) — compiler enforce.
- **Hai cách xử lý annotation**:
  1. **Runtime reflection** (như demo): quét class/method/field, đọc annotation, hành động. Spring/Hibernate/JUnit dùng cách này → có chi phí reflection lúc khởi động/chạy.
  2. **Annotation processor** (compile-time, `javax.annotation.processing`): xử lý lúc **biên dịch**, sinh code/file mới (Lombok, MapStruct, Dagger). Không chi phí runtime, an toàn kiểu.
- Demo dựng **mini-framework**: `@Loggable` (gọi method kèm log) + `@NotEmpty`/`@Min` (validate field) — đúng cách Spring AOP / Bean Validation hoạt động ở mức ý tưởng.

## ▶️ Cách dùng
```bash
javac Main.java && java Main
```
Output mẫu:
```
[DEBUG] gọi createUser()
  -> tạo user
[INFO] gọi deleteUser()
  -> xoá user
❌ username không được rỗng
❌ age phải >= 18 (đang 15)
```

## 🎨 Bản vẽ — vòng đời theo retention
```
   .java ──javac──▶ .class ──java──▶ JVM runtime
    │SOURCE            │CLASS           │RUNTIME
    └ @Override        └ (mặc định)     └ @Loggable, @Entity... ◄ reflection đọc
      Lombok                              Spring/JPA/JUnit
```

## ⚠️ Cạm bẫy & lưu ý senior
- **Quên `@Retention(RUNTIME)`** → reflection không thấy annotation (mặc định là CLASS) → "framework không chạy" khó hiểu.
- **Reflection annotation có chi phí** (quét + đọc) → framework cache metadata lúc khởi động; đừng quét trong hot path.
- **Annotation chỉ là metadata** — phải có **bộ xử lý** (reflection/processor) mới có tác dụng; tự nó không làm gì.
- Lạm dụng annotation ("annotation hell") → logic ẩn, khó debug. Cân bằng giữa khai báo và tường minh.
- `@Inherited` chỉ áp dụng cho class (không method/field) và chỉ kế thừa theo `extends`.

## 🔗 Bài tiếp theo
👉 [39 — Reflection](../39-reflection)
