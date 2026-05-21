# BoxingGame

[TODO: One-line project description]

## Download

- Windows build: https://drive.google.com/drive/folders/13cnuhkRQYDdwi1-vYE1ColNT0CXwtlDu?hl=vi

## Features

- 1v1 boxing gameplay (keyboard + camera modes)
- Gym training mini-game
- Skill cutscene animations
- HUD (HP/Mana/Round)

## Controls

**Keyboard**

- **P1:** D = Punch, A = Kick, W = Block, S = Duck, SPACE = Skill
- **P2:** RIGHT = Punch, LEFT = Kick, UP = Block, DOWN = Duck, ENTER = Skill

**Camera AI (Finger)**

- 0 = IDLE, 1 = Punch, 2 = Kick, 3 = Block, 4 = Duck, 5 = Skill

**Gym**

- ENTER = Concentric, SPACE = Eccentric, ESC = Menu

## Requirements

- Java: [TODO]
- Gradle: included via wrapper
- Python (for camera modes): [TODO]

## Run (Desktop)

```bash
.\gradlew.bat lwjgl3:run
```

## Camera AI Setup

Install Python dependencies:

```bash
pip install -r python_controller\requirements.txt
```

Supported modes (auto-launched by the game):

- `CAMERA_AI`
- `CAMERA_POSE`
- `CAMERA_GYM_POSE`

## Project Structure

- `core/` – main game logic
- `lwjgl3/` – desktop launcher
- `python_controller/` – camera AI controller
- `assets/` – textures, audio, videos
- `Design/` – diagrams and design docs

## Screenshots

[TODO: add images]

## Credits

[TODO]

## License

[TODO]
