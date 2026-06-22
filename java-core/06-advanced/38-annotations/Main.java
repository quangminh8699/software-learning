/*
 * BÀI 38: ANNOTATIONS — Metadata, retention, target, đọc bằng reflection
 * ================================================================
 * WHAT  : Annotation = METADATA gắn vào code (class/method/field...), không đổi
 *         logic trực tiếp; được công cụ/compiler/runtime đọc để sinh hành vi.
 * WHY   : Khai báo thay vì viết tay (cấu hình, validate, mapping ORM, DI...).
 *         Là nền của Spring, JPA, JUnit, Lombok... Hiểu để tự viết framework nhỏ.
 * HOW   : @Retention quyết annotation sống tới đâu (SOURCE/CLASS/RUNTIME). Chỉ
 *         RUNTIME mới đọc được bằng reflection lúc chạy. @Target giới hạn nơi gắn.
 * WHEN  : Khi muốn metadata khai báo + xử lý tập trung (validate, route, inject).
 * WHICH : built-in (@Override, @Deprecated...) vs custom (tự định nghĩa + xử lý).
 */
import java.lang.annotation.*;
import java.lang.reflect.*;

public class Main {
    public static void main(String[] args) throws Exception {

        // === Phần 1: Annotation built-in quen thuộc ===
        // @Override, @Deprecated, @SuppressWarnings, @FunctionalInterface...
        // -> compiler dùng để kiểm tra/cảnh báo (xem các bài trước).

        // === Phần 2: Đọc custom annotation bằng REFLECTION lúc runtime ===
        // ⚙️ Chỉ annotation @Retention(RUNTIME) mới còn trong .class lúc chạy để đọc.
        UserService svc = new UserService();

        // "Mini framework": quét method có @Loggable rồi gọi kèm log.
        for (Method m : UserService.class.getDeclaredMethods()) {
            if (m.isAnnotationPresent(Loggable.class)) {
                Loggable ann = m.getAnnotation(Loggable.class);
                System.out.println("[" + ann.level() + "] gọi " + m.getName() + "()");
                m.invoke(svc);    // gọi method qua reflection
            }
        }

        // === Phần 3: Đọc annotation trên FIELD — mô phỏng validate (như Bean Validation) ===
        Account acc = new Account();
        acc.username = "";          // rỗng -> vi phạm @NotEmpty
        acc.age = 15;               // < 18 -> vi phạm @Min(18)
        validate(acc);
    }

    // "Mini validator": đọc @NotEmpty / @Min trên field rồi kiểm tra.
    static void validate(Object obj) throws IllegalAccessException {
        for (Field f : obj.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            Object value = f.get(obj);
            if (f.isAnnotationPresent(NotEmpty.class) && value instanceof String s && s.isEmpty())
                System.out.println("❌ " + f.getName() + " không được rỗng");
            if (f.isAnnotationPresent(Min.class) && value instanceof Integer n) {
                int min = f.getAnnotation(Min.class).value();
                if (n < min) System.out.println("❌ " + f.getName() + " phải >= " + min + " (đang " + n + ")");
            }
        }
    }
}

// === Định nghĩa custom annotations ===

// @Retention(RUNTIME): còn sống lúc chạy -> reflection đọc được.
// @Target: chỉ gắn lên METHOD.
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Loggable {
    String level() default "INFO";       // phần tử có giá trị mặc định
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface NotEmpty {}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Min { int value(); }

class UserService {
    @Loggable(level = "DEBUG")
    void createUser() { System.out.println("  -> tạo user"); }

    @Loggable                          // dùng default level = INFO
    void deleteUser() { System.out.println("  -> xoá user"); }

    void internalHelper() { System.out.println("  (không log - không annotation)"); }
}

class Account {
    @NotEmpty String username;
    @Min(18) int age;
}
