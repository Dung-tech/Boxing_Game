package input;

/**
 * Interface chung cho mọi loại thiết bị đầu vào.
 * Giúp Controller không cần quan tâm dữ liệu đến từ đâu (AI hay Bàn phím).
 */
public interface InputController {
    /**
     * Trả về hành động hiện tại nhận được từ thiết bị.
     * @return String tên hành động (PUNCH, KICK, BLOCK, NONE...)
     */
    String getNextAction();
}
