package com.ralf.bottomsheet

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ralf.bottomsheet.demo.Page1Fragment
import com.ralf.bottomsheet.utils.AppImmersiveUtils

class BottomSheetActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)
        setContentView(R.layout.activity_bottom_sheet)
        handleImmersiveMode()
        BottomSheet.showBottomSheet(
            BottomSheetOption(
                "DialogBottomSheet",
                supportFragmentManager,
                Page1Fragment.newInstance("", ""),
                "Page1Fragment"
            )
        )
    }

    private fun handleImmersiveMode() {
        val view = findViewById<View>(android.R.id.content)
        view.setPadding(0, 0, 0, 0)
        AppImmersiveUtils.setStatusBarColor(this, Color.TRANSPARENT)
    }
}