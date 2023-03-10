package com.wsm.gettext

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.wsm.gettext.databinding.ActivityDetailBinding
import com.wsm.gettext.databinding.ActivityMainBinding

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val getText = intent.getStringExtra("text")

        binding.tvContent.text = getText
        binding.ivCopy.setOnClickListener {
            copyTextToClipboard()
        }
    }

    private fun copyTextToClipboard() {
        val textToCopy = binding.tvContent.text
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", textToCopy)
        clipboardManager.setPrimaryClip(clipData)

        val snack =
            Snackbar.make(binding.tvContent, "Text copied to clipboard", Snackbar.LENGTH_LONG)
        snack.show()

//        Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_LONG).show()
    }
}