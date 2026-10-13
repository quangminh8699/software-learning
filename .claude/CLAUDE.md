# Hướng dẫn viết bài học software

## 1. Bối cảnh repository

Repository này là nơi lưu trữ các bài học về software, bao gồm các chủ đề như Java Core, Spring, Git, Design Pattern, Concurrency và System Design.

Mỗi file Markdown là một bài học độc lập. Mục tiêu quan trọng nhất là giúp người mới **hiểu bản chất**, không chỉ ghi nhớ định nghĩa hoặc copy code.

Khi tạo bài mới, tham khảo template tại:

```text
.claude/templates/software-lesson.md
```

Template là khung định hướng. Chỉ sử dụng những phần thực sự phù hợp với chủ đề; không cố thêm một mục nếu nó không giúp người đọc hiểu bài hơn.

---

## 2. Đối tượng người đọc

Viết cho người mới học software:

- Có thể chưa biết các khái niệm nền tảng liên quan.
- Cần hiểu **vì sao** một khái niệm tồn tại trước khi học cách dùng.
- Học tốt hơn khi có ví dụ đời sống, sơ đồ đơn giản và code ngắn.
- Dễ nhầm các khái niệm gần giống nhau.
- Cần biết trường hợp thực tế nên dùng và không nên dùng.

Không mặc định rằng người đọc đã biết một thuật ngữ chỉ vì thuật ngữ đó phổ biến với developer có kinh nghiệm.

---

## 3. Quy tắc ngôn ngữ

### Viết bằng tiếng Việt

- Phần giải thích, tiêu đề, chú thích code và ví dụ sử dụng tiếng Việt tự nhiên.
- Viết câu ngắn, trực tiếp, tránh văn phong hàn lâm.
- Ưu tiên các từ quen thuộc như “cách hoạt động”, “vấn đề”, “trường hợp sử dụng” thay vì những cách diễn đạt khó hiểu.

### Giữ nguyên thuật ngữ chuyên ngành bằng tiếng Anh

Không dịch cưỡng ép các thuật ngữ mà developer thường gặp bằng tiếng Anh, ví dụ:

- `thread`, `lock`, `deadlock`, `race condition`
- `class`, `object`, `interface`, `method`
- `runtime`, `compile time`, `heap`, `stack`
- `request`, `response`, `API Gateway`, `BFF`
- `branch`, `remote`, `commit`, `merge`, `rebase`

Giải thích thuật ngữ ngay lần đầu xuất hiện:

```markdown
**Deadlock** là trạng thái hai hoặc nhiều `thread` chờ khóa của nhau vô thời hạn.
```

Không viết kiểu nửa Việt nửa Anh khi câu tiếng Việt rõ ràng hơn:

```markdown
<!-- Không nên -->
Chúng ta sẽ implement một solution để handle problem này.

<!-- Nên -->
Chúng ta sẽ cài đặt một giải pháp để xử lý vấn đề này.
```

Các keyword, API, class name, command và tên pattern phải giữ nguyên chính tả gốc và đặt trong backtick khi phù hợp: `synchronized`, `git fetch`, `Strategy Pattern`.

---

## 4. Trình tự giải thích

Ưu tiên trình tự từ trực giác đến kỹ thuật:

1. **Định nghĩa ngắn:** Khái niệm là gì?
2. **Một câu dễ nhớ:** Nếu chỉ nhớ một điều, người đọc nên nhớ gì?
3. **Ví dụ đời sống:** Tình huống quen thuộc nào có cùng bản chất?
4. **Vấn đề:** Vì sao cần khái niệm hoặc kỹ thuật này?
5. **Cách hoạt động:** Nó xử lý vấn đề từng bước như thế nào?
6. **Code hoặc command:** Biểu diễn cơ chế bằng một ví dụ tối thiểu.
7. **So sánh:** Nó khác gì với khái niệm dễ nhầm?
8. **Ứng dụng:** Khi nào nên dùng và khi nào không nên dùng?
9. **Tóm tắt:** Cô đọng lại các ý quan trọng.

Không mở đầu bằng một đoạn định nghĩa dài hoặc một khối code lớn khi người mới chưa biết vấn đề mà code đang giải quyết.

---

## 5. Ví dụ đời sống

Ví dụ đời sống phải làm rõ **cơ chế cốt lõi**, không chỉ làm bài viết sinh động.

Ví dụ tốt trong repository:

- `Deadlock` dùng hai xe mắc kẹt trong con hẻm để minh họa `circular wait`.
- `Remote & Origin` dùng danh bạ điện thoại để giải thích alias trỏ tới URL.
- `Strategy Pattern` dùng nhiều cách di chuyển đến công ty để minh họa các thuật toán có thể thay thế nhau.
- `API Gateway` và `BFF` dùng cửa ra vào và người phục vụ trong nhà hàng để phân biệt trách nhiệm.

Sau ví dụ, luôn nối lại với khái niệm kỹ thuật:

```markdown
Trong ví dụ này, chiếc xe tương ứng với `thread`, còn đoạn đường đang bị chiếm tương ứng với `lock`.
```

Tránh ví dụ có quá nhiều nhân vật, nhiều ngoại lệ hoặc cần giải thích dài hơn chính khái niệm.

---

## 6. Code sample và command

