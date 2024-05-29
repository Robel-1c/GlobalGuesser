package com.example.globalguesser

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private lateinit var logo : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // store list of easy flags
        easyFlags["australia"] = R.drawable.australia
        easyFlags["india"] = R.drawable.india
        easyFlags["france"] = R.drawable.france
        easyFlags["croatia"] = R.drawable.croatia
        easyFlags["new zealand"] = R.drawable.new_zealand

        // store list of medium flags
        mediumFlags["ghana"] = R.drawable.ghana
        mediumFlags["georgia"] = R.drawable.georgia
        mediumFlags["egypt"] = R.drawable.egypt
        mediumFlags["ethiopia"] = R.drawable.ethiopia
        mediumFlags["bangladesh"] = R.drawable.bangladesh

        // store list of hard flags
        hardFlags["azerbaijan"] = R.drawable.azerbaijan
        hardFlags["gabon"] = R.drawable.gabon
        hardFlags["kazakhstan"] = R.drawable.kazakhstan
        hardFlags["guinea"] = R.drawable.guinea
        hardFlags["ivory coast"] = R.drawable.c_te_d_ivoire

        // display logo
        logo = findViewById(R.id.globe_logo);
        logo.setImageResource(R.drawable.globe_logo);

        // set up difficulty level selector
        val spinnerId = findViewById<Spinner>(R.id.difficulty)
        val difficulty  = arrayOf("Easy" ,"Medium","Hard")
        val arrayadp = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, difficulty)
        spinnerId.adapter = arrayadp
        spinnerId.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                difficultyLevel = difficulty[position];
                Toast.makeText(this@MainActivity, "difficulty is ${difficulty[position]}", Toast.LENGTH_LONG)

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                difficultyLevel = difficulty[0];
                Toast.makeText(this@MainActivity, "difficulty is  easy", Toast.LENGTH_LONG)
            }
        }
    }

    // when "play" is clicked
    fun modifyView( v: View){
        var intent : Intent = Intent (this, GameActivity::class.java)

        // transition from home screen to game screen
        val options = ActivityOptions.makeCustomAnimation(
            this,
            R.anim.slide_up_enter,
            R.anim.slide_up_exit
        )
        startActivity(intent, options.toBundle())
    }

    companion object {
        // difficulty level - accessible to Game.kt
        lateinit var difficultyLevel : String

        // list of flags in categories of difficulty
        var easyFlags = HashMap<String, Int>()
        var mediumFlags = HashMap<String, Int>()
        var hardFlags = HashMap<String, Int>()
    }
}