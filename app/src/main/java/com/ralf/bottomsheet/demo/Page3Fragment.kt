package com.ralf.bottomsheet.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.ralf.bottomsheet.BottomSheet
import com.ralf.bottomsheet.R
import kotlinx.android.synthetic.main.fragment_page2.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class Page3Fragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_page2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.back).setOnClickListener {
            BottomSheet.removeChildFragment(
                "DialogBottomSheet",
                "Page3Fragment",
                R.anim.bottom_sheet_slide_out
            )
        }
        rv_2?.layoutManager = LinearLayoutManager(context)
        rv_2?.adapter = MyAdapter(getDemoList2())
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Page3Fragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}