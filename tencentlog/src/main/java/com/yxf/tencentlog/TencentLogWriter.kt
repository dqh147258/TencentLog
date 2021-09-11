package com.yxf.tencentlog

import android.util.Log
import com.yxf.tencentlog.proto.Cls
import com.yxf.tencentlog.proto.Cls.LogGroup
import com.yxf.tencentlog.proto.Cls.LogGroupList
import com.yxf.tencentlog.sign.QcloudClsSignature
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import net.jpountz.lz4.LZ4Factory
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody


class TencentLogWriter(
    private val secretId: String,
    private val secretKey: String,
    private val topic: String,
    private val area: String,
    private val isIntranet: Boolean = false
) {


    companion object {
        private val httpClient by lazy {
            OkHttpClient.Builder().build()
        }
    }

    private val domain = if (isIntranet) "cls.tencentyun.com" else "cls.tencentcs.com"
    private val host = "${area}.$domain"

    private val path = "/structuredlog"
    private val topicParams = "topic_id=$topic"
    private val url = "https://${host}${path}?${topicParams}"

    private val compressor by lazy { LZ4Factory.fastestInstance().fastCompressor() }

    private fun getHttpRequest(logGroupList: Cls.LogGroupList): Request {
        val body = compressor.compress(logGroupList.toByteArray())
            .toRequestBody("binary".toMediaTypeOrNull())
        return Request.Builder().apply {
            url(url)
            post(body)
            addHeader("x-cls-compress-type", "lz4")
            addHeader("Content-Type", "application/x-protobuf")
            addHeader("Authorization", getAuthorization())
        }.build()
    }


    private fun getAuthorization(): String {
        val paramsMap = HashMap<String, String>().apply {
            put("topic_id", topic)
        }

        val headerMap = HashMap<String, String>().apply {
            put("User-Agent", "AuthSDK")
        }

        return QcloudClsSignature.buildSignature(
            secretId,
            secretKey,
            "POST",
            path,
            paramsMap,
            headerMap,
            1000000
        )
    }

    public fun log(
        map: Map<String, String>,
        callback: ((result: Boolean, message: String) -> Unit)? = null
    ) {
        val log = Cls.Log.newBuilder().apply {
            val iterator = map.iterator()
            while (iterator.hasNext()) {
                val item = iterator.next()
                addContents(Cls.Log.Content.newBuilder().apply {
                    key = item.key
                    value = item.value
                }.build())
            }
        }
            .setTime(System.currentTimeMillis())
            .build()
        val logGroup = LogGroup.newBuilder().addLogs(log).build()
        val logGrList = LogGroupList.newBuilder().addLogGroupList(logGroup).build()
        log(logGrList, callback)
    }

    public fun log(
        logGroupList: LogGroupList,
        callback: ((result: Boolean, message: String) -> Unit)? = null
    ) {
        GlobalScope.launch(Dispatchers.Main) {
            val deferred = GlobalScope.async(Dispatchers.IO) {
                logSync(logGroupList)
            }
            val result = deferred.await()
            callback?.invoke(result.isNullOrEmpty(), result)
        }
    }

    public suspend fun logSync(logGroupList: Cls.LogGroupList): String {
        val request = getHttpRequest(logGroupList)
        val response = httpClient.newCall(request).execute()
        return response.body?.string() ?: ""
    }


}