# Bài 19: Records & Sealed Classes — Dữ liệu bất biến + phân cấp đóng

Hai tính năng Java hiện đại (17/21) đưa Java gần với "algebraic data types": `record` (product type) + `sealed` (sum type).

## 📖 Mô tả
- **`record`**: lớp dữ liệu **bất biến**, compiler tự sinh constructor, accessor, `equals`, `hashCode`, `toString`.
- **`sealed`**: giới hạn **chính xác** những lớp con được phép → cho phép `switch` kiểm tra **đầy đủ** lúc compile.

## 🔧 Kỹ thuật
- `record Point(int x, int y) {}` → có sẵn: `Point(int,int)`, `x()`, `y()`, `equals/hashCode/toString`.
- **Compact constructor** `Point { ... }`: validate/normalize mà không lặp lại tham số.
- `sealed interface S permits A, B` → chỉ `A`, `B` được implements; mỗi con phải `final`/`sealed`/`non-sealed`.
- Kết hợp **switch pattern** (Java 21): không cần `default` nếu đã phủ hết permitted types.

## ⚙️ Dưới nắp capo (Under the hood)
- `record` biên dịch thành `final class` kế thừa `java.lang.Record`, các **component** thành **field `private final`**. `equals/hashCode/toString` được sinh dựa trên **tất cả** component (qua `invokedynamic` + `ObjectMethods` bootstrap) → đảm bảo nhất quán, không quên field.
- `record` **bất biến nông** (shallow immutable): field final, không setter. Nhưng nếu component là kiểu khả biến (`List`) thì nội dung vẫn đổi được → cần defensive copy trong compact constructor nếu muốn bất biến thật.
- `sealed` ghi danh sách `permits` vào **class file** (attribute `PermittedSubclasses`). Compiler + JVM dùng nó để: (1) chặn lớp con ngoài danh sách, (2) cho `switch` biết tập con **đóng** → **exhaustiveness checking**: thêm loại mới mà quên nhánh switch → **lỗi compile** (an toàn hơn `default` âm thầm).
- Bộ ba `sealed + record + pattern switch` = **pattern matching trên dữ liệu** kiểu functional, nhưng vẫn tĩnh & an toàn.

## ▶️ Cách dùng
```bash
javac Main.java && java Main        # cần JDK 17+ (record/sealed), 21 cho switch pattern
javap -p Point                     # thấy field final + accessor tự sinh
```
Output mẫu:
```
p1 = Point[x=3, y=4]
p1.equals(p2) = true
x=3, y=4
hashCode bằng nhau? true
Range độ rộng = 9
Validate trong record: to < from
Circle   area = 12.57
Square   area = 9.00
Circle   area = 3.14
```

## 🎨 Bản vẽ — sum type đóng
```
   sealed interface Shape permits Circle, Square
            │
       ┌────┴────┐
    Circle     Square           switch(s):
   (record)   (record)            case Circle c -> ...
                                   case Square s -> ...   ← KHÔNG cần default
   Thêm Triangle mà quên nhánh → compiler báo lỗi ngay.
```

## ⚠️ Cạm bẫy & lưu ý senior
- record **không** thay được mọi class: không dùng khi cần định danh (identity), vòng đời, hoặc state khả biến — record là **value object**.
- Bất biến **nông**: `record R(List<Integer> xs)` vẫn cho sửa list → copy phòng thủ trong compact constructor + trả bản unmodifiable ở accessor nếu cần.
- `sealed` giúp domain model "đóng" → refactor an toàn; rất hợp với DDD / state machine / AST.
- record tự sinh `equals` theo **mọi** component → cẩn thận khi component không nên tham gia định danh.

## 🔗 Bài tiếp theo
👉 [20 — Exception Handling](../../03-core-apis/20-exception-handling) (bắt đầu Module 03)
