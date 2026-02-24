# 贡献指南

感谢你对 AI Music Dispatcher 的关注！我们欢迎各种形式的贡献，包括代码、文档、问题报告和功能建议。

## 📌 行为准则

我们承诺创建一个友好、包容的环境。所有参与者都应该：

- 尊重他人的观点和经验
- 接受建设性的批评
- 关注对社区最有益的事情
- 对其他社区成员表示同情

## 🐛 报告问题

在报告问题时，请提供以下信息：

1. **问题描述** - 清楚地描述问题是什么
2. **复现步骤** - 如何复现这个问题
3. **预期行为** - 应该发生什么
4. **实际行为** - 实际发生了什么
5. **环境信息** - Java 版本、操作系统、MySQL 版本等
6. **日志输出** - 相关的错误日志

## 💡 提出功能建议

在提出新功能建议时：

1. 明确说明功能解决的问题
2. 提供尽可能详细的功能描述
3. 提供示例用例
4. 指出可能的替代方案

## 🔧 开发流程

### 1. Fork 仓库

点击 GitHub 页面上的 "Fork" 按钮，创建你自己的仓库副本。

### 2. 克隆仓库

```bash
git clone https://github.com/your-username/ai-music-dispatcher.git
cd ai-music-dispatcher
```

### 3. 创建特性分支

```bash
git checkout -b feature/your-feature-name
```

分支命名约定：
- `feature/` - 新功能
- `fix/` - 修复问题
- `docs/` - 文档改进
- `test/` - 测试相关
- `refactor/` - 代码重构

### 4. 进行改动

在开发前，请确保理解以下准则：

#### 代码风格

- 使用 2 个空格进行缩进（或配置编辑器为 2 空格）
- 遵循 Java 命名约定
- 添加必要的代码注释，特别是对复杂逻辑的解释
- 使用 Lombok 注解（@Data, @Slf4j 等）简化代码

#### 提交信息

遵循 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

```
<type>(<scope>): <subject>

<body>

<footer>
```

例如：
```
feat(tts): add voice speed adjustment

Allow users to adjust TTS voice speed through configuration file.
Supports values from -1.0 to 1.0.

Closes #123
```

提交类型：
- `feat` - 新功能
- `fix` - 修复问题
- `docs` - 文档
- `style` - 代码风格
- `refactor` - 重构
- `test` - 测试
- `chore` - 构建脚本、依赖等

### 5. 提交并推送

```bash
git add .
git commit -m "your commit message"
git push origin feature/your-feature-name
```

### 6. 发起 Pull Request

在 GitHub 上创建 Pull Request，并填写以下信息：

- **标题** - 简洁描述改动
- **描述** - 详细说明改动内容、原因和测试方法
- **关联 Issue** - 如果有相关 Issue，使用 `Closes #123`

#### PR 检查清单

在提交 PR 前，请确保：

- [ ] 代码遵循项目风格准则
- [ ] 添加了适当的测试用例
- [ ] 更新了相关文档
- [ ] 提交信息清晰有意义
- [ ] 没有引入新的编译警告
- [ ] 测试通过（本地运行 `mvn test`）
- [ ] 代码不包含敏感信息（API Key、密码等）

## 📚 文档改进

如果你发现文档不清楚或缺失：

1. Fork 仓库
2. 编辑 `README.md` 或其他文档文件
3. 提交 PR 并描述你的改进

## 🤖 扩展 AI 大模型支持

如果你想添加对新 AI 大模型的支持，以下是完整指南：

### 方案 A：配置方式替换（推荐 - 仅配置，无需编码）

对于大多数 API 兼容或 JSON 格式相似的模型，你可以直接在 `application.yml` 中配置，无需修改代码：

1. **在 `application.yml` 中更新 API 配置**
   ```yaml
   gemini:
     api-key: "your-new-model-api-key"
     api-url: https://your-model-api-endpoint
     system-prompt: "你的自定义提示词"
   ```

2. **测试连接**
   ```bash
   mvn spring-boot:run
   # 查看日志确认 API 调用成功
   ```

**适用场景**: APIs 格式相似的模型（OpenAI、通义千问、讯飞星火等）

### 方案 B：代码方式扩展（用于完全不同的 API 格式）

如果新模型的 API 格式与 Gemini 差异很大（请求/响应结构不同），需要修改 `GeminiService.java`：

1. **修改请求构建方法 `buildGeminiRequest()`**
   ```java
   // src/main/java/com/example/aimusicdispatcher/generator/GeminiService.java
   
   private YourModelRequest buildYourModelRequest(String prompt) {
       // 根据你的模型 API 文档构建请求对象
       YourModelRequest request = new YourModelRequest();
       request.setPrompt(prompt);
       request.setModel("your-model-name");
       request.setMaxTokens(50);
       return request;
   }
   ```

2. **修改响应解析方法 `extractTextFromGeminiResponse()`**
   ```java
   private String extractTextFromYourModelResponse(YourModelResponse response) {
       // 根据你的模型 API 文档解析响应
       if (response.getResult() != null) {
           return response.getResult().getText();
       }
       return FALLBACK_INTRO_TEXT;
   }
   ```

