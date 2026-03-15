package input;

public interface InputController {

    boolean kick();

    boolean duck();  //cúi xuống

    boolean punch();

    boolean block();

    boolean skill();

    /**
     * HÀM QUAN TRỌNG NHẤT: Cập nhật dữ liệu mỗi khung hình.
     */
    void update(float delta);

    /**
     * Hàm reset: Xóa bỏ các trạng thái input sau khi đã xử lý xong.
     */
    void reset();

}
