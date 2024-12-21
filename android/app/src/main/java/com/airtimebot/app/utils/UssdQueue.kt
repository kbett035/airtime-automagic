package com.airtimebot.app.utils

import android.util.Log
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

data class UssdRequest(
    val amount: Float,
    val phone: String,
    val messageId: String,
    val retryCount: Int = 0,
    val scheduledTime: Date? = null
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
                        val request = queue.peek() // Just peek, don't remove yet
                        
                        request?.let {
                            val currentTime = System.currentTimeMillis()
                            val scheduledTime = request.scheduledTime?.time ?: currentTime
                            
                            if (currentTime >= scheduledTime) {
                                queue.poll() // Now remove it
                                Log.d(TAG, "Processing request: $it")
                                // Process the request
                                delay(2000) // Add delay between USSD calls
                            } else {
                                // Wait until scheduled time
                                val waitTime = scheduledTime - currentTime
                                delay(waitTime)
                            }
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