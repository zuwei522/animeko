/*
 * Copyright (C) 2024-2025 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.domain.session.auth

import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.first
import me.him188.ani.app.data.repository.RepositoryException
import me.him188.ani.app.domain.session.AccessTokenPair
import me.him188.ani.app.domain.session.SessionStateProvider
import me.him188.ani.app.domain.settings.BangumiMirrorProvider
import me.him188.ani.app.domain.settings.NoBangumiMirrorProvider
import me.him188.ani.client.apis.BangumiAniApi
import me.him188.ani.client.models.AniUserAuthRoutingLoginResponse
import me.him188.ani.utils.ktor.ApiInvoker
import me.him188.ani.utils.platform.Platform
import me.him188.ani.utils.platform.currentPlatform
import kotlin.time.Duration.Companion.milliseconds


interface OAuthClient {
    /**
     * 获取 OAuth 注册链接, 通过此链接完成的 OAuth 授权将会自动注册一个新的 Ani 用户.
     *
     * @throws me.him188.ani.app.data.repository.RepositoryException
     * @throws IllegalArgumentException requestId 为空
     */
    suspend fun getOAuthRegisterLink(requestId: String): String

    /**
     * 获取 OAuth 绑定链接, 通过此链接完成的 OAuth 授权将会绑定到已有的 Ani 用户.
     *
     * @throws me.him188.ani.app.data.repository.RepositoryException
     * @throws IllegalArgumentException requestId 为空
     *
     */
    suspend fun getOAuthBindLink(requestId: String): String

    /**
     * 获取 OAuth 绑定或登录结果, 此结果将直接用于登录 ani 用户.
     *
     * @return null 表示还没有结果.
     * @throws me.him188.ani.app.data.repository.RepositoryException
     * @throws IllegalArgumentException requestId 为空
     */
    suspend fun getResult(requestId: String): OAuthResult?
}

data class OAuthResult(
    val tokens: AccessTokenPair,
    val expiresInSeconds: Long,
    val refreshToken: String,
)

fun AniUserAuthRoutingLoginResponse.toOAuthResult(): OAuthResult {
    return OAuthResult(
        tokens = AccessTokenPair(
            aniAccessToken = this.tokens.accessToken,
            expiresAtMillis = this.tokens.expiresAtMillis,
            bangumiAccessToken = this.tokens.bangumiAccessToken,
        ),
        expiresInSeconds = this.tokens.expiresAtMillis.milliseconds.inWholeSeconds,
        refreshToken = this.tokens.refreshToken,
    )
}

class BangumiOAuthClient(
    private val bangumiApi: ApiInvoker<BangumiAniApi>,
    private val sessionStateProvider: SessionStateProvider,
    private val mirrorProvider: BangumiMirrorProvider = NoBangumiMirrorProvider,
    private val platform: Platform = currentPlatform(),
) : OAuthClient {
    override suspend fun getOAuthRegisterLink(requestId: String): String {
        require(requestId.isNotBlank()) { "requestId must not be blank or empty" }
        val resp = try {
            bangumiApi.invoke {
                oauth(requestId, platform.name.lowercase(), platform.arch.displayName.lowercase()).body()
            }
        } catch (e: Throwable) {
            throw RepositoryException.wrapOrThrowCancellation(e)
        }
        return rewriteToMirrorIfEnabled(resp.url)
    }

    override suspend fun getOAuthBindLink(requestId: String): String {
        require(requestId.isNotBlank()) { "requestId must not be blank or empty" }

        val resp = try {
            bangumiApi.invoke {
                bind(requestId, platform.name.lowercase(), platform.arch.displayName.lowercase()).body()
            }
        } catch (e: Throwable) {
            throw RepositoryException.wrapOrThrowCancellation(e)
        }
        return rewriteToMirrorIfEnabled(resp.url)
    }

    /**
     * Ani 服务器返回的 OAuth 授权链接指向 Bangumi 主站 (如 https://bgm.tv/oauth/authorize?...).
     * 若用户启用了镜像且配置了主站根域名, 把链接的主站 origin 替换为镜像根域名,
     * 使登录授权页也在镜像站点上打开; 未启用/未配置主站镜像时原样返回.
     */
    private suspend fun rewriteToMirrorIfEnabled(url: String): String {
        val settings = mirrorProvider.settings.first()
        if (!settings.enabled) return url
        val mirrorRoot = settings.rootBaseUrl.trimEnd('/')
        if (mirrorRoot.isBlank()) return url
        val defaultOrigin = "https://bgm.tv"
        return if (url.startsWith(defaultOrigin) && mirrorRoot != defaultOrigin) {
            mirrorRoot + url.removePrefix(defaultOrigin)
        } else {
            url
        }
    }

    override suspend fun getResult(requestId: String): OAuthResult? {
        require(requestId.isNotBlank()) { "requestId must not be blank or empty" }

        try {
            val resp = bangumiApi.invoke {
                getToken(requestId).body()
            }

            return resp.toOAuthResult()
        } catch (ex: ClientRequestException) {
            when (ex.response.status) {
                HttpStatusCode.TooEarly -> return null
                else -> throw RepositoryException.wrapOrThrowCancellation(ex)
            }
        } catch (e: Throwable) {
            throw RepositoryException.wrapOrThrowCancellation(e)
        }
    }
}
