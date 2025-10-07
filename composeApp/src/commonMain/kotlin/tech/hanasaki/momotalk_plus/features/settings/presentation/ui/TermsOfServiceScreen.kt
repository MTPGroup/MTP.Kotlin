package tech.hanasaki.momotalk_plus.features.settings.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.woowla.compose.icon.collections.ionicons.Ionicons
import com.woowla.compose.icon.collections.ionicons.ionicons.Filled
import com.woowla.compose.icon.collections.ionicons.ionicons.filled.ChevronBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsOfServiceScreen(
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "服务条款",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Ionicons.Filled.ChevronBack,
                            contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
                TermsSection(
                    title = "1. 服务协议的接受",
                    content = "欢迎使用 MomoTalk Plus。通过访问或使用本应用，您同意受本服务条款的约束。如果您不同意这些条款，请不要使用本应用。"
                )
            }

            item {
                TermsSection(
                    title = "2. 服务描述",
                    content = "MomoTalk Plus 是一款角色扮演聊天应用，提供以下服务：\n" +
                            "• AI 驱动的角色对话功能\n" +
                            "• 多角色管理和自定义\n" +
                            "• 聊天记录管理和云端同步\n" +
                            "• 跨平台支持（Android、iOS、Desktop）\n" +
                            "• 主题和个性化设置"
                )
            }

            item {
                TermsSection(
                    title = "3. 账户注册",
                    content = "• 您必须提供准确、完整的注册信息\n" +
                            "• 您有责任维护账户密码的安全性\n" +
                            "• 您对账户下的所有活动负责\n" +
                            "• 一个人只能注册一个账户\n" +
                            "• 您必须年满 13 岁才能注册账户\n" +
                            "• 如发现任何未经授权的使用，请立即通知我们"
                )
            }

            item {
                TermsSection(
                    title = "4. 用户行为规范",
                    content = "使用本应用时，您不得：\n" +
                            "• 发布违法、有害、威胁、辱骂或令人反感的内容\n" +
                            "• 侵犯他人的知识产权或隐私权\n" +
                            "• 传播病毒、恶意代码或其他有害组件\n" +
                            "• 试图未经授权访问系统或网络\n" +
                            "• 滥用或过度使用服务资源\n" +
                            "• 进行商业广告或垃圾信息传播\n" +
                            "• 冒充他人或虚假陈述与他人的关系"
                )
            }

            item {
                TermsSection(
                    title = "5. 知识产权",
                    content = "• 本应用及其原创内容、功能和特性归 MomoTalk Plus 所有\n" +
                            "• 受版权、商标和其他知识产权法保护\n" +
                            "• 您不得复制、修改、分发或反向工程本应用\n" +
                            "• 用户生成的内容版权归用户所有\n" +
                            "• 使用本应用，您授予我们使用、存储和处理您内容的权利"
                )
            }

            item {
                TermsSection(
                    title = "6. AI 服务使用",
                    content = "• AI 生成的内容仅供娱乐和参考\n" +
                            "• 我们不保证 AI 回复的准确性或适用性\n" +
                            "• AI 可能偶尔产生不当或不准确的内容\n" +
                            "• 请勿分享敏感或私人信息给 AI\n" +
                            "• 您对如何使用 AI 生成的内容负责\n" +
                            "• 我们保留调整或限制 AI 功能的权利"
                )
            }

            item {
                TermsSection(
                    title = "7. 免责声明",
                    content = "• 本应用按\"现状\"提供，不提供任何明示或暗示的保证\n" +
                            "• 我们不保证服务不会中断或无错误\n" +
                            "• 我们不对任何数据丢失或损坏负责\n" +
                            "• 我们不对第三方服务或链接的内容负责\n" +
                            "• 使用本应用的风险由您自行承担"
                )
            }

            item {
                TermsSection(
                    title = "8. 责任限制",
                    content = "在法律允许的最大范围内，MomoTalk Plus 及其开发者不对以下情况承担责任：\n" +
                            "• 间接、偶然、特殊或后果性损害\n" +
                            "• 利润、数据或使用损失\n" +
                            "• 业务中断\n" +
                            "• 第三方服务的问题\n" +
                            "• 用户内容或行为导致的损害"
                )
            }

            item {
                TermsSection(
                    title = "9. 账户终止",
                    content = "我们保留在以下情况下暂停或终止您账户的权利：\n" +
                            "• 违反本服务条款\n" +
                            "• 从事非法或不当行为\n" +
                            "• 长期不活跃\n" +
                            "• 应法律要求\n" +
                            "• 您可以随时删除自己的账户"
                )
            }

            item {
                TermsSection(
                    title = "10. 服务变更和终止",
                    content = "• 我们可能随时修改、暂停或终止服务\n" +
                            "• 我们将尽力提前通知重大变更\n" +
                            "• 我们不对服务的修改或终止承担责任\n" +
                            "• 终止后，您的数据可能会被删除"
                )
            }

            item {
                TermsSection(
                    title = "11. 条款变更",
                    content = "我们可能会不时更新这些服务条款。重大变更时，我们会通过应用内通知或邮件通知您。继续使用本应用即表示您接受修订后的条款。"
                )
            }

            item {
                TermsSection(
                    title = "12. 适用法律",
                    content = "本服务条款受相关法律管辖并按其解释。因本条款引起的任何争议将在适当的司法管辖区解决。"
                )
            }

            item {
                TermsSection(
                    title = "13. 可分割性",
                    content = "如果本条款的任何条款被认定为无效或不可执行，其余条款将继续有效。"
                )
            }

            item {
                TermsSection(
                    title = "14. 联系方式",
                    content = "如果您对本服务条款有任何疑问，请通过以下方式联系我们：\n\n" +
                            "• 邮箱：hanasakayui2022@gmail.com\n" +
                            "• GitHub：https://github.com/MTPGroup/MTP.Kotlin\n\n" +
                            "感谢您使用 MomoTalk Plus！"
                )
            }

            item {
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun TermsSection(
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

