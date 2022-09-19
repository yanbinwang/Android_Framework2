package com.example.mvvm.activity

import android.Manifest
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.base.utils.function.view.clicks
import com.example.common.base.BaseTitleActivity
import com.example.common.constant.ARouterPath
import com.example.common.constant.Extras
import com.example.mvvm.R
import com.example.mvvm.databinding.ActivityMainBinding
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions

/**
 * Created by WangYanBin on 2020/8/14.
 * MVVM中，Activity的代码量应该非常少，它并不和数据及控件直接交互，可不写页面的ViewModel，但有xml存在的页面则必须传入Binding文件
 * 1）如果页面涉及到View操作必须在xml文件里套入layout和data标签，让系统生成对应的Binding文件传入基类注入
 * 2）不处理逻辑的代码以及从任何入口进到Activity内的数据放在Activity处理（例如上一个类传过来的数据，事件点击---类似P层回调）其余都丢给ViewModel处理，然后通过LiveData回调给Activity
 * 3）需要处理逻辑的方法，例如网络请求获取的数据，都放在ViewModel中，涉及到UI刷新则通过回调让Activity去修改数据
 */
/**
 * 注解必需的描述
 * @RuntimePermissions		注册一个ActivityorFragment来处理权限
 * @NeedsPermission		注释执行需要一个或多个权限的操作的方法
 * @OnShowRationale		注释一个解释为什么需要权限的方法。它传入一个PermissionRequest对象，该对象可用于根据用户输入继续或中止当前的权限请求。如果您没有为方法指定任何参数，编译器将生成process${NeedsPermissionMethodName}ProcessRequestand cancel${NeedsPermissionMethodName}ProcessRequest. 您可以使用这些方法代替PermissionRequest（例如： with DialogFragment）
 * @OnPermissionDenied		注释如果用户未授予权限则调用的方法
 * @OnNeverAskAgain		注释如果用户选择让设备“不再询问”权限时调用的方法
 * @RuntimePermissions
 * class MainActivity : AppCompatActivity(), View.OnClickListener {
 *
 * @NeedsPermission(Manifest.permission.CAMERA)
 * fun showCamera() {
 * supportFragmentManager.beginTransaction()
 * .replace(R.id.sample_content_fragment, CameraPreviewFragment.newInstance())
 * .addToBackStack("camera")
 * .commitAllowingStateLoss()
 * }
 *
 * @OnShowRationale(Manifest.permission.CAMERA)
 * fun showRationaleForCamera(request: PermissionRequest) {
 * showRationaleDialog(R.string.permission_camera_rationale, request)
 * }
 *
 * @OnPermissionDenied(Manifest.permission.CAMERA)
 * fun onCameraDenied() {
 * Toast.makeText(this, R.string.permission_camera_denied, Toast.LENGTH_SHORT).show()
 * }
 *
 * @OnNeverAskAgain(Manifest.permission.CAMERA)
 * fun onCameraNeverAskAgain() {
 * Toast.makeText(this, R.string.permission_camera_never_askagain, Toast.LENGTH_SHORT).show()
 * }
 * }
 */
@RuntimePermissions
@Route(path = ARouterPath.MainActivity)
class MainActivity : BaseTitleActivity<ActivityMainBinding>(), View.OnClickListener {

    override fun initView() {
        super.initView()
        titleBuilder.setTitle("10086").getDefault()
    }

    override fun initEvent() {
        super.initEvent()
        clicks(binding.btnLogin, binding.btnList, binding.btnDownload)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_login -> navigation(ARouterPath.LoginActivity)
            R.id.btn_list -> navigation(ARouterPath.TestListActivity, Extras.MOBILE to "test")
            R.id.btn_download -> {

            }
        }
    }

    var PERMMISSIONS = arrayOf<String>(
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.READ_SMS,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    @NeedsPermission()
    private fun test(){

    }



}