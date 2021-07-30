package io.agora.meeting.ui.framework

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.widget.Toolbar
import io.agora.meeting.ui.base.KBaseFragment
import io.agora.meeting.ui.databinding.WebSimpleFragmentBinding

class SimpleWebFragment : KBaseFragment() {

    private lateinit var binding: WebSimpleFragmentBinding
    private lateinit var mUrl: String

    private var navigation: SimpleWebNavigation? = null

    companion object {

        fun newInstance(url: String): SimpleWebFragment {
            return SimpleWebFragment().apply {
                arguments = SimpleWebFragmentArgs(url).toBundle()
            }
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = WebSimpleFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mUrl = SimpleWebFragmentArgs.fromBundle(requireArguments()).url
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is SimpleWebNavigation){
            navigation = context
        }
    }

    override fun onDetach() {
        if(navigation == requireActivity()){
            navigation = null
        }
        super.onDetach()
    }

    override fun onDestroy() {
        super.onDestroy()
        navigation = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAppBar(binding.toolbar, false)
        val webSettings = binding.webview.settings
        webSettings.javaScriptEnabled = true
        binding.webview.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                if(activity != null){
                    setupAppBar(binding.toolbar, false, view.title)
                }
            }
        }
        binding.webview.loadUrl(mUrl)
    }

    override fun setupAppBar(toolbar: Toolbar?, isLight: Boolean, title: String?) {
        super.setupAppBar(toolbar, isLight, title)
        binding.toolbar.setNavigationOnClickListener {
            navigation?.navigateUp() ?: requireActivity().finish()
        }
    }

    fun setNavigation(nav: SimpleWebNavigation){
        navigation = nav
    }

    interface SimpleWebNavigation {
        fun navigateUp(): Boolean
    }
}