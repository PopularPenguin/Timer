package com.popularpenguin.timer

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.TextView
import kotlinx.coroutines.*

class TimerView (context: Context, attrs: AttributeSet) : TextView(context, attrs) {

    private lateinit var job: CompletableJob

    private val secondsOnTimer: Int

    private val normalColor: Int // the color of the timer after it starts ticking
    private val mediumWarningTime: Int
    private val mediumWarningColor: Int
    private val lowWarningTime: Int
    private val lowWarningColor: Int

    private val startText: String
    private val endText: String
    private val startTextDefault = "Start"
    private val endTextDefault = "Done"
    private val startTextColor: Int
    private val endTextColor: Int

    init {
        // Get the attributes set from XML
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.TimerView)

        secondsOnTimer = attributes.getInt(R.styleable.TimerView_start_time, 60)

        normalColor = attributes.getColor(
            R.styleable.TimerView_normal_color,
            Color.BLACK
        )
        mediumWarningTime = attributes.getInt(
            R.styleable.TimerView_medium_warning,
            secondsOnTimer / 5
        )
        mediumWarningColor = attributes.getColor(
            R.styleable.TimerView_medium_warning_color,
            Color.YELLOW
        )
        lowWarningTime = attributes.getInt(
            R.styleable.TimerView_low_warning,
            secondsOnTimer / 20
        )
        lowWarningColor = attributes.getColor(
            R.styleable.TimerView_low_warning_color,
            Color.RED
        )

        startText = attributes.getString(R.styleable.TimerView_start_text) ?:
                startTextDefault
        startTextColor = attributes.getColor(
            R.styleable.TimerView_start_text_color,
            Color.BLACK
        )
        endText = attributes.getString(R.styleable.TimerView_end_text) ?:
            endTextDefault
        endTextColor = attributes.getColor(
            R.styleable.TimerView_end_text_color,
            Color.BLACK
        )

        attributes.recycle()

        text = startText
        setTextColor(startTextColor)
    }

    fun toggle() {
        if (!::job.isInitialized) {
            initJob()
        } else if (job.isActive) {
            cancelJob()
        }
        else {
            resetJob()
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
                    in (lowWarningTime + 1)..mediumWarningTime -> mediumWarningColor
                    in 0..lowWarningTime -> lowWarningColor

                    else -> Color.BLACK
                }
                setTextColorOnMainThread(textColor)

                timer -= 1

                delay(1000L)
            }

            setTextOnMainThread(endText)
            setTextColorOnMainThread(endTextColor)
        }
    }

    private fun initJob() {
        job = Job()

        startJob(job)
    }

    private fun cancelJob() {
        if (job.isActive) {
            job.cancel(CancellationException("Cancelled job"))
        }
    }

    private fun resetJob() {
        if (job.isActive || job.isCompleted) {
            job.cancel(CancellationException("Resetting job"))
        }

        initJob()
    }

    private suspend fun setTextColorOnMainThread(color: Int) {
        withContext(Dispatchers.Main) {
            setTextColor(color)
        }
    }

    private suspend fun setTextOnMainThread(input: String) {
        withContext(Dispatchers.Main) {
            text = input
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