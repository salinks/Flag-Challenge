package com.jd.fc


import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.OnClickListener
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.chivorn.datetimeoptionspicker.DateTimePickerView
import com.google.gson.Gson
import com.jd.fc.Constants.FCQSEC
import com.jd.fc.Constants.TOTAL_QUESTIONS
import com.jd.fc.Constants.TOTAL_TIME
import com.jd.fc.databinding.ActivityMainBinding
import com.jd.fc.model.QuestionsModel
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs


class MainActivity : AppCompatActivity(), OnClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dtpvCustomTime: DateTimePickerView
    private lateinit var sharedPreferences: SharedPreferences
    var selectedTime = ""
    var cTimer: CountDownTimer? = null
    private lateinit var optAdapter: OptionAdapter
    var iTimer: CountDownTimer? = null
    var questionsModel: QuestionsModel? = null
    var qIndex = 0
    var validationIndex = -1
    var qNumber = 1
    var totalScore = 0
    var timeInterval = 0
    private var countDownTimer: Handler? = null
    private var runnable: Runnable? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE)
        binding.rcvAnsOptions.layoutManager = GridLayoutManager(this, 2)
        initTimePicker()
        onClickFunctions()


    }

    private fun onClickFunctions() {
        binding.llScheduleTime.setOnClickListener(this)
        binding.btnSave.setOnClickListener(this)
    }

    override fun onClick(p0: View) {
        when (p0.id) {
            R.id.llScheduleTime -> {
                dtpvCustomTime.show()
            }

            R.id.btnSave -> {
                if (TextUtils.isEmpty(selectedTime)) {
                    Toast.makeText(applicationContext, "Select Time", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(applicationContext, "Time Scheduled", Toast.LENGTH_SHORT).show()
                    saveTime()
                    checkForScheduleTime()
                }

            }
        }
    }

    override fun onResume() {
        super.onResume()
        questionsModel = getQuestions()
        if (!questionsModel?.questions?.isNullOrEmpty()!!) {
            TOTAL_QUESTIONS = questionsModel?.questions!!.size
        }
        checkForScheduleTime()
    }

    private fun checkForScheduleTime() {
        cTimer?.cancel()
        var scheduleTime = sharedPreferences.getString("scheduleTime", "")
        totalScore = sharedPreferences.getInt("totalScore", 0)
        if (scheduleTime.isNullOrEmpty()) {
            val editor = sharedPreferences.edit()
            editor.putInt("totalScore", 0)
            editor.putString("scheduleTime", "")
            editor.apply()
            selectedTime = ""
            totalScore = 0
            timeInterval = FCQSEC
            showScheduleView("show")

        } else {
            selectedTime = scheduleTime

            val time2 = getTime(Calendar.getInstance().time)
            val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val date1 = format.parse(time2)
            val date2 = format.parse(selectedTime)
            val difference = date2.time - date1.time
            var secs = TimeUnit.MILLISECONDS.toSeconds(difference)
            if (secs < 0) {
                if (abs(secs) in 0..TOTAL_TIME) {
                        var oldTime = abs(secs).toDouble() / FCQSEC.toDouble()
                        var strOldTime = oldTime.toString()
                        var ti = strOldTime.substring(strOldTime.indexOf(".")).toDouble()


                        qIndex = oldTime.toInt()
                        qNumber = (qIndex + 1)
                        timeInterval = FCQSEC - (ti * FCQSEC).toInt()
                        showScheduleView("questions")
                        showQuestions()



                } else {
                    selectedTime = ""
                    saveTime()
                    showScheduleView("show")
                }

            } else {
                if (secs in 1..20) {
                    qIndex = 0
                    qNumber = 1
                    timeInterval = FCQSEC
                    startInitialCountTimer(TimeUnit.SECONDS.toMillis(secs))
                } else {
                    initialTimeChecker(TimeUnit.SECONDS.toMillis(secs))
                    showScheduleView("show")
                }
            }
        }


    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showQuestions() {
        binding.tvQuestionNumber.text = qNumber.toString()
        var mData = questionsModel?.questions?.get(qIndex)
        Glide.with(this)
            .load("https://flagcdn.com/w320/${mData?.countryCode?.lowercase(Locale.getDefault())}.png")
            .into(binding.ivFlagIcon)

        optAdapter = OptionAdapter()
        optAdapter.showAnswer = false
        optAdapter.setCorrectAnswerId = mData?.answerId
        mData?.countries?.let { optAdapter.setData(this, it) }
        binding.rcvAnsOptions.adapter = optAdapter


        if (qIndex <= (TOTAL_QUESTIONS - 1)) {

            startQuestionsTimer()
        }
    }

    private fun startQuestionsTimer() {


        var duration = timeInterval
        countDownTimer = Handler(Looper.getMainLooper())

        runnable = object : Runnable {
            override fun run() {
                if (duration > 0) {
                    countDownTimer!!.postDelayed(this, 1000)
                    var remSec = duration


                    if (remSec >= 10) {
                        var mSecs = (remSec - 10).toString()
                        binding.tvRemTime.text = ""
                        if (mSecs.length == 1) {
                            mSecs = "0$mSecs"
                        }
                        binding.tvQuestionTimer.text = "00:$mSecs"
                    } else {
                        var mSecs = remSec.toString()
                        if (mSecs.length == 1) {
                            mSecs = "0$mSecs"
                        }
                        binding.tvQuestionTimer.text = "00:00"
                        validateAnswer()
                        optAdapter.showAnswer = true
                        optAdapter.notifyDataSetChanged()
                        if (qIndex < (TOTAL_QUESTIONS - 1)) {

                            binding.tvRemTime.text = "Next question in 00:$mSecs seconds"
                        } else {

                            showScheduleView("results")
                            if (remSec >= 5) {
                                var mSecs = (remSec - 5).toString()
                                if (mSecs.length == 1) {
                                    mSecs = "0$mSecs"
                                }
                                binding.tvRemTime.text = "Results in 00:$mSecs seconds"
                                binding.tvGameOver.visibility = VISIBLE
                                binding.llScore.visibility = GONE

                            } else {
                                binding.tvRemTime.text = ""
                                binding.tvScore.text = "$totalScore/15"
                                binding.tvGameOver.visibility = GONE
                                binding.llScore.visibility = VISIBLE


                            }
                        }
                    }
                    duration--
                } else {
                    timeInterval = FCQSEC
                    runnable?.let { countDownTimer?.removeCallbacks(it) }
                    binding.tvRemTime.text = ""
                    qIndex++
                    qNumber = qIndex + 1
                    if (qIndex <= (TOTAL_QUESTIONS - 1)) {
                        showQuestions()
                    } else {
                        resetView()
                    }


                }
            }
        }

        countDownTimer!!.post(runnable!!);
    }


    private fun validateAnswer() {

        if (validationIndex < qIndex) {
            var mData = questionsModel?.questions?.get(qIndex)
            var ans = mData?.countries?.filter { it.isSelected && it.id == mData?.answerId }
            if (!ans?.isEmpty()!!) {
                totalScore++
                val editor = sharedPreferences.edit()
                editor.putInt("totalScore", totalScore)
                editor.apply()
            }
            validationIndex = qIndex
        }

    }


    private fun resetView() {
        val editor = sharedPreferences.edit()
        editor.putInt("totalScore", 0)
        editor.putString("scheduleTime", "")
        editor.apply()
        qIndex = 0
        totalScore = 0
        qNumber = 1
        showScheduleView("show")
        selectedTime = ""
        binding.tvSchHours.text = "0"
        binding.tvSchHour.text = "0"
        binding.tvSchMinutes.text = "0"
        binding.tvSchMinute.text = "0"
        binding.tvSchSeconds.text = "0"
        binding.tvSchSecond.text = "0"
    }


    private fun initialTimeChecker(secs: Long) {


        cTimer = object : CountDownTimer(secs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished).toInt() == 20) {
                    cTimer?.cancel()
                    startInitialCountTimer(millisUntilFinished)
                }

            }

            override fun onFinish() {
                cTimer?.cancel()
            }
        }
        cTimer?.start()
    }

    private fun startInitialCountTimer(secs: Long) {
        showScheduleView("hide")
        iTimer = object : CountDownTimer(secs, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                var mSecs = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished).toString()
                if (mSecs.length == 1) {
                    mSecs = "0$mSecs"
                }
                binding.tvScheduleTimer.text = "00:$mSecs"
            }

            override fun onFinish() {
                showScheduleView("questions")
                showQuestions()
                iTimer?.cancel()
            }
        }
        iTimer?.start()
    }

    private fun showScheduleView(s: String) {
        when (s) {
            "show" -> {
                binding.llScheduleView.visibility = VISIBLE
                binding.llScheduleTimerView.visibility = GONE
                binding.llQuestionsView.visibility = GONE
                binding.relQuestionTimer.visibility = GONE
                binding.tvRemTime.visibility = GONE
                binding.llGameOverView.visibility = GONE

            }

            "hide" -> {
                binding.llScheduleView.visibility = GONE
                binding.llScheduleTimerView.visibility = VISIBLE
                binding.llQuestionsView.visibility = GONE
                binding.relQuestionTimer.visibility = GONE
                binding.tvRemTime.visibility = GONE
                binding.llGameOverView.visibility = GONE

            }

            "questions" -> {
                binding.llScheduleView.visibility = GONE
                binding.llScheduleTimerView.visibility = GONE
                binding.llGameOverView.visibility = GONE
                binding.relQuestionTimer.visibility = VISIBLE
                binding.llQuestionsView.visibility = VISIBLE
                binding.tvRemTime.visibility = VISIBLE

            }

            "results" -> {
                binding.llScheduleView.visibility = GONE
                binding.llScheduleTimerView.visibility = GONE
                binding.llQuestionsView.visibility = GONE
                binding.relQuestionTimer.visibility = GONE
                binding.tvRemTime.visibility = VISIBLE
                binding.llGameOverView.visibility = VISIBLE

            }
        }

    }


    private fun saveTime() {

        val editor = sharedPreferences.edit()
        editor.putString("scheduleTime", selectedTime)
        editor.apply()

    }


    private fun showTimeInView(time: String) {
        selectedTime = time
        binding.tvSchHours.text = time.subSequence(0, 1)
        binding.tvSchHour.text = time.subSequence(1, 2)
        binding.tvSchMinutes.text = time.subSequence(3, 4)
        binding.tvSchMinute.text = time.subSequence(4, 5)
        binding.tvSchSeconds.text = time.subSequence(6, 7)
        binding.tvSchSecond.text = time.subSequence(7, 8)

    }


    fun readJSONFromAsset(): String? {
        var json: String? = null
        try {
            val inputStream: InputStream = assets.open("questions.json")
            json = inputStream.bufferedReader().use { it.readText() }
        } catch (ex: Exception) {
            ex.printStackTrace()
            return null
        }
        return json
    }

    fun getQuestions(): QuestionsModel {
        return Gson().fromJson(readJSONFromAsset(), QuestionsModel::class.java)
    }

    private fun initTimePicker() {
        val selectedDate = Calendar.getInstance()
        val startDate = Calendar.getInstance()
        startDate[2024, 1] = 23
        val endDate = Calendar.getInstance()
        endDate[2027, 2] = 28

        dtpvCustomTime = DateTimePickerView.Builder(
            this
        ) { date, v ->
            showTimeInView(getTime(date))
        }
            .setDate(selectedDate)
            .setRangDate(startDate, endDate)
            .setLayoutRes(
                R.layout.datetimeoptionspicker_custom_time
            ) { v ->
                val tvSubmit = v.findViewById<View>(R.id.tv_finish) as TextView
                val ivCancel = v.findViewById<View>(R.id.iv_cancel) as ImageView
                tvSubmit.setOnClickListener {
                    dtpvCustomTime.returnData()
                    dtpvCustomTime.dismiss()
                }
                ivCancel.setOnClickListener { dtpvCustomTime.dismiss() }
            }
            .setContentSize(18)
            .setType(booleanArrayOf(false, false, false, true, true, true))
            .setLabel("y", "m", "d", " h", " m", " s")
            .setLineSpacingMultiplier(1.2f)
            .setTextXOffset(0, 0, 0, 40, 0, -40)
            .isCenterLabel(false)
            .setDividerColor(-0xdb5263)
            .build()
    }

    private fun getTime(date: Date): String {
        val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return format.format(date)
    }

    override fun onPause() {
        super.onPause()
        runnable?.let { countDownTimer?.removeCallbacks(it) }
    }
}
