package com.example.thirdparty.auth.google.bean

import android.net.Uri
import android.os.Parcelable
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.parcelize.Parcelize

@Parcelize
data class GoogleInfoBean(
//    var id: String? = null,
//    var email: String? = null,
//    var displayName: String? = null,
//    var photoUrl: Uri? = null,
//    var idToken: String? = null,
    var id: String? = null,
    var idToken: String? = null,
    var displayName: String? = null,
    var familyName: String? = null,
    var givenName: String? = null,
    var profilePictureUri: Uri? = null,
    var phoneNumber: String? = null
) : Parcelable {
//    constructor(info: GoogleSignInAccount) : this(info.id, info.email, info.displayName, info.photoUrl, info.idToken)
    constructor(info: GoogleIdTokenCredential) : this(info.id, info.idToken, info.displayName, info.familyName, info.givenName, info.profilePictureUri, info.phoneNumber)
}