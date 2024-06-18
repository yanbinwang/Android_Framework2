package com.example.thirdparty.google

import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import com.example.common.utils.builder.shortToast
import com.example.common.utils.function.getManifestString
import com.example.thirdparty.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status.RESULT_INTERNAL_ERROR
import com.google.android.gms.tasks.Task

/**
 * google三方登录
 */
class GoogleAuthUtil(private val mActivity: FragmentActivity) {
    private val mGoogleSignInClient by lazy {
        GoogleSignIn.getClient(mActivity, GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(GOOGLE_AUTH_API.orEmpty())
                .requestEmail()
                .build())
    }

    companion object {
        val GOOGLE_AUTH_API = getManifestString("GOOGLE_AUTH_API")
    }

    init {
        signOut()
    }

    fun signIn(success: (bean: GoogleInfoBean) -> Unit, cancel: () -> Unit, failed: () -> Unit) {
        val account = GoogleSignIn.getLastSignedInAccount(mActivity)
        when {
            account != null -> {
                if (account.id.isNullOrEmpty()) {
                    R.string.authOpenIdError.shortToast()
                    return
                }
                success(GoogleInfoBean(account))
            }
            else -> callSignIn(success, cancel, failed)
        }
    }

    private fun callSignIn(success: (bean: GoogleInfoBean) -> Unit, cancel: () -> Unit, failed: () -> Unit) {
        mActivity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            handleSignInResult(task, success, cancel, failed)
//            mActivity.clearOnActivityResultListener()
            signOut()
        }.launch(mGoogleSignInClient.signInIntent)
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>, success: (account: GoogleInfoBean) -> Unit, cancel: () -> Unit, failed: () -> Unit, ) {
        try {
            val account = completedTask.getResult(ApiException::class.java) ?: throw ApiException(RESULT_INTERNAL_ERROR)
            if (account.id.isNullOrEmpty()) {
                R.string.authOpenIdError.shortToast()
                return
            }
            success(GoogleInfoBean(account))
        } catch (e: ApiException) {
            when (e.statusCode) {
                12501 -> {
                    R.string.authCancel.shortToast()
                    cancel()
                }
                7 -> {
                    R.string.authNetworkFail.shortToast()
                    failed()
                }
                else -> {
                    R.string.authError.shortToast()
                    failed()
                }
            }
        }
    }

    fun signOut() {
        try {
            mGoogleSignInClient.signOut()
            mGoogleSignInClient.revokeAccess()
        } catch (_: Exception) {
        }
    }

}