# Bài 23: Collections — List (ArrayList vs LinkedList)

90% câu hỏi "ArrayList hay LinkedList?" trong phỏng vấn — và câu trả lời gần như luôn là ArrayList.

## 📖 Mô tả
`List` là chuỗi phần tử **có thứ tự**, **cho trùng**. Hai cài đặt: `ArrayList` (mảng động) và `LinkedList` (liên kết đôi). Chọn đúng = hiểu cấu trúc bên trong.

## 🔧 Kỹ thuật — độ phức tạp
| Thao tác | ArrayList | LinkedList |
|----------|:---------:|:----------:|
| `get(i)` | **O(1)** | O(n) |
| `add` (cuối) | amortized O(1) | O(1) |
| `add/remove` đầu | O(n) | **O(1)** |
| `add/remove` giữa | O(n) | O(n) tìm + O(1) nối |
| bộ nhớ/phần tử | thấp (mảng) | cao (Node: 2 con trỏ) |
| cache CPU | **thân thiện** (liền khối) | kém (rải rác) |

## ⚙️ Dưới nắp capo (Under the hood)
```
   ArrayList: một MẢNG liên tục trên heap
   [a][b][c][d][_][_]   size=4, capacity=6
   get(i) = elementData[i]  → O(1)
   đầy → grow ~1.5x → Arrays.copyOf → O(n) (hiếm) → amortized O(1)

   LinkedList: các Node rải rác
   head ⇄ [a] ⇄ [b] ⇄ [c] ⇄ tail
   get(i): đi từ đầu/cuối → O(n);  addFirst/Last: chỉ nối con trỏ → O(1)
```
- **ArrayList thắng gần như mọi lúc** vì: truy cập index O(1), **dữ liệu liền khối** → thân thiện cache CPU (prefetch), ít overhead bộ nhớ. Ngay cả chèn giữa O(n) cũng thường nhanh hơn LinkedList O(n) do `System.arraycopy` cực tối ưu + không cache miss.
- **LinkedList** chỉ hợp khi: làm **Deque/Queue** với add/remove ở **hai đầu** liên tục và **không** truy cập theo index. Mỗi Node tốn thêm 2 tham chiếu (prev/next) + object header → tốn RAM, nhiều cache miss.
- **Resize amortized**: chứng minh O(1) trung bình bằng phân tích khấu hao — tổng chi phí copy 1+2+4+...+n = 2n → chia n thao tác = O(1)/thao tác.
- **fail-fast iterator**: ArrayList/LinkedList giữ `modCount`; sửa cấu trúc khi đang for-each → `ConcurrentModificationException`.

## ▶️ Cách dùng
```bash
javac Main.java && java Main
```
Output mẫu:
```
get(1) O(1): chuối
Sau chèn giữa: [táo, xoài, chuối, cam]
nums = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]
LinkedList như deque: [0, 1, 2]
peekFirst=0, peekLast=2
Sau removeIf chẵn: [1, 3, 5]
sort: [1, 3, 5, 9]
binarySearch(9): 3
max: 9
List.of là bất biến -> không add được
```

## 🎨 Bản vẽ — chọn cài đặt
```
   Cần get(i) nhanh / duyệt nhiều?      → ArrayList  (mặc định)
   Hàng đợi add/remove 2 đầu liên tục?  → ArrayDeque (tốt hơn LinkedList!)
   Cần index + chèn giữa cực nhiều?     → cân nhắc cấu trúc khác (cây)
```

## ⚠️ Cạm bẫy & lưu ý senior
- **ArrayDeque > LinkedList** cho stack/queue: nhanh hơn, ít rác hơn. LinkedList gần như "di sản".
- **Khởi tạo capacity** khi biết kích thước (`new ArrayList<>(n)`) → tránh nhiều lần resize/copy.
- **`List.of`/`Arrays.asList`** trả list **bất biến / cố định kích thước** → `add` ném `UnsupportedOperationException`.
- **Sửa khi for-each** → `ConcurrentModificationException`; dùng `removeIf`/`Iterator.remove`/`ListIterator`.
- `subList` trả **view** (không phải copy) → sửa view ảnh hưởng list gốc.

## 🔗 Bài tiếp theo
👉 [24 — Collections: Set & Map](../24-collections-set-map)
