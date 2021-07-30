package io.agora.meeting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.agora.meeting.common.BaseFragment
import io.agora.meeting.common.Constant
import io.agora.meeting.common.model.ExampleBean

class ReadyFragment : BaseFragment() {

    private lateinit var exampleBean: ExampleBean

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exampleBean = requireArguments().getParcelable(Constant.DATA)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_ready_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            setTitle(exampleBean.nameStrId)
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }
        view.findViewById<TextView>(R.id.tips).apply {
            setText(getString(exampleBean.tipStrId))
        }
        view.findViewById<View>(R.id.next).setOnClickListener {
            it.postDelayed({ activity?.onBackPressed() }, 300)
            ExampleActivity.launch(requireActivity(), exampleBean)
        }
    }

}