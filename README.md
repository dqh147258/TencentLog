# TencentLog
腾讯云结构化日志写入

[![](https://www.jitpack.io/v/dqh147258/TencentLog.svg)](https://www.jitpack.io/#dqh147258/TencentLog)

## 引用
```groovy
	allprojects {
		repositories {
			//...
			maven { url 'https://www.jitpack.io' }
		}
	}
```

```groovy
	dependencies {
	        implementation 'com.github.dqh147258:TencentLog:1.0.5'
	}
```

## 使用
```kotlin
    companion object {
        private const val TENCENT_CLOUD_LOG_SECRET_ID = "your secret id"
        private const val TENCENT_CLOUD_LOG_SECRET_KEY = "your secret key"
        private const val TENCENT_CLOUD_LOG_AREA = "the area id of your tencent log server"
        private const val TENCENT_CLOUD_LOG_TOPIC_ID = "your topic id"
    }
```

```kotlin
    TencentLogManager.init(TENCENT_CLOUD_LOG_SECRET_ID, TENCENT_CLOUD_LOG_SECRET_KEY, TENCENT_CLOUD_LOG_AREA)
    val logWriter = TencentLogManager.instance.getLogWriter(TENCENT_CLOUD_LOG_TOPIC_ID)
    val map = HashMap<String, String>()
    map["test"] = "just for test"
    logWriter.log(map) { result, message ->
        if (!result) {
            Log.w(TAG, "report log to tencent cloud failed, message: $message")
        }
    }
```