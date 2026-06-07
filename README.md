# 🥊 HUST Boxing Game

<!-- 📸 [1] BANNER: Ảnh bìa / logo game, kích thước ngang ~1200px -->
<!-- ![Banner](assets/screenshots/banner.png) -->

> Game đối kháng 1v1 với 3 chế độ điều khiển: bàn phím, nhận diện ngón tay (Camera AI), và nhận diện tư thế cơ thể (Camera Pose) — được xây dựng bằng Java (LibGDX) và Python (MediaPipe).

Bài tập lớn môn **Lập trình Hướng đối tượng** — Đại học Bách khoa Hà Nội (HUST)

## 📥 Download & Chạy nhanh

1. Tải file **`stage.zip`** từ **`Releases`**
2. **Giải nén** toàn bộ file `stage.zip` ra một thư mục (ví dụ: `D:\BoxingGame\`)
3. Mở thư mục vừa giải nén, tìm và chạy file **`BoxingGame.exe`**
> ⚠️ **Lưu ý:** Phải giải nén toàn bộ trước khi chạy. Không mở trực tiếp file `.exe` từ bên trong file `.zip` vì sẽ thiếu tài nguyên (ảnh, âm thanh, font) và game sẽ lỗi.

## ✨ Tính năng

- **Đối kháng 1v1:** Ronaldo (P1) vs Messi (P2) — đấm, đá, đỡ, né, tuyệt chiêu
- **3 chế độ điều khiển:** Keyboard, Camera AI (đếm ngón tay), Camera Pose (tư thế thân trên)
- **Hệ thống chiến đấu:** Khắc chế đòn-thế (Punch↔Block, Kick↔Duck, Skill phá mọi phòng thủ)
- **Mana & Tuyệt chiêu:** Tích 10 Mana → kích hoạt skill với cắt cảnh hoạt ảnh chuyên nghiệp
- **Chế độ Gym:** Mini-game tập luyện thể lực với camera body tracking
- **Bo 3 hiệp:** Mỗi hiệp 180 giây, thắng 2/3 để giành chiến thắng
- **HUD đầy đủ:** Thanh HP, Mana, Round, bảng điểm giữa hiệp, Camera Preview
- **Âm thanh sống động:** Nhạc nền + hiệu ứng đòn đánh + nhạc tuyệt chiêu riêng


## 🎬 Demo Video
https://drive.google.com/file/d/1dH6oDqAaAmSWUi-o0yAMSGrEQfM8znBr/view?usp=sharing

## 🎮 Điều khiển

### Keyboard Mode

| Hành động | Player 1 | Player 2 |
|-----------|----------|----------|
| Đấm (Punch) | `D` | `→` |
| Đá (Kick) | `A` | `←` |
| Đỡ (Block) | `W` | `↑` |
| Né (Duck) | `S` | `↓` |
| Tuyệt chiêu (Skill) | `SPACE` | `ENTER` |

### Camera AI Mode (đếm ngón tay)

| Số ngón | Hành động |
|---------|-----------|
| 0 | IDLE (nắm tay — reset) |
| 1 | Punch |
| 2 | Kick |
| 3 | Block |
| 4 | Duck |
| 5 | Skill |

> Cơ chế chống spam: Sau mỗi đòn, người chơi phải nắm tay lại (0 ngón) để reset rồi mới ra đòn tiếp.

### Camera Pose Mode (tư thế thân trên)

| Tư thế | Hành động |
|--------|-----------|
| Đứng yên phòng thủ | IDLE |
| Hai tay che mặt | Block |
| Hạ thấp người/vai | Duck |
| Đấm 1 tay thẳng | Punch |
| Đấm móc / biên độ lớn | Kick |
| Hai tay cùng đấm thẳng | Skill |

### Gym Mode

| Phím | Hành động |
|------|-----------|
| `ENTER` | Concentric (co cơ) |
| `SPACE` | Eccentric (duỗi cơ) |
| `ESC` | Về menu |

> Gym Mode cũng hỗ trợ Camera Pose — nhận diện góc tay để phát hiện pha concentric/eccentric.

## 🛠️ Yêu cầu

| Thành phần | Phiên bản |
|------------|-----------|
| Java JDK | 17+ |
| Gradle | Đã bao gồm qua wrapper |
| Python | 3.10+ (cho các chế độ Camera) |
| Webcam | Cần cho Camera AI / Camera Pose |

## 🚀 Chạy từ mã nguồn

### 1. Chạy game (Desktop)

```bash
.\gradlew.bat lwjgl3:run
```

### 2. Cài đặt Camera AI (tùy chọn)

```bash
pip install -r python_controller\requirements.txt
```

> Game sẽ **tự động** khởi chạy Python controller khi chọn chế độ Camera AI, Camera Pose hoặc Gym Pose từ menu.

### Chế độ camera được hỗ trợ

| Chế độ | Mô tả | TCP Port |
|--------|--------|----------|
| `CAMERA_AI` | Đếm ngón tay qua MediaPipe Hands | 65432 |
| `CAMERA_POSE` | Nhận diện tư thế thân trên qua MediaPipe Pose | 65432 |
| `CAMERA_GYM_POSE` | Nhận diện góc tay cho Gym Mode | 65433 |
| Camera Preview | Stream JPEG frame về game | 65434 |

## 📁 Cấu trúc dự án

```
Boxing_Game/
├── core/                          # Game logic chính (Java)
│   └── src/main/java/
│       ├── main/                  # Entry point (Main extends Game)
│       ├── screen/                # 5 màn hình game
│       ├── entity/                # Fighter, Gymer + components
│       ├── controller/            # FighterController, P1/P2Controller
│       ├── input/                 # InputController interface + implementations
│       ├── system/                # CombatSystem, RoundSystem, GymSession
│       ├── ui/                    # GameHUD, HealthBar, ManaBar, Manual
│       ├── sound/                 # SoundManager
│       ├── effect/                # EffectManager (particle effects)
│       └── util/                  # Constants, AIControllerLauncher
├── lwjgl3/                        # Desktop launcher
├── python_controller/             # Camera AI controller (Python)
│   └── core/
│       ├── main.py                # Orchestrator chính
│       ├── hand_detector.py       # MediaPipe Hands
│       ├── gesture_recognizer.py  # Finger count → action
│       ├── pose_detector.py       # MediaPipe Pose (split P1/P2)
│       ├── pose_gesture_recognizer.py # Body pose → action
│       ├── gym_pose_detector.py   # Gym pose detection
│       ├── gym_pose_recognizer.py # Arm angle → concentric/eccentric
│       └── mediapipe_utils.py     # Model complexity config
├── assets/                        # Textures, audio, videos
├── Design/                        # UML diagrams & design docs
├── build.gradle                   # Gradle build config
└── requirements.txt               # Python dependencies
```

## 🏗️ Công nghệ sử dụng

| Thành phần | Công nghệ |
|------------|-----------|
| Game Framework | LibGDX (OpenGL) |
| Build Tool | Gradle |
| AI / Camera | MediaPipe + OpenCV (Python) |
| Giao tiếp Java ↔ Python | TCP Socket (port 65432, 65433, 65434) |
| Font tiếng Việt | LibGDX FreeType |
| Đồ họa | Sprite nhân vật Ronaldo/Messi, hình nền võ đài + phòng gym |
| Âm thanh | SFX đòn đánh + nhạc nền kịch tính |

## 🎯 OOP Concepts Applied

- **Encapsulation:** Private fields + getter/setter trong Fighter, SoundManager, RoundSystem
- **Inheritance:** FighterController → P1/P2Controller; 5 Screen → ScreenAdapter
- **Polymorphism:** InputController interface — KeyboardInput vs GestureReceiver tại runtime
- **Abstraction:** Interface InputController, abstract class FighterController, Runnable cho TCP threads
- **Design Patterns:** Singleton, Strategy, Template Method, Façade, Adapter, State (enum), Observer (TCP)



## 📄 License

Bài tập lớn môn Lập trình Hướng đối tượng — HUST 2025.
