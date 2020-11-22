package com.android.crossfadeplayer

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private val REQUEST_CODE_1 = 1 //request code для pickAudioFile
    private val REQUEST_CODE_2 = 2

    private var mp1: MediaPlayer? = null
    private var mp2: MediaPlayer? = null

    private var song1:Uri? = null
    private var song2:Uri? = null


    private var number = 1 //номер проигрываемой песни

    private var crossFadeTime = 2 //кр

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        controlSound()
    }

    private fun controlSound() {
        selectSong()

        fb_play.setOnClickListener {//play button
                errorCheck()
        }


        //seekbar listener
        sb_seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

                seekBar.max = 10
                if (progress >= 2) {
                    crossFadeTime = progress
                }
                Log.d("Seekbar", "CrossFade:" + crossFadeTime)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    fun errorCheck() {//проверка на ошибки
        if (song1 !=null && song2 != null){
            Log.d("mp","SONG1: $song1+"+"SONG2: $song2")
            songCycle()
            fb_play.isClickable = false
            fb_1.isClickable = false
            fb_2.isClickable = false
        } else {
            Toast.makeText(applicationContext, "Error: Select 2 audiofiles to play",Toast.LENGTH_SHORT).show()
        }
    }

    fun songCycle() {//воспроизведение песен, в зависимости от текущего номера

            if (number == 1) {
                start1()
            }
            if (number == 2) {
                start2()
            }

    }

    fun start1() {//запуск плеера+песни и fadeIn+fadeOut
        mp1 = MediaPlayer.create(this, song1)
        mp1?.start()

        if (mp2 != null) {
            fadeIn(mp1)
        }

        while (mp1 != null) {
            if (mp1!!.currentPosition / 1000 > mp1!!.duration / 1000- crossFadeTime){
                number = 2
                fadeOut(mp1)
            }
        }
    }

    fun start2() {
        mp2 = MediaPlayer.create(this, song2)
        mp2?.start()

        if (mp1 != null) {
            fadeIn(mp2)
        }

        while (mp2 != null) {
            if (mp2!!.currentPosition / 1000 > mp2!!.duration / 1000- crossFadeTime){
                number = 1
                fadeOut(mp2)
            }
        }
    }


    private fun selectSong() {//выбор аудиофайла
        fb_1.setOnClickListener {
            pickAudioFile(REQUEST_CODE_1)
        }
        fb_2.setOnClickListener {
            pickAudioFile(REQUEST_CODE_2)
        }
    }

    fun fadeOut(mp: MediaPlayer?) {//постепенно уменьшает громкость песни
        var volumeNumber = 1.0f

        GlobalScope.launch {

            while (mp !=null && volumeNumber > 0.0f){
                if (volumeNumber <= 0.1f) {
                    mp.release()
                    break
                }
                mp.setVolume(volumeNumber, volumeNumber)
                delay(50 * crossFadeTime.toLong())
                volumeNumber -= 0.1f
            }
        }
        songCycle()
    }

    fun fadeIn(mp: MediaPlayer?) {//постепенно увеличивает громкость песни
        var volumeNumber = 0.1f

        GlobalScope.launch {

            while (volumeNumber < 1.0f){
                mp?.setVolume(volumeNumber, volumeNumber)
                delay(50 * crossFadeTime.toLong())
                volumeNumber += 0.1f
            }
        }
    }

    private fun pickAudioFile(requestCode:Int) {
        val intent = Intent(Intent.ACTION_GET_CONTENT, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
        intent.setType("audio/*")
        startActivityForResult(intent, requestCode)
    }

   override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {

            REQUEST_CODE_1 -> {//песня №1 = песня выбранная после клика на №1
                if (resultCode == Activity.RESULT_OK) {
                    song1 = data!!.data
                }
            }

            REQUEST_CODE_2 -> {//песня №2 = песня выбранная после клика на №2
                if (resultCode == Activity.RESULT_OK) {
                    song2 = data!!.data
                }
            }
        }
    }
}