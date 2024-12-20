package com.airtimebot.app.utils

import android.util.Log
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

data class UssdRequest(
    val amount: Float,
    val phone: String,
    val messageId: String,
    val retryCount: Int = 0
)

class UssdQueue private constructor() {
    private val queue = ConcurrentLinkedQueue<UssdRequest>()
    private val isProcessing = AtomicBoolean(false)
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    
    companion object {
        private const val TAG = "UssdQueue"
        @Volatile
        private var instance: UssdQueue? = null
        
        fun getInstance(): UssdQueue {
            return instance ?: synchronized(this) {
                instance ?: UssdQueue().also { instance = it }
            }
        }
    }

    fun enqueue(request: UssdRequest) {
        queue.offer(request)
        Log.d(TAG, "Request enqueued: $request")
        processQueue()
    }

    private fun processQueue() {
        if (isProcessing.compareAndSet(false, true)) {
            scope.launch {
                try {
                    while (queue.isNotEmpty()) {
                        val request = queue.poll()
                        request?.let {
                            Log.d(TAG, "Processing request: $it")
                            // Process the request
                            delay(2000) // Add delay between USSD calls
                        }
                    }
                } finally {
                    isProcessing.set(false)
                    if (queue.isNotEmpty()) {
                        processQueue()
                    }
                }
            }
        }
    }

    fun clear() {
        queue.clear()
        scope.cancel()
    }
}