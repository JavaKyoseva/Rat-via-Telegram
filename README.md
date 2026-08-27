# Forsaken: Advanced Remote Administration Tool (RAT) via Telegram

**Forsaken** is an advanced remote administration and system management tool designed for Android devices, utilizing the Telegram Bot API as a serverless Command and Control (C2) interface. It allows users to securely manage, monitor, and automate their devices from anywhere in the world without requiring an external server setup.

## ⚠️ Disclaimer & Educational Purposes ONLY
**IMPORTANT:** This project is strictly for **EDUCATIONAL PURPOSES AND PERSONAL SYSTEM MANAGEMENT ONLY**. Unauthorized access to devices, data theft, or spying on others is illegal and punishable by law. The developer takes no responsibility for any misuse of this tool. Use this software only on your own devices for learning cybersecurity, Android system architecture, automation, and network communication.

## 🚀 Key Features & Architecture

*   **Serverless C2 Infrastructure:** Operates entirely via Telegram Bot API long-polling. No dedicated backend server or database hosting is required; Telegram handles the communication pipeline securely.
*   **Persistent Foreground Service:** Powered by `ShellService` with `START_STICKY` implementation, ensuring continuous background operation and automatic recovery if terminated by the system.
*   **Advanced Shizuku & Shell Execution:** Execute standard shell commands or elevate privileges using Shizuku (`/shizuku`) with built-in binder status and permission pre-checks.
*   **Accessibility & Screen Automation (`/grapy`):** Deep node inspection (`see`), coordinate-based touch simulation (`touch`), text injection, and automatic permission granting.
*   **Live Clipboard Monitoring:** Automatically intercepts and reports newly copied clipboard data to the admin in real time.
*   **Long-Output Chunking:** Automatically segments responses exceeding Telegram's 4000-character limit to ensure complete data delivery.
*   **Comprehensive Toolset:** From fine-grained device settings (`/settings`, `/volume`) to media capture, file explorers, and sensor diagnostics.

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

## 🛠️ Complete Command Reference

Here is the complete operational command set available via your Telegram bot:

### ⚙️ System & Control Commands
*   `/help [komut]` - Displays the main menu or detailed help for a specific command.
*   `/settings list` - Lists all configurable device settings.
*   `/settings <ayar> <değer>` - Manages system settings (flash, rotation, airplane mode, DND, screen timeout, etc.).
*   `/volume <tür> <yüzde>` - Adjusts volume levels (`media`, `ring`, `alarm`, `call`).
*   `/sysinfo` - Provides a comprehensive device status report (Battery health/temperature, RAM, Storage, CPU specs).
*   `/vibrate [ms]` - Triggers haptic vibration for a specified duration.
*   `/sensor [tür|list]` - Reads live hardware sensor data.
*   `/reset_all` - Clears temporary data and resets configurations.

### 📁 File & Storage Management
*   `/file <dizin>` - Lists files and folders in a specified path.
*   `/data` - Lists the application's working target directory (`/storage/emulated/0/panel/`).
*   `/download <yol>` - Sends a device file directly to Telegram.
*   `/upload` - Upload files or photos directly to the bot chat to save them to the device.
*   `/filexp [komut]` - Advanced file manager engine.
*   `/find <isim>` - Searches for files and directories across storage.
*   `/zip <yol>` - Compresses a target folder into a ZIP archive.

### 🚀 Apps & Processes
*   `/apps` - Lists all installed applications.
*   `/open_app <paket>` - Launches a specific application package.
*   `/kill <paket>` - Force-stops a running application process.
*   `/apk <yol>` - Silently or interactively installs an APK via Shizuku.
*   `/open_site <url>` - Opens a web URL in the device browser.
*   `/send_intent <action> [data] [pkg]` - Broadcasts custom Android Intents.

### 📍 Media & Automation
*   `/locate` - Fetches precise GPS/Network coordinates and sends a Google Maps link.
*   `/screenshot` - Captures an instant high-resolution screenshot.
*   `/cam_front` / `/cam_back` - Captures secret photos using front or rear cameras.
*   `/microphone [saniye]` - Records ambient audio and sends the audio file.
*   `/screenrecord [saniye|stop]` - Starts or stops video screen recording.
*   `/grapy <see|touch>` - Accessibility-driven screen node inspection and tap automation.
*   `/wallpaper [yol]` - Sets the device wallpaper from a local path or recent capture.
*   `/play <dosya_yolu>` - Plays an audio file locally.
*   `/stop_audio` - Stops media playback.
*   `/tts <metin>` - Converts text to speech and reads it aloud on the device.

### 💬 Communication & Data
*   `/sms_list` - Retrieves recent SMS messages.
*   `/sms_send <tel> <msj>` - Sends an SMS to a specific number.
*   `/calls` - Lists call history logs.
*   `/phone` - Dumps device contacts.
*   `/callnum <no>` - Initiates a phone call.
*   `/getclip` - Reads the current clipboard text.
*   `/setclip <metin>` - Writes text to the device clipboard.
*   `/accounts` - Lists registered user accounts on the device.
*   `/permission` - Reports critical runtime permission statuses.
*   `/media_log` - Retrieves media playback logs.

### 🌐 Connectivity & Network
*   `/connections <aksiyon> [alt] [param]` - Network and connection diagnostic tools.
*   `/http <get|post|ping> <hedef>` - Executes network requests from the device.

### 💻 Shell & Privilege
*   `/shell <komut>` - Executes standard shell (`sh`) commands.
*   `/shizuku <komut>` - Executes elevated commands via Shizuku integration.

---

## 💡 How It Works (Under the Hood)

1.  **Initialization:** Upon boot or manual launch, `ShellService` starts as a foreground notification service to prevent OS background termination. It reads `config.txt` for Telegram credentials.
2.  **Polling & Security:** The background worker polls Telegram's `getUpdates` API. Every incoming command message ID is strictly validated against the authorized `ADMIN_CHAT_ID`. Unauthorized senders are ignored.
3.  **Execution & Response:** Commands are routed to specialized helper classes or shell executors. Output data is formatted, split if it exceeds Telegram limits, and delivered back to the admin chat instantly.

## 🛡️ Security & Optimization Notice
Because this application possesses remote administration capabilities, security scanners may flag it as a "Potentially Unwanted Application" (PUA). On modern Android systems (Android 10+), ensure you **disable battery optimization** for the app to guarantee uninterrupted background connectivity.

---
*Developed with dedication by **JavaKyoseva** | [GitHub Repository](https://github.com/JavaKyoseva/Rat-via-Telegram)*
