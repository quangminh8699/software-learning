# Bài 10: Constructors — Khởi tạo object & thứ tự init

Constructor đảm bảo object "ra đời" ở trạng thái hợp lệ — và thứ tự khởi tạo là câu hỏi phỏng vấn senior kinh điển.

## 📖 Mô tả
Constructor là method đặc biệt (cùng tên class, không kiểu trả về) chạy khi `new`. Bài này gồm: overloading, **constructor chaining** (`this(...)`, `super(...)`), và **thứ tự khởi tạo** chính xác.

## 🔧 Kỹ thuật
- **Default constructor**: nếu bạn không viết constructor nào, compiler thêm `ClassName() {}` rỗng.
- **Overloading**: nhiều constructor khác tham số.
- **`this(...)`**: gọi constructor khác **cùng class** — phải là câu lệnh **đầu tiên**.
- **`super(...)`**: gọi constructor **lớp cha** — phải là câu lệnh đầu tiên (chèn ngầm nếu thiếu).

## ⚙️ Dưới nắp capo (Under the hood)
Thứ tự khi `new Child()`:
```
   1. [Class loading - 1 lần duy nhất]
        static field init + static {} của Parent, rồi Child
   2. [Mỗi lần new]
        super() → khởi tạo phần Parent TRƯỚC:
            - instance initializer + field init của Parent
            - thân constructor Parent
        rồi mới tới Child:
            - instance initializer + field init của Child (theo thứ tự viết)
            - thân constructor Child
```
- `static {}` chạy **một lần** lúc class được nạp lần đầu (xem bài 40 — class loading).
- `super(...)` luôn chạy **trước** thân constructor con → đảm bảo "phần cha" sẵn sàng trước khi con dùng.
- Vì thứ tự này, **gọi method bị override trong constructor cha** rất nguy hiểm: nó chạy bản con khi field con **chưa** được khởi tạo → đọc ra `null`/`0`.

## ▶️ Cách dùng
```bash
javac Main.java && java Main
```
Output mẫu:
```
Pizza size=M, cheese=false
Pizza size=L, cheese=false
Pizza size=L, cheese=true

--- Tạo Child lần 1 ---
[static] Parent được nạp
[static] Child được nạp
[init ] Parent instance initializer
[ctor ] Parent()
[init ] Child instance initializer
[ctor ] Child()
--- Tạo Child lần 2 (static KHÔNG chạy lại) ---
[init ] Parent instance initializer
[ctor ] Parent()
[init ] Child instance initializer
[ctor ] Child()
```

## 🎨 Bản vẽ — chaining
```
   new Pizza()
       │ this("M", false)
       ▼
   Pizza(String,boolean)   ← mọi đường đều đổ về constructor "đầy đủ"
       (một chỗ duy nhất chứa logic khởi tạo → DRY)
```

## ⚠️ Cạm bẫy & lưu ý senior
- **Gọi method overridable trong constructor**: bản override ở lớp con chạy khi field con chưa init → bug khó tìm. Khởi tạo nên gọi method `private`/`final`.
- Quên `this(...)`/`super(...)` phải là **dòng đầu** → lỗi biên dịch.
- Constructor làm quá nhiều việc (I/O, đăng ký listener) → khó test, dễ rò rỉ `this` ra ngoài khi object chưa hoàn chỉnh.
- Nhiều tham số cùng kiểu → khó đọc và dễ truyền nhầm thứ tự → cân nhắc **Builder** (bài 45).

## 🔗 Bài tiếp theo
👉 [11 — Encapsulation](../11-encapsulation)
