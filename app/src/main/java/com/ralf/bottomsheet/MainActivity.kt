package com.ralf.bottomsheet

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.ralf.bottomsheet.demo.Page2Fragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // 1 继承 DialogBottomSheetFragment
        dialog_bottom_sheet?.setOnClickListener {
            startActivity(Intent(this, BottomSheetActivity::class.java))
        }
        // 3 自定义 BottomSheetFragment,不使用 BottomSheetDialog
        bottom_sheet?.setOnClickListener {
            BottomSheet.showBottomSheet(
                BottomSheetOption(
                    "NonDialogBottomSheet",
                    supportFragmentManager,
                    Page2Fragment.newInstance("", ""),
                    "Page2Fragment",
                    isShowDialog = false,
                    layoutRes = R.id.bottom_container,
                    needActivity = false,
                    stateChangedCallback = object : BottomSheetBehavior.BottomSheetCallback() {
                        override fun onStateChanged(bottomSheet: View, newState: Int) {
                            Log.d("MainActivity", "onStateChanged: $newState")
                        }

                        override fun onSlide(bottomSheet: View, slideOffset: Float) {
                            Log.d("MainActivity", "onSlide: $slideOffset")
                        }

                    }
                )
            )
        }
        custom_bottom_sheet?.setOnClickListener {
            // 3 自定义 BottomSheetNestedLayout
            BottomSheetNestedFragment.showPanel(supportFragmentManager, R.id.bottom_container, null)

        }
    }
}