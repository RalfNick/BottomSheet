package com.ralf.bottomsheet.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.ralf.bottomsheet.BottomSheetNestedFragment
import com.ralf.bottomsheet.R
import kotlinx.android.synthetic.main.fragment_page1.*


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class NestedPageFragment1 : Fragment() {

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
        return inflater.inflate(R.layout.fragment_page_nested_1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.share_panel).setOnClickListener {
            (parentFragment as? BottomSheetNestedFragment)?.addChildFragment(
                "NestedPageFragment2",
                NestedPageFragment2.newInstance("", "")
            )
        }
        rv_1?.layoutManager = LinearLayoutManager(context)
        rv_1?.adapter = MyAdapter(getDemoList())
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NestedPageFragment1().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}