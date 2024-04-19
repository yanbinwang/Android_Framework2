package com.example.mvvm.utils.finger

import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey


/**
 * 指纹加解密
 * 默认情况下约定逻辑是已经做了支持指纹的判断
 */
@RequiresApi(Build.VERSION_CODES.M)
class FingerGenerator {
    private var cipher: Cipher? = null
    private var keyStore: KeyStore? = null
    private val DEFAULT_KEY_NAME = "default_key"

    init {
        try {
            //对称加密
            keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore?.load(null)
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            val builder = KeyGenParameterSpec.Builder(DEFAULT_KEY_NAME, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setUserAuthenticationRequired(true)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setInvalidatedByBiometricEnrollment(true)
            }
            keyGenerator.init(builder.build())
            keyGenerator.generateKey()
            //添加加密key
            val key = keyStore?.getKey(DEFAULT_KEY_NAME, null) as? SecretKey
            cipher = Cipher.getInstance(
                KeyProperties.KEY_ALGORITHM_AES + "/"
                        + KeyProperties.BLOCK_MODE_CBC + "/"
                        + KeyProperties.ENCRYPTION_PADDING_PKCS7)
            cipher?.init(Cipher.ENCRYPT_MODE, key)
        } catch (_: Exception) {
        }
    }

    /**
     * 获取加解密方式
     */
    fun getCryptoObject(isGenerator: Boolean): FingerprintManager.CryptoObject? {
        return if (isGenerator) FingerprintManager.CryptoObject(cipher ?: return null) else null
    }

}