# 常见问题 (FAQ)

## 安装和配置

### Q: 我应该使用哪个 Java 版本？

**A:** 该项目需要 Java 17 或更高版本。你可以使用：
- Oracle JDK 17+
- OpenJDK 17+
- Amazon Corretto 17+

```bash
java -version  # 检查你的 Java 版本
```

### Q: 我可以使用 MySQL 5.7 吗？

**A:** 不推荐。项目使用 MySQL 8.0+ 特定的功能。如果必须使用 5.7，请在 `application.yml` 中改用 `MySQL5Dialect`：

```yaml
spring:
  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL5Dialect
```

### Q: 如何修改服务器端口？

**A:** 在 `application.yml` 中配置：

```yaml
server:
  port: 8090  # 改为你想要的端口
```

### Q: 数据库用户密码泄露了怎么办？

**A:** 立即修改密码！

```sql
-- 使用 MySQL 命令行
ALTER USER 'silver_guardian'@'localhost' IDENTIFIED BY 'new_password';
FLUSH PRIVILEGES;
```

然后更新 `application.yml` 中的密码。

## 前端和集成相关

### Q: 前端项目是什么？

**A:** 前端项目是 [dycast](https://github.com/skmcj/dycast)，它提供了直播间点歌和弹幕交互的用户界面。本项目提供后端服务，两者配合使用。

### Q: 前后端如何通信？

**A:** 通过以下两种方式：

1. **WebSocket**: 用于实时通信（点歌请求、弹幕消息）
   - 连接地址: `ws://backend-host:8080/ws/barrage`
   - 支持点歌、送礼、点赞等消息类型

2. **REST API**: 用于查询操作
   - 搜索音乐: `GET /api/music/search?keyword=xxx`
   - 检查权限: `GET /api/permission/check?userId=xxx`

详见 [INTEGRATION.md](INTEGRATION.md)

### Q: 我需要部署前端吗？

**A:** 需要。系统是前后端分离的：
- **本项目**: 后端服务，提供 API 和 WebSocket 接口
- **dycast**: 前端项目，提供用户交互界面

两者都需要部署才能完整运行直播间点歌系统。

### Q: 如何配置前端连接到后端？

**A:** 在 dycast 项目中配置环境变量：

```bash
# .env 或 .env.local
VITE_API_URL=http://your-backend-host:8080
VITE_WS_URL=ws://your-backend-host:8080
```

### Q: 前后端可以分别部署在不同的服务器吗？

**A:** 可以。你可以：
1. 将前端部署到 CDN 或静态文件服务器
2. 将后端部署到云服务器（AWS、Azure 等）
3. 使用 Nginx 或 API Gateway 处理跨域和代理

配置示例见 [INTEGRATION.md](INTEGRATION.md) 中的"配置跨域和代理"部分。

### Q: WebSocket 连接失败怎么办？

**A:** 检查以下几点：
1. 后端服务是否正常运行
2. 防火墙是否允许 WebSocket 连接
3. 代理/负载均衡器是否正确处理 WebSocket 升级
4. 前端配置的 WebSocket URL 是否正确
5. 检查浏览器控制台的错误信息

### Q: 如何在生产环境使用 HTTPS/WSS？

**A:** 使用 Nginx 配置 SSL 证书，并更新环境变量：

```bash
VITE_API_URL=https://your-domain.com
VITE_WS_URL=wss://your-domain.com
```

## 功能相关

### Q: 如何添加 Gemini API Key？

**A:** 访问 https://ai.google.dev，获取你的 API Key，然后在 `application.yml` 中配置：

```yaml
gemini:
  api-key: YOUR_API_KEY_HERE
```

**重要**: 不要将 API Key 提交到 Git！使用环境变量：

```yaml
gemini:
  api-key: ${GEMINI_API_KEY}
```

运行时：
```bash
export GEMINI_API_KEY=your_actual_key
mvn spring-boot:run
```

### Q: TTS 支持哪些语言？

**A:** 当前使用 Edge TTS，支持的语言包括：

- 中文：`zh-CN` 
- 英文：`en-US`
- 日语：`ja-JP`
- 韩语：`ko-KR`

完整列表请参考 [Edge TTS 文档](https://github.com/rany2/edge-tts)。

### Q: 我可以改变 TTS 语音吗？

**A:** 可以！在 `application.yml` 中修改：

```yaml
tts:
  voice: zh-CN-XiaoxiaoNeural  # 改为其他可用语音
  rate: -0.15  # 调整语速（-1.0 到 1.0）
```

### Q: 音乐库路径设置有什么限制？

**A:** 需要以下条件：
- 路径必须存在
- 应用用户需要读权限
- 支持的音频格式：MP3、WAV、FLAC、AAC

```yaml
music:
  local-path: /path/to/your/music  # 确保此目录存在且可读
```

### Q: 如何启用/禁用权限系统？

**A:** 在 `application.yml` 中调整：

```yaml
app:
  permission:
    enabled: false  # 改为 true 启用
    like-minutes: 5
    gift-minutes: 20
```

### Q: WebSocket 连接超时是什么原因？

**A:** 可能的原因：
1. **防火墙阻止** - 检查防火墙规则
2. **代理设置** - WebSocket 可能被代理拦截
3. **连接地址错误** - 确认 URL 格式：`ws://host:port/ws/barrage`
4. **浏览器兼容性** - 使用现代浏览器

## 性能相关

### Q: 应用占用太多内存怎么办？

**A:** 调整 JVM 内存参数：

```bash
mvn spring-boot:run \
  -Dserver.tomcat.threads.max=100 \
  -Dspring.cache.caffeine.spec=maximumSize=1000
```

或在启动脚本中：
```bash
java -Xms512m -Xmx1g -jar app.jar
```

### Q: 数据库查询很慢怎么办？

**A:** 检查以下几点：
1. 创建必要的数据库索引
2. 调整连接池大小
3. 启用查询日志进行分析

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20  # 根据并发量调整
  jpa:
    show-sql: true  # 启用 SQL 日志
```

### Q: 如何查看系统性能指标？

**A:** 启用 Spring Boot Actuator：

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
```

然后访问：
- 健康检查：`http://localhost:8080/actuator/health`
- 指标：`http://localhost:8080/actuator/metrics`

## 部署相关

### Q: 可以用 Docker 部署吗？

**A:** 可以！步骤：
1. 构建镜像：`docker build -t ai-music-dispatcher:latest .`
2. 使用 compose 文件：`docker-compose up -d`

详见 [DEPLOYMENT.md](DEPLOYMENT.md)。

### Q: 生产环境需要更改什么配置？

**A:** 关键配置：

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # 改为 validate（不自动创建表）
  datasource:
    url: jdbc:mysql://prod-host:3306/db?useSSL=true  # 启用 SSL

server:
  compression:
    enabled: true  # 启用压缩

logging:
  level:
    root: INFO  # 改为 INFO 级别
```

### Q: 如何在 Linux 后台运行应用？

**A:** 使用 nohup 或 tmux：

```bash
# 方式一：使用 nohup
nohup java -jar app.jar > app.log 2>&1 &

# 方式二：使用 tmux
tmux new-session -d -s app 'java -jar app.jar'

# 查看 tmux session
tmux list-sessions
```

### Q: 如何设置开机自启？

**A:** 使用 systemd（Linux）：

```bash
sudo nano /etc/systemd/system/ai-music-dispatcher.service
```

```ini
[Unit]
Description=AI Music Dispatcher
After=network.target

[Service]
Type=simple
ExecStart=/usr/bin/java -jar /path/to/app.jar
Restart=on-failure

[Install]
WantedBy=multi-user.target
```

然后：
```bash
sudo systemctl enable ai-music-dispatcher
sudo systemctl start ai-music-dispatcher
```

## 故障排查

### Q: 应用启动失败，显示"Cannot find main class"

**A:** 确保编译成功：

```bash
mvn clean package

# 检查 JAR 文件
ls -la target/*.jar
```

### Q: 收到错误 "Access denied for user 'silver_guardian'"

**A:** 检查数据库凭证：

```bash
# 验证数据库连接
mysql -u silver_guardian -p silver_guardian -e "SELECT 1;"

# 检查 application.yml 中的用户名和密码
```

### Q: "The server time zone value 'UTC' is unrecognized"

**A:** 在 JDBC URL 中添加时区设置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/db?serverTimezone=Asia/Shanghai
```

### Q: 日志显示 "Cannot resolve symbol 'Xxx'" 

**A:** 这通常是 IDE 缓存问题：

```bash
# 清除 Maven 缓存并重新编译
mvn clean install -U

# 在 IDE 中刷新（Ctrl/Cmd + F5）
```

### Q: WebSocket 消息未被接收

**A:** 检查：
1. 消息格式是否正确（应该是 JSON）
2. WebSocket 连接状态
3. 日志中是否有错误信息

```javascript
// 测试代码
const ws = new WebSocket('ws://localhost:8080/ws/barrage');
ws.onopen = () => {
  console.log('Connected');
  ws.send(JSON.stringify({type: 'song_request', userId: 'test'}));
};
ws.onerror = (err) => console.error('Error:', err);
```

## 贡献相关

### Q: 如何提交 Pull Request？

**A:** 按照以下步骤：

1. Fork 仓库
2. 创建特性分支：`git checkout -b feature/my-feature`
3. 提交改动：`git commit -m "feat: add my feature"`
4. 推送分支：`git push origin feature/my-feature`
5. 在 GitHub 上创建 Pull Request

详见 [CONTRIBUTING.md](CONTRIBUTING.md)。

### Q: 如何报告 Bug？

**A:** 在 GitHub Issues 中点击"New issue"并选择"Bug report"模板。提供：
- 清晰的问题描述
- 复现步骤
- 预期和实际行为
- Java 和数据库版本
- 相关日志

### Q: 可以提什么样的功能建议？

**A:** 欢迎任何合理的建议！请在 Issues 中提出，描述：
- 功能的用途
- 解决的问题
- 建议的实现方式

## 其他问题

### Q: 项目文档在哪里？

**A:** 文档包括：
- `README.md` - 项目概览
- `INSTALL.md` - 安装指南
- `CONTRIBUTING.md` - 贡献指南
- `DEPLOYMENT.md` - 部署指南
- `SECURITY.md` - 安全指南
- GitHub Wiki（如有）

### Q: 如何联系维护者？

**A:** 通过以下方式：
- 📧 在 Issues 中打开问题
- 💬 GitHub Discussions（如启用）
- 📝 查看 CONTRIBUTING.md 中的联系方式

### Q: 项目采用什么开源许可证？

**A:** 采用 **MIT 许可证**。详见 [LICENSE](LICENSE) 文件。

### Q: 我可以在商业项目中使用吗？

**A:** 可以！MIT 许可证允许商业使用。只需：
- 包含许可证副本
- 声明重大变更
- 不提供担保

更多详情见 [LICENSE](LICENSE)。

---

## 未找到答案？

- 🔍 在现有 Issues 中搜索
- 📖 查看完整文档
- 💬 创建新 Issue 提问
- 🤝 加入社区讨论

感谢你的提问！
