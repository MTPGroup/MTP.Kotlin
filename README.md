# MomoTalk Plus (MTP)

<div align="center" >
<img src="./assets/logo.png" alt="MTP Logo" width="200"/>
</div>

## 项目简介

MomoTalk Plus (MTP) 是一款基于 Kotlin Multiplatform 开发的角色扮演聊天应用。本项目使用现代化的技术栈，旨在提供流畅的原生跨平台应用体验。

## 功能特点

### 已实现功能

- ✅ 用户认证系统
    - 登录
    - 注册
    - 忘记密码
- ✅ 主页导航
    - 聊天列表
    - 联系人列表
    - Tab 导航（无动画切换）
- ✅ 聊天功能
    - 聊天列表展示
    - 滑动操作（置顶、删除）
    - 头像点击交互
- ✅ 响应式 UI
    - 底部导航栏自动显示/隐藏

### 开发中功能

- 🚧 主题系统
- 🚧 创建联系人功能

## 技术架构

### 技术栈

- **框架**：Kotlin Multiplatform
- **UI**：Compose Multiplatform
- **架构模式**：Clean Architecture + MVI
- **网络请求**：Ktor
- **序列化**：kotlinx.serialization
- **依赖注入**：Koin
- **导航**：Navigation Compose
- **异步编程**：Kotlin Coroutines
- **数据存储**：Kotlin Multiplatform Settings
- **构建工具**：Gradle

### 项目结构

```
MTP.Kotlin/
├── composeApp/                           # 主应用模块
│   └── src/
│       ├── androidMain/                  # Android 平台特定代码
│       ├── commonMain/                   # 所有平台共享的 Kotlin 代码
│       │   └── kotlin/
│       │       └── tech/hanasaki/momotalk_plus/
│       │           ├── app/              # 应用层
│       │           │   ├── di/          # 依赖注入配置
│       │           │   ├── ui/          # 应用级 UI
│       │           │   └── viewmodel/   # 应用级 ViewModel
│       │           ├── core/            # 核心模块
│       │           └── features/        # 功能模块
│       │               ├── auth/        # 认证功能
│       │               │   └── presentation/
│       │               │       └── ui/
│       │               ├── chats/       # 聊天功能
│       │               │   └── presentation/
│       │               │       ├── ui/
│       │               │       │   └── widgets/
│       │               │       └── viewmodel/
│       │               ├── contacts/    # 联系人功能
│       │               │   └── presentation/
│       │               └── home/        # 主页功能
│       │                   └── presentation/
│       ├── desktopMain/                  # 桌面平台 (JVM) 特定代码
│       └── iosMain/                      # iOS 平台特定代码
├── iosApp/                               # iOS 应用的 Xcode 项目
├── gradle/                               # Gradle 配置
│   ├── libs.versions.toml               # 依赖版本管理
│   └── wrapper/
├── build.gradle.kts                      # Gradle 根项目构建文件
└── settings.gradle.kts
```

### 架构设计

项目采用 **Clean Architecture** 和 **MVI** 架构模式：

- **Presentation Layer**: UI 和 ViewModel，负责用户界面和用户交互
- **Domain Layer**: 业务逻辑和用例，包含核心业务规则
- **Data Layer**: 数据源和仓储，处理数据的获取和存储

## 开发指南

### 环境要求

- JDK 17 或更高版本
- IntelliJ IDEA 2025.2.2 或更高版本 (推荐) 或 Android Studio
- Kotlin 2.1.0 或更高版本
- Kotlin Multiplatform 插件
- (可选) Xcode 用于 iOS 开发
- (可选) Android SDK 用于 Android 开发

### 快速开始

1. **克隆仓库**

```bash
git clone https://github.com/MTPGroup/MTP.Kotlin.git
cd MTP.Kotlin
```

2. **打开项目**

使用 IntelliJ IDEA 或 Android Studio 打开项目。等待 Gradle 同步完成并下载所有依赖。

3. **构建项目**

```bash
./gradlew build
```

4. **运行项目**

在 IDE 中选择对应的运行配置（例如 composeApp, desktop, iosApp）来构建和运行应用。

```bash
# 运行桌面版
./gradlew :composeApp:run

# 构建 Android 版
./gradlew :composeApp:assembleDebug
```

### 推荐的 IDE 设置

- IntelliJ IDEA + Kotlin Multiplatform Plugin
- Android Studio + Kotlin Multiplatform Plugin

### 开发规范

#### 代码风格

- 遵循 Kotlin 官方编码规范
- 使用 4 空格缩进
- 函数名使用驼峰命名法
- 常量使用大写字母和下划线

#### Git 提交规范

```
✨ 新功能
🐛 修复 bug
📝 文档更新
🎨 代码格式调整
♻️ 重构
✅ 测试相关
🔧 构建工具或辅助工具的变动
```

## 依赖管理

主要依赖项在 `gradle/libs.versions.toml` 中管理，包括：

- Compose Multiplatform
- Kotlin Coroutines
- Koin (依赖注入)
- Navigation Compose
- Material3
- Ktor (网络请求)
- kotlinx.serialization

## 贡献指南

欢迎贡献代码、报告问题或提出新功能建议。请遵循以下步骤：

1. Fork 本仓库
2. 创建新分支 (`git checkout -b feature/your-feature`)
3. 提交您的更改 (`git commit -m 'feat: Add some feature'`)
4. 推送到分支 (`git push origin feature/your-feature`)
5. 创建 Pull Request

## 许可证

[GNU General Public License v3.0](./LICENSE)

## 联系我们

如果您有任何问题或建议，请通过以下方式联系我们：

- 作者: hanasaki
- GitHub: [MTPGroup](https://github.com/MTPGroup)
- 电子邮件: [hanasakayui2022@gmail.com](mailto:hanasakayui2022@gmail.com)
- GitHub Issues：在本项目的 [Issues](https://github.com/MTPGroup/MTP.Kotlin/issues) 页面提交问题或建议

## 致谢

- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Koin](https://insert-koin.io/)
- [Ktor](https://ktor.io/)

---

**注意**: 本项目正在积极开发中，API 可能会发生变化。
