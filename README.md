# AI Music Dispatcher 🎵

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17-orange)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.3-green)](https://spring.io/projects/spring-boot)

简体中文 | [English](README_EN.md)

AI Music Dispatcher 是一个针对直播间的智能音乐和弹幕处理系统。通过集成 Google Gemini、Edge TTS 等 AI 技术，自动为观众点歌生成个性化的主播口播，支持实时弹幕处理、权限管理和音乐库管理。

🎥 **前端项目**: 点歌和直播间互动的前端界面请参考 [dycast](https://github.com/skmcj/dycast) 项目。

## ✨ 主要特性

- 🎤 **AI 口播生成**: 使用 Google Gemini 2.0 Flash 为点歌自动生成幽默风趣的口播文案
- 🔊 **文本转语音**: 集成 Edge TTS，支持中文/英文等多语言和多种语音选择
- 💬 **弹幕处理**: 实时处理直播间弹幕，支持点歌、送礼、点赞等互动功能
- 🎵 **音乐库管理**: 本地音乐库管理，支持查询和播放列表管理
- 👥 **权限管理**: 灵活的权限系统，支持基于点赞/送礼的点歌权限控制
- 📊 **缓存优化**: 使用 Caffeine 缓存库优化性能，减少重复 API 调用
- 🔐 **异常处理**: 全局异常处理，确保系统稳定性
- 📝 **结构化日志**: 使用 Log4j2 进行详细的日志记录和追踪

## 🎯 性能优化与定制化

### ⚡ 令牌节省与缓存策略
- **智能缓存机制**: AI 口播文案在首次生成后会被缓存，相同的歌曲不会重复调用 Gemini API，大幅节省 Token 消耗
- **缓存存储**: 使用 `IntroCache` 表永久存储已生成的口播，新上线的系统可以直接复用历史数据

### 📅 定时更新支持
- **后续扩展**: 内置 `PlaybackWorker` 定时任务框架，支持定期更新口播内容池
- **灵活更新**: 可根据需要在闲时自动生成新的口播变体，保持内容新鲜度
- **零停机更新**: 定时任务与实时播放异步执行，不影响直播间服务

### 🤖 AI 大模型定制化
现在支持多种 AI 大模型选择！默认配置使用 Google Gemini，但你可以轻松替换为其他喜欢的大模型。

**支持的大模型方案：**
- ✅ **Google Gemini** (默认) - Google 官方高性能模型，API: `https://generativelanguage.googleapis.com`
- ✅ **OpenAI GPT 系列** - GPT-4, GPT-3.5 等，API: `https://api.openai.com/v1/chat/completions`
- ✅ **Anthropic Claude** - 优秀的文本生成能力，API: `https://api.anthropic.com/v1/messages`
- ✅ **阿里云通义千问** - 国内优质选择，API: `https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation`
- ✅ **讯飞星火大模型** - 国内另一选择，API: `https://spark-api.xf-yun.com/v1/chat/completions`
- ✅ **其他任何兼容 API 的大模型** - 自定义扩展

**快速替换步骤：**

1. **修改 `application.yml` 中的 API 配置**
   ```yaml
   # 替换为 OpenAI
   gemini:
     api-key: "sk-your-openai-api-key"
     api-url: https://api.openai.com/v1/chat/completions
     system-prompt: "你是一个幽默风趣的直播间DJ..."
   
   # 或替换为阿里通义千问
   gemini:
     api-key: "sk-your-dashscope-api-key"
     api-url: https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation
   ```

2. **修改 `GeminiService.java` 中的请求/响应处理**（如果 API 格式不同）
   - 调整 `buildGeminiRequest()` 方法以适配目标模型的请求格式
   - 修改 `extractTextFromGeminiResponse()` 方法以解析目标模型的响应格式

3. **重新编译运行**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

**各模型优缺点对比：**

| 模型 | 优点 | 缺点 | 适合场景 |
|------|------|------|---------|
| **Google Gemini** | 免费配额足、性能好、多模态 | 需国际网、API稍复杂 | 个人使用、低成本需求 |
| **OpenAI GPT** | 强大、稳定、社区大 | 收费、需国际支付 | 专业应用、商业化 |
| **Claude** | 生成质量高、理解力强 | 收费、响应较慢 | 内容质量优先 |
| **通义千问** | 国内直连、支持中文优、便宜 | 免费额度相对少 | 国内用户首选 |
| **讯飞星火** | 国内支持、成本低 | 模型能力有限 | 成本优先 |

**完整的扩展指南请参考 [CONTRIBUTING.md](CONTRIBUTING.md)**

### 🎤 TTS 工具定制化
- **开放式设计**: `TtsService` 采用插件化架构，目前支持 Edge TTS
- **易于扩展**: 用户可以自行实现自定义 TTS 提供商（如果使用商业 TTS 服务）
- **拟人化优化**: 
  - 支持调节语速、音调等参数
  - 可集成业界更好的 TTS 方案（如Microsoft Azure Speech、百度、阿里等）
  - 支持多种语音角色选择，增强直播间代入感
- **快速集成**: 参考 [CONTRIBUTING.md](CONTRIBUTING.md) 文档，3步即可集成新的 TTS 工具

## 📋 系统需求

- Java 17 或更高版本
- MySQL 8.0+
- Maven 3.8+

## � 音乐文件准备

⚠️ **重要声明**: 本系统播放的所有音乐文件必须是本地已合法获取的音乐，不涉及在线音乐流媒体的获取。

### 音乐来源要求
- ✅ **推荐** 用户个人收藏的音乐文件
- ✅ **推荐** 从合法途径下载获得的音乐（含版权许可）
- ✅ **推荐** 原创创作的音乐内容
- ❌ **禁止** 未经授权的在线音乐下载或转录
- ❌ **禁止** 侵犯他人著作权的音乐内容

### 目录结构示例
```
music/
├── 歌手A/
│   ├── 歌曲1.mp3
│   ├── 歌曲2.mp3
│   └── 专辑1/
│       └── 歌曲3.mp3
└── 歌手B/
    └── 歌曲4.mp3
```

### 支持的音频格式
- **MP3** (推荐，兼容性最好)
- **WAV**
- **FLAC**
- **OGG**

### 数据库中的音乐库配置
系统启动时会自动扫描指定目录下的音乐文件，并将信息存储在 `MusicLibrary` 表中供用户查询。用户负责确保所有音乐文件的使用符合相关法律法规。

## �🚀 快速开始

**首次使用？** 👉 [5分钟快速启动指南](QUICKSTART.md) 👈

### 1. 克隆仓库

```bash
git clone https://github.com/chenshoulu202/ai-music-dispatcher.git
cd ai-music-dispatcher
```

### 2. 配置环境

修改 `src/main/resources/application.yml` 文件，配置以下关键参数：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/your_database?useUnicode=true&characterEncoding=utf-8
    username: your_username
    password: your_password

gemini:
  api-key: YOUR_GEMINI_API_KEY  # 从 Google AI Studio 获取
  
music:
  local-path: /path/to/your/music  # 本地乐库路径（必须是本地已合法获取的音乐文件）

tts:
  voice: zh-CN-liaoning-XiaobeiNeura  # 选择语音类型
  output-dir: /path/to/tts/output
```

> 📌 **重要**: `music.local-path` 应指向包含您自己拥有版权或已获得授权的本地音乐文件的目录。系统不处理在线流媒体或未授权的音乐内容。

### 3. 创建数据库

```bash
# 使用 application.yml 中配置的数据库名称
mysql -u root -p
CREATE DATABASE silver_guardian;
-- Hibernate 会自动创建表结构（根据 ddl-auto: update 配置）
```

### 4. 编译运行

```bash
# 编译项目
mvn clean package

# 运行应用
mvn spring-boot:run

# 或者使用 JAR 文件运行
java -jar target/ai-music-dispatcher-0.0.1-SNAPSHOT.jar
```

应用将在 `http://localhost:8080` 启动。

### 5. 启动前端应用

本后端与前端项目 [dycast](https://github.com/skmcj/dycast) 配合使用：

```bash
# 克隆前端仓库
git clone https://github.com/skmcj/dycast.git
cd dycast

# 安装依赖和启动
npm install
npm run dev
```

更多集成细节请参考 [INTEGRATION.md](INTEGRATION.md)

## 🏗️ 项目架构

### 系统架构概览

该项目按照后端微服务架构设计：

- **后端** (本仓库): AI Music Dispatcher - 提供 WebSocket 接口和 REST API
- **前端** ([dycast](https://github.com/skmcj/dycast)): 提供点歌和弹幕交互界面

### 后端项目结构

```
src/main/java/com/example/aimusicdispatcher
├── config/           # Spring 配置类
│   ├── GeminiProperties.java       # Gemini API 配置
│   ├── MusicProperties.java        # 音乐库配置
│   ├── TtsProperties.java          # TTS 配置
│   ├── PermissionProperties.java   # 权限配置
│   └── ws/
│       └── WebSocketConfig.java    # WebSocket 配置
├── connector/        # 连接器层
│   ├── BarrageController.java      # 弹幕控制器
│   └── DyWebSocketHandler.java     # WebSocket 处理器
├── dispatcher/       # 消息分发器
│   ├── MessageDispatcher.java      # 消息分发逻辑
│   └── BarrageFilterService.java   # 弹幕过滤服务
├── generator/        # AI 生成服务
│   ├── GeminiService.java          # Gemini API 调用
│   ├── TtsService.java             # TTS 文本转语音
│   └── TextCleaningService.java    # 文本清理
├── entity/           # 数据库实体
│   ├── IntroCache.java             # 口播缓存
│   └── MusicLibrary.java           # 音乐库
├── model/            # 数据模型
│   ├── barrage/      # 弹幕相关
│   ├── dy/           # 抖音直播相关
│   ├── gemini/       # Gemini 响应
│   └── playlist/     # 播放列表
├── repository/       # 数据持久层
├── scheduler/        # 定时任务
├── service/          # 业务服务层
├── util/             # 工具类
└── AiMusicDispatcherApplication.java  # 主应用类
```

## 🔌 API 端点

### WebSocket 连接
- **连接地址**: `ws://localhost:8080/ws/barrage`
- **消息格式**: JSON

#### 支持的消息类型

1. **点歌请求** (SongRequest)
   ```json
   {
     "type": "song_request",
     "userId": "user123",
     "userName": "用户名",
     "songName": "算什么男人",
     "artistName": "黄龄",
     "timestamp": 1708684800000
   }
   ```

2. **送礼事件** (GiftEvent)
   ```json
   {
     "type": "gift",
     "userId": "user123",
     "userName": "用户名",
     "giftName": "红心",
     "quantity": 5,
     "timestamp": 1708684800000
   }
   ```

3. **点赞事件** (LikeEvent)
   ```json
   {
     "type": "like",
     "userId": "user123",
     "userName": "用户名",
     "count": 10,
     "timestamp": 1708684800000
   }
   ```

### REST 端点

- `GET /api/music/search?keyword=歌曲名` - 搜索音乐
- `GET /api/music/library` - 获取音乐库列表
- `GET /api/permission/check?userId=xxx` - 检查用户权限

## ⚙️ 配置说明

### Gemini 配置

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `gemini.api-key` | Google Gemini API Key | - |
| `gemini.api-url` | API 端点地址 | `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent` |
| `gemini.system-prompt` | 系统提示词 | 预设的直播DJ提示词 |

### TTS 配置

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `tts.provider` | TTS 服务提供商 | `edge-tts` |
| `tts.voice` | 语音类型 | `zh-CN-liaoning-XiaobeiNeura` |
| `tts.rate` | 语速调节 (-1.0~1.0) | `-0.15` |
| `tts.output-dir` | 输出目录 | `./tts_output` |
| `tts.audio-format` | 音频格式 | `mp3` |
| `tts.sample-rate` | 采样率 (Hz) | `44100` |

### 权限配置

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `app.permission.enabled` | 是否启用权限校验 | `true` |
| `app.permission.like-minutes` | 点赞授予权限时长 | `5` |
| `app.permission.gift-minutes` | 送礼授予权限时长 | `20` |

## 📦 依赖清单

- **Spring Boot 3.2.3** - Web 框架
- **Spring WebSocket** - WebSocket 支持
- **Spring Data JPA** - 数据持久化
- **MySQL Connector** - 数据库驱动
- **Caffeine Cache** - 本地缓存
- **Log4j2** - 日志框架
- **Lombok** - 代码简化
- **Hibernate** - ORM 框架

## 🛠️ 开发指南

### 项目结构约定

- `service/` - 核心业务逻辑
- `repository/` - 数据库交互
- `controller/` - HTTP 和 WebSocket 端点
- `model/` - DTO 和实体类
- `config/` - Spring 配置类
- `util/` - 工具类和辅助函数

### 贡献代码

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

更多详情请查看 [CONTRIBUTING.md](CONTRIBUTING.md)

## 📝 日志记录

应用使用 Log4j2 进行日志记录，配置文件位于 `src/main/resources/log4j2.xml`。

日志级别说明：
- **DEBUG** - 详细调试信息
- **INFO** - 一般信息
- **WARN** - 警告信息
- **ERROR** - 错误信息

## 🌟 相关项目

### 前端项目

| 项目 | 描述 | 链接 |
|------|------|------|
| **dycast** | 点歌和直播交互前端界面，与本后端配合使用 | [GitHub](https://github.com/skmcj/dycast) |

### 完整系统架构

要运行完整的直播间点歌系统，需要同时部署本项目（后端）和 dycast（前端）。

详见 [集成指南](INTEGRATION.md)，其中包含了：
- 系统架构和数据流
- 前后端通信协议
- 部署和运行说明
- 常见问题解答

## 🐛 已知问题

- 暂无

## 📄 许可证

本项目采用 MIT 许可证，详见 [LICENSE](LICENSE)。

## 🙏 致谢

感谢以下项目对本项目的启发和支持：

**前端项目:**
- [dycast](https://github.com/skmcj/dycast) - 点歌和直播间互动前端，提供完整的用户交互界面

**后端依赖:**
- [Spring Boot](https://spring.io/projects/spring-boot) - Java Web 框架
- [Google Gemini](https://gemini.google.com) - AI 模型服务
- [Edge TTS](https://github.com/rany2/edge-tts) - 文本转语音服务

## 📧 联系方式 vx:chenshoulu202

- **Issues**: 使用 GitHub Issues 报告问题或提出建议
- **讨论**: 使用 GitHub Discussions 进行技术讨论

---

⭐ 如果项目对你有帮助，请点个 Star！
