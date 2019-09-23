package com.popularpenguin.timer

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

class MainActivity : AppCompatActivity() {

    private lateinit var job: CompletableJob

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        timerTextView.text = "Click"
        timerTextView.setOnClickListener {
            if (!::job.isInitialized) {
                initJob()
            } else {
                resetJob()
            }
        }
    }

    private fun TextView.startJob(job: Job) {
        CoroutineScope(IO + job).launch {
            var timer = 10

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

            setTextOnMainThread("Done")
            setTextColorOnMainThread(Color.BLACK)
        }
    }

    private fun initJob() {
        job = Job()

        timerTextView.startJob(job)
    }

    private fun resetJob() {
        if (job.isActive || job.isCompleted) {
            job.cancel(CancellationException("Resetting job"))
        }

        initJob()
    }

    private suspend fun setTextColorOnMainThread(color: Int) {
        withContext(Main) {
            timerTextView.setTextColor(color)
        }
    }

    private suspend fun setTextOnMainThread(input: String) {
        withContext(Main) {
            timerTextView.text = input
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
