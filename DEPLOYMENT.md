# 部署指南

本文档提供了 AI Music Dispatcher 在不同环境中的部署说明。

## 📋 前置条件

- Java 17+
- MySQL 8.0+
- Maven 3.8+ 或 Docker
- 2GB+ RAM
- 100MB+ 磁盘空间

## 本地开发部署

### 1. 环境准备

```bash
# 克隆仓库
git clone https://github.com/chenshoulu202/ai-music-dispatcher.git
cd ai-music-dispatcher

# 创建本地配置文件（可选,覆盖默认配置）
cp src/main/resources/application.yml src/main/resources/application-dev.yml
```

### 2. 数据库设置

```bash
# 使用 MySQL 命令行
mysql -u root -p

# 创建数据库
CREATE DATABASE silver_guardian CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 创建应用用户（推荐）
CREATE USER 'silver_guardian'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON silver_guardian.* TO 'silver_guardian'@'localhost';
FLUSH PRIVILEGES;
```

### 3. 配置 application.yml

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/silver_guardian?useUnicode=true&characterEncoding=utf-8&useSSL=false
    username: silver_guardian
    password: your_password
  jpa:
    hibernate:
      ddl-auto: create-drop  # 开发时使用,启动时创建表,关闭时删除

gemini:
  api-key: your_gemini_api_key

music:
  local-path: /Users/username/Music  # 调整为你的音乐目录

tts:
  output-dir: ./tts_output
```

### 4. 运行应用

```bash
# 方式一: 使用 Maven
mvn clean install
mvn spring-boot:run

# 方式二: 编译后运行
mvn clean package
java -jar target/ai-music-dispatcher-0.0.1-SNAPSHOT.jar
```

应用将在 `http://localhost:8080` 启动。

## 生产部署

### 生产环境配置

创建 `src/main/resources/application-prod.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://your-db-host:3306/silver_guardian?useUnicode=true&characterEncoding=utf-8&useSSL=true
    username: ${DB_USER}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 10
      connection-timeout: 30000
      idle-timeout: 600000
  jpa:
    hibernate:
      ddl-auto: validate  # 生产环境使用 validate
    show-sql: false  # 不显示 SQL

server:
  port: 8080
  compression:
    enabled: true
    min-response-size: 1024

gemini:
  api-key: ${GEMINI_API_KEY}

music:
  local-path: ${MUSIC_PATH}

tts:
  output-dir: ${TTS_OUTPUT_DIR}

logging:
  level:
    root: INFO
    com.example.aimusicdispatcher: INFO
```

### Linux/macOS 服务部署

#### 创建 systemd 服务文件

```bash
sudo nano /etc/systemd/system/ai-music-dispatcher.service
```

```ini
[Unit]
Description=AI Music Dispatcher
After=network.target

[Service]
Type=simple
User=appuser
WorkingDirectory=/opt/ai-music-dispatcher
Environment="GEMINI_API_KEY=your_api_key"
Environment="DB_USER=silver_guardian"
Environment="DB_PASSWORD=your_password"
ExecStart=/usr/bin/java -jar /opt/ai-music-dispatcher/ai-music-dispatcher.jar --spring.profiles.active=prod
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

#### 启动服务

```bash
# 重新加载 systemd
sudo systemctl daemon-reload

# 启动服务
sudo systemctl start ai-music-dispatcher

# 开机自启
sudo systemctl enable ai-music-dispatcher

# 查看日志
sudo journalctl -u ai-music-dispatcher -f
```

### Windows 服务部署

使用 NSSM (Non-Sucking Service Manager)：

```bash
# 下载并安装 NSSM
# https://nssm.cc/download

# 安装服务
nssm install AiMusicDispatcher "C:\Program Files\Java\jdk-17\bin\java.exe" "-jar C:\app\ai-music-dispatcher.jar"

# 设置环境变量
nssm set AiMusicDispatcher AppEnvironmentExtra GEMINI_API_KEY=your_api_key DB_USER=silver_guardian DB_PASSWORD=your_password

# 启动服务
nssm start AiMusicDispatcher

# 查看状态
nssm status AiMusicDispatcher
```

## Docker 部署

### 方式一: 使用现有 Dockerfile

```bash
# 构建镜像
docker build -t ai-music-dispatcher:latest .

# 运行容器
docker run -d \
  --name ai-music-dispatcher \
  -p 8080:8080 \
  -e GEMINI_API_KEY=your_api_key \
  -e DB_USER=silver_guardian \
  -e DB_PASSWORD=your_password \
  -e DB_HOST=mysql-host \
  -v /path/to/music:/app/music \
  -v /path/to/tts_output:/app/tts_output \
  ai-music-dispatcher:latest
```

### 方式二: Docker Compose

创建 `docker-compose.yml`：

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
    networks:
      - app_network

  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/silver_guardian?useUnicode=true&characterEncoding=utf-8
      SPRING_DATASOURCE_USERNAME: silver_guardian
      SPRING_DATASOURCE_PASSWORD: app_password
      GEMINI_API_KEY: your_api_key
      MUSIC_LOCAL_PATH: /app/music
    volumes:
      - ./music:/app/music
      - ./tts_output:/app/tts_output
    depends_on:
      - mysql
    networks:
      - app_network

volumes:
  mysql_data:

networks:
  app_network:
```

运行：

```bash
docker-compose up -d
```

## Kubernetes 部署

### 创建 Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ai-music-dispatcher
spec:
  replicas: 2
  selector:
    matchLabels:
      app: ai-music-dispatcher
  template:
    metadata:
      labels:
        app: ai-music-dispatcher
    spec:
      containers:
      - name: app
        image: ai-music-dispatcher:latest
        ports:
        - containerPort: 8080
        env:
        - name: GEMINI_API_KEY
          valueFrom:
            secretKeyRef:
              name: app-secret
              key: gemini-api-key
        - name: DB_USER
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: username
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: password
        - name: DB_HOST
          value: mysql-service
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10

---
apiVersion: v1
kind: Service
metadata:
  name: ai-music-dispatcher-service
spec:
  type: LoadBalancer
  ports:
  - port: 80
    targetPort: 8080
  selector:
    app: ai-music-dispatcher
```

部署命令：

```bash
# 创建 Secret
kubectl create secret generic app-secret --from-literal=gemini-api-key=your_key
kubectl create secret generic db-secret --from-literal=username=user --from-literal=password=pass

# 部署
kubectl apply -f deployment.yaml

# 检查状态
kubectl get pods
kubectl logs <pod-name>
```

## 性能优化

### JVM 参数优化

```bash
java -jar ai-music-dispatcher.jar \
  -Xms512m \
  -Xmx1g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200
```

### 数据库连接池优化

在 `application.yml` 中：

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20      # 根据并发量调整
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

### 缓存优化

```yaml
# 调整 Caffeine 缓存大小（在代码中配置）
CacheBuilder.newBuilder()
  .maximumSize(10000)
  .expireAfterWrite(10, TimeUnit.MINUTES)
```

## 监控和日志

### 启用 Spring Boot Actuator

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
```

### 集成监控

- **Prometheus**: 收集指标
- **Grafana**: 可视化迈标
- **ELK Stack**: 日志分析

## 故障排查

### 常见问题

#### 1. 数据库连接失败
```bash
# 检查 MySQL 状态
mysql -u silver_guardian -p -h localhost -e "SELECT 1"

# 查看日志
tail -f logs/app.log | grep ERROR
```

#### 2. 内存不足
```bash
# 增加 JVM 内存
java -Xms1g -Xmx2g -jar ai-music-dispatcher.jar
```

#### 3. WebSocket 连接问题
- 检查防火墙设置
- 验证 URL 格式
- 检查跨域设置

### 日志位置

- 应用日志: `logs/application.log`
- 错误日志: `logs/error.log`

## 备份和恢复

### 数据库备份

```bash
# 完整备份
mysqldump -u silver_guardian -p silver_guardian > backup.sql

# 压缩备份
mysqldump -u silver_guardian -p silver_guardian | gzip > backup.sql.gz

# 恢复
mysql -u silver_guardian -p silver_guardian < backup.sql
```

## 更新升级

```bash
# 1. 备份数据库
mysqldump -u user -p database > backup.sql

# 2. 获取新版本
git pull origin main

# 3. 重新编译
mvn clean package

# 4. 停止旧版本
systemctl stop ai-music-dispatcher

# 5. 部署新版本
cp target/*.jar /opt/ai-music-dispatcher/

# 6. 启动新版本
systemctl start ai-music-dispatcher

# 7. 验证
curl http://localhost:8080/actuator/health
```

## 安全建议

- 使用强密码
- 启用 HTTPS/TLS
- 定期更新依赖
- 使用防火墙限制访问
- 定期备份数据库
- 监控日志和告警

更多信息请参阅 [SECURITY.md](SECURITY.md)。
