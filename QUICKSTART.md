# 快速启动指南

本指南将帮您快速启动整个直播间点歌系统（包括前后端）。

## ⚡ 5分钟快速启动

### 前置条件

- ✅ Java 17+
- ✅ MySQL 8.0+
- ✅ Node.js 16+（前端需要）
- ✅ Git

### 第一步：准备后端

```bash
# 1. 克隆后端仓库
git clone https://github.com/chenshoulu202/ai-music-dispatcher.git
cd ai-music-dispatcher

# 2. 创建数据库
mysql -u root -p
CREATE DATABASE silver_guardian;
EXIT;

# 3. 编辑 application.yml，配置以下关键项：
# - 数据库连接信息
# - Gemini API Key（从 https://ai.google.dev 获取）
# - 音乐文件路径

# 4. 编译并运行后端
mvn clean install
mvn spring-boot:run

# 等待看到 "Started AiMusicDispatcherApplication"
# 后端将运行在 http://localhost:8080
```

### 第二步：准备前端

```bash
# 1. 新开一个终端，克隆前端仓库
git clone https://github.com/skmcj/dycast.git
cd dycast

# 2. 安装依赖
npm install

# 3. 配置连接到后端
# 创建 .env.local 文件（或编辑 .env）
echo "VITE_API_URL=http://localhost:8080" > .env.local
echo "VITE_WS_URL=ws://localhost:8080" >> .env.local

# 4. 启动前端开发服务器
npm run dev

# 打开浏览器访问显示的地址（通常是 http://localhost:5173）
```

### 完成！

现在你应该可以：
- 在前端界面进行点歌
- 接收 AI 生成的口播
- 听到 TTS 生成的语音

## 🐳 使用 Docker 启动（推荐用于生产）

### 一行命令启动

```bash
# 使用 docker-compose 快速启动整个系统
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止系统
docker-compose down
```

访问 `http://localhost` 即可使用。

### 配置说明

编辑 `docker-compose.yml`，修改以下环境变量：

```yaml
environment:
  GEMINI_API_KEY: your_api_key_here
  MUSIC_LOCAL_PATH: /app/music
```

## 📋 配置步骤详解

### 后端配置 (application.yml)

```yaml
# 数据库配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/silver_guardian
    username: silver_guardian
    password: your_db_password
  jpa:
    hibernate:
      ddl-auto: update  # 首次运行使用 update，后续改为 validate

# Gemini API 配置（可替换为其他大模型）
gemini:
  api-key: YOUR_GEMINI_API_KEY
  api-url: https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key={apiKey}
  system-prompt: "你是一个幽默风趣的直播间DJ..."  # 可选自定义

# 音乐库配置
music:
  local-path: /path/to/your/music

# TTS 配置
tts:
  provider: edge-tts
  voice: zh-CN-liaoning-XiaobeiNeura
  rate: -0.15
  output-dir: ./tts_output

# 权限配置
app:
  permission:
    enabled: true
    like-minutes: 5
    gift-minutes: 20
```

#### 🤖 大模型替换指南

你可以将 Gemini 替换为任何喜欢的大模型，无需修改代码！只需在 `application.yml` 中调整配置即可。

**常用大模型快速配置：**

**OpenAI GPT-4 / GPT-3.5:**
```yaml
gemini:
  api-key: "sk-your-openai-api-key"
  api-url: https://api.openai.com/v1/chat/completions
  system-prompt: "你是一个幽默风趣的直播间DJ..."
```

**阿里通义千问:**
```yaml
gemini:
  api-key: "sk-your-dashscope-api-key"
  api-url: https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation
  system-prompt: "你是一个幽默风趣的直播间DJ..."
```

**Anthropic Claude:**
```yaml
gemini:
  api-key: "sk-ant-your-claude-api-key"
  api-url: https://api.anthropic.com/v1/messages
  system-prompt: "你是一个幽默风趣的直播间DJ..."
```

**讯飞星火大模型:**
```yaml
gemini:
  api-key: "your-sparkdesk-api-key"
  api-url: https://spark-api.xf-yun.com/v1/chat/completions
  system-prompt: "你是一个幽默风趣的直播间DJ..."
```

