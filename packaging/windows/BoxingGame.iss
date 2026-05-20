#define MyAppName "BoxingGame"
#define MyAppVersion "1.0.0"
#define MyAppPublisher "BoxingGame Team"
#define MyAppExeName "Launcher.bat"
#define MyAppDirName "BoxingGame"
#define MyOutputBaseFilename "BoxingGame_Installer"

[Setup]
AppId={{7E7AFB0A-6B58-4F04-9A34-A612D4A2F8B1}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
DefaultDirName={autopf}\{#MyAppDirName}
; Optional no-admin alternative:
; DefaultDirName={userappdata}\{#MyAppDirName}
DefaultGroupName={#MyAppName}
OutputDir=.
OutputBaseFilename={#MyOutputBaseFilename}
Compression=lzma2/max
SolidCompression=yes
WizardStyle=modern
ArchitecturesAllowed=x64compatible
ArchitecturesInstallIn64BitMode=x64compatible
PrivilegesRequired=admin
SetupIconFile=..\..\lwjgl3\icons\logo.ico
UninstallDisplayIcon={app}\logo.ico

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "Create a desktop shortcut"; GroupDescription: "Additional icons:"; Flags: unchecked

[Files]
Source: "..\..\lwjgl3\build\libs\BoxingGame-1.0.0.jar"; DestDir: "{app}"; Flags: ignoreversion
Source: "..\..\python_controller\dist\AI_Controller\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "..\..\assets\*"; DestDir: "{app}\assets"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "jre\*"; DestDir: "{app}\jre"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "Launcher.bat"; DestDir: "{app}"; Flags: ignoreversion
Source: "..\..\lwjgl3\icons\logo.ico"; DestDir: "{app}"; DestName: "logo.ico"; Flags: ignoreversion

[Icons]
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; WorkingDir: "{app}"; IconFilename: "{app}\logo.ico"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; WorkingDir: "{app}"; IconFilename: "{app}\logo.ico"; Tasks: desktopicon

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "Launch {#MyAppName}"; Flags: nowait postinstall skipifsilent

