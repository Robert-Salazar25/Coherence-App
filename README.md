# 🧠 Coherence 
## Junior Developer | 📱 Platform: Android (Kotlin) | ⏳ Status: Funcional Completo
Una aplicación profesional de biofeedback que sirve como interfaz para un dispositivo personalizado construido con ESP32 y sensor MAX30102, desarrollado como proyecto de tesis de TSU en Ingeniería Electrónica. El firmware del dispositivo fue programado en Arduino IDE, enviando datos en formato "TEMP, HR, SPO2, HRV, GSR, STATUS" a través de Bluetooth Low Energy. El sistema completo permite medir y monitorear variables fisiológicas en tiempo real para el análisis de coherencia cardíaca y gestión del estrés.

La aplicación se conecta mediante Bluetooth Low Energy (BLE) al dispositivo personalizado, recibiendo datos crudos que son procesados y visualizados en una interfaz moderna e intuitiva. Implementa algoritmos científicos para calcular la coherencia cardíaca basada en HRV, frecuencia cardíaca, temperatura periférica y conductancia de la piel, proporcionando una herramienta profesional para entrenamiento en biofeedback.

## 🎯 Características Principales

### 📊 Monitoreo en Tiempo Real
- 6 variables fisiológicas: Frecuencia cardíaca (HR), Variabilidad (HRV), Oxígeno (SpO2), Temperatura, Conductancia (GSR), Estado de contacto
- Conexión BLE con ESP32: Integración completa con dispositivo hardware personalizado
- Protocolo Arduino: Datos sincronizados con formato: "36.5, 72, 98, 45, 35, BUENA"
- Parsing robusto: Manejo de conversiones como GSR (×10) y validación de estado

### 🧮 Algoritmos de Coherencia
- Dos algoritmos científicos: Original: HRV (70%) + Temperatura (20%) + GSR (10%), Mejorado: HRV (50%) + HR (20%) + Touch (20%) + Temperatura (10%)
- Cálculo de estrés: Basado en coherencia inversa (0-100%)
- Validaciones hardware: Detección de contacto "BUENA"/"SIN_DEDO" del ESP32

### 🧘 Ejercicios Guiados
- Respiración 4-7-8: Inhala (4s), Mantén (7s), Exhala (8s)
- Animaciones visuales sincronizadas con datos del ESP32
- Sonidos guiados para cada fase de respiración
- Sesiones temporizadas (2, 5, 10, 15, 20 minutos configurables)

### 📈 Historial y Análisis
- Vista semanal y mensual con gráficos interactivos
- Tendencias visuales con gráficos de línea suavizados
- Calendario interactivo con días de actividad
- Detalle completo de sesiones con 5 gráficos individuales
- Exportación y eliminación de datos históricos

### ⚙️ Sistema Completo de Configuración
- Modo oscuro/claro con persistencia
- Recordatorios diarios programables
- Gestión de sonidos y notificaciones
- Duración personalizable de sesiones
- Gestión de permisos BLE (Android 12+ compatible)

## 🏗️ Arquitectura Técnica

### 🛠️ Stack Tecnológico Principal
- Lenguaje: Kotlin 100%
- UI Moderna: Jetpack Compose + Material Design 3
- Arquitectura: Clean Architecture + MVVM
- Base de datos: Room con TypeConverters
- Concurrencia: Coroutines + Flow/StateFlow
- Inyección Dependencias: Dagger Hilt
- BLE: Android Bluetooth Low Energy API
- Persistencia: DataStore para preferencias
- Notificaciones: AlarmManager + NotificationCompat

### 📱 Pantallas Implementadas
- WelcomeScreen - Bienvenida con gestión de permisos BLE
- MainScreen - Dashboard principal con datos en tiempo real del ESP32
- ExerciseScreen - Ejercicio de respiración con animaciones
- HistoryScreen - Historial semanal/mensual con gráficos
- SessionDetailScreen - Análisis detallado post-sesión
- SettingsScreen - Configuración completa de la app

## 🔧 Integración Hardware ESP32 + Arduino

### 🔌 Protocolo de Comunicación
- Dispositivo: ESP32 con firmware Arduino IDE
- Sensor: MAX30102 para mediciones fisiológicas
- Formato datos: "TEMP, HR, SPO2, HRV, GSR, STATUS" 
- UUID BLE: UART estándar (6E400001-B5A3-F393-E0A9-E50E24DCCA9E)
- Estado contacto: "BUENA" (fingerDetected) / "SIN_DEDO"


## 🎓 Lo Que Este Proyecto Demuestra
### 💡 Habilidades Técnicas Comprobadas
- Integración hardware/software: ESP32 (Arduino) + Android (Kotlin)
- Clean Architecture aplicada: Separación real de capas en proyecto complejo
- Protocolos de comunicación: BLE personalizado con parsing robusto
- Algoritmos científicos: Procesamiento de señales fisiológicas
- UI/UX avanzado: Animaciones fluidas y feedback en tiempo real

### 🏗️ Decisiones Arquitectónicas Clave
- Separación real: Domain puro (sin Android), Data (implementaciones), UI (presentación)
- Flujos unidireccionales: UI → ViewModel → UseCase → Repository → Data → Hardware
- Testabilidad: Cada capa puede testearse independientemente
- Escalabilidad: Añadir nuevas fuentes de datos (API, sensores) es trivial
- Mantenibilidad: Cambios aislados gracias a interfaces e inyección

## 👨‍💻 Sobre el Desarrollador
Robert Salazar - Desarrollador Junior apasionado por la ingeniería de software limpia y las aplicaciones con impacto real en la salud y bienestar. Este proyecto demuestra la capacidad de integrar sistemas hardware (ESP32/Arduino) con software móvil profesional, aplicando arquitectura empresarial en un contexto educativo de ingeniería electrónica.

Filosofía de desarrollo:

- Arquitectura sobre conveniencia
- Código mantenible sobre código "inteligente"
- Integración hardware/software fluida
- Aprendizaje continuo mediante proyectos desafiantes

## 📞 Conectemos
- 💼 LinkedIn: https://www.linkedin.com/in/robert-salazar-630250360
- 📧 Email: robl.sala25@gmail.com

¿Buscas un desarrollador Junior con habilidades en integración hardware/software, arquitectura limpia y atención al detalle en proyectos complejos? ¡Hablemos!
