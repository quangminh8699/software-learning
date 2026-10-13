## SAVE VÀ SAVEANDFLUSH LÀ GÌ?

`save()` và `saveAndFlush()` là hai method lưu dữ liệu của `JpaRepository` trong Spring Data JPA. Cả hai đều lưu một entity, nhưng khác nhau ở **thời điểm câu SQL thực sự được gửi xuống database**.

> **Hiểu đơn giản:** `save()` ghi vào bộ nhớ tạm rồi để đó. `saveAndFlush()` ghi xong đẩy SQL xuống DB ngay lập tức.

Để hiểu được sự khác biệt này, trước hết cần phân biệt ba khái niệm mà người mới rất hay gộp làm một: **persistence context**, **flush** và **commit**.

---

## Ví dụ đời sống

Hãy tưởng tượng bạn là bồi bàn trong một nhà hàng:

- Khách gọi món, bạn **viết vào sổ tay** của mình. Bếp chưa biết gì cả.
- Gom được vài order, bạn **mang giấy xuống bếp**. Bếp bắt đầu nấu.
- Khách ăn xong, **thanh toán**. Lúc này đơn hàng mới thật sự chốt, không hủy được nữa.

Trong ví dụ này:

| Đời sống | Trong JPA |
|---|---|
| Sổ tay bồi bàn | **Persistence context** — bộ nhớ tạm của Hibernate trong RAM |
| Mang giấy xuống bếp | **flush** — gửi câu SQL xuống database |
| Khách thanh toán | **commit** — dữ liệu trở thành vĩnh viễn |

Điểm chung quan trọng là: **ba bước này xảy ra ở ba thời điểm khác nhau**. Viết vào sổ tay không có nghĩa bếp đã nấu, và bếp nấu xong không có nghĩa khách đã trả tiền.

---

## Persistence context là gì?

**Persistence context** là vùng nhớ tạm mà Hibernate dùng để theo dõi các entity trong một transaction. Nó nằm trong RAM của ứng dụng, **không phải** trong database.

Khi bạn gọi `save()`, Hibernate không chạy `INSERT` ngay. Nó chỉ ghi nhận: *"có một `User` mới cần lưu"*. Câu SQL được hoãn lại.

Vì sao lại hoãn? Vì gom nhiều câu lệnh gửi một lượt **nhanh hơn nhiều** so với gửi từng câu một. Mỗi lần gửi xuống DB là một lần đi mạng (round-trip). Giống bồi bàn gom 5 order rồi xuống bếp một chuyến, thay vì chạy xuống bếp 5 lần.

---

## Flush là gì?

**Flush** là hành động đẩy toàn bộ thay đổi đang nằm trong persistence context xuống database dưới dạng câu SQL.

Flush xảy ra ở ba thời điểm:

1. Khi transaction **commit** — đây là trường hợp phổ biến nhất, xảy ra tự động.
2. Khi bạn gọi **thủ công** qua `saveAndFlush()` hoặc `entityManager.flush()`.
3. Khi Hibernate chạy một **JPQL query** cần đọc dữ liệu vừa thay đổi (chế độ `FlushMode.AUTO` mặc định).

---

## Flush khác commit như thế nào?

Đây là điểm quan trọng nhất của bài, và cũng là chỗ người mới nhầm nhiều nhất.

| | flush | commit |
|---|---|---|
| Làm gì | Gửi câu SQL xuống DB | Chốt transaction vĩnh viễn |
| Còn rollback được không? | **Còn** | Không |
| Transaction khác thấy chưa? | **Chưa** | Rồi |

Sau khi flush, database **đã nhận và đã chạy** câu `INSERT`. Nhưng nếu sau đó code ném exception và transaction rollback, bản ghi đó **biến mất hoàn toàn**.

```java
@Transactional
public void demo() {
    User u = new User("minh");
    userRepository.saveAndFlush(u);       // INSERT đã chạy xuống DB

    throw new RuntimeException("lỗi");    // transaction rollback
    // Kết quả: bản ghi "minh" KHÔNG tồn tại trong DB
}
```

