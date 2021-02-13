package com.example.youtubeplayer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.youtubeplayer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnPlay.setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)
            val bundle = Bundle()
            bundle.putString("youtube_url", binding.edYoutubeUrl.text.toString())
            intent.putExtras(bundle)
            startActivity(intent)
        }
    }
}