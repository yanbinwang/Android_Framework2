package com.example.thirdparty.auth.google

import android.content.Context
import android.os.CancellationSignal
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialManagerCallback
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.fragment.app.FragmentActivity
import com.example.common.utils.builder.shortToast
import com.example.common.utils.function.getManifestString
import com.example.framework.utils.function.doOnDestroy
import com.example.thirdparty.R
import com.example.thirdparty.auth.google.bean.GoogleInfoBean
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

/**
 * google三方登录
 * https://developer.android.google.cn/identity/sign-in/credential-manager-siwg?hl=zh-cn
 * https://blog.csdn.net/zll18201518375/article/details/138577963
 */
class GoogleAuthUtil2(private val mActivity: FragmentActivity) : CoroutineScope {
    private var builderJob: Job? = null
    private val credentialManager by lazy { CredentialManager.create(mActivity) }
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Main + job

    companion object {
        /**
         * AndroidManifest中配置的id
         */
        private val GOOGLE_AUTH_API = getManifestString("GOOGLE_AUTH_API")

//        /**
//         * 生成notice值
//         */
//        @JvmStatic
//        private fun generateNonce(): String {
//            val secureRandom = SecureRandom()
//            val nonceBytes = ByteArray(16)
//            secureRandom.nextBytes(nonceBytes)
//            return nonceBytes.base64Encode()
//        }

        /**
         * 谷歌服务是否可用
         */
        fun Context?.isGooglePlayServicesAvailable(): Boolean {
            this ?: return false
            return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS
        }

    }

    init {
        mActivity.doOnDestroy {
            builderJob?.cancel()
            job.cancel()
        }
        signOut()
    }

    /**
     * 开始登录
     */
    fun signIn(onSuccess: (bean: GoogleInfoBean) -> Unit, onCancel: () -> Unit, onFailed: () -> Unit) {
        if (!mActivity.isGooglePlayServicesAvailable()) {
            R.string.authError.shortToast()
            return
        }
        R.string.authInitiate.shortToast()
        builderJob?.cancel()
        builderJob = launch {
            try {
//                val nonce = generateNonce() // 生成随机 nonce
//                val result = credentialManager.getCredential(
//                    mActivity, GetCredentialRequest.Builder()
//                        .addCredentialOption(
//                            GetGoogleIdOption.Builder()
//                                //检查用户是否有任何之前用于登录应用的账号。用户可以从可用账号中选择一个账号进行登录
//                                .setFilterByAuthorizedAccounts(true)
//                                //为回访用户启用自动登录功能
//                                .setAutoSelectEnabled(true)
//                                //设置后台配置好的应用appid
//                                .setServerClientId(GOOGLE_AUTH_API.orEmpty())
//                                //设置 Nonce 以提高安全性
//                                .setNonce(nonce)
//                                //开始构建
//                                .build()
//                        )
//                        .build()
//                )
                signOut()
                val result = credentialManager.getCredential(mActivity, GetCredentialRequest.Builder().addCredentialOption(GetSignInWithGoogleOption.Builder(GOOGLE_AUTH_API.orEmpty()).build()).build())
                handleSignInResult(result, onSuccess)
            } catch (e: Exception) {
                when (e) {
//                       //无可用凭证，引导用户注册。
//                       is NoCredentialException -> showSignUpPrompt()
                    is GetCredentialCancellationException -> {
                        R.string.authCancel.shortToast()
                        onCancel()
                    }
                    else -> {
                        R.string.authError.shortToast()
                        onFailed()
                    }
                }
            }
        }
    }

    private fun handleSignInResult(result: GetCredentialResponse, onSuccess: (account: GoogleInfoBean) -> Unit) {
        //处理成功返回的凭据
        when (val credential = result.credential) {
            //GoogleIdToken凭据
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        //使用googleIdTokenCredentials并提取ID进行验证和在服务器上进行身份验证
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        onSuccess(GoogleInfoBean(googleIdTokenCredential))
                    } catch (e: GoogleIdTokenParsingException) {
                        throw RuntimeException("收到无效的google id令牌响应:$e")
                    }
                } else {
                    throw RuntimeException("意外的凭据类型")
                }
            }
//            //密钥凭证
//            is PublicKeyCredential -> {
//                // Share responseJson such as a GetCredentialResponse on your server to
//                // validate and authenticate
//                responseJson = credential.authenticationResponseJson
//            }
//            //密码凭据
//            is PasswordCredential -> {
//                // Send ID and password to your server to validate and authenticate.
//                val username = credential.id
//                val password = credential.password
//            }
            else -> {
                throw RuntimeException("意外的凭据类型")
            }
        }
    }

    private fun signOut() {
        credentialManager.clearCredentialStateAsync(ClearCredentialStateRequest(), CancellationSignal(), Executors.newSingleThreadExecutor(), object : CredentialManagerCallback<Void?, ClearCredentialException> {
            override fun onError(e: ClearCredentialException) {
            }

            override fun onResult(result: Void?) {
            }
        })
    }

}