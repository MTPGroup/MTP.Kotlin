# MomoTalk Plus (MTP)

<div align="center">

![MTP Logo](./assets/logo.png)

</div>

MomoTalk Plus (MTP) 是一款基于 Kotlin Multiplatform 开发的角色扮演聊天应用。本项目使用现代化的技术栈，旨在提供流畅的原生跨平台应用体验。

## 项目简介

MomoTalk Plus 是一款模拟游戏《蔚蓝档案》(Blue Archive)
中通讯软件的应用，允许用户与游戏中的学生角色进行对话互动。项目集成了大语言模型 (LLM) 功能，使角色对话更加智能和自然。

## 功能特点

- [x] **原生跨平台**：基于 Kotlin Multiplatform，为 Android 和桌面端提供原生性能
- [x] **现代化UI**：使用 Jetpack Compose 构建的声明式、响应式用户界面
- [ ] **角色扮演聊天**：与游戏中的学生角色进行对话互动
- [ ] **多模型支持**：可配置多个 LLM 模型和 API 接入点
- [ ] **数据持久化**：本地存储聊天记录和用户数据
- [ ] **用户个性化**：支持自定义用户头像和用户名
- [ ] **流式响应**：支持流式AI回复，实时查看生成内容
- [ ] **角色自定义**：允许创建和编辑自定义角色，包括角色提示词和头像

## 模型配置

考虑在`v1.0.0`版本中移除自定义模型配置

## 技术架构

### 技术栈

- **框架**：Kotlin Multiplatform
- **UI**：Compose Multiplatform
- **网络请求**：Ktor
- **序列化**：kotlinx.serialization
- **依赖注入**：Koin
- **数据存储**：[WIP]

### 项目结构

```bash
├── composeApp/ # 共享模块，包含Compose UI和通用业务逻辑
│   ├── src/ 
│   │   ├── androidMain/ # Android 平台特定代码
│   │   ├── commonMain/ # 所有平台共享的 Kotlin 代码 (核心、数据、领域层)
│   │   ├── desktopMain/ # 桌面平台 (JVM) 特定代码
│   │   └── iosMain/ # iOS 平台特定代码
│   └── build.gradle.kts
├── iosApp/ # iOS 应用的 Xcode 项目
└── build.gradle.kts # Gradle 根项目构建文件
```

## 开发指南

### 环境要求

- JDK 17 或更高版本
- IntelliJ IDEA (推荐) 或 Android Studio
- Kotlin Multiplatform 插件
- (可选) Xcode 用于 iOS 开发
- (可选) Android SDK 用于 Android 开发

### 开发步骤

1. 克隆仓库

```bash
git clone https://github.com/MTPGroup/MTP.Kotlin
cd MomotalkPlus
```

2. 使用 IntelliJ IDEA 或 Android Studio 打开项目。 等待 Gradle 同步完成并下载所有依赖。
3. 在 IDE 中选择对应的运行配置（例如 composeApp, desktop, iosApp）来构建和运行应用。

### 推荐的 IDE 设置

- IntelliJ IDEA + Kotlin Multiplatform Plugin
- Android Studio + Kotlin Multiplatform Plugin

## 贡献指南

欢迎贡献代码、报告问题或提出新功能建议。请遵循以下步骤：

1. Fork 本仓库
2. 创建新分支 (`git checkout -b feature/your-feature`)
3. 提交您的更改 (`git commit -m 'Add some feature'`)
4. 推送到分支 (`git push origin feature/your-feature`)
5. 创建 Pull Request

## 许可证

[GNU General Public License v3.0](./LICENSE)

## 联系我们

如果您有任何问题或建议，请通过以下方式联系我们：

- 电子邮件: [hanasakayui2022@gmail.com](mailto:hanasakayui2022@gmail.com)
- GitHub Issues：在本项目的 [Issues](https://github.com/MTPGroup/MTP.Kotlin/issues) 页面提交问题或建议