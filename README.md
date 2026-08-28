# Forsaken: Advanced Remote Administration Tool (RAT) via Telegram

Forsaken is an advanced remote administration system designed for Android devices, utilizing the Telegram Bot API as a command and control (C2) interface. It allows users to manage and monitor their devices seamlessly from anywhere.

## ⚠️ Disclaimer & Educational Purposes ONLY
**IMPORTANT:** This project is for **EDUCATIONAL PURPOSES ONLY**. Unauthorized access to devices, data theft, or spying on others is illegal and punishable by law. The developer takes no responsibility for any misuse of this tool. Use this software only on your own devices for the purpose of learning cybersecurity, Android system architecture, and network communication.

## 🚀 Key Features
*   **Telegram-Based C2:** Manage your device via Telegram bot commands.
*   **Foreground Service:** Operates continuously in the background to ensure persistent connectivity.
*   **Comprehensive Command Set:**
    *   Remote Screenshots (`/screenshot`)
    *   Camera Access (Front/Back) (`/cam_front`, `/cam_back`)
    *   Audio Recording (`/microphone`)
    *   File Management & Transfer (`/file`, `/upload`, `/download`)
    *   SMS & Call Logs (`/sms_list`, `/calls`)
    *   Shell & Shizuku Command Execution (`/shell`, `/shizuku`)
    *   And many more...

## ⚙️ Setup and Configuration

### Requirements
*   AndroidIDE or Android Studio (Java Development Environment)
*   Telegram Bot API Token (Get it from @BotFather)
*   Telegram Admin Chat ID (Get your own ID from @userinfobot)

### Setup Steps
1.  **Create a Bot:** Create a new bot via `@BotFather` on Telegram and save the `API_TOKEN`.
2.  **Get Chat ID:** Use `@userinfobot` to find your Telegram `CHAT_ID`.
3.  **Configuration:**
    *   Navigate to `app/src/main/assets/config.txt` in the project.
    *   Fill in the required fields:
        ```text
        BOT_TOKEN=your_token_here
        ADMIN_CHAT_ID=your_chat_id_here
        ```
4.  **Build:** Compile the project using your IDE (`Build` or `assembleDebug`).

## 💡 How It Works
Forsaken runs as a `Foreground Service` named `ShellService`. Once the app is installed and launched, the service initializes and starts listening for commands via the Telegram API.

*   **Command Processing:** Incoming messages are parsed by the `handleCommand` method within the application.
*   **Security:** The system validates the sender's ID against the `ADMIN_CHAT_ID` defined in the config file. Only authorized users can execute commands.
*   **Persistence:** The service utilizes `START_STICKY`, ensuring that it attempts to restart if it is terminated by the system.

## 🛡️ Security & Privacy Notice
As this tool provides full remote control capabilities, it is categorized as a "Potentially Unwanted Application" (PUA) by security scanners. Modern Android systems (Android 10+) may restrict background service execution. For testing, ensure you disable "Battery Optimization" for this application to maintain continuous operation.

---
*Developed by JavaKyoseva | [GitHub Repository](https://github.com/JavaKyoseva/Rat-via-Telegram)*
