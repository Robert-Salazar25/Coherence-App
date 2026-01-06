# 🧠 Coherence 
## Junior Developer | 📱 Platform: Android (Kotlin) | ⏳ Status: Complete & Functional
A professional biofeedback application serving as an interface for a custom device built with ESP32 and MAX30102 sensor, developed as a TSU thesis project in Electronic Engineering. The device firmware was programmed in Arduino IDE, sending data in "TEMP, HR, SPO2, HRV, GSR, STATUS" format via Bluetooth Low Energy. The complete system allows measuring and monitoring physiological variables in real-time for heart coherence analysis and stress management.

The application connects via Bluetooth Low Energy (BLE) to the custom device, receiving raw data that is processed and visualized in a modern, intuitive interface. It implements scientific algorithms to calculate heart coherence based on HRV, heart rate, peripheral temperature, and skin conductance, providing a professional tool for biofeedback training.

## 🎯  Key Features

### 📊 Real-Time Monitoring
- 6 physiological variables: Heart rate (HR), Variability (HRV), Oxygen (SpO2), Temperature, Skin Conductance (GSR), Contact status
- BLE connection with ESP32: Complete integration with custom hardware device
- Arduino protocol: Data synchronized in format: "36.5, 72, 98, 45, 35, BUENA"
- Robust parsing: Handling conversions like GSR (×10) and status validation

### 🧮 Coherence Algorithms
- Two scientific algorithms: Original: HRV (70%) + Temperature (20%) + GSR (10%), Improved: HRV (50%) + HR (20%) + Touch (20%) + Temperature (10%)
- Stress calculation: Based on inverse coherence (0-100%)
- Hardware validations: Contact detection "BUENA"/"SIN_DEDO" from ESP32

### 🧘 Guided Exercises
- 4-7-8 Breathing: Inhale (4s), Hold (7s), Exhale (8s)
- Visual animations synchronized with ESP32 data
- Guided sounds for each breathing phase
- Timed sessions (2, 5, 10, 15, 20 minutes configurable)

### 📈 History & Analysis
- Weekly and monthly view with interactive charts
- Visual trends with smoothed line graphs
- Interactive calendar with activity days
- Complete session detail with 5 individual charts
- Export and deletion of historical data

### ⚙️ Complete Configuration System
- Dark/light mode with persistence
- Programmable daily reminders
- Sound and notification management
- Customizable session duration
- BLE permission management (Android 12+ compatible)

## 🏗️ Technical Architecture

### 🛠️ Main Technology Stack
- Language: Kotlin 100%
- Modern UI: Jetpack Compose + Material Design 3
- Architecture: Clean Architecture + MVVM
- Database: Room with TypeConverters
- Concurrency: Coroutines + Flow/StateFlow
- Dependency Injection: Dagger Hilt
- BLE: Android Bluetooth Low Energy API
- Notifications: AlarmManager + NotificationCompat

### 📱 Implemented Screens
- WelcomeScreen - Welcome with BLE permission management
- MainScreen - Main dashboard with real-time ESP32 data 
- ExerciseScreen - Breathing exercise with animations
- HistoryScreen - Weekly/monthly history with charts
- SessionDetailScreen - Detailed post-session analysis
- SettingsScreen - Complete app configuration

## 🔧 ESP32 + Arduino Hardware Integration

### 🔌 Communication Protocol
- Device: ESP32 with Arduino IDE firmware
- Sensor: MAX30102 for physiological measurements
- Data format: "TEMP, HR, SPO2, HRV, GSR, STATUS"
- BLE UUID: Standard UART (6E400001-B5A3-F393-E0A9-E50E24DCCA9E) 
- Contact status: "BUENA" (fingerDetected) / "SIN_DEDO" (no finger)

## 🎓 What This Project Demonstrates
### 💡 Proven Technical Skills
- Hardware/software integration: ESP32 (Arduino) + Android (Kotlin)
- Applied Clean Architecture: Real layer separation in complex project
- Communication protocols: Custom BLE with robust parsing
- Scientific algorithms: Physiological signal processing
- Advanced UI/UX: Fluid animations and real-time feedback

### 🏗️ Key Architectural Decisions
- Real separation: Pure domain (no Android), Data (implementations), UI (presentation)
- Unidirectional flows: UI → ViewModel → UseCase → Repository → Data → Hardware
- Testability: Each layer can be tested independently
- Scalability: Adding new data sources (APIs, sensors) is trivial
- Maintainability: Isolated changes thanks to interfaces and injection

## 👨‍💻 About the Developer
Robert Salazar - Junior Developer passionate about clean software engineering and applications with real impact on health and well-being. This project demonstrates the ability to integrate hardware systems (ESP32/Arduino) with professional mobile software, applying enterprise architecture in an educational electronic engineering context.

Development philosophy:

- Architecture over convenience
- Maintainable code over "clever" code
- Fluid hardware/software integration
- Continuous learning through challenging projects

## 📞 Let's Connect
- 💼 LinkedIn: https://www.linkedin.com/in/robert-salazar-630250360
- 📧 Email: robl.sala25@gmail.com

Looking for a Junior Developer with skills in hardware/software integration, clean architecture, and attention to detail in complex projects? Let's talk!
