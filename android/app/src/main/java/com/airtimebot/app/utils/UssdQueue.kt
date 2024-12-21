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
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var processingJob: Job? = null
    
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
            processingJob = scope.launch {
                try {
                    while (queue.isNotEmpty()) {
                        val request = queue.peek()
                        
                        request?.let {
                            val currentTime = System.currentTimeMillis()
                            val scheduledTime = request.scheduledTime?.time ?: currentTime
                            
                            if (currentTime >= scheduledTime) {
                                queue.poll()
                                Log.d(TAG, "Processing request: $it")
                                // Process the request
                                delay(2000) // Add delay between USSD calls
                            } else {
                                val waitTime = scheduledTime - currentTime
                                delay(waitTime)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing queue: ${e.message}")
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
        processingJob?.cancel()
        queue.clear()
        scope.coroutineContext.cancelChildren()
        Log.d(TAG, "Queue cleared and processing stopped")
    }

    fun size(): Int = queue.size
}