3. **在 `generateIntroText()` 方法中使用新的构建和解析方法**
   ```java
   public String generateIntroText(String user, String songName) {
       // ... 准备 prompt ...
       
       YourModelRequest request = buildYourModelRequest(prompt);
       HttpEntity<YourModelRequest> entity = new HttpEntity<>(request, headers);
       
       // ... API 调用 ...
       
       return extractTextFromYourModelResponse(response.getBody());
   }
   ```

4. **创建对应的模型 DTO 类**
   - 在 `src/main/java/com/example/aimusicdispatcher/model/` 下创建新文件夹（如 `yourmodel/`）
   - 创建对应的请求和响应 DTO 类
   - 参考现有的 `gemini/` 文件夹结构

5. **测试和验证**
   ```bash
   mvn clean test
   mvn spring-boot:run
   ```

6. **提交 PR**（如果你想贡献给项目）
   - 遵循提交信息规范
   - 添加测试用例
   - 更新文档说明

### 完整示例：集成 OpenAI GPT

以下是一个完整的集成示例，展示如何添加 OpenAI GPT 支持：

**Step 1: 创建请求/响应模型**
```java
// src/main/java/com/example/aimusicdispatcher/model/openai/OpenAIMessage.java
public class OpenAIMessage {
    private String role;
    private String content;
    // Getters/Setters...
}

// src/main/java/com/example/aimusicdispatcher/model/openai/OpenAIRequest.java
public class OpenAIRequest {
    private String model;
    private List<OpenAIMessage> messages;
    private double temperature;
    private int max_tokens;
    // Getters/Setters...
}

// src/main/java/com/example/aimusicdispatcher/model/openai/OpenAIResponse.java
public class OpenAIResponse {
    private List<Choice> choices;
    
    public static class Choice {
        private OpenAIMessage message;
        // Getters/Setters...
    }
    // Getters/Setters...
}
```

**Step 2: 修改 GeminiService**
```java
// 在 GeminiService 中添加 OpenAI 支持
private String extractTextFromOpenAIResponse(OpenAIResponse response) {
    if (response.getChoices() != null && !response.getChoices().isEmpty()) {
        return response.getChoices().get(0).getMessage().getContent();
    }
    return FALLBACK_INTRO_TEXT;
}
```

**Step 3: 配置**
```yaml
gemini:
  api-key: "sk-your-openai-key"
  api-url: https://api.openai.com/v1/chat/completions
  system-prompt: "你是一个幽默风趣的直播间DJ..."
```

## 🎤 扩展 TTS 支持

类似地，如果你想添加新的 TTS 提供商：

### 当前 TTS 架构

```
TtsService (接口)
├── EdgeTtsService (Edge TTS 实现)
└── [你的自定义 TTS 实现]
```

### 添加新的 TTS 提供商

1. **创建新的 TTS 实现类**
   ```java
   // src/main/java/com/example/aimusicdispatcher/service/YourTtsService.java
   @Service
   public class YourTtsService implements TtsService {
       @Override
       public void synthesizeAndSave(String text, String outputPath) {
           // 你的 TTS 实现
       }
   }
   ```

2. **在配置中选择 TTS 提供商**
   ```yaml
   tts:
     provider: your-tts-provider
   ```

3. **在 `TtsFactory.java` 中注册新提供商**（如果存在）
   ```java
   if ("your-tts-provider".equals(provider)) {
       return new YourTtsService(config);
   }
   ```

## 📦 添加新依赖

如果你的大模型集成需要新的 Maven 依赖：

1. **在 `pom.xml` 中添加**
   ```xml
   <dependency>
       <groupId>com.your.library</groupId>
       <artifactId>your-sdk</artifactId>
       <version>1.0.0</version>
   </dependency>
   ```

2. **更新文档** - 在 README 的依赖部分添加说明

3. **提交 PR** - 包含依赖版本说明和使用说明


## 🧪 测试

在提交 PR 前，请进行充分的测试：

```bash
# 运行所有测试
mvn test

# 运行特定测试
mvn test -Dtest=MessageDispatcherTest
```

## 📛 获得帮助

如果你在任何时候需要帮助：

1. 查看现有的 Issues 和 Pull Requests
2. 在 Discussion 中提问
3. 查看项目文档

## ✅ 审核流程

所有 Pull Request 都将经过以下审核：

1. **自动检查** - CI 检查代码质量和测试通过
2. **人工审核** - 主维护者审核代码和设计
3. **回馈和改进** - 根据审核意见进行必要的改动
4. **合并** - 通过审核后合并到主分支

## 🎯 优先级

我们将更快地审核和合并以下类型的 PR：

- 🔴 **高优先级**: 安全漏洞、严重问题的修复
- 🟡 **中优先级**: 新功能、重要的 Bug 修复、文档改进
- 🟢 **低优先级**: 代码优化、琐碎的文档修正

## 📋 发布流程

项目遵循 Semantic Versioning (semver)：

- **MAJOR** - 不兼容的 API 改动
- **MINOR** - 新增功能（向后兼容）
- **PATCH** - Bug 修复（向后兼容）

## 🙏 致谢

感谢所有贡献者的支持！你的贡献使这个项目变得更好。

---

如有任何疑问，欢迎在 Issues 中提问！