- Dùng ví dụ nhỏ nhất có thể nhưng vẫn thể hiện đúng bản chất.
- Code phải hợp lệ về cú pháp và nhất quán trong toàn bộ bài.
- Chú thích tập trung vào **lý do** hoặc điểm quan trọng, không diễn giải lại từng dòng hiển nhiên.
- Nếu trình bày lỗi hoặc anti-pattern, ghi rõ `Không nên`, `Có vấn đề` hoặc `Violation`.
- Khi có giải pháp, đặt code sai và code đúng gần nhau để dễ so sánh.
- Ghi kết quả mong đợi ngay sau code khi kết quả giúp hiểu cơ chế.
- Không dùng API, option hoặc hành vi chưa được kiểm chứng.
- Không đưa production code quá phức tạp vào bài nhập môn. Có thể nói rõ ví dụ đã được đơn giản hóa.

Ví dụ:

```java
// Không nên: hai thread có thể lấy lock theo thứ tự ngược nhau
synchronized (lockA) {
    synchronized (lockB) {
        transfer();
    }
}
```

---

## 7. Bảng, sơ đồ và liên kết

Chỉ dùng bảng khi cần so sánh nhiều thuộc tính có cùng cấu trúc, ví dụ:

- `Overloading` và `Overriding`.
- `Deadlock`, `Livelock` và `Starvation`.
- `API Gateway` và `BFF`.

Dùng text diagram khi cần mô tả luồng hoặc quan hệ:

```text
Client → API Gateway → BFF → Service
```

Không tạo sơ đồ cho một ý có thể nói rõ trong một câu.

Khi nhắc đến bài khác trong repository, sử dụng Markdown link tương đối nếu file đó thực sự tồn tại. Không để link giả hoặc placeholder trong bài hoàn chỉnh.

---

## 8. Hiểu nhầm và giới hạn

Với những chủ đề dễ nhầm, thêm một cảnh báo ngắn:

```markdown
> ⚠️ **Hiểu nhầm thường gặp:** `origin` không phải keyword bắt buộc của Git. Nó chỉ là tên mặc định của một `remote`.
```

Mỗi cảnh báo phải có:

- Điều người mới thường hiểu sai.
- Vì sao cách hiểu đó sai hoặc chưa đủ.
- Cách hiểu chính xác.

Luôn nói rõ trade-off hoặc giới hạn quan trọng. Không mô tả một pattern, tool hoặc kỹ thuật như giải pháp phù hợp cho mọi trường hợp.

---

## 9. Giữ tính chính xác

Trước khi hoàn tất một bài:

- Phân biệt rõ quy tắc của ngôn ngữ, hành vi của runtime và convention của cộng đồng.
- Không biến một lời khuyên phổ biến thành quy tắc tuyệt đối.
- Ghi rõ version nếu hành vi phụ thuộc version.
- Không bịa API, command, option, output hoặc benchmark.
- Với chủ đề có nhiều cách giải thích, chọn cách đơn giản nhưng không làm sai bản chất.
- Nếu chưa chắc chắn về một chi tiết kỹ thuật, cần kiểm chứng trước khi khẳng định.

---

## 10. Khi chỉnh sửa bài hiện có

- Đọc toàn bộ bài trước khi sửa.
- Giữ lại các ví dụ và cách diễn đạt đang làm rõ bài học.
- Không viết lại toàn bộ chỉ để ép bài vào template.
- Sửa các đoạn tiếng Anh sang tiếng Việt nếu chúng là phần giải thích; vẫn giữ thuật ngữ chuyên ngành bằng tiếng Anh.
- Loại bỏ nội dung trùng lặp, mâu thuẫn hoặc đi quá xa khỏi chủ đề chính.
- Giữ Markdown link và code sample đang đúng; cập nhật chúng nếu thay đổi làm liên kết hoặc code không còn hợp lệ.
- Không tự ý sửa bài khác ngoài phạm vi được yêu cầu.

---

## 11. Quy ước Markdown

- Theo convention của các bài hiện có, bắt đầu bài bằng heading cấp 2: `## <TÊN CHỦ ĐỀ> LÀ GÌ?`.
- Dùng heading theo đúng thứ bậc; các mục con của một section dùng `###`, không nhảy thẳng sang `####`.
- Chừa một dòng trống quanh heading, list, bảng và code block.
- Dùng backtick cho keyword, command, class name, method name và đoạn code ngắn.
- Code block phải có language identifier như `java`, `bash`, `json` hoặc `text`.
- Dùng emoji có chọn lọc để đánh dấu phần quan trọng; không đặt emoji ở mọi heading.
- Nhấn mạnh bằng bold vừa đủ, không bold cả đoạn dài.

Tên file và vị trí thư mục cần theo convention đang có của nhóm chủ đề liên quan. Nếu tạo một nhóm chủ đề mới, sử dụng tên thư mục ngắn, rõ nghĩa và nhất quán.

---

## 12. Checklist trước khi hoàn tất

- [ ] Phần giải thích chính được viết bằng tiếng Việt.
- [ ] Thuật ngữ chuyên ngành quan trọng vẫn giữ bằng tiếng Anh.
- [ ] Thuật ngữ mới được giải thích ở lần xuất hiện đầu tiên.
- [ ] Người mới hiểu được vấn đề trước khi đọc giải pháp.
- [ ] Có ví dụ đời sống nếu ví dụ đó thực sự giúp làm rõ cơ chế.
- [ ] Code và command ngắn, đúng cú pháp và có mục đích rõ ràng.
- [ ] Có phân biệt với khái niệm dễ nhầm nếu cần.
- [ ] Có nêu trường hợp nên dùng, không nên dùng hoặc trade-off quan trọng.
- [ ] Không có nội dung mâu thuẫn, lặp lại hoặc khẳng định chưa kiểm chứng.
- [ ] Phần tóm tắt đủ ngắn để ôn lại nhanh.
- [ ] Không còn placeholder hoặc Markdown link không hợp lệ.
