## [TÊN KHÁI NIỆM] LÀ GÌ?

<!--
Cách dùng template:
1. Thay toàn bộ nội dung trong [DẤU NGOẶC VUÔNG].
2. Xóa các comment hướng dẫn trước khi hoàn tất bài.
3. Có thể bỏ hoặc gộp mục nếu không phù hợp với chủ đề.
4. Phần giải thích viết bằng tiếng Việt; thuật ngữ chuyên ngành giữ bằng tiếng Anh.
-->

<!-- Định nghĩa trong 2-4 câu. Nói rõ nó thuộc lĩnh vực nào và mục đích chính là gì. -->

**[Tên khái niệm]** là [định nghĩa ngắn, chính xác và dễ hiểu]. Nó được dùng để [mục đích chính].

> **Hiểu đơn giản:** [Một câu dễ nhớ, có thể hiểu ngay cả khi chưa đọc phần còn lại].

---

## Ví dụ đời sống

<!--
Chọn một tình huống quen thuộc có cùng cơ chế với khái niệm.
Sau ví dụ, ánh xạ rõ từng thành phần đời sống sang thành phần kỹ thuật.
-->

Hãy tưởng tượng [mô tả tình huống quen thuộc].

- [Sự việc hoặc thành phần thứ nhất].
- [Sự việc hoặc thành phần thứ hai].
- [Kết quả hoặc vấn đề xuất hiện].

Trong ví dụ này:

| Đời sống | Trong software |
|---|---|
| [Thành phần đời sống A] | `[Thành phần kỹ thuật A]` |
| [Thành phần đời sống B] | `[Thành phần kỹ thuật B]` |

Điểm chung quan trọng là: **[nêu cơ chế cốt lõi mà ví dụ minh họa]**.

---

## Vấn đề cần giải quyết

<!-- Giải thích điều gì khó, sai hoặc bất tiện khi chưa có khái niệm này. -->

Giả sử [bối cảnh kỹ thuật đơn giản]. Nếu không sử dụng **[tên khái niệm]**:

- [Vấn đề 1 và hậu quả].
- [Vấn đề 2 và hậu quả].
- [Vấn đề 3 nếu thực sự cần].

Ví dụ có vấn đề:

```text
[Code, command, request/response hoặc flow tối thiểu thể hiện vấn đề]
```

Vấn đề cốt lõi không phải là [biểu hiện bề mặt], mà là **[nguyên nhân thật sự]**.

---

## Cách hoạt động

<!-- Chia cơ chế thành 3-5 bước theo đúng thứ tự xảy ra. -->

1. **[Bước 1]:** [Điều gì xảy ra và vì sao].
2. **[Bước 2]:** [Điều gì xảy ra và vì sao].
3. **[Bước 3]:** [Điều gì xảy ra và kết quả].

```text
[Thành phần A] → [Thành phần B] → [Thành phần C]
```

<!-- Chỉ giữ sơ đồ nếu luồng hoặc quan hệ khó hiểu khi diễn đạt bằng một câu. -->

### Các thành phần chính

| Thành phần | Trách nhiệm |
|---|---|
| `[Thành phần A]` | [Nó làm gì] |
| `[Thành phần B]` | [Nó làm gì] |

---

## Ví dụ code

<!--
Đổi language identifier cho phù hợp: java, bash, json, sql...
Nếu có anti-pattern, đặt ví dụ sai trước và giải pháp đúng ngay sau đó.
-->

### Cách chưa phù hợp

```java
// [Nêu ngắn gọn vì sao đoạn này có vấn đề]
[CODE THỂ HIỆN VẤN ĐỀ]
```

Đoạn code trên có vấn đề vì [giải thích nguyên nhân và hậu quả].

### Cách sử dụng [tên khái niệm]

```java
// [Chú thích điểm quan trọng, tập trung vào lý do]
[CODE THỂ HIỆN GIẢI PHÁP]
```

Kết quả:

```text
[OUTPUT HOẶC TRẠNG THÁI SAU KHI CHẠY]
```

Điểm cần chú ý:

- `[Dòng hoặc thành phần quan trọng]`: [ý nghĩa].
- `[Dòng hoặc thành phần quan trọng]`: [ý nghĩa].

---

## So sánh với khái niệm dễ nhầm

<!-- Chỉ giữ mục này khi có khái niệm người mới thường nhầm lẫn. -->

| Tiêu chí | [Khái niệm đang học] | [Khái niệm dễ nhầm] |
|---|---|---|
| Mục đích | [Mục đích A] | [Mục đích B] |
| Cách hoạt động | [Cơ chế A] | [Cơ chế B] |
| Khi sử dụng | [Trường hợp A] | [Trường hợp B] |

> **Cách phân biệt nhanh:** [Một câu chỉ ra khác biệt cốt lõi].

---

## Khi nào nên dùng?

Nên dùng **[tên khái niệm]** khi:

- [Tình huống thực tế 1].
- [Tình huống thực tế 2].
- [Dấu hiệu cho thấy giải pháp này phù hợp].

Không nên dùng hoặc cần cân nhắc khi:

- [Tình huống đơn giản hơn, chưa cần giải pháp này].
- [Trade-off về độ phức tạp, performance, memory hoặc maintenance].
- [Giải pháp thay thế phù hợp hơn].

---

## Hiểu nhầm thường gặp

> ⚠️ **Hiểu nhầm:** “[Phát biểu sai hoặc chưa đầy đủ mà người mới thường tin].”
>
> **Thực tế:** [Giải thích chính xác vì sao phát biểu trên sai hoặc chưa đủ].

### Lỗi phổ biến

- **[Lỗi 1]:** [Nguyên nhân, hậu quả và cách tránh].
- **[Lỗi 2]:** [Nguyên nhân, hậu quả và cách tránh].

---

## Tóm tắt

```text
[TÊN KHÁI NIỆM]
├── Là gì       → [Định nghĩa cực ngắn]
├── Giải quyết  → [Vấn đề chính]
├── Hoạt động   → [Cơ chế chính]
├── Nên dùng    → [Tình huống phù hợp]
└── Cẩn thận    → [Trade-off hoặc lỗi quan trọng]
```

> **Một câu ghi nhớ:** [Một câu cô đọng nhất để người đọc nhớ lại toàn bộ bài].

<!--
Checklist trước khi xóa comment này:
- Nội dung chính đã viết bằng tiếng Việt chưa?
- Thuật ngữ chuyên ngành đã giữ bằng tiếng Anh chưa?
- Ví dụ đời sống có thật sự cùng cơ chế không?
- Code/command có đúng và đủ nhỏ không?
- Có khẳng định nào cần kiểm chứng không?
- Đã xóa mục không phù hợp và toàn bộ placeholder chưa?
-->
