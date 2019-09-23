package com.popularpenguin.timer

import android.graphics.Color
import android.widget.TextView
import kotlinx.coroutines.*

class TimerTextView(
    private val view: TextView,
    private var secondsOnTimer: Int = 60,
    startText: String = "",
    private val endText: String = ""
) {

    private lateinit var job: CompletableJob

    init {
        view.text = startText

        view.setOnClickListener {
            if (!::job.isInitialized) {
                initJob()
            } else {
                resetJob()
            }
        }
    }

    private fun startJob(job: Job) {
        CoroutineScope(Dispatchers.IO + job).launch {
            var timer = secondsOnTimer

            repeat(timer + 1) {
                val minutes = timer / 60
                val seconds = timer % 60

                val formattedText = formatTimerString(minutes, seconds)
                setTextOnMainThread(formattedText)

                val textColor = when (timer) {
                    in 5..9 -> Color.YELLOW
                    in 0..4 -> Color.RED

                    else -> Color.BLACK
                }
                setTextColorOnMainThread(textColor)

                timer -= 1

                delay(1000L)
            }

            setTextOnMainThread(endText)
            setTextColorOnMainThread(Color.BLACK)
        }
    }

    private fun initJob() {
        job = Job()

        startJob(job)
    }

    private fun resetJob() {
        if (job.isActive || job.isCompleted) {
            job.cancel(CancellationException("Resetting job"))
        }

        initJob()
    }

    private suspend fun setTextColorOnMainThread(color: Int) {
        withContext(Dispatchers.Main) {
            view.setTextColor(color)
        }
    }

    private suspend fun setTextOnMainThread(input: String) {
        withContext(Dispatchers.Main) {
            view.text = input
        }
    }

    private fun formatTimerString(minutes: Int, seconds: Int): String {
        val minutesString = "$minutes"
        val secondsString = when (seconds) {
            0 -> "00"
            in 1..9 -> "0$seconds"

            else -> "$seconds"
        }

        return "$minutesString:$secondsString"
    }
}