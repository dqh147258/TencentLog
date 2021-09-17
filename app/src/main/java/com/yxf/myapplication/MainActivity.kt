package com.yxf.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.yxf.tencentlog.TencentLogManager

class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName

    companion object {
        private const val TENCENT_CLOUD_LOG_SECRET_ID = "your secret id"
        private const val TENCENT_CLOUD_LOG_SECRET_KEY = "your secret key"
        private const val TENCENT_CLOUD_LOG_AREA = "the area id of your tencent log server"
        private const val TENCENT_CLOUD_LOG_TOPIC_ID = "your topic id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        TencentLogManager.init(TENCENT_CLOUD_LOG_SECRET_ID, TENCENT_CLOUD_LOG_SECRET_KEY, TENCENT_CLOUD_LOG_AREA)
        val logWriter = TencentLogManager.instance.getLogWriter(TENCENT_CLOUD_LOG_TOPIC_ID)
        val map = HashMap<String, String>()
        map["test"] = "just for test"
        logWriter.log(map) { result, message ->
            if (!result) {
                Log.w(TAG, "report log to tencent cloud failed, message: $message")
            }
        }
    }
}