# AI Music Dispatcher 🎵

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17-orange)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.3-green)](https://spring.io/projects/spring-boot)

[简体中文](README.md) | English

AI Music Dispatcher is an intelligent music and barrage (danmaku) processing system designed for live streaming. By integrating AI technologies such as Google Gemini and Edge TTS, it automatically generates personalized host voiceovers for song requests from viewers, supporting real-time barrage processing, permission management, and music library management.

🎥 **Frontend Project**: For the frontend interface for song requests and live stream interactions, please refer to the [dycast](https://github.com/skmcj/dycast) project.

## ✨ Key Features

- 🎤 **AI Voiceover Generation**: Uses Google Gemini 2.0 Flash to automatically generate humorous and witty voiceovers for song requests
- 🔊 **Text-to-Speech**: Integrates Edge TTS, supporting multiple languages including Chinese and English with various voice options
- 💬 **Barrage Processing**: Real-time processing of live stream comments, supporting song requests, gift giving, likes, and other interactive features
- 🎵 **Music Library Management**: Local music library management with search and playlist management support
- 👥 **Permission Management**: Flexible permission system supporting song request permissions based on likes/gifts
- 📊 **Cache Optimization**: Uses Caffeine cache library to optimize performance and reduce redundant API calls
- 🔐 **Exception Handling**: Global exception handling to ensure system stability
- 📝 **Structured Logging**: Detailed logging and tracing using Log4j2

## 🎯 Performance Optimization and Customization

### ⚡ Token Savings and Caching Strategy
- **Smart Caching Mechanism**: AI voiceovers are cached after first generation. Identical songs won't trigger repeated Gemini API calls, significantly reducing Token consumption
- **Cache Storage**: Uses `IntroCache` table for permanent storage of generated voiceovers, allowing new systems to directly reuse historical data

### 📅 Scheduled Update Support
- **Future Extension**: Built-in `PlaybackWorker` scheduled task framework supporting periodic updates to the voiceover content pool
- **Flexible Updates**: Automatically generate new voiceover variants during idle times to keep content fresh
- **Zero-Downtime Updates**: Scheduled tasks run asynchronously with live playback, not affecting live stream service

### 🎤 TTS Tool Customization
- **Open Architecture**: `TtsService` uses a plugin-based architecture, currently supporting Edge TTS
- **Easy Extension**: Users can implement custom TTS providers (for commercial TTS services)
- **Humanization Optimization**: 
  - Support for adjusting speech rate, tone, and other parameters
  - Can integrate industry-leading TTS solutions (such as Microsoft Azure Speech, Baidu, Alibaba, etc.)
  - Multiple voice role options to enhance immersion in live streams
- **Quick Integration**: Refer to [CONTRIBUTING.md](CONTRIBUTING.md) documentation to integrate new TTS tools in 3 steps

## 📋 System Requirements

- Java 17 or higher
- MySQL 8.0+
- Maven 3.8+

## 📁 Music File Preparation

⚠️ **Important Declaration**: All music files played by this system must be legally obtained local music and do not involve obtaining music from online streaming media.

### Music Source Requirements
- ✅ **Recommended** User's personal music collection
- ✅ **Recommended** Music obtained through legitimate channels (with copyright permission)
- ✅ **Recommended** Original creative music content
- ❌ **Prohibited** Unauthorized online music downloads or transcription
- ❌ **Prohibited** Music content that infringes on others' copyrights

### Directory Structure Example
```
music/
├── Artist A/
│   ├── Song 1.mp3
│   ├── Song 2.mp3
│   └── Album 1/
│       └── Song 3.mp3
└── Artist B/
    └── Song 4.mp3
```

### Supported Audio Formats
- **MP3** (Recommended, best compatibility)
- **WAV**
- **FLAC**
- **OGG**

### Music Library Configuration in Database
The system automatically scans music files in the specified directory at startup and stores information in the `MusicLibrary` table for user queries. Users are responsible for ensuring that all music file usage complies with applicable laws and regulations.

## 🚀 Quick Start

**First time using?** 👉 [5-Minute Quick Start Guide](QUICKSTART.md) 👈

### 1. Clone Repository

```bash
git clone https://github.com/chenshoulu202/ai-music-dispatcher.git
cd ai-music-dispatcher
```

### 2. Configure Environment

Modify the `src/main/resources/application.yml` file and configure the following key parameters:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/your_database?useUnicode=true&characterEncoding=utf-8
    username: your_username
    password: your_password

gemini:
  api-key: YOUR_GEMINI_API_KEY  # Obtain from Google AI Studio
  
music:
  local-path: /path/to/your/music  # Local music library path (must contain legally obtained music files)

tts:
  voice: zh-CN-liaoning-XiaobeiNeura  # Choose voice type
  output-dir: /path/to/tts/output
```

> 📌 **Important**: `music.local-path` should point to a directory containing music files you own or have authorized. The system does not handle online streaming media or unauthorized music content.

### 3. Create Database

```bash
# Use the database name configured in application.yml
mysql -u root -p
CREATE DATABASE silver_guardian;
-- Hibernate will automatically create the table structure (based on ddl-auto: update configuration)
```

### 4. Compile and Run

```bash
# Compile the project
mvn clean package

# Run the application
mvn spring-boot:run

# Or run using JAR file
java -jar target/ai-music-dispatcher-0.0.1-SNAPSHOT.jar
```

The application will start at `http://localhost:8080`.

### 5. Start Frontend Application

This backend works with the frontend project [dycast](https://github.com/skmcj/dycast):

```bash
# Clone frontend repository
git clone https://github.com/skmcj/dycast.git
cd dycast

# Install dependencies and start
npm install
npm run dev
```

For more integration details, refer to [INTEGRATION.md](INTEGRATION.md)

## 🏗️ Project Architecture

### System Architecture Overview

This project is designed following a backend microservice architecture:

- **Backend** (this repository): AI Music Dispatcher - Provides WebSocket interface and REST API
- **Frontend** ([dycast](https://github.com/skmcj/dycast)): Provides song request and barrage interaction interface

### Backend Project Structure

```
src/main/java/com/example/aimusicdispatcher
├── config/           # Spring configuration classes
│   ├── GeminiProperties.java       # Gemini API configuration
│   ├── MusicProperties.java        # Music library configuration
│   ├── TtsProperties.java          # TTS configuration
│   ├── PermissionProperties.java   # Permission configuration
│   └── ws/
│       └── WebSocketConfig.java    # WebSocket configuration
├── connector/        # Connector layer
│   ├── BarrageController.java      # Barrage controller
│   └── DyWebSocketHandler.java     # WebSocket handler
├── dispatcher/       # Message dispatcher
│   ├── MessageDispatcher.java      # Message dispatch logic
│   └── BarrageFilterService.java   # Barrage filter service
├── generator/        # AI generation services
│   ├── GeminiService.java          # Gemini API calls
│   ├── TtsService.java             # TTS text-to-speech
│   └── TextCleaningService.java    # Text cleaning
├── entity/           # Database entities
│   ├── IntroCache.java             # Voiceover cache
│   └── MusicLibrary.java           # Music library
├── model/            # Data models
│   ├── barrage/      # Barrage related
│   ├── dy/           # Douyin live related
│   ├── gemini/       # Gemini responses
│   └── playlist/     # Playlist
├── repository/       # Data persistence layer
├── scheduler/        # Scheduled tasks
├── service/          # Business service layer
├── util/             # Utility classes
└── AiMusicDispatcherApplication.java  # Main application class
```

## 🔌 API Endpoints

### WebSocket Connection
- **Connection URL**: `ws://localhost:8080/ws/barrage`
- **Message Format**: JSON

#### Supported Message Types

1. **Song Request** (SongRequest)
   ```json
   {
     "type": "song_request",
     "userId": "user123",
     "userName": "Username",
     "songName": "Song Name",
     "artistName": "Artist Name",
     "timestamp": 1708684800000
   }
   ```

2. **Gift Event** (GiftEvent)
   ```json
   {
     "type": "gift",
     "userId": "user123",
     "userName": "Username",
     "giftName": "Gift Name",
     "quantity": 5,
     "timestamp": 1708684800000
   }
   ```

3. **Like Event** (LikeEvent)
   ```json
   {
     "type": "like",
     "userId": "user123",
     "userName": "Username",
     "count": 10,
     "timestamp": 1708684800000
   }
   ```

### REST Endpoints

- `GET /api/music/search?keyword=song_name` - Search music
- `GET /api/music/library` - Get music library list
- `GET /api/permission/check?userId=xxx` - Check user permission

## ⚙️ Configuration Guide

### Gemini Configuration

| Parameter | Description | Default Value |
|-----------|-------------|--------|
| `gemini.api-key` | Google Gemini API Key | - |
| `gemini.api-url` | API endpoint address | `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent` |
| `gemini.system-prompt` | System prompt | Preset live DJ prompt |

### TTS Configuration

| Parameter | Description | Default Value |
|-----------|-------------|--------|
| `tts.provider` | TTS service provider | `edge-tts` |
| `tts.voice` | Voice type | `zh-CN-liaoning-XiaobeiNeura` |
| `tts.rate` | Speech rate adjustment (-1.0~1.0) | `-0.15` |
| `tts.output-dir` | Output directory | `./tts_output` |
| `tts.audio-format` | Audio format | `mp3` |
| `tts.sample-rate` | Sample rate (Hz) | `44100` |

### Permission Configuration

| Parameter | Description | Default Value |
|-----------|-------------|--------|
| `app.permission.enabled` | Enable permission verification | `true` |
| `app.permission.like-minutes` | Permission duration for likes | `5` |
| `app.permission.gift-minutes` | Permission duration for gifts | `20` |

## 📦 Dependency List

- **Spring Boot 3.2.3** - Web framework
- **Spring WebSocket** - WebSocket support
- **Spring Data JPA** - Data persistence
- **MySQL Connector** - Database driver
- **Caffeine Cache** - Local cache
- **Log4j2** - Logging framework
- **Lombok** - Code simplification
- **Hibernate** - ORM framework

## 🛠️ Development Guide

### Project Structure Convention

- `service/` - Core business logic
- `repository/` - Database interaction
- `controller/` - HTTP and WebSocket endpoints
- `model/` - DTOs and entity classes
- `config/` - Spring configuration classes
- `util/` - Utility classes and helper functions

### Contributing Code

1. Fork this repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

For more details, please see [CONTRIBUTING.md](CONTRIBUTING.md)

## 📝 Logging

The application uses Log4j2 for logging, with configuration file located at `src/main/resources/log4j2.xml`.

Log level descriptions:
- **DEBUG** - Detailed debug information
- **INFO** - General information
- **WARN** - Warning information
- **ERROR** - Error information

## 🌟 Related Projects

### Frontend Project

| Project | Description | Link |
|---------|-------------|------|
| **dycast** | Frontend interface for song requests and live stream interactions, works with this backend | [GitHub](https://github.com/skmcj/dycast) |

### Complete System Architecture

To run a complete live stream song request system, you need to deploy both this project (backend) and dycast (frontend).

See [Integration Guide](INTEGRATION.md), which includes:
- System architecture and data flow
- Frontend and backend communication protocol
- Deployment and running instructions
- FAQs

## 🐛 Known Issues

- None

## 📄 License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

## 🙏 Acknowledgments

Thanks to the following projects for inspiration and support:

**Frontend Project:**
- [dycast](https://github.com/skmcj/dycast) - Song request and live stream interaction frontend providing complete user interface

**Backend Dependencies:**
- [Spring Boot](https://spring.io/projects/spring-boot) - Java Web framework
- [Google Gemini](https://gemini.google.com) - AI model service
- [Edge TTS](https://github.com/rany2/edge-tts) - Text-to-speech service

## 📧 Contact: vx:chenshoulu202

- **Issues**: Use GitHub Issues to report problems or make suggestions
- **Discussions**: Use GitHub Discussions for technical discussions

---

⭐ If this project helps you, please give it a Star!
