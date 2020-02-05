package com.kieronquinn.app.darq.activities

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.kieronquinn.app.darq.R
import com.kieronquinn.app.darq.utils.isDarkTheme
import kotlinx.android.synthetic.main.activity_modal_faq.*

class ModalFaqActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isDarkTheme = isDarkTheme(this)
        if (isDarkTheme){
            setTheme(R.style.AppTheme_Dark)
            window.statusBarColor = getColor(R.color.toolbar_dark)
            window.decorView.systemUiVisibility = 0
        }
        else{
            setTheme(R.style.AppTheme_Light)
            window.statusBarColor = getColor(android.R.color.white)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        setContentView(R.layout.activity_modal_faq)
        if(isDarkTheme) home.imageTintList = ColorStateList.valueOf(Color.WHITE)
        else home.imageTintList = ColorStateList.valueOf(Color.BLACK)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
        home.setImageResource(R.drawable.ic_close)
        toolbar_title.text = getString(R.string.setting_non_root_faq)
        home.setOnClickListener { finish() }
    }

}