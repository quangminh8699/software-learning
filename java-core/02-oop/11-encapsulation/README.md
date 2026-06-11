# Bài 11: Encapsulation — Đóng gói & bảo toàn invariant

Đóng gói không phải "viết getter/setter cho mọi field" — mà là **bảo vệ tính đúng đắn** của object.

## 📖 Mô tả
Encapsulation: giấu trạng thái nội bộ (`private`), chỉ lộ ra **hành vi** qua method công khai. Nhờ đó class tự bảo vệ **invariant** (điều luôn đúng, vd "nhiệt độ ≥ -273.15") và tự do đổi cài đặt bên trong.

## 🔧 Kỹ thuật — 4 mức truy cập
| Modifier | Cùng class | Cùng package | Lớp con khác package | Mọi nơi |
|----------|:----------:|:------------:|:--------------------:|:-------:|
| `private` | ✅ | ❌ | ❌ | ❌ |
| *(default)* | ✅ | ✅ | ❌ | ❌ |
| `protected` | ✅ | ✅ | ✅ | ❌ |
| `public` | ✅ | ✅ | ✅ | ✅ |

Nguyên tắc: **mở ít nhất có thể** (least privilege). Mặc định `private`, chỉ nâng khi cần.

## ⚙️ Dưới nắp capo (Under the hood)
- Access modifier được **compiler enforce** (báo lỗi biên dịch khi vi phạm) và lưu thành **access flags** trong file `.class`. JVM verifier cũng kiểm tra khi link → không thể "lách" bằng cách gọi bytecode tay (trừ khi dùng Reflection với `setAccessible`, xem bài 39).
- `getFahrenheit()` là **computed property**: không có field `fahrenheit`. Người dùng không biết giá trị được **lưu** hay **tính lại** → đó chính là sức mạnh đóng gói: **đổi cài đặt mà không phá API**.
- Đóng gói tạo **ranh giới module**: code ngoài chỉ phụ thuộc *hợp đồng công khai*, không phụ thuộc *biểu diễn nội bộ* → giảm coupling, dễ refactor.

## ▶️ Cách dùng
```bash
javac Main.java && java Main
```
Output mẫu:
```
25°C = 77.0°F
Bị chặn: Nhiệt độ dưới 0 tuyệt đối: -500.0
Giá trị vẫn an toàn: 25.0°C
Lưu nội bộ kiểu gì là chuyện riêng của class.
```

## 🎨 Bản vẽ
```
   Code ngoài  ──────▶  ┌─────────── Temperature ───────────┐
   chỉ thấy:            │  public  getCelsius/setCelsius     │ ← hợp đồng
   public API           │  ────────────────────────────────  │
                        │  private double celsius            │ ← giấu kín
                        │  private toKelvin()                │
                        └────────────────────────────────────┘
   Mọi thay đổi state đều qua "cổng" public có validation.
```

## ⚠️ Cạm bẫy & lưu ý senior
- **Getter/setter mù quáng** cho mọi field = đóng gói giả: nếu setter không validate gì thì chẳng khác field public. Chỉ tạo accessor khi cần.
- **Leaking mutable state**: getter trả thẳng `List`/mảng nội bộ → bên ngoài sửa được → vỡ invariant. Trả **bản sao** hoặc `Collections.unmodifiableList`.
- Đóng gói tốt giúp đổi `celsius` → `kelvin` nội bộ mà không ai bên ngoài bị ảnh hưởng — đây là tiêu chí thiết kế cấp techlead.
- `record` (bài 19) là cách đóng gói dữ liệu bất biến gọn hơn nhiều khi không cần setter.

## 🔗 Bài tiếp theo
👉 [12 — Inheritance](../12-inheritance)