Món ăn đã nấu xong, nhưng khách bỏ về không trả tiền — món bị đổ đi.

---

## Sau khi flush, bản ghi nằm ở đâu?

Nó **nằm trong database thật**. Không phải một vùng chờ riêng nào cả.

Nhưng nó bị gắn nhãn: *"dòng này do transaction số 123 tạo ra, mà 123 chưa commit"*.

Khi flush, database làm ba việc:

1. **Ghi dòng dữ liệu** vào trang dữ liệu (data page) trong bộ nhớ của DB, sau đó xuống file trên đĩa.
2. **Ghi vào undo log** — bản ghi chép cách hoàn tác, nội dung đại ý là *"nếu transaction 123 rollback thì xóa dòng này đi"*.
3. **Giữ lock** trên dòng đó, để transaction khác không sửa cùng lúc.

Chính nhờ **undo log** mà rollback thực hiện được. Dữ liệu đã nằm trong DB, nhưng DB luôn biết đường quay lui.

```text
                    ┌─────────────────────────────────┐
   save()           │           DATABASE              │
      │             │                                 │
      ▼             │  ┌───────────────────────────┐  │
 ┌──────────┐       │  │ users                     │  │
 │   RAM    │       │  │ 1  lan   [committed]      │  │
 │ (sổ tay) │       │  │ 2  minh  [txn 123, chưa]  │  │ ← sau khi flush
 │  minh    │       │  └───────────────────────────┘  │
 └──────────┘       │                                 │
      │             │  ┌───────────────────────────┐  │
      │  flush      │  │ undo log                  │  │
      └────────────►│  │ "txn 123 rollback → xóa 2"│  │
                    │  └───────────────────────────┘  │
                    └─────────────────────────────────┘
```

---

## Chưa commit, vì sao query vẫn đọc được?

Vì query đó chạy trên **cùng một connection**, tức là **cùng một transaction**.

Đây là quy tắc nền tảng của database:

> **Một transaction luôn nhìn thấy thay đổi của chính nó, kể cả khi chưa commit.**

Tiếng Anh gọi là *read-your-own-writes*. Nếu `INSERT` xong mà `SELECT` không thấy gì thì gần như không viết được logic nào cả.

```java
@Transactional
public void demo() {
    userRepository.saveAndFlush(new User("minh"));

    jdbcTemplate.queryForObject("SELECT count(*) FROM users", Integer.class);
    // → 1
}
```

Nhìn qua thì `userRepository` và `jdbcTemplate` là hai thứ khác nhau hoàn toàn. Nhưng khi `@Transactional` mở transaction, Spring lấy một connection từ pool và **gắn nó vào thread hiện tại**. Mọi thứ chạy trong thread đó đều lấy đúng connection ấy ra dùng, không xin connection mới.

```text
@Transactional mở transaction
        │
        └── connection #7 ──┬── userRepository.saveAndFlush()
                            └── jdbcTemplate.query()

        Cùng connection → cùng transaction → thấy thay đổi của nhau
```

### Còn transaction khác thì sao?

Đây mới là ý nghĩa thật của "chưa commit":

```text
Transaction 123 (của bạn)         Transaction 456 (user khác)
─────────────────────────         ──────────────────────────
INSERT "minh"  (đã flush)
                                   SELECT * FROM users
                                   → KHÔNG thấy "minh"
COMMIT
                                   SELECT * FROM users
                                   → thấy "minh"
```

Database đọc nhãn và suy luận: *"dòng này thuộc transaction 123, mà 123 chưa commit, mà tôi là 456 → bỏ qua"*. Nếu 456 cần đọc bản cũ của một dòng đang bị 123 sửa, DB lấy bản cũ từ undo log đưa cho nó.

Cơ chế giữ nhiều phiên bản của cùng một dòng này tên là **MVCC** (Multi-Version Concurrency Control). PostgreSQL và MySQL (InnoDB) đều dùng, chi tiết cài đặt khác nhau nhưng ý tưởng giống nhau.

