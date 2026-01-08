package tech.hanasaki.azusa.auth

import io.ktor.server.auth.Principal
import java.util.UUID

data class UserPrincipal(val userId: UUID) : Principal
