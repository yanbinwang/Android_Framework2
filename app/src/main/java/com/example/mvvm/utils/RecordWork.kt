package com.example.mvvm.utils

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.framework.utils.logWTF

/**
 * 录音工作者
 */
class RecordWork(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    /**
     * 3s后返回成功
     */
    override fun doWork(): Result {
        val value = inputData.getString("input_data_key")
        "work->start\nvalue->$value".logWTF
        Thread.sleep(3000)
        "work->finish\nvalue->$value".logWTF
//        return Result.success()
        return Result.success(workDataOf("out_data_key" to "out_work_data"))
    }
}