---

## Ví dụ code

### Trong method không có `@Transactional`

Điều nhiều người không biết: bản thân method `save()` của Spring Data JPA **đã được đánh dấu `@Transactional` sẵn** trong `SimpleJpaRepository`.

Nên khi method của bạn không có transaction:

```java
public void taoUser() {                       // KHÔNG có @Transactional
    userRepository.save(new User("minh"));    // dòng này chạy xong là đã commit
}
```

Diễn biến:

```text
gọi save()  →  Spring tự mở transaction mới
            →  ghi vào persistence context
            →  flush
            →  commit
            →  đóng transaction
```

Tất cả gói gọn trong một dòng. Vì transaction kết thúc ngay lập tức, mà kết thúc transaction thì bắt buộc phải flush rồi commit.

👉 **Trong trường hợp này, `save()` và `saveAndFlush()` cho kết quả giống hệt nhau.**

### Trong method có `@Transactional`

```java
@Transactional
public void taoUser() {
    userRepository.save(new User("minh"));    // dòng A
    lamViecKhac();                            // dòng B
    userRepository.save(new User("lan"));     // dòng C
}                                             // dòng D — kết thúc method
```

Bây giờ transaction thuộc về **method của bạn**. `save()` chỉ tham gia ké vào transaction đang có sẵn, không mở transaction riêng.

```text
dòng A  →  ghi "minh" vào persistence context. DB chưa biết gì.
dòng B  →  DB vẫn chưa biết gì.
dòng C  →  ghi "lan" vào persistence context. DB vẫn chưa biết gì.
dòng D  →  method kết thúc → flush cả 2 INSERT → commit
```

Đổi dòng A thành `saveAndFlush`:

```java
@Transactional
public void taoUser() {
    userRepository.saveAndFlush(new User("minh"));  // dòng A
    lamViecKhac();                                  // dòng B
    userRepository.save(new User("lan"));           // dòng C
}                                                   // dòng D
```

```text
dòng A  →  INSERT "minh" xuống DB NGAY (nhưng chưa commit)
dòng B  →  DB đã có "minh" ở trạng thái chưa commit
dòng C  →  ghi "lan" vào persistence context
dòng D  →  flush "lan" → commit cả hai
```

Khác biệt duy nhất là **thời điểm câu SQL chạy**. Trạng thái cuối cùng sau dòng D thì giống nhau.

---

## So sánh

| Tiêu chí | `save()` | `saveAndFlush()` |
|---|---|---|
| Thời điểm gửi SQL | Hoãn tới lúc flush hoặc commit | Ngay tại dòng gọi |
| Có commit không | Không (trừ khi tự mở transaction) | **Không** |
| Batch insert | Giữ được, Hibernate gom nhiều câu | Mất, vì flush từng lần |
| Lỗi constraint nổ khi nào | Lúc commit | Ngay tại dòng gọi |
| Hiệu năng trong vòng lặp | Tốt | Kém |

Riêng về ID tự sinh: nếu entity dùng `GenerationType.IDENTITY`, Hibernate **buộc phải** chạy `INSERT` ngay tại `save()` để lấy ID từ DB. Trường hợp này `save()` cũng gửi SQL ngay, không hoãn được.

### Bảng phân biệt theo ngữ cảnh

| | Method thường | Method có `@Transactional` |
|---|---|---|
| `save()` | flush + commit ngay lập tức | Chỉ ghi vào RAM, flush lúc method kết thúc |
| `saveAndFlush()` | flush + commit ngay lập tức | flush ngay tại dòng gọi, commit lúc method kết thúc |
| Hai cái có khác nhau? | **Không** | **Có** — khác thời điểm gửi SQL |

> **Cách phân biệt nhanh:** `saveAndFlush()` chỉ đổi *thời điểm* câu SQL chạy bên trong một transaction, không đổi *kết quả cuối cùng*.

---

## Khi nào nên dùng?

Nên dùng `saveAndFlush()` khi đang ở trong transaction và cần DB thấy dữ liệu ngay giữa chừng:

**1. Muốn bắt lỗi sớm**

```java
@Transactional
public void dangKy(User u) {
    userRepository.saveAndFlush(u);   // trùng email → exception nổ NGAY đây
    guiEmailChaoMung(u);              // → không tốn công gửi email vô ích
}
```

Nếu dùng `save()`, lỗi trùng email chỉ nổ lúc method kết thúc — sau khi email đã gửi đi rồi.

**2. Cần chạy native query hoặc JDBC thô ngay sau đó**

```java
@Transactional
public void demo() {
    userRepository.saveAndFlush(new User("minh"));

    // Query này đi thẳng xuống DB, không đọc persistence context.
    // Nếu dùng save() thay vì saveAndFlush(), kết quả sẽ là 0.
    jdbcTemplate.queryForObject("SELECT count(*) FROM users", Integer.class);
}
```

Không nên dùng khi:

- **Lưu nhiều entity trong vòng lặp.** Mỗi lần flush là một round-trip xuống DB, mất hoàn toàn khả năng batch của Hibernate.

  ```java
  // Không nên: 1000 lần đi xuống DB
  for (User u : danhSach) {
      userRepository.saveAndFlush(u);
  }

  // Nên: Hibernate gom lại gửi theo lô
  for (User u : danhSach) {
      userRepository.save(u);
  }
  ```

- **Chỉ để "cho chắc dữ liệu đã lưu".** Commit transaction đã tự flush rồi, không cần gọi thêm.

---

## Hiểu nhầm thường gặp

> ⚠️ **Hiểu nhầm:** "`saveAndFlush()` nghĩa là commit bản ghi xuống DB luôn."
>
> **Thực tế:** flush chỉ gửi câu SQL xuống DB, transaction vẫn đang mở. Rollback sau đó vẫn xóa sạch bản ghi, và transaction khác vẫn chưa nhìn thấy nó. Commit là một bước riêng, xảy ra khi method có `@Transactional` kết thúc.

> ⚠️ **Hiểu nhầm:** "`save()` không có `@Transactional` thì dữ liệu chưa xuống DB."
>
> **Thực tế:** ngược lại. Method `save()` của `SimpleJpaRepository` tự mở transaction riêng khi không có transaction nào đang chạy, nên nó flush và commit ngay lập tức. Chính khi **có** `@Transactional` thì dữ liệu mới bị hoãn lại.

### Lỗi phổ biến

- **Đọc dữ liệu từ connection khác rồi tưởng dữ liệu chưa lưu.** Nếu nhảy sang thread khác hoặc gọi method có `@Transactional(propagation = REQUIRES_NEW)`, bạn dùng connection khác, tức transaction khác, nên sẽ **không thấy** dữ liệu chưa commit.

  ```java
  @Transactional
  public void demo() {
      userRepository.saveAndFlush(new User("minh"));

      CompletableFuture.runAsync(() -> {
          userRepository.count();   // thread khác → connection khác → KHÔNG thấy "minh"
      });
  }
  ```

  Đây là nguồn của rất nhiều bug kiểu *"tôi lưu rồi mà sao query không ra"*.

- **Rắc `saveAndFlush()` khắp nơi cho an tâm.** Vừa chậm vừa không giải quyết được vấn đề gì, vì flush không phải commit.

---

## Tóm tắt

```text
SAVE vs SAVEANDFLUSH
├── save()          → ghi vào persistence context, SQL gửi sau
├── saveAndFlush()  → ghi + đẩy SQL xuống DB ngay, vẫn trong transaction
├── flush ≠ commit  → flush còn rollback được, người khác chưa thấy
├── Không @Transactional → hai method như nhau
├── Có @Transactional    → khác nhau ở thời điểm gửi SQL
└── Mặc định dùng save(), chỉ đổi khi có lý do cụ thể
```

> **Một câu ghi nhớ:** `saveAndFlush()` mang giấy order xuống bếp sớm hơn, nhưng khách vẫn chưa thanh toán.
