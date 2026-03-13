package com.example.thirdparty.auth.google

import android.content.Context
import android.os.CancellationSignal
import android.util.Base64
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.CreateCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.example.common.utils.NetWorkUtil.isNetworkAvailable
import com.example.common.utils.builder.shortToast
import com.example.common.utils.function.getManifestString
import com.example.common.utils.i18n.string
import com.example.framework.utils.function.doOnDestroy
import com.example.thirdparty.R
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.SecureRandom
import kotlin.coroutines.cancellation.CancellationException

///**
// * google三方登录
// */
//class GoogleAuthUtil(private val mActivity: FragmentActivity) {
//    private var onActivityResultListener: ((result: ActivityResult) -> Unit)? = null
//    private val mActivityResult = mActivity.registerResult { onActivityResultListener?.invoke(it) }
//    private val mGoogleSignInClient by lazy {
//        GoogleSignIn.getClient(mActivity, GoogleSignInOptions
//            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestIdToken(GOOGLE_AUTH_API.orEmpty())
//            .requestEmail()
//            .build())
//    }
//
//    companion object {
//        val GOOGLE_AUTH_API = getManifestString("GOOGLE_AUTH_API")
//    }
//
//    init {
//        mActivity.doOnDestroy {
//            onActivityResultListener = null
//            mActivityResult?.unregister()
//        }
//        signOut()
//    }
//
//    fun signIn(onSuccess: (bean: GoogleInfoBean) -> Unit, onCancel: () -> Unit, onFailed: () -> Unit) {
//        R.string.authInitiate.shortToast()
//        val account = GoogleSignIn.getLastSignedInAccount(mActivity)
//        when {
//            account != null -> {
//                if (account.id.isNullOrEmpty()) {
//                    R.string.authOpenIdError.shortToast()
//                    return
//                }
//                onSuccess(GoogleInfoBean(account))
//            }
//            else -> callSignIn(onSuccess, onCancel, onFailed)
//        }
//    }
//
//    private fun callSignIn(onSuccess: (bean: GoogleInfoBean) -> Unit, onCancel: () -> Unit, onFailed: () -> Unit) {
////        mActivity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
////            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
////            handleSignInResult(task, onSuccess, onCancel, onFailed)
//////            mActivity.clearOnActivityResultListener()
////            signOut()
////        }.launch(mGoogleSignInClient.signInIntent)
//        if (null == onActivityResultListener) {
//            onActivityResultListener = {
//                val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
//                handleSignInResult(task, onSuccess, onCancel, onFailed)
//                onActivityResultListener = null
//                signOut()
//            }
//        }
//        mActivityResult?.launch(mGoogleSignInClient.signInIntent)
//    }
//
//    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>, onSuccess: (account: GoogleInfoBean) -> Unit, onCancel: () -> Unit, onFailed: () -> Unit, ) {
//        try {
//            val account = completedTask.getResult(ApiException::class.java) ?: throw ApiException(RESULT_INTERNAL_ERROR)
//            if (account.id.isNullOrEmpty()) {
//                R.string.authOpenIdError.shortToast()
//                return
//            }
//            onSuccess(GoogleInfoBean(account))
//        } catch (e: ApiException) {
//            when (e.statusCode) {
//                12501 -> {
//                    R.string.authCancel.shortToast()
//                    onCancel()
//                }
//                7 -> {
//                    R.string.authNetworkFail.shortToast()
//                    onFailed()
//                }
//                else -> {
//                    R.string.authError.shortToast()
//                    onFailed()
//                }
//            }
//        }
//    }
//
//    private fun signOut() {
//        try {
//            mGoogleSignInClient.signOut()
//            mGoogleSignInClient.revokeAccess()
//        } catch (_: Exception) {
//        }
//    }
//
//}
/**
 * google三方登录
 * https://developer.android.google.cn/identity/sign-in/credential-manager-siwg?hl=zh-cn
 * https://blog.csdn.net/zll18201518375/article/details/138577963
 */
class GoogleAuthUtil(private val mActivity: FragmentActivity) {
    private var signInJob: Job? = null
    private var signOutJob: Job? = null
    private val cancelSignal = CancellationSignal()
    private val credentialManager by lazy { CredentialManager.create(mActivity) }

    companion object {
        /**
         * AndroidManifest中配置的谷歌项目clientId
         */
        private val GOOGLE_AUTH_API = getManifestString("GOOGLE_AUTH_API")

        /**
         * 生成谷歌登录专用 Nonce（谷歌强制要求）
         */
        private fun generateNonce(): String {
            val nonceBytes = ByteArray(16)
            SecureRandom().nextBytes(nonceBytes)
            return Base64.encodeToString(nonceBytes, Base64.NO_PADDING or Base64.NO_WRAP or Base64.URL_SAFE)
        }

        /**
         * 谷歌服务是否可用
         */
        fun Context?.isGooglePlayServicesAvailable(): Boolean {
            this ?: return false
            return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS
        }

    }

