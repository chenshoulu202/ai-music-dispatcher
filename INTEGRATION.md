# 项目集成指南

## 项目全景

AI Music Dispatcher 系统采用前后端分离架构，包含两个主要仓库：

```
┌─────────────────────────────────────────┐
│          直播间观众浏览器                 │
└────────────────┬────────────────────────┘
                 │
                 │ HTTP / WebSocket
                 ↓
    ┌────────────────────────┐
    │   dycast (前端项目)     │
    │ 点歌和弹幕交互界面      │
    │ GitHub: skmcj/dycast   │
    └────────┬───────────────┘
             │ WebSocket + REST API
             │ ws://backend:8080/ws/barrage
             │
             ↓
    ┌─────────────────────────────────┐
    │  AI Music Dispatcher (后端)      │
    │  This Repository                │
    │  - Gemini AI 口播生成           │
    │  - Edge TTS 文本转语音           │
    │  - 弹幕处理和权限管理            │
    │  - 音乐库管理                    │
    └────────┬────────────────────────┘
             │
             ↓
    ┌─────────────────────────────────┐
    │   MySQL 数据库                   │
    └─────────────────────────────────┘
```

## 前端项目 (dycast)

### 项目信息

- **名称**: dycast
- **仓库**: [https://github.com/skmcj/dycast](https://github.com/skmcj/dycast)
- **功能**: 直播间点歌和弹幕交互前端
- **技术栈**: Vue.js / React（根据项目实现）

### 主要功能

- 🎤 点歌请求界面
- 💬 实时弹幕显示
- 🎁 送礼和点赞交互
- 🔊 音乐播放控制
- 📊 实时统计和权限显示

### 获取前端代码

```bash
git clone https://github.com/skmcj/dycast.git
cd dycast
npm install
npm run dev
```

详细安装说明请参考 [dycast 项目文档](https://github.com/skmcj/dycast/blob/main/README.md)

## 后端项目 (AI Music Dispatcher)

本项目提供以下核心功能：

- 🎤 AI 口播生成（支持 Gemini、OpenAI、Claude、通义千问等多种大模型）
- 🔊 文本转语音（Edge TTS）
- 💬 弹幕处理和分发
- 🎵 音乐库管理
- 👥 权限控制系统
- 📊 数据缓存和优化

### 🤖 AI 大模型选择与配置

系统默认使用 Google Gemini，但支持切换到任何喜欢的大模型。

**快速配置（无需代码修改）：**

在 `application.yml` 中更新配置即可：

```yaml
# OpenAI GPT
gemini:
  api-key: "sk-your-openai-key"
  api-url: https://api.openai.com/v1/chat/completions

# 阿里通义千问
gemini:
  api-key: "sk-your-dashscope-key"
  api-url: https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation

# Anthropic Claude
gemini:
  api-key: "sk-ant-your-key"
  api-url: https://api.anthropic.com/v1/messages
```

**需要代码修改的大模型：** 

如果你的大模型 API 格式与 Gemini 差异较大，请参考 [CONTRIBUTING.md](CONTRIBUTING.md#-扩展-ai-大模型支持) 的完整开发指南。

更多详情见 [README.md 的大模型定制化章节](README.md#-ai-大模型定制化)

### 获取后端代码

```bash
git clone https://github.com/chenshoulu202/ai-music-dispatcher.git
cd ai-music-dispatcher
mvn clean install
mvn spring-boot:run
```

## 前后端交互协议

### WebSocket 连接

#### 1. 前端连接后端

```javascript
// 连接到后端 WebSocket 服务
const ws = new WebSocket('ws://localhost:8080/ws/barrage');

ws.onopen = () => {
  console.log('Connected to AI Music Dispatcher');
};

ws.onerror = (error) => {
  console.error('WebSocket error:', error);
};

ws.onclose = () => {
  console.log('Disconnected from server');
};
```

#### 2. 前端发送点歌请求

```json
{
  "type": "song_request",
  "userId": "user_id_123",
  "userName": "用户昵称",
  "songName": "算什么男人",
  "artistName": "黄龄",
  "timestamp": 1708684800000
}
```

#### 3. 前端发送送礼事件

```json
{
  "type": "gift",
  "userId": "user_id_123",
  "userName": "用户昵称",
  "giftName": "红心",
  "giftIcon": "https://example.com/gift.png",
  "quantity": 5,
  "timestamp": 1708684800000
}
```

#### 4. 前端发送点赞事件

```json
{
  "type": "like",
  "userId": "user_id_123",
  "userName": "用户昵称",
  "count": 10,
  "timestamp": 1708684800000
}
```

#### 5. 后端返回处理结果

```json
{
  "status": "success",
  "code": "200",
  "message": "点歌成功",
  "data": {
    "intro": "感谢用户昵称点的歌《算什么男人》，黄龄的经典之作，祝大家听歌愉快！",
    "audioUrl": "https://example.com/tts_output/intro_xxx.mp3",
    "timestamp": 1708684800000
  }
}
```

### REST API 端点

#### 获取音乐库

```bash
curl -X GET http://localhost:8080/api/music/library
```

响应示例：

```json
{
  "code": "200",
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "算什么男人",
      "artist": "黄龄",
      "duration": 270,
      "path": "/path/to/music/file.mp3"
    }
  ]
}
```

#### 搜索音乐

```bash
curl -X GET http://localhost:8080/api/music/search?keyword=黄龄
```

#### 检查用户权限

```bash
curl -X GET http://localhost:8080/api/permission/check?userId=user_123
```

响应示例：

```json
{
  "code": "200",
  "message": "success",
  "data": {
    "userId": "user_123",
    "hasPermission": true,
    "reason": "like",
    "remainingTime": 300000,
    "expiresAt": 1708685100000
  }
}
```

## 部署和运行

### 开发环境

#### 1. 启动后端服务

```bash
cd ai-music-dispatcher
mvn spring-boot:run
# 服务运行在 http://localhost:8080
```

#### 2. 启动前端开发服务器

```bash
cd dycast
npm run dev
# 前端运行在 http://localhost:5173 或其他配置的端口
```

#### 3. 测试集成

打开浏览器访问前端地址，检查：
- WebSocket 连接是否正常
- 点歌请求是否成功发送
- 是否能接收到后端响应

### 生产环境

#### 使用 Docker Compose 整体部署

创建 `docker-compose.prod.yml`：

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: root_password
      MYSQL_DATABASE: silver_guardian
      MYSQL_USER: silver_guardian
      MYSQL_PASSWORD: app_password
    volumes:
      - mysql_data:/var/lib/mysql

  backend:
    image: ai-music-dispatcher:latest
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/silver_guardian?useUnicode=true&characterEncoding=utf-8
      SPRING_DATASOURCE_USERNAME: silver_guardian
      SPRING_DATASOURCE_PASSWORD: app_password
      GEMINI_API_KEY: ${GEMINI_API_KEY}
      MUSIC_LOCAL_PATH: /app/music
    depends_on:
      - mysql

  frontend:
    image: dycast:latest
    ports:
      - "80:80"
    environment:
      VITE_API_URL: http://your-backend-url:8080
      VITE_WS_URL: ws://your-backend-url:8080
    depends_on:
      - backend

volumes:
  mysql_data:
```

启动：

```bash
docker-compose -f docker-compose.prod.yml up -d
```

## 配置跨域和代理

### 前端配置（使用 Nginx 反向代理）

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态文件
    location / {
        root /usr/share/nginx/html;
        try_files $uri $uri/ /index.html;
    }

    # 代理到后端 API
    location /api {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 代理 WebSocket
    location /ws {
        proxy_pass http://backend:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

### 后端配置（Spring Boot CORS）

在 `src/main/java/.../config/` 目录中创建 CORS 配置：

```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("http://localhost:5173", "https://your-domain.com")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true)
                    .maxAge(3600);
            }
        };
    }
}
```

## 常见问题

### Q: 前后端如何通信？

**A:** 通过以下两种方式：
1. **WebSocket**: 用于实时通信（弹幕、点歌）
2. **REST API**: 用于查询操作（搜索音乐、检查权限）

### Q: 前端需要做什么配置？

**A:** 在 dycast 项目中配置后端地址：

```javascript
// .env 或 .env.local
VITE_API_URL=http://localhost:8080
VITE_WS_URL=ws://localhost:8080
```

### Q: 如何处理 WebSocket 连接失败？

**A:** 检查以下几点：
1. 后端是否正常运行
2. 防火墙是否阻止 WebSocket 连接
3. 代理配置是否正确处理了 WebSocket 升级请求
4. 前后端地址是否正确

### Q: 如何在生产环境使用 HTTPS？

**A:** 使用 Nginx 配置 SSL 证书，并在环境变量中使用 `https://` 和 `wss://` 地址：

```bash
VITE_API_URL=https://your-domain.com
VITE_WS_URL=wss://your-domain.com
```

### Q: 可以分别部署前后端吗？

**A:** 可以。只要配置正确的跨域和代理规则即可：
- 前端可以部署到 CDN 或独立服务器
- 后端可以部署到云服务器（AWS、Azure、阿里云等）
- 使用 API Gateway 处理跨域请求

## 贡献代码

如果你对两个项目都有改进建议：

1. **后端改进**: 在本仓库提交 Issue 或 PR
2. **前端改进**: 在 [dycast](https://github.com/skmcj/dycast) 仓库提交 Issue 或 PR
3. **集成问题**: 在相关仓库的 Issues 中讨论

## 许可证

- **后端** (AI Music Dispatcher): MIT License
- **前端** (dycast): 请参考其仓库的许可证

## 相关资源

- 后端仓库: [AI Music Dispatcher](https://github.com/chenshoulu202/ai-music-dispatcher)
- 前端仓库: [dycast](https://github.com/skmcj/dycast)
- 后端文档: [README.md](README.md), [DEPLOYMENT.md](DEPLOYMENT.md)
- 前端文档: 参考 dycast 项目文档

---

如有问题，欢迎在 GitHub Issues 中反馈！
