# Forsaken: Advanced Remote Administration & Automation Tool (RAT) via Telegram

**Forsaken** is an elite, highly modular remote administration and system management framework engineered specifically for Android devices. By leveraging the Telegram Bot API as a secure, serverless Command and Control (C2) interface, Forsaken empowers developers and power users to monitor, automate, and manage their devices globally without requiring any external backend infrastructure, VPS, or database hosting.

---

## ⚠️ Disclaimer & Educational Purposes ONLY
**IMPORTANT:** This project is strictly designed for **EDUCATIONAL PURPOSES, SYSTEM MANAGEMENT, AND DEVICE AUTOMATION ONLY**. Unauthorized access to devices, data theft, or spying on others is illegal and punishable by law. The developer takes no responsibility for any misuse of this tool. Use this software exclusively on your own devices for advanced Android system architecture, security research, and personal automation testing.

---

## 🌟 What Makes Forsaken Unique? (The Edge Over Standard Tools)

Unlike generic or basic Telegram-based remote scripts that rely solely on rudimentary shell execution, **Forsaken** is built like a robust production-grade application with several architectural breakthroughs:

*   📱 **Shizuku Privilege Escalation (`/shizuku`):** Rather than being restricted by unprivileged shell limits or requiring full, detectable root (`su`) binaries, Forsaken integrates **Shizuku**. This allows you to execute powerful, near-root system commands safely and cleanly through binder communication.
*   🤖 **Advanced Accessibility Automation (`/grapy`):** Forsaken features an intelligent accessibility engine capable of deep node tree inspection (`see`), coordinate-based touch simulation (`touch`), scrolling, text injection, and automated permission granting. It can "see" and interact with UI elements programmatically.
*   🔄 **Smart Long-Output Chunking:** Telegram imposes a strict 4000-character limit per message. Forsaken features an automated text-segmentation algorithm that intelligently splits oversized terminal outputs, logs, or file lists into sequential chunks with built-in throttling, ensuring **zero data loss**.
*   ⚡ **Live Clipboard & Event Interception:** Unlike tools that only pull data on-demand, Forsaken runs a persistent background listener that intercepts and reports newly copied clipboard contents in real time.
*   🧩 **Clean Modular Architecture:** Built with decoupled helper controllers (`SettingsController`, `ConnectionsHelper`, `SensorHelper`, `AudioPlayerHelper`, etc.) rather than a messy monolithic script, ensuring high maintainability and extensibilidad.

---

## 🚀 Key Features & Architecture

*   **Serverless C2 Pipeline:** Operates entirely via Telegram Bot API long-polling. Your device talks directly to Telegram's encrypted API endpoints.
*   **Persistent Foreground Service:** Powered by `ShellService` with a `START_STICKY` implementation and custom notification channel to guarantee continuous background operation and automatic recovery if terminated by aggressive OS memory killers.
*   **Comprehensive Granular Control:** Ranging from fine-grained hardware configurations (`/settings`, `/volume`, `/sensor`) to stealth media capture (`/cam_front`, `/cam_back`, `/microphone`, `/screenrecord`).
*   **Robust File & Process Engine:** Full directory exploration, zip archiving, remote file downloading, direct uploads, and app package lifecycle management (`/apps`, `/kill`, `/apk`).

---

## ⚙️ Setup and Configuration

