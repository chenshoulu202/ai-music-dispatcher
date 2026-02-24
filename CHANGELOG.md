# 变更日志

所有对该项目的更改都将记录在此文件中。

## 格式遵循

本文件遵循 [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) 规范。
项目遵循 [Semantic Versioning](https://semver.org/spec/v2.0.0.html)。

## [0.0.1] - 2024-02-24

### 新增 (Added)

- ✨ 初始版本发布
- 🎤 AI 口播生成功能，集成 Google Gemini 2.0 Flash
- 🔊 文本转语音（TTS）功能，支持 Edge TTS
- 💬 实时弹幕处理和分发
- 🎵 本地音乐库管理
- 👥 灵活的权限管理系统（基于点赞/送礼）
- 📊 Caffeine 缓存优化
- 🔐 全局异常处理
- 📝 详细的日志记录系统
- 🧪 单元测试框架集成
- 📖 完整的项目文档（README、CONTRIBUTING 等）

### 技术栈

- **Java 17**
- **Spring Boot 3.2.3**
- **Spring WebSocket**
- **Spring Data JPA + Hibernate**
- **MySQL 8.0+**
- **Caffeine Cache 3.1.8**
- **Log4j2**
- **Lombok**
- **Maven 3.8+**

### 功能细节

#### 🎤 Gemini AI 集成
- 自动生成个性化口播文案
- 支持自定义系统提示词
- 实时 API 调用和缓存

#### 🔊 TTS 支持
- 支持多种语言和语音类型
- 可配置的语速调节
- 支持多种音频输出格式（MP3、WAV）

#### 💬 弹幕处理
- WebSocket 长连接支持
- 支持点歌、送礼、点赞等事件
- 实时消息分发和处理

#### 🎵 音乐库
- 本地文件系统集成
- 音乐查询和过滤
- 播放列表管理

#### 👥 权限系统
- 基于点赞的权限授予
- 基于送礼的权限授予
- 可配置的权限时长

### 已知限制

- 仅支持 MySQL 数据库
- TTS 依赖于边缘网络（Edge TTS）
- Gemini API 需要 Internet 连接

### 未来计划

- [ ] 支持更多 TTS 提供商（如讯飞、百度等）
- [ ] 支持 PostgreSQL 数据库
- [ ] 集成更多 LLM 模型（如 Claude、GPT 等）
- [ ] 支持直播间其他数据源（B站、抖音等）
- [ ] 提高 TTS 缓存策略
- [ ] 前端 Web 界面
- [ ] Docker 容器化部署

### 贡献者

感谢所有贡献者的支持！

---

## 版本发布历史

### 如何贡献

如果你想贡献新功能或报告问题，请参考 [CONTRIBUTING.md](CONTRIBUTING.md)。

### 版本发布流程

1. 更新 `CHANGELOG.md`
2. 更新版本号（在 `pom.xml` 中）
3. 提交 PR 进行审核
4. 合并后发行新版本

---

**最后更新**: 2024-02-24
