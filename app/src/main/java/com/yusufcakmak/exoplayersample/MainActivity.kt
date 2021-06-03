package com.yusufcakmak.exoplayersample

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import at.huber.youtubeExtractor.VideoMeta
import at.huber.youtubeExtractor.YouTubeExtractor
import at.huber.youtubeExtractor.YtFile
import com.yusufcakmak.exoplayersample.databinding.ActivityMainBinding
import vimeoextractor.OnVimeoExtractionListener
import vimeoextractor.VimeoExtractor
import vimeoextractor.VimeoVideo
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    var type = "";


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.btnRadioPlayer.setOnClickListener {
            val intent = Intent(this@MainActivity, RadioPlayerActivity::class.java)
            startActivity(intent)
        }

        binding.btnVimeoVideoPlayer.setOnClickListener {


            binding.progress.visibility = View.VISIBLE
            val intent = Intent(this@MainActivity, VideoPlayerActivity::class.java)
            intent.putExtra("video","554612392")
            intent.putExtra("type","vimeo")
            startActivity(intent)

        }

        binding.btnYoutubeVideoPlayer.setOnClickListener {


            binding.progress.visibility = View.VISIBLE

            val intent = Intent(this@MainActivity, VideoPlayerActivity::class.java)
            intent.putExtra("video","https://www.youtube.com/watch?v=lgT1eZj9shQ")
            intent.putExtra("type","youtube")
            startActivity(intent)


        }
    }

}

