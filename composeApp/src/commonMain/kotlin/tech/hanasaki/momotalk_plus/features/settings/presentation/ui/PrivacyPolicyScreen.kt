package tech.hanasaki.momotalk_plus.features.settings.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import tech.hanasaki.momotalk_plus.app.ui.widgets.MTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            MTopBar(
                title = "隐私政策",
                onNavigateBack = onNavigateBack,
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Text(
                    text = "最后更新日期：2025年10月",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                PolicySection(
                    title = "1. 信息收集",
                    content = "MomoTalk Plus 致力于保护您的隐私。我们收集以下类型的信息：\n" +
                            "• 账户信息：包括用户名、邮箱地址和密码（加密存储）\n" +
                            "• 聊天记录：与 AI 角色的对话内容\n" +
                            "• 使用数据：应用使用情况、设备信息和日志数据\n" +
                            "• 本地设置：主题偏好、通知设置等配置信息"
                )
            }

            item {
                PolicySection(
                    title = "2. 信息使用",
                    content = "我们使用收集的信息用于：\n" +
                            "• 提供和改进应用服务\n" +
                            "• 个性化用户体验\n" +
                            "• 发送服务通知和更新\n" +
                            "• 分析应用使用情况以优化性能\n" +
                            "• 维护应用安全和防止滥用"
                )
            }

            item {
                PolicySection(
                    title = "3. 数据存储",
                    content = "• 本地存储：设置和配置数据存储在您的设备上\n" +
                            "• 云端存储：聊天记录和账户信息存储在安全的云服务器上\n"
                )
            }

            item {
                PolicySection(
                    title = "4. 数据共享",
                    content = "我们不会出售、交易或转让您的个人信息给第三方。以下情况除外：\n" +
                            "• 获得您的明确同意\n" +
                            "• 遵守法律法规要求\n" +
                            "• 保护我们的权利和安全\n" +
                            "• 与可信的服务提供商合作（受保密协议约束）"
                )
            }

            item {
                PolicySection(
                    title = "5. AI 模型使用",
                    content = "• 我们使用第三方 AI 服务提供角色对话功能\n" +
                            "• 您的对话内容可能被发送到 AI 服务提供商进行处理\n" +
                            "• 我们选择符合隐私标准的 AI 服务提供商\n"
                )
            }

            item {
                PolicySection(
                    title = "6. Cookie 和追踪技术",
                    content = "我们使用 Cookie 和类似技术来：\n\n" +
                            "• 记住您的登录状态\n" +
                            "• 保存您的偏好设置\n" +
                            "• 分析应用使用情况\n" +
                            "• 改善用户体验"
                )
            }

            item {
                PolicySection(
                    title = "7. 您的权利",
                    content = "您拥有以下权利：\n" +
                            "• 访问您的个人信息\n" +
                            "• 更正不准确的信息\n" +
                            "• 删除您的账户和数据\n" +
                            "• 导出您的数据\n" +
                            "• 拒绝或限制数据处理\n" +
                            "• 撤回同意"
                )
            }

            item {
                PolicySection(
                    title = "8. 数据安全",
                    content = "我们采取以下措施保护您的数据：\n" +
                            "• 使用 HTTPS 加密传输\n" +
                            "• 加密存储敏感信息\n" +
                            "• 定期安全审计\n" +
                            "• 访问控制和权限管理\n" +
                            "• 安全的第三方服务集成"
                )
            }

            item {
                PolicySection(
                    title = "9. 儿童隐私",
                    content = "MomoTalk Plus 不针对 13 岁以下的儿童。我们不会故意收集儿童的个人信息。如果您是家长或监护人，并且认为您的孩子向我们提供了个人信息，请联系我们。"
                )
            }

            item {
                PolicySection(
                    title = "10. 政策更新",
                    content = "我们可能会不时更新本隐私政策。重大变更时，我们会通过应用内通知或邮件通知您。继续使用应用即表示您接受更新后的政策。"
                )
            }

            item {
                PolicySection(
                    title = "11. 联系我们",
                    content = "如果您对本隐私政策有任何疑问或建议，请通过以下方式联系我们：\n\n" +
                            "• 邮箱：hanasakayui2022@gmail.com\n" +
                            "• GitHub：https://github.com/MTPGroup/MTP.Kotlin"
                )
            }

            item {
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun PolicySection(
    title: String,
    content: String,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight.times(1.5f)
            )
        }
    }
}

