package com.android.crossfadeplayer

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    //media player 1
    private var mp: MediaPlayer? = null
    //song list
    private var currentSong = mutableListOf(R.raw.man,
        R.raw.twice)
    //song number
    private var number = 0
    //crossfade duration
    var crossFadeTime = 2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        controlSound()
    }

    private fun controlSound() {
        songSwitch()

        //press play to start playing default song
        fb_play.setOnClickListener {

            if (mp == null) {
                mp = MediaPlayer.create(this, currentSong[number])
                mp?.start()
                crossFade()
            }
        }


        //seekbar listener
        sb_seekbar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

                seekBar.max = 10
                if (progress >= 2) {
                    crossFadeTime = progress
                }
                Log.d("Seekbar", "CrossFade:"+crossFadeTime)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }


        })

    }




    //press button = switch song
    private fun songSwitch() {
        fb_1.setOnClickListener {
            number = 0
            Log.d("MA", "Current song is ${currentSong}")
        }
        fb_2.setOnClickListener {
            number = 1
            Log.d("MA", "Current song is ${currentSong}")
        }
    }



    private fun crossFade() {
        //media player 2
        var mp2: MediaPlayer? = null
        var secondsCount = 0
        var playerCreated = false



        //fade from low volume to max
        fun fadeIn(mp: MediaPlayer?) {
            var volumeNumber = 0.100f
            mp?.setVolume(volumeNumber, volumeNumber)

            GlobalScope.launch {

                while (volumeNumber < 0.500f) {
                    delay(100 * crossFadeTime.toLong())
                    volumeNumber += 0.025f
                    Log.d("FadeInVolume", "${volumeNumber}")
                }
            }
        }

        //fade from max volume to low
        fun fadeOut(mp: MediaPlayer?, fadeFrom: Int) {
            var volumeNumber = 0.500f
            mp?.setVolume(volumeNumber, volumeNumber)


            GlobalScope.launch {

                while (volumeNumber > 0.050f) {
                    delay(100 * crossFadeTime.toLong())
                    volumeNumber -= 0.025f
                    Log.d("FadeOutVolume", "${volumeNumber}")
                }
            }
        }

        //if number is not last choose nextSong
        //else if number is last in list start from zero
        fun nextSong() {
            if (number + 1 < currentSong.size) {
                number += 1
            } else if (number + 1 == currentSong.size) {
                number = 0
            }
        }

        //release player when needed
        fun release(mp: MediaPlayer?) {
            if (mp !== null) {
                mp.stop()
                mp.reset()
                mp.release()
            }
        }

        /*release player when called
        * choose next song
        * start playing
        * start fadingIn
        * start fadingOut when time comes */
        fun player2() {
            release(mp2)
            nextSong()
            mp2 = MediaPlayer.create(this, currentSong[number])
            mp2?.start()
            fadeIn(mp2)
            fadeOut(mp2, mp2!!.duration - crossFadeTime)
            playerCreated = true
        }

        fun player1() {
            release(mp)
            nextSong()
            mp = MediaPlayer.create(this, currentSong[number])
            mp?.start()
            fadeIn(mp)
            fadeOut(mp, mp!!.duration - crossFadeTime)
        }


        GlobalScope.launch {

            while (secondsCount >= 0 && mp !=null) {

                //initialise player1 if seconds = song duration
                if (secondsCount == mp!!.duration / 1000) {
                    secondsCount = 0
                    player1()
                }

                //initialise player2 if first player is about to finish song
                if (secondsCount >= mp!!.duration / 1000 - crossFadeTime
                    && !playerCreated
                ) {
                    player2()
                }

                //delay 1second
                delay(1000)

                // count seconds of how much song is being played
                secondsCount++
                Log.d("SecondsCount", "${secondsCount}")
            }
        }
    }
}