package ExoPlayer

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.exoplayersample.databinding.ActivityMainBinding
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

