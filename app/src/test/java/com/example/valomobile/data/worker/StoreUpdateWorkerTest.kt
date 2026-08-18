package com.example.valomobile.data.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

class StoreUpdateWorkerTest {

    private lateinit var context: Context
    private lateinit var workerParams: WorkerParameters
    private lateinit var worker: StoreUpdateWorker

    @Before
    fun setup() {
        context = mock(Context::class.java)
        workerParams = mock(WorkerParameters::class.java)
        worker = StoreUpdateWorker(context, workerParams)
    }

    @Test
    fun doWork_returnsSuccess() {
        kotlinx.coroutines.runBlocking {
            val result = worker.doWork()
            assertEquals(ListenableWorker.Result.success(), result)
        }
    }
}
