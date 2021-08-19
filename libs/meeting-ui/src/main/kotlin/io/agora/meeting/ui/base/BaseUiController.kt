package io.agora.meeting.ui.base

import android.content.Context
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.observe
import androidx.viewbinding.ViewBinding
import io.agora.meeting.ui.MeetingUi
import io.agora.meeting.ui.R
import io.agora.meeting.ui.util.ToastUtil


open class BaseUiController<Binding : ViewBinding, VM : BaseViewModel>(
        private val bindingClass: Class<Binding>,
        private val vmClass: Class<VM>,
){

    internal var index = 0

    private var lifecycleOwner: LifecycleOwner? = null

    protected var binding: Binding? = null
        private set

    protected var viewModel: VM? = null
        private set

    private var mLoadingDialog: AlertDialog? = null

    var visibleToUser = false
        set(value) {
            if(value != field){
                field = value
                binding?.let {
                    onVisibleToUser(value)
                }
            }
        }

    var attachFragment: KBaseFragment? = null
        internal set

    fun createViewBinding(context: Context, lifecycleOwner: LifecycleOwner, container: View?, attachParent: Boolean = true): Binding {
        this.lifecycleOwner = lifecycleOwner
        val mInflate = bindingClass.getDeclaredMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java)
        mInflate.isAccessible = true
        val viewBinding = mInflate.invoke(null, LayoutInflater.from(context), container, attachParent)
        if (viewBinding is ViewDataBinding) {
            viewBinding.lifecycleOwner = lifecycleOwner
        }
        binding = viewBinding as Binding
        onViewCreated()
        return binding!!
    }

    fun bindViewModel(owner: ViewModelStoreOwner): VM {
        // createViewModel
        val globalVMFactory = MeetingUi.globalVMFactory
        viewModel = if (globalVMFactory != null) ViewModelProvider(owner, globalVMFactory).get(vmClass) else ViewModelProvider(owner).get(vmClass)
        binding?.let {
            val mSetViewModel = it.javaClass.getDeclaredMethod("setViewModel", vmClass)
            mSetViewModel.isAccessible = true
            mSetViewModel.invoke(it, viewModel)
        }
        onViewModelBind()
        return viewModel!!
    }

    fun destroy() {
        onDestroy()
    }

    fun requireContext() = binding?.root?.context?: throw IllegalStateException("the ui controller has been destroy")

    fun requireFragmentManager(): FragmentManager {
        attachFragment?.childFragmentManager?.let {
            return it
        }
        binding?.root?.context?.let {
            if (it is AppCompatActivity) {
                return it.supportFragmentManager
            }
        }
        throw IllegalStateException("the ui controller has been destroy")
    }

    fun requireLifecycleOwner() = lifecycleOwner ?: throw IllegalStateException("the ui controller has been destroy")

    fun requireViewModel() = viewModel ?: throw IllegalStateException("the ui controller has been destroy")

    fun requireBinding() = binding ?: throw IllegalStateException("the ui controller has been destroy")

    fun isLandscape(): Boolean{
        val rect = Rect()
        requireBinding().root.getWindowVisibleDisplayFrame(rect)
        return rect.width() > rect.height()
    }

    open fun onViewCreated() {}

    open fun onViewModelBind() {
        viewModel?.failureEvent?.observe(lifecycleOwner!!) { event ->
            val error = event.getContentIfNotHandled()?: return@observe
            ToastUtil.showShort(error.message)
        }
        viewModel?.loadingEvent?.observe(lifecycleOwner!!) {
            val show = it.getContentIfNotHandled() ?: return@observe
            if (show) {
                showLoadingDialog()
            } else {
                dismissLoadingDialog()
            }
        }
    }

    open fun onDestroy() {
        binding = null
        viewModel = null
        lifecycleOwner = null
        dismissLoadingDialog()
        mLoadingDialog = null
    }

    open fun onVisibleToUser(visible: Boolean) {}

    fun showLoadingDialog() {
        val context = binding?.root?.context ?: return
        if (mLoadingDialog == null) {
            val progressBar = ProgressBar(context)
            val padding = context.resources.getDimension(R.dimen.loading_dialog_padding).toInt()
            progressBar.setPadding(padding, padding, padding, padding)
            mLoadingDialog = AlertDialog.Builder(context)
                    .setView(progressBar)
                    .setCancelable(false)
                    .show()
        } else {
            mLoadingDialog?.show()
        }
    }

    fun dismissLoadingDialog() {
        mLoadingDialog?.dismiss()
    }
}