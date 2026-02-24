# 安全政策

## 报告安全漏洞

我们非常重视项目的安全。如果你发现了安全漏洞，请**不要**在公开的 Issue 中报告。

### 安全漏洞报告流程

1. **发送电子邮件** 到 security@example.com（请将此邮箱替换为实际联系方式）
2. **包含以下信息**:
   - 漏洞的详细描述
   - 影响的版本
   - 复现步骤
   - 潜在的影响
   - 建议的修复方案（如有）

3. **等待确认** - 我们会在 48 小时内确认收到你的报告
4. **共享信息** - 一旦漏洞被修复，我们会向报告者征询意见后公开发布

## 安全最佳实践

### 配置安全

为了保护你的部署环境，请遵循以下建议：

#### 1. 敏感信息管理
```yaml
# ❌ 不要在代码中暴露敏感信息
gemini:
  api-key: YOUR_API_KEY  # 不好的做法

# ✅ 使用环境变量
gemini:
  api-key: ${GEMINI_API_KEY}
```

#### 2. 数据库安全
- 使用强密码
- 限制数据库访问权限
- 定期备份数据库
- 使用 SSL 连接数据库

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/db?useSSL=true&requireSSL=true
    username: ${DB_USER}
    password: ${DB_PASSWORD}
```

#### 3. WebSocket 安全
- 使用 WSS（WebSocket Secure）而不是 WS
- 验证来源
- 实施速率限制
- 验证输入数据

#### 4. API 密钥管理
- 不要将密钥提交到版本控制
- 定期轮换密钥
- 使用密钥管理服务（如 AWS Secrets Manager）
- 为不同环境使用不同的密钥

### 依赖安全

#### 检查依赖漏洞
```bash
# 使用 OWASP Dependency-Check
mvn org.owasp:dependency-check-maven:check

# 检查特定漏洞
mvn org.apache.maven.plugins:maven-dependency-plugin:analyze
```

#### 保持依赖更新
```bash
# 检查可用的更新
mvn versions:display-dependency-updates

# 更新依赖
mvn versions:use-latest-releases
```

### 代码安全

#### 输入验证
```java
// ✅ 良好的做法 - 验证用户输入
if (!isValidSongName(songName)) {
    throw new IllegalArgumentException("Invalid song name");
}

// ✅ 使用 Spring Validation
@Valid
public ResponseEntity<String> requestSong(@Valid @RequestBody SongRequest request) {
    // 处理请求
}
```

#### SQL 注入防护
```java
// ✅ 使用 JPA 预编译查询
@Query("SELECT m FROM MusicLibrary m WHERE m.name = :name")
MusicLibrary findByName(@Param("name") String name);

// ❌ 避免字符串拼接
Query query = entityManager.createQuery("SELECT * FROM music WHERE name = '" + name + "'");
```

#### 敏感日志
```java
// ❌ 不要记录敏感信息
logger.info("User logged in with API key: " + apiKey);

// ✅ 只记录必要信息
logger.info("User {} logged in successfully", userId);
```

### 部署安全

#### Docker 安全
```dockerfile
# ✅ 使用更新的基础镜像
FROM eclipse-temurin:17-jre-alpine

# ✅ 运行非 root 用户
USER appuser

# ✅ 设置只读根文件系统
RUN chmod 555 /

# ✅ 暴露特定端口
EXPOSE 8080
```

#### 网络安全
- 使用 HTTPS/TLS
- 启用 CORS 仅允许可信域
- 实施速率限制
- 使用 Web 应用防火墙（WAF）

## 安全头部配置

```java
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .headers()
                .contentSecurityPolicy("default-src 'self'")
                .and()
                .xssProtection()
                .and()
                .frameOptions().denyAll()
            .and()
            // 其他配置
            .build();
        
        return http.build();
    }
}
```

## 已修复的漏洞

| 版本 | 漏洞 | 修复 | 日期 |
|------|------|------|------|
| - | - | - | - |

## 第三方安全工具

我们使用以下工具进行安全检查：

- [OWASP Dependency-Check](https://owasp.org/www-project-dependency-check/) - 扫描依赖漏洞
- [SonarQube](https://www.sonarqube.org/) - 代码质量和安全分析
- [GitHub Security](https://github.com/features/security) - GitHub 内置安全功能

## 安全更新政策

- 安全修复将被标记为紧急优先级
- 关键安全问题会发布补丁版本
- 我们建议用户订阅项目的 Release 通知

## 参考资源

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [Java Security Best Practices](https://www.oracle.com/java/technologies/javase/seccodeguide.html)

## 联系方式

如有安全相关疑问，请联系：
- **邮箱**: security@example.com  <!-- 请更新为实际邮箱 -->
- **GitHub Security Advisory**: 使用 GitHub 的安全建议功能

感谢你帮助保护项目的安全！