    init {
        mActivity.lifecycle.doOnDestroy {
            signInJob?.cancel()
            signOutJob?.cancel()
            cancelSignal.cancel()
        }
    }

    /**
     * 谷歌登录
     *
     * 用户凭证 (GoogleIdTokenCredential):
     * id = credential.id,
     *   idToken = credential.idToken,
     *   displayName = credential.displayName,
     *   email = credential.email,
     *   profilePicture = credential.profilePictureUri?.toString()
     *
     * 注销:
     * credentialManager.clearCredentialState(ClearCredentialStateRequest()) -> 挂起协程版
     */
    fun signIn(onSuccess: (bean: GoogleIdTokenCredential) -> Unit = {}, onCancel: () -> Unit = {}, onFailed: (String) -> Unit = {}) {
        if (!mActivity.isGooglePlayServicesAvailable()) {
            R.string.authError.shortToast()
            return
        }
        R.string.authInitiate.shortToast()
        signInJob?.cancel()
        signInJob = mActivity.lifecycleScope.launch(Main.immediate) {
            runCatching {
                withContext(IO) {
                    // 执行登录前先进行登出
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                    // 配置谷歌登录参数执行登录
                    val request = GetCredentialRequest.Builder().addCredentialOption(GetGoogleIdOption.Builder()
                        // 检查用户是否有任何之前用于登录应用的账号。用户可以从可用账号中选择一个账号进行登录
                        .setFilterByAuthorizedAccounts(true)
                        // 为回访用户启用自动登录功能
                        .setAutoSelectEnabled(true)
                        // 设置后台配置好的应用clientId
                        .setServerClientId(GOOGLE_AUTH_API.orEmpty())
                        // 设置 Nonce 以提高安全性
                        .setNonce(generateNonce())
                        // 开始构建
                        .build()).build()
                    credentialManager.getCredential(mActivity, request)
                }
            }.onSuccess { response ->
                handleSignInResult(response, onSuccess)
            }.onFailure { e ->
                handleError(e, onCancel, onFailed)
            }
        }
    }

    /**
     * 解析登录结果
     */
    private fun handleSignInResult(response: GetCredentialResponse, onSuccess: (GoogleIdTokenCredential) -> Unit = {}) {
        when (val credential = response.credential) {
            // GoogleIdToken凭据
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        // 使用googleIdTokenCredentials并提取ID进行验证和在服务器上进行身份验证
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        if (googleIdTokenCredential.id.isEmpty()) {
                            throw RuntimeException(string(R.string.authOpenIdError))
                        }
                        onSuccess(googleIdTokenCredential)
                    } catch (e: GoogleIdTokenParsingException) {
                        throw RuntimeException("收到无效的google id令牌响应:$e")
                    }
                } else {
                    throw RuntimeException("意外的凭据类型")
                }
            }
//            //密钥凭证
//            is PublicKeyCredential -> {
//                responseJson = credential.authenticationResponseJson
//            }
//            //密码凭据
//            is PasswordCredential -> {
//                val username = credential.id
//                val password = credential.password
//            }
            else -> {
                throw RuntimeException("意外的凭据类型")
            }
        }
    }

    /**
     * 统一错误处理
     * https://developer.android.google.cn/identity/sign-in/credential-manager-troubleshooting-guide?hl=zh-cn
     */
    private fun handleError(e: Throwable, onCancel: () -> Unit = {}, onFailed: (String) -> Unit = {}) {
        when (e) {
            // 用户取消了通行密钥注册或检索
            is GetCredentialCancellationException, is CreateCredentialCancellationException -> {
                R.string.authCancel.shortToast()
                onCancel()
            }
            // 无可用凭证，可引导用户注册
            is NoCredentialException -> {
                R.string.authError.shortToast()
                onFailed("未找到可用账号")
            }
            // 协程取消，不处理
            is CancellationException -> {
            }
            // 其余无特殊操作一律回调失败
            else -> {
                (if (!isNetworkAvailable()) R.string.authNetworkFail else R.string.authError).shortToast()
                onFailed(e.message ?: "登录失败")
            }
        }
    }

    /**
     * 登出
     */
    fun signOutJob() {
        signOutJob?.cancel()
        signOutJob = mActivity.lifecycleScope.launch(Main.immediate) {
            runCatching {
                withContext(IO) { credentialManager.clearCredentialState(ClearCredentialStateRequest()) }
            }
        }
    }

}