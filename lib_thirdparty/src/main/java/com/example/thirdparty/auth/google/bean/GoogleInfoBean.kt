package com.example.thirdparty.auth.google.bean

import android.net.Uri
import android.os.Parcelable
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.parcelize.Parcelize

@Parcelize
data class GoogleInfoBean(
    var id: String? = null,
    var email: String? = null,
    var displayName: String? = null,
    var photoUrl: Uri? = null,
    var idToken: String? = null,
) : Parcelable {
    //    constructor(info: GoogleSignInAccount) : this(info.id, info.email, info.displayName, info.photoUrl)
    constructor(info: GoogleSignInAccount) : this(info.id, info.email, info.displayName, info.photoUrl, info.idToken)
}