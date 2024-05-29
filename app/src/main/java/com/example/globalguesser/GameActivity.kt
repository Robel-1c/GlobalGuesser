package com.example.globalguesser

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

class GameActivity : AppCompatActivity() {
    // variables for the timer
    private lateinit var countdownText : TextView
    private lateinit var countDownTimer : CountDownTimer
    private lateinit var progressBar : ProgressBar
    private var currentProgress : Int = 0
    private var timeLeftInMilliseconds : Long = 61000 // to offset the transitions

    // list of flags
    private lateinit var flagMap : HashMap<String, Int>
    private var flags : ArrayList<String> = ArrayList()

    private lateinit var flagImage : ImageView
    private lateinit var flagText : EditText
    private lateinit var adView : AdView
    private var currFlagIndex : Int = 0

    private var difficultyLabel : String = MainActivity.difficultyLevel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_view)

        // advertisement
        createAd()

        // set progress bar
        progressBar = findViewById(R.id.progress_bar)
        progressBar.max = 5

        // views
        flagImage = findViewById(R.id.flag_image)
        flagText = findViewById(R.id.flag_text)

        // store persistent data
        sharedPreferences = this.getSharedPreferences("Game", Context.MODE_PRIVATE)

        // get the right flags based on difficulty level
        flagMap = when (MainActivity.difficultyLevel) {
            "Easy" -> MainActivity.easyFlags
            "Medium" -> MainActivity.mediumFlags
            else -> MainActivity.hardFlags
        }

        // add all flags to the queue of flags to guess
        for(e in flagMap.keys) {
            flags.add(e)
        }

        // set the first flag
        flagMap[flags[0]]?.let { flagImage.setBackgroundResource(it) }

        // initialize game variable
        game = when (difficultyLabel) {
            "Easy" -> Game(flags[0], sharedPreferences.getLong("bestTimeEasy", 100), difficultyLabel)
            "Medium" -> Game(flags[0], sharedPreferences.getLong("bestTimeMedium", 100), difficultyLabel)
            else -> Game(flags[0], sharedPreferences.getLong("bestTimeHard", 100), difficultyLabel)
        }

        // allow clicking "enter" on keyboard to submit
        flagText.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if(keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN){
                // same as pressing enter button
                checkAnswer(v)
                return@OnKeyListener true
            } else {
                return@OnKeyListener false
            }
        })

        // timer
        countdownText = findViewById(R.id.timer)
        startTimer()
    }

    override fun onPause() {
        super.onPause()
        stopTimer()
        if( adView != null )
            adView.pause()
    }

    override fun onStop() {
        super.onStop()
        stopTimer()
        if( adView != null )
            adView.resume()
    }

    override fun onDestroy() {
        if( adView != null )
            adView.destroy()
        super.onDestroy()
    }

    private fun stopTimer() {
        countDownTimer?.cancel()
    }

    private fun createAd( ) {
        adView = AdView( this )
        var adSize : AdSize = AdSize( AdSize.FULL_WIDTH, AdSize.AUTO_HEIGHT )
        adView.setAdSize( adSize )

        var adUnitId : String = "ca-app-pub-3940256099942544/6300978111"
        adView.adUnitId = adUnitId

        var builder : AdRequest.Builder = AdRequest.Builder( )
        builder.addKeyword( "learning" ).addKeyword( "games" )
        var request : AdRequest = builder.build()

        // add adView to linear layout
        var layout : LinearLayout = findViewById( R.id.ad_view )
        layout.addView( adView )

        // load the ad
        adView.loadAd( request )
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeLeftInMilliseconds, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMilliseconds = millisUntilFinished
                updateTimer()
            }

            override fun onFinish() {
                countdownText.text = "Time's Up!"
                modifyData()
            }
        }.start()
    }

    // check if answer is correct when ENTER is clicked
    fun checkAnswer(v : View) {
        if(game.isGuessCorrect(flagText.text.toString())) {
            flagText.setText("")
            flags.removeAt(currFlagIndex)

            //Updating the progress bar
            currentProgress = ++currentProgress
            progressBar.progress = currentProgress

            if (flags.isEmpty() || flags.size < 1)
                modifyData()
            else if (currFlagIndex > flags.size - 1) {
                currFlagIndex = 0
                game.setCurrFlag(flags[currFlagIndex])
                flagMap[flags[currFlagIndex]]?.let { flagImage.setBackgroundResource(it) }
            } else {
                game.setCurrFlag(flags[currFlagIndex])
                flagMap[flags[currFlagIndex]]?.let { flagImage.setBackgroundResource(it) }
            }
        } else {
            flagText.setText("")
            Toast.makeText(this, "Wrong Answer", Toast.LENGTH_LONG).show()
        }
    }

    // move to a new flag when SKIP is clicked
    fun skipFlag(v : View) {
        if(game.getNumFlagsGuessed() == 5) {
            modifyData()
        } else if(flags.size == 1) {
            Toast.makeText(this, "Last Remaining Flag", Toast.LENGTH_LONG).show()
        } else if(currFlagIndex == flags.size - 1) {
            // loop back
            currFlagIndex = 0
            game.setCurrFlag(flags[currFlagIndex])
            flagMap[flags[currFlagIndex]]?.let { flagImage.setBackgroundResource(it) }
        } else {
            currFlagIndex += 1
            game.setCurrFlag(flags[currFlagIndex])
            flagMap[flags[currFlagIndex]]?.let { flagImage.setBackgroundResource(it) }
        }
    }

    private fun updateTimer() {
        val minutes = (timeLeftInMilliseconds / 1000) / 60
        val seconds = (timeLeftInMilliseconds / 1000) % 60

        val timeLeftFormatted = String.format("%02d:%02d", minutes, seconds)
        countdownText.text = timeLeftFormatted
    }


    fun modifyData(){
        // store how long it took to finish this round
        game.setCurrentTime(60 - (timeLeftInMilliseconds / 1000))

        // jump to game over screen with transition
        val intent : Intent = Intent (this, GameOver::class.java)

        val options = ActivityOptions.makeCustomAnimation(
            this,
            R.anim.fade_in,
            R.anim.fade_out
        )
        startActivity(intent, options.toBundle())
    }

    companion object {
        lateinit var game : Game
        lateinit var sharedPreferences : SharedPreferences
    }
}