package com.example.thirdparty.auth.google

import androidx.activity.result.ActivityResult
import androidx.fragment.app.FragmentActivity
import com.example.common.utils.builder.shortToast
import com.example.common.utils.function.getManifestString
import com.example.common.utils.function.registerResult
import com.example.framework.utils.function.doOnDestroy
import com.example.thirdparty.R
import com.example.thirdparty.auth.google.bean.GoogleInfoBean
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
    private var onActivityResultListener: ((result: ActivityResult) -> Unit)? = null
    private val mActivityResult = mActivity.registerResult { onActivityResultListener?.invoke(it) }
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
        mActivity.doOnDestroy {
            onActivityResultListener = null
            mActivityResult?.unregister()
        }
        signOut()
    }

    fun signIn(onSuccess: (bean: GoogleInfoBean) -> Unit, onCancel: () -> Unit, onFailed: () -> Unit) {
        R.string.authInitiate.shortToast()
        val account = GoogleSignIn.getLastSignedInAccount(mActivity)
        when {
            account != null -> {
                if (account.id.isNullOrEmpty()) {
                    R.string.authOpenIdError.shortToast()
                    return
                }
                onSuccess(GoogleInfoBean(account))
            }
            else -> callSignIn(onSuccess, onCancel, onFailed)
        }
    }

    private fun callSignIn(onSuccess: (bean: GoogleInfoBean) -> Unit, onCancel: () -> Unit, onFailed: () -> Unit) {
//        mActivity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
//            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
//            handleSignInResult(task, onSuccess, onCancel, onFailed)
////            mActivity.clearOnActivityResultListener()
//            signOut()
//        }.launch(mGoogleSignInClient.signInIntent)
        if (null == onActivityResultListener) {
            onActivityResultListener = {
                val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
                handleSignInResult(task, onSuccess, onCancel, onFailed)
                onActivityResultListener = null
                signOut()
            }
        }
        mActivityResult?.launch(mGoogleSignInClient.signInIntent)
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>, onSuccess: (account: GoogleInfoBean) -> Unit, onCancel: () -> Unit, onFailed: () -> Unit, ) {
        try {
            val account = completedTask.getResult(ApiException::class.java) ?: throw ApiException(RESULT_INTERNAL_ERROR)
            if (account.id.isNullOrEmpty()) {
                R.string.authOpenIdError.shortToast()
                return
            }
            onSuccess(GoogleInfoBean(account))
        } catch (e: ApiException) {
            when (e.statusCode) {
                12501 -> {
                    R.string.authCancel.shortToast()
                    onCancel()
                }
                7 -> {
                    R.string.authNetworkFail.shortToast()
                    onFailed()
                }
                else -> {
                    R.string.authError.shortToast()
                    onFailed()
                }
            }
        }
    }

    private fun signOut() {
        try {
            mGoogleSignInClient.signOut()
            mGoogleSignInClient.revokeAccess()
        } catch (_: Exception) {
        }
    }

}