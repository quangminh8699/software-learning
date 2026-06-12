# Bài 24: Collections — Set & Map (HashMap nội tại)

`HashMap` là cấu trúc dữ liệu được hỏi nhiều nhất trong phỏng vấn senior. Bài này mổ xẻ bên trong nó.

## 📖 Mô tả
- **Set**: tập **không trùng**. `HashSet` (nhanh, vô thứ tự), `TreeSet` (sorted), `LinkedHashSet` (giữ thứ tự chèn).
- **Map**: ánh xạ **key → value**. `HashMap`, `TreeMap`, `LinkedHashMap`.

## 🔧 Kỹ thuật
| Cài đặt | Thứ tự | get/put | Nền tảng |
|---------|--------|---------|----------|
| `HashMap` | không | O(1) trung bình | bảng băm |
| `TreeMap` | sorted theo key | O(log n) | cây đỏ-đen |
| `LinkedHashMap` | thứ tự chèn / truy cập | O(1) | hash + linked list |

Method hữu ích: `getOrDefault`, `merge`, `computeIfAbsent`, `putIfAbsent`.

## ⚙️ Dưới nắp capo (Under the hood) — HashMap
```
   HashMap = mảng bucket (mặc định 16, luôn lũy thừa 2)
   key.hashCode() → trộn bit (h ^ (h>>>16)) → index = hash & (n-1)

   bucket[5] → (k1,v1) → (k2,v2)        ← va chạm: danh sách liên kết
   bucket[9] → CÂY ĐỎ-ĐEN               ← list dài ≥ 8 & bảng ≥ 64 → treeify → O(log n)

   load factor 0.75: khi size > 0.75*capacity → RESIZE gấp đôi + rehash
```
- **put(k,v)**: tính hash → tìm bucket → so `equals` trong bucket → thay hoặc thêm.
- **Treeify**: một bucket quá đông (≥8 phần tử) chuyển từ linked list sang **cây đỏ-đen** → chống tấn công hash collision (DoS) + O(log n) thay O(n).
- **Resize**: khi vượt `load factor` (0.75) → tạo bảng gấp đôi, rehash lại → O(n) lần đó. Biết trước kích thước → `new HashMap<>(expected/0.75)` để né resize.
- **Hợp đồng `hashCode`/`equals`** (cực kỳ quan trọng):
  - `a.equals(b)` ⇒ `a.hashCode() == b.hashCode()` (bắt buộc).
  - Quên override → dùng mặc định `Object` (theo địa chỉ) → key "biến mất" (xem PointBad).
  - `hashCode` lệch `equals` → tra cứu sai. Dùng `Objects.hash(...)` + `Objects.equals(...)`.
- **`==` index dùng `& (n-1)`** thay `% n` được vì capacity luôn là lũy thừa 2 → nhanh hơn.

## ▶️ Cách dùng
```bash
javac Main.java && java Main
```
Output mẫu:
```
HashSet khử trùng: [a, b, c] (vô thứ tự)
TreeSet sorted: [a, b, c]
LinkedHashSet giữ thứ tự chèn: [z, a, m]
stock = {táo=13, cam=5}
getOrDefault('lê',0) = 0
sau computeIfAbsent: {nho=0, táo=13, cam=5}
Tần suất: {a=3, b=2, c=1}
PointBad tra lại (thiếu equals/hashCode): null
PointGood tra lại (có equals/hashCode): A
TreeMap sorted: {1=một, 2=hai, 3=ba}
floorKey(2)=2, ceilingKey(2)=2
```

## 🎨 Bản vẽ — va chạm & treeify
```
   bucket[i]:  (k1)→(k2)→(k3)... ≥8 & bảng≥64 →  cây đỏ-đen
                  O(n) trong bucket               O(log n)
```

## ⚠️ Cạm bẫy & lưu ý senior
- **Object khả biến làm key**: đổi field tham gia `hashCode` sau khi put → key lạc bucket → không tìm lại được. Key nên **bất biến**.
- **Quên `hashCode` khi override `equals`** (hoặc ngược lại) → bug im lặng. `record` (bài 19) sinh sẵn cặp này.
- **HashMap không thread-safe**: resize đồng thời từ nhiều thread (Java 7) gây vòng lặp vô hạn; dùng `ConcurrentHashMap` (bài 35).
- **TreeMap cần Comparable/Comparator** cho key; null key không hợp lệ.
- Iteration order của `HashMap` **không đảm bảo** và có thể đổi giữa các phiên bản — đừng phụ thuộc.

## 🔗 Bài tiếp theo
👉 [25 — Iterator & Comparator](../25-iterator-comparator)