### Requirements
*   **Development Environment:** AndroidIDE or Android Studio (Java environment)
*   **Telegram Bot Token:** Obtained from [@BotFather](https://t.me/BotFather)
*   **Telegram Admin Chat ID:** Obtained from [@userinfobot](https://t.me/userinfobot)

### Setup Steps
1.  **Create a Bot:** Message `@BotFather` on Telegram, create a new bot, and copy your `BOT_TOKEN`.
2.  **Get Chat ID:** Message `@userinfobot` to retrieve your numeric Telegram `CHAT_ID`.
3.  **Configure Credentials:**
    *   Navigate to `app/src/main/assets/config.txt` in the project structure.
    *   Fill in your credentials:
        ```text
        BOT_TOKEN=your_bot_token_here
        ADMIN_CHAT_ID=your_chat_id_here
        ```
4.  **Build & Install:** Compile the project using your IDE (`Build` or `assembleDebug`) and install the APK on the target device.

---

## 🛠️ Complete Operational Command Reference

Here is the complete command set available via your Telegram bot interface:

### ⚙️ System & Control Commands
*   `/help [command]` - Displays the main interactive menu or detailed help for a specific command.
*   `/settings list` - Lists all modifiable system settings.
*   `/settings <setting> <value>` - Changes system settings (flashlight, screen rotation, airplane mode, DND, screen timeout, etc.).
*   `/volume <type> <percent>` - Adjusts volume levels (`media`, `ring`, `alarm`, `call`).
*   `/sysinfo` - Detailed device system, battery health/temperature, RAM, storage, and hardware report.
*   `/vibrate [ms]` - Vibrates the device for the specified millisecond duration.
*   `/sensor [type|list]` - Reads live hardware sensor data.
*   `/reset_all` - Resets all configurations and temporary working data.

### 📁 File and Storage Management
*   `/file <directory>` - Lists files and folders in the specified path.
*   `/data` - Lists the panel working directory (`TARGET_DIR`).
*   `/download <path>` - Sends a file from the device directly to Telegram.
*   `/upload` - Saves a file or photo to the device by sending it as a message to the bot.
*   `/filexp [command]` - Advanced file manager infrastructure engine.
*   `/find <name>` - Searches for files and directories across storage.
*   `/zip <path>` - Compresses a target folder into a ZIP archive.

### 🚀 Applications and Processes
*   `/apps` - Lists all installed applications.
*   `/open_app <package>` - Launches a specific application package.
*   `/kill <package>` - Force-terminates a running application process.
*   `/apk <path>` - Silently or interactively installs the specified APK file via Shizuku.
*   `/open_site <url>` - Opens a website link in the device browser.
*   `/send_intent <action> [data] [pkg]` - Broadcasts custom Android Intents.
*   `/toast <message>` - Shows a custom toast message on the device screen.
*   `/popup <title>,<message>,<duration>` - Displays an alert popup dialog on the device screen.
### 📍 Media and Automation
*   `/locate` - Gets instant GPS/Network coordinates and generates an active Google Maps link.
*   `/screenshot` - Captures an instant high-resolution screenshot.
*   `/cam_front` - Takes a hidden background photo using the front camera.
*   `/cam_back` - Takes a hidden background photo using the rear camera.
*   `/microphone [seconds]` - Records ambient audio and transmits the audio file.
*   `/screenrecord [seconds|stop]` - Starts or stops real-time screen video recording.
*   `/grapy <see|touch|scroll>` - Accessibility screen analysis, touch tap, and scroll automation.
*   `/wallpaper [path]` - Changes the device wallpaper from a local path or recent capture.
*   `/play <file_path>` - Plays an audio file locally on the device.
*   `/stop_audio` - Stops playing audio media.
*   `/tts <text>` - Converts text to speech on the device and reads it aloud.

### 💬 Communication and Data
*   `/sms_list` - Lists recently received SMS messages.
*   `/sms_send <phone> <msg>` - Sends an SMS to the specified phone number.
*   `/calls` - Lists device call history logs.
*   `/phone` - Dumps phonebook contacts.
*   `/callnum <no>` - Dials and calls the specified number.
*   `/getclip` - Reads text from the device clipboard.
*   `/setclip <text>` - Writes text to the device clipboard.
*   `/accounts` - Lists registered user accounts on the device.
*   `/permission` - Reports critical runtime permission statuses.
*   `/media_log` - Lists media playback logs.

### 🌐 Connection and Network
*   `/connections <action> [sub] [param]` - Advanced connection and network diagnostic tools.
*   `/http <get|post|ping> <target>` - Executes network requests from the device.

### 💻 Shell and Privilege
*   `/shell <command>` - Executes a standard shell (`sh`) command.
*   `/shizuku <command>` - Executes elevated commands using Shizuku privileges.

---

## 💡 How It Works (Under the Hood)

1.  **Initialization & Security:** Upon boot or manual launch, `ShellService` initializes as a foreground service. It reads `config.txt` to load credentials and validates every incoming Telegram message ID strictly against the authorized `ADMIN_CHAT_ID`. Unrecognized or unauthorized senders are instantly filtered out.
2.  **Polling Pipeline:** The background worker continuously polls Telegram's `getUpdates` endpoint. When a command is received, it routes the payload to the corresponding helper controller or shell execution handler.
3.  **Delivery & Chunking:** Processed outputs are formatted, evaluated against Telegram's character limits, chunked if necessary, and delivered securely back to the admin chat in real time.

---
*Architected and developed with precision by **JavaKyoseva** | [GitHub Repository](https://github.com/JavaKyoseva/Rat-via-Telegram)*
