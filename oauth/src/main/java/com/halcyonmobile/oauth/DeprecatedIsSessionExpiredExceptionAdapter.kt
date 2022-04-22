package com.halcyonmobile.oauth

import com.halcyonmobile.oauth.IsSessionExpiredException as DeprecatedIsSessionExpiredException
import com.halcyonmobile.oauth.dependencies.IsSessionExpiredException
import retrofit2.HttpException

@Deprecated("Only needed as long as com.halcyonmobile.oauth.IsSessionExpiredException] is kept.", level = DeprecationLevel.WARNING)
internal class DeprecatedIsSessionExpiredExceptionAdapter(
    private val delegate: DeprecatedIsSessionExpiredException
) : IsSessionExpiredException {
    override fun invoke(throwable: Throwable): Boolean =
        throwable is HttpException && delegate.invoke(throwable)
}