> 💡 **更多信息**: 如果你的模型 API 请求/响应格式与 Gemini 完全不同，可能需要修改 `GeminiService.java`。详见 [README.md](README.md#-ai-大模型定制化) 的完整指南。


### 前端配置 (.env)

```
# 后端服务地址
VITE_API_URL=http://localhost:8080
VITE_WS_URL=ws://localhost:8080

# 可选：前端端口
VITE_PORT=5173
```

## 🔧 故障排查

### 问题：后端启动失败

```bash
# 检查 Java 版本
java -version  # 应该是 17+

# 检查 MySQL 连接
mysql -u silver_guardian -p silver_guardian -e "SELECT 1;"

# 查看详细错误日志
mvn spring-boot:run -X
```

### 问题：前端连接不上后端

```bash
# 1. 检查后端是否运行
curl http://localhost:8080/actuator/health

# 2. 检查防火墙
# macOS: System Preferences > Security & Privacy > Firewall
# Linux: sudo ufw status / sudo iptables -L
# Windows: Windows Defender Firewall > Advanced Settings

# 3. 检查前端配置
cat .env.local  # 确保 API_URL 和 WS_URL 正确
```

### 问题：Gemini API 失败

```bash
# 检查 API Key 是否正确
# 访问 https://ai.google.dev 重新获取

# 在 application.yml 中更新
gemini:
  api-key: your_new_api_key
```

### 问题：TTS 未生成音频

```bash
# 检查输出目录是否存在并可写
mkdir -p tts_output
chmod 755 tts_output

# 检查日志（最后50行）
tail -50 logs/application.log

# 检查 Edge TTS 依赖（在后端日志中查看）
```

## 📊 验证部署

### 检查后端

```bash
# 检查健康状态
curl http://localhost:8080/actuator/health

# 应该返回：
# {
#   "status": "UP"
# }

# 检查数据库连接
curl http://localhost:8080/actuator/health/db

# 获取可用的音乐
curl http://localhost:8080/api/music/library
```

### 检查前端

在浏览器中打开 http://localhost:5173，应该可以看到点歌界面。

### 检查 WebSocket

在浏览器开发者工具中：

```javascript
// 在控制台执行
const ws = new WebSocket('ws://localhost:8080/ws/barrage');
ws.onopen = () => console.log('Connected!');
ws.onerror = (err) => console.error('Error:', err);
```

应该看到 "Connected!" 的消息。

## 🚀 生产部署

### 使用 Linux 服务运行

```bash
# 1. 创建 systemd 服务文件
sudo nano /etc/systemd/system/ai-music-dispatcher.service

# 2. 填入以下内容
[Unit]
Description=AI Music Dispatcher
After=network.target mysql.service

[Service]
Type=simple
ExecStart=/usr/bin/java -Xms512m -Xmx1g -jar /opt/ai-music-dispatcher/app.jar
Restart=on-failure
User=appuser

[Install]
WantedBy=multi-user.target

# 3. 启动服务
sudo systemctl start ai-music-dispatcher
sudo systemctl enable ai-music-dispatcher

# 4. 查看状态
sudo systemctl status ai-music-dispatcher
```

### 使用 Nginx 反向代理

```nginx
upstream backend {
    server 127.0.0.1:8080;
}

server {
    listen 80;
    server_name your-domain.com;

    # 前端静态文件
    location / {
        root /usr/share/nginx/html/dycast;
        try_files $uri $uri/ /index.html;
    }

    # 后端 API
    location /api {
        proxy_pass http://backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # WebSocket
    location /ws {
        proxy_pass http://backend;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
    }
}
```

## 📚 下一步

- 📖 查看 [README.md](README.md) 了解项目特性
- 🔗 查看 [INTEGRATION.md](INTEGRATION.md) 了解前后端集成详情
- 🚀 查看 [DEPLOYMENT.md](DEPLOYMENT.md) 了解完整的部署指南
- 💡 查看 [FAQ.md](FAQ.md) 解决常见问题
- 🛠️ 查看 [CONTRIBUTING.md](CONTRIBUTING.md) 参与项目贡献

## 常见命令速查表

```bash
# 后端相关
mvn clean install          # 编译项目
mvn spring-boot:run        # 运行应用
mvn test                   # 运行测试
mvn clean package          # 打包成 JAR

# 前端相关
npm install                # 安装依赖
npm run dev                # 开发模式
npm run build              # 生产打包
npm run preview            # 预览打包结果

# 数据库相关
mysql -u root -p           # 连接 MySQL
CREATE DATABASE silver_guardian;  # 创建数据库
DROP DATABASE silver_guardian;    # 删除数据库

# Docker 相关
docker-compose up -d      # 启动容器
docker-compose down       # 停止容器
docker-compose logs -f    # 查看日志
docker ps                 # 列出运行中的容器
```

## 获取帮助

遇到问题？请：

1. 查看 [FAQ.md](FAQ.md) 和 [INTEGRATION.md](INTEGRATION.md)
2. 搜索现有的 GitHub Issues
3. 创建新的 Issue 描述问题

---

**成功启动后，记得：**
- 更改数据库默认密码 ✅
- 保护好 Gemini API Key ✅
- 定期备份数据库 ✅

祝你使用愉快！ 🎉
