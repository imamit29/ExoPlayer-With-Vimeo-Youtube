package ExoPlayer

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.util.SparseArray
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import at.huber.youtubeExtractor.VideoMeta
import at.huber.youtubeExtractor.YouTubeExtractor
import at.huber.youtubeExtractor.YtFile
import com.exoplayersample.databinding.ActivityVideoPlayerBinding
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import vimeoextractor.OnVimeoExtractionListener
import vimeoextractor.VimeoExtractor
import vimeoextractor.VimeoVideo
import java.util.*
import kotlin.collections.ArrayList


class VideoPlayerActivity : Activity() {

    private lateinit var simpleExoPlayer: SimpleExoPlayer
    private lateinit var mediaDataSourceFactory: DataSource.Factory
    private lateinit var binding : ActivityVideoPlayerBinding
    var STREAM_URL =""
    var fullscreen = false
    var lastPos = 0
    var listOfValues = arrayListOf<String>()
    var listOfKeys = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.extras?.getString("type").equals("youtube")){
            binding.vImage.visibility = View.GONE
            binding.yImage.visibility = View.VISIBLE
        }else{
            binding.vImage.visibility = View.VISIBLE
            binding.yImage.visibility = View.GONE
        }

        binding.fullscreenButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                if (fullscreen) {
                    binding.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                    simpleExoPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    fullscreen = false
                } else {
                    binding.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
                    simpleExoPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                    window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    fullscreen = true
                }
            }
        })

        binding.playerView.setControllerVisibilityListener(object : PlayerControlView.VisibilityListener {
            override fun onVisibilityChange(visibility: Int) {

                if (visibility == View.VISIBLE) {
                    binding.options.visibility = View.VISIBLE

                    if (intent.extras?.getString("type").equals("youtube")){
                        binding.vImage.visibility = View.GONE
                        binding.yImage.visibility = View.VISIBLE
                    }else{
                        binding.vImage.visibility = View.VISIBLE
                        binding.yImage.visibility = View.GONE
                    }
                } else {
                    binding.options.visibility = View.GONE
                    binding.vImage.visibility = View.GONE
                    binding.yImage.visibility = View.GONE
                }
            }

        })

        playVideo(intent.extras?.getString("type")!!, intent.extras?.getString("video")!!)
    }


    fun playVideo(vtype : String,video : String){

        if (vtype.equals("vimeo")){
            VimeoExtractor.getInstance().fetchVideoWithIdentifier(video, null, object : OnVimeoExtractionListener {
                override fun onSuccess(video: VimeoVideo) {
                    if (video == null) {

                    } else {

                        val streams = video.streams
                        val keySet: Set<String> = streams.keys

                        // Creating an ArrayList of keys
                        // by passing the keySet
                        listOfKeys = ArrayList(keySet)

                        // Getting Collection of values from HashMap
                        val values: Collection<String> = streams.values

                        // Creating an ArrayList of values
                        listOfValues = ArrayList(values)
                        println("The Keys of the Map are "
                                + listOfKeys)
                        println("The Values of the Map are "
                                + listOfValues)

                        STREAM_URL = listOfValues!!.get(0);
                        initializePlayer()
                    }
                }

                override fun onFailure(throwable: Throwable) {
                   throwable.printStackTrace()
                }
            })
        }else{


            //@SuppressLint("StaticFieldLeak")
            object : YouTubeExtractor(this) {
                override fun onExtractionComplete(ytFiles: SparseArray<YtFile>, vMeta: VideoMeta) {
                    if (ytFiles != null) {



                        if (ytFiles == null) {
                            // Something went wrong we got no urls. Always check this.
                            finish()
                            return
                        }




                        var i: Int = 0
                        var itag: Int
                        while (i < ytFiles.size()) {
                            itag = ytFiles.keyAt(i)
                            // ytFile represents one file with its url and meta data
                            val ytFile: YtFile? = ytFiles.get(itag)

                            // Just add videos in a decent format => height -1 = audio

                            if (ytFile!!.getFormat().getHeight() == -1 || ytFile.getFormat().getHeight() >= 360) {

                                var btnText = if (ytFile.getFormat().getHeight() === -1){ "Audio" }else ytFile.getFormat().getHeight().toString() + "p"

                                if (!btnText.equals("Audio")){
                                    listOfValues.add(ytFile.getUrl())
                                    listOfKeys!!.add(btnText)
                                }
                            }
                            i++
                        }
                        STREAM_URL = listOfValues!!.get(0);
                        initializePlayer()
                    }
                }
            }.extract(video, true, true)
        }

    }

    private fun initializePlayer() {

        runOnUiThread {
            mediaDataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, "mediaPlayerSample"))

            val mediaSource = ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(MediaItem.fromUri(STREAM_URL))

            val mediaSourceFactory: MediaSourceFactory = DefaultMediaSourceFactory(mediaDataSourceFactory)

            simpleExoPlayer = SimpleExoPlayer.Builder(this)
                    .setMediaSourceFactory(mediaSourceFactory)
                    .build()

            val param = PlaybackParameters(1F)
            simpleExoPlayer.setPlaybackParameters(param)

            simpleExoPlayer.addMediaSource(mediaSource)

            simpleExoPlayer.playWhenReady = true

            simpleExoPlayer.prepare()

            simpleExoPlayer.seekTo(lastPos.toLong())

            simpleExoPlayer.addListener(object : Player.EventListener {
                override fun onPlayerError(error: ExoPlaybackException) {
                    Toast.makeText(this@VideoPlayerActivity, "Video not available", Toast.LENGTH_SHORT).show()
                }
            })

            binding.playerView.setShutterBackgroundColor(Color.TRANSPARENT)
            binding.playerView.player = simpleExoPlayer
            binding.playerView.requestFocus()
        }


    }

    private fun releasePlayer() {
        simpleExoPlayer.release()
    }

    public override fun onPause() {
        super.onPause()

        if (Util.SDK_INT <= 23) releasePlayer()
    }

    public override fun onStop() {
        super.onStop()

        if (Util.SDK_INT > 23) releasePlayer()
    }

    fun speedClick(v: View){

        val popup = PopupMenu(this@VideoPlayerActivity, v)
        //Inflating the Popup using xml file
        //Inflating the Popup using xml file
        popup.menu.add("0.25x")
        popup.menu.add("0.5x")
        popup.menu.add("0.75x")
        popup.menu.add("Normal")
        popup.menu.add("1.25x")
        popup.menu.add("1.5x")
        popup.menu.add("1.75x")
        popup.menu.add("2x")

        //registering popup with OnMenuItemClickListener
        var param = PlaybackParameters(1F)
        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener { item ->
            if (item.title.equals("0.25x")) {
                param = PlaybackParameters(0.25F)
                binding.speed.setText("0.25x")
            } else if (item.title.equals("0.5x")) {
                param = PlaybackParameters(0.5F)
                binding.speed.setText("0.5x")
            }else if (item.title.equals("0.75x")) {
                param = PlaybackParameters(0.75F)
                binding.speed.setText("0.75x")
            }else if (item.title.equals("Normal")) {
                param = PlaybackParameters(1F)
                binding.speed.setText("NOR")
            }else if (item.title.equals("1.25x")) {
                param = PlaybackParameters(1.25F)
                binding.speed.setText("1.25x")
            }else if (item.title.equals("1.5x")) {
                param = PlaybackParameters(1.5F)
                binding.speed.setText("1.5x")
            }else if (item.title.equals("1.75x")) {
                param = PlaybackParameters(1.75F)
                binding.speed.setText("0.75x")
            }else if (item.title.equals("2x")) {
                param = PlaybackParameters(2F)
                binding.speed.setText("2x")
            }
            simpleExoPlayer.setPlaybackParameters(param)


            true


        }

        popup.show()

    }

    fun qualityClick(v: View){

        val names: ArrayList<String> = listOfKeys!!
        val namesArr = names.toTypedArray()

        val popup = PopupMenu(this@VideoPlayerActivity, v)

        for(item in namesArr){
            popup.menu.add(item)
        }

        popup.setOnMenuItemClickListener { items ->
            for(item in namesArr.indices){

                if (namesArr[item].equals(items.title)){

                    STREAM_URL = listOfValues!!.get(item);

                    lastPos = simpleExoPlayer.currentPosition.toInt()

                    if (Util.SDK_INT <= 23) initializePlayer()
                    if (Util.SDK_INT > 23) initializePlayer()
                }
            }
            true
        }
        popup.show()

    }

    override fun onBackPressed() {

        if (fullscreen) {
            binding.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            simpleExoPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT);
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            fullscreen = false
        }else{
            super.onBackPressed()
        }
    }
}