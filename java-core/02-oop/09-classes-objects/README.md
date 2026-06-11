# Bài 09: Classes & Objects — Khuôn mẫu vs thực thể

OOP bắt đầu ở đây: phân biệt **class** (bản thiết kế) với **object** (ngôi nhà xây từ bản thiết kế).

## 📖 Mô tả
- **Class**: khuôn mẫu định nghĩa **field** (trạng thái) + **method** (hành vi).
- **Object**: thực thể cụ thể tạo từ class bằng `new`, sống trên **heap**, có trạng thái riêng.
- **`this`**: tham chiếu tới chính object đang thao tác.

## 🔧 Kỹ thuật
| Thành phần | Vai trò |
|------------|---------|
| `field` (instance) | trạng thái — mỗi object 1 bản |
| `static field` | trạng thái dùng chung toàn class |
| `constructor` | khởi tạo object |
| `method` | hành vi |
| `this` | trỏ object hiện tại, phân biệt field vs tham số |

## ⚙️ Dưới nắp capo (Under the hood)
`new BankAccount(...)` thực hiện 4 bước:
```
   1) Cấp phát trên HEAP vùng cho các instance field + object header
   2) Gán mặc định: số→0, boolean→false, reference→null
   3) Chạy constructor (gán giá trị thật, this.x = ...)
   4) Trả về THAM CHIẾU (địa chỉ) cho biến bên trái dấu =

   STACK                         HEAP
   a1 ─────────────────────────▶ [ header | owner="Alice" | balance=140 ]
   alias ──────────────────────┘ (cùng object → a1 == alias)
   a2 ─────────────────────────▶ [ header | owner="Bob"   | balance=30  ]

   Method Area / Metaspace:  static instanceCount = 3  (dùng chung)
```
- **instance field** nằm **trong từng object** trên heap → object càng nhiều field, càng tốn heap.
- **static field** nằm ở **Metaspace** (theo class), không nhân bản theo object.
- Biến tham chiếu trên **stack** chỉ giữ địa chỉ; gán `alias = a1` sao chép **địa chỉ**, không sao chép object → cả hai sửa cùng một object.
- Khi không còn tham chiếu nào trỏ tới object → nó thành "rác", **GC** sẽ thu hồi (xem bài 43).

## ▶️ Cách dùng
```bash
javac Main.java && java Main
```
Output mẫu:
```
Alice có số dư: 140.0
Bob có số dư: 30.0
Qua a1 thấy số dư: 1140.0 (chung object với alias)
a1 == alias ? true
Tổng số account đã tạo: 3
temp = null -> object Temp đủ điều kiện bị GC dọn
```

## 🎨 Bản vẽ — class vs object
```
        CLASS (khuôn)                OBJECTS (thực thể trên heap)
   ┌──────────────────┐        ┌────────────┐  ┌────────────┐
   │ BankAccount      │  new   │ owner=Alice│  │ owner=Bob  │
   │  - owner         │ ─────▶ │ balance=140│  │ balance=30 │
   │  - balance       │        └────────────┘  └────────────┘
   │  + deposit()     │   (1 khuôn → N object, mỗi object trạng thái riêng)
   └──────────────────┘
```

## ⚠️ Cạm bẫy & lưu ý senior
- Nhầm `a1 = a2` là "sao chép object" → thực chất chỉ chung tham chiếu (cần `clone`/copy constructor nếu muốn bản sao thật).
- `static` field dùng làm trạng thái có thể đổi trong app đa thread → cần đồng bộ (xem Module 05).
- Quá nhiều field/object nhỏ → áp lực GC; cân nhắc tái dùng object hoặc dùng primitive.
- `this` trong constructor: dùng để gọi constructor khác (`this(...)`) — constructor chaining (bài 10).

## 🔗 Bài tiếp theo
👉 [10 — Constructors](../10-constructors)
