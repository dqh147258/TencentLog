package com.yxf.tencentlog

class TencentLogManager {

    companion object {
        private lateinit var secretKey: String
        private lateinit var secretId: String
        private lateinit var area: String

        public fun init(id: String, key: String, area: String) {
            secretId = id
            secretKey = key
            this.area = area
        }

        val instance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { TencentLogManager() }

        var isIntranet = false
    }


    private val writerMap = HashMap<String, TencentLogWriter>()

    private constructor()


    public fun getLogWriter(topic: String): TencentLogWriter {
        return writerMap[topic] ?: TencentLogWriter(
            secretId,
            secretKey,
            topic,
            area,
            isIntranet
        ).also { writerMap[topic] = it }
    }


}