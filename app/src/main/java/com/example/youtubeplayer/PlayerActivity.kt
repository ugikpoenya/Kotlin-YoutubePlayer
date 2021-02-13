package com.example.youtubeplayer

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.SparseArray
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.WindowManager
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import at.huber.youtubeExtractor.VideoMeta
import at.huber.youtubeExtractor.YouTubeExtractor
import at.huber.youtubeExtractor.YtFile
import com.example.youtubeplayer.databinding.ActivityPlayerBinding
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory

class PlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerBinding
    var isFullScr:Boolean = false
    var isPlaying: Boolean = false
    var player: SimpleExoPlayer? = null
    var mediaSource: MediaSource? = null
    private var playerHeight: Int = 0
    var youtube_url=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        val bundle = intent.extras
        youtube_url= bundle?.getString("youtube_url").toString()
        binding.txtLog.append("Play URL : "+ youtube_url)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.statusBarColor = ContextCompat.getColor(this,R.color.black);
        }

        binding.imgBack.setOnClickListener {
            releasePlayer()
            finish()
        }

        playerHeight = binding.play.layoutParams.height
        binding.imgFullScr.setOnClickListener {
            if (isFullScr) {
                supportActionBar?.show()

                isFullScr = false
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                binding.play.layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    playerHeight
                )
                binding.imgFullScr.setBackgroundResource(R.drawable.exo_controls_fullscreen_enter)
            } else {
                binding.imgFullScr.setBackgroundResource(R.drawable.exo_controls_fullscreen_exit)
                supportActionBar?.hide()
                isFullScr = true
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                )
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                binding.play.layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT
                )
            }
        }
        initVideoPlayer()
    }

    @SuppressLint("StaticFieldLeak")
    fun initVideoPlayer() {
        binding.progressBar.visibility = VISIBLE
        if (player != null) {
            player?.release()
        }
        binding.playerLayout.visibility = VISIBLE
        val bandwidthMeter = DefaultBandwidthMeter()
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector)
        binding.videoView.player = player

        binding.videoView.setControllerVisibilityListener { visibility ->
            if (visibility == 0) {
                binding.imgBack.visibility = VISIBLE
                binding.imgFullScr.visibility = VISIBLE
            } else {
                binding.imgBack.visibility = GONE
                binding.imgFullScr.visibility = GONE
            }
        }
        player?.addListener(object : Player.DefaultEventListener() {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playWhenReady && playbackState == Player.STATE_READY) {
                    isPlaying = true
                    binding.progressBar.visibility = GONE
                } else if (playbackState == Player.STATE_READY) {
                    binding.progressBar.visibility = GONE
                    isPlaying = false
                } else if (playbackState == Player.STATE_BUFFERING) {
                    isPlaying = false
                    binding.progressBar.visibility = VISIBLE
                } else {
                    isPlaying = false
                }
            }
        })

        var youtubeStreamUrl=""
        object: YouTubeExtractor(this){
            override fun onExtractionComplete(ytFiles: SparseArray<YtFile>, vMeta: VideoMeta){
                if (ytFiles == null) {
                    binding.txtLog.append("\n ytFiles  Null")
                }else{
                    val iTags = arrayOf(22,18)
                    for (iTag in iTags) {
                        val ytGet=ytFiles.get(iTag)
                        if ( ytGet != null) {
                            if(youtubeStreamUrl.isEmpty()) youtubeStreamUrl = ytGet.url
                        }
                    }
                    if(youtubeStreamUrl.isEmpty()){
                        binding.txtLog.append("\n Youtube  Null")
                    }else{
                        val uri = Uri.parse(youtubeStreamUrl)
                        mediaSource = mediaSource(uri)
                        player?.prepare(mediaSource, true, false)
                        player?.seekTo(100)
                        player?.playWhenReady = true
                        binding.txtLog.append("\n youtubeDownloadUrl "+youtubeStreamUrl)
                    }
                }
            }
        }.extract(youtube_url, true, true)
    }



    override fun onPause() {
        super.onPause()
        if (isPlaying && player != null) {
            player?.playWhenReady = false
        }
    }

    override fun onBackPressed() {
        releasePlayer()
        super.onBackPressed()

    }

    override fun onResume() {
        super.onResume()
        if (player != null) {
            player?.playWhenReady = true
        }
    }

    fun releasePlayer() {
        player?.playWhenReady = true
        player?.stop()
        player?.release()
        player = null
        binding.videoView?.player = null
    }

    override fun onStop() {
        super.onStop()
        binding.txtLog.append("\n STOP $isPlaying")
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.txtLog.append("\n DESTROY")
    }

    private fun mediaSource(uri: Uri): MediaSource {
        return ExtractorMediaSource.Factory(
            DefaultHttpDataSourceFactory( this.packageName)
        ).createMediaSource(uri)
    }
}