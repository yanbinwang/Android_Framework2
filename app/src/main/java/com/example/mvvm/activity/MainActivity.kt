package com.example.mvvm.activity

import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseActivity
import com.example.common.config.ARouterPath
import com.example.framework.utils.logWTF
import com.example.mvvm.databinding.ActivityMainBinding
import com.example.mvvm.utils.RecordWork

/**
 */
@Route(path = ARouterPath.MainActivity)
class MainActivity : BaseActivity<ActivityMainBinding>() {
    private val manager by lazy { WorkManager.getInstance(this) }

    override fun initView() {
        super.initView()
        //队列是会被正常加入的，但是多了约束条件后，则会在网络连接后才执行，只要代码执行了，哪怕应用进程被划掉，也会在满足条件的情况下执行
        //（除非用户在设备信息里完全结束app进程，这就只能等应用再次启动时才能执行）
        //约束条件
        val constrains = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)//网络连接的情况下执行
            .build()
        //单次请求
        val workRequest = OneTimeWorkRequestBuilder<RecordWork>()
            .setConstraints(constrains)
            //传递数据
            .setInputData(workDataOf("input_data_key" to "input_work_data"))
            .build()
        //加入队列
        manager.enqueue(workRequest)
        manager.getWorkInfoByIdLiveData(workRequest.id).observe(this) {
            val state = it.state//状态
            "state:${state}".logWTF
            if (state == WorkInfo.State.SUCCEEDED) {
                "${it.outputData.getString("out_data_key")}".logWTF
            }
        }
    }

}