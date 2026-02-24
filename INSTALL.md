# 安装指南

## 系统要求

| 组件 | 版本 | 备注 |
|------|------|------|
| Java | 17+ | OpenJDK 或 Oracle JDK |
| Maven | 3.8+ | 构建和包管理 |
| MySQL | 8.0+ | 数据库 |
| RAM | 2GB+ | 最低内存要求 |
| 磁盘 | 100MB+ | 最小磁盘空间 |

## 操作系统支持

- ✅ macOS 10.14+
- ✅ Linux (Ubuntu 18.04+, CentOS 7+)
- ✅ Windows 10+

## 详细安装步骤

### 步骤 1: 安装 Java 17

#### macOS

```bash
# 使用 Homebrew
brew install openjdk@17

# 配置环境变量
echo 'export JAVA_HOME=$(/usr/libexec/java_home -v 17)' >> ~/.zshrc
source ~/.zshrc

# 验证安装
java -version
```

#### Linux (Ubuntu/Debian)

```bash
# 更新包列表
sudo apt update

# 安装 OpenJDK 17
sudo apt install openjdk-17-jdk

# 验证安装
java -version
```

#### Windows

下载并安装 OpenJDK 17：
1. 访问 https://jdk.java.net/17
2. 下载对应操作系统的安装包
3. 运行安装程序
4. 设置环境变量 `JAVA_HOME`
5. 在命令行验证: `java -version`

### 步骤 2: 安装 Maven

#### macOS

```bash
brew install maven

# 验证安装
mvn -version
```

#### Linux (Ubuntu/Debian)

```bash
sudo apt install maven

# 验证安装
mvn -version
```

#### Windows

1. 下载 Maven: https://maven.apache.org/download.cgi
2. 解压到合适位置，如 `C:\tools\apache-maven-3.8.x`
3. 设置环境变量 `MAVEN_HOME`
4. 将 `%MAVEN_HOME%\bin` 添加到 `PATH`
5. 在命令行验证: `mvn -version`

### 步骤 3: 安装 MySQL

#### macOS

```bash
# 使用 Homebrew
brew install mysql@8.0

# 启动 MySQL
brew services start mysql@8.0

# 初始化
mysql_secure_installation

# 验证连接
mysql -u root -p
```

#### Linux (Ubuntu/Debian)

```bash
# 安装 MySQL
sudo apt install mysql-server

# 运行初始化脚本
sudo mysql_secure_installation

# 验证服务
sudo systemctl status mysql
```

#### Windows

1. 访问 https://dev.mysql.com/downloads/mysql/
2. 下载 MySQL Community Server
3. 运行安装程序并完成配置
4. 启动 MySQL 服务
5. 使用 MySQL Workbench 或命令行连接

### 步骤 4: 克隆并配置项目

```bash
# 克隆仓库
git clone https://github.com/chenshoulu202/ai-music-dispatcher.git
cd ai-music-dispatcher

# 创建数据库
mysql -u root -p
```

在 MySQL 中执行：

```sql
CREATE DATABASE silver_guardian 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

CREATE USER 'silver_guardian'@'localhost' 
IDENTIFIED BY 'your_secure_password';

GRANT ALL PRIVILEGES ON silver_guardian.* 
TO 'silver_guardian'@'localhost';

FLUSH PRIVILEGES;
EXIT;
```

### 步骤 5: 配置应用

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/silver_guardian?useUnicode=true&characterEncoding=utf-8
    username: silver_guardian
    password: your_secure_password

gemini:
  api-key: YOUR_GEMINI_API_KEY
  # 从 https://ai.google.dev 获取 API Key

music:
  local-path: /path/to/your/music

tts:
  output-dir: ./tts_output
```

### 步骤 6: 编译并运行

```bash
# 清理之前的构建
mvn clean

# 编译项目
mvn compile

# 运行测试（可选）
mvn test

# 运行应用
mvn spring-boot:run
```

应用将在 `http://localhost:8080` 启动。

## Docker 安装（推荐）

如果你已安装 Docker，可以使用更简单的方法：

```bash
# 使用 Docker Compose
docker-compose up -d

# 查看日志
docker-compose logs -f app

# 停止应用
docker-compose down
```

## 验证安装

```bash
# 1. 检查应用健康状态
curl http://localhost:8080/actuator/health

# 2. 检查数据库连接（查看日志）
tail -f logs/application.log

# 3. 连接 WebSocket
wscat -c ws://localhost:8080/ws/barrage
```

## 常见安装问题

### Java 版本过低

```bash
# 检查当前 Java 版本
java -version

# 如果版本不是 17，请升级
# macOS: brew install openjdk@17
# Linux: sudo apt install openjdk-17-jdk
```

### MySQL 连接失败

```bash
# 检查 MySQL 是否运行
mysql -u root -p

# 检查 application.yml 中数据库配置
# 确保用户名、密码、主机、端口正确
```

### 端口 8080 已被占用

```yaml
# 在 application.yml 中修改端口
server:
  port: 8090  # 改为其他可用端口
```

### Maven 下载依赖缓慢

```bash
# 编辑 ~/.m2/settings.xml，添加阿里云镜像
<mirrors>
  <mirror>
    <id>aliyun</id>
    <mirrorOf>central</mirrorOf>
    <name>Aliyun Maven</name>
    <url>https://maven.aliyun.com/repository/central</url>
  </mirror>
</mirrors>
```

## 项目结构验证

```
ai-music-dispatcher/
├── src/
│   ├── main/
│   │   ├── java/com/example/aimusicdispatcher/
│   │   └── resources/
│   │       └── application.yml
│   └── test/
├── pom.xml
├── README.md
└── ...
```

## 下一步

- 查看 [README.md](README.md) 了解项目功能
- 阅读 [CONTRIBUTING.md](CONTRIBUTING.md) 参与贡献
- 参考 [DEPLOYMENT.md](DEPLOYMENT.md) 进行生产部署

## 获得帮助

- 📖 查看项目文档
- 🐛 在 GitHub Issues 中报告问题
- 💬 在 GitHub Discussions 中提问

---

**安装有问题？**在 [GitHub Issues](https://github.com/chenshoulu202/ai-music-dispatcher/issues) 中联系我们！
