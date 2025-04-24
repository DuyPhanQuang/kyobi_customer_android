package com.kyobi.featurecommon.auth.session

import com.kyobi.domain.model.LoggedInUser

data class Session(
    val userId: String? = null,
) {
    companion object {
        fun fromLoggedInUser(user: LoggedInUser?): Session {
            return Session(
                userId = user?.id,
            )
        }
    }
}