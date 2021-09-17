package io.agora.meeting.ui.module.main.render.mix

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import io.agora.meeting.context.bean.RenderInfo
import io.agora.meeting.ui.R
import io.agora.meeting.ui.base.BaseUiController
import io.agora.meeting.ui.databinding.MainRenderMixContainerBinding

class MixLayoutUC : BaseUiController<MainRenderMixContainerBinding, MixLayoutVM>(
        MainRenderMixContainerBinding::class.java,
        MixLayoutVM::class.java
) {

    var boardClickListener: ((View?) -> Unit)? = null
    var layoutChangeListener: ((MixLayoutVM.MixLayoutType, Int) -> Unit)? = null

    // true: long click of video will display the stats info layout
    // false: hide the stats info layout
    var statsDisplayEnable = false
        set(value) {
            field = value
            viewModel?.statsDisplayEnable?.value = value
        }

    override fun onViewModelBind() {
        super.onViewModelBind()
        requireViewModel().statsDisplayEnable.value = statsDisplayEnable
        requireViewModel().layoutInfo.observe(requireLifecycleOwner()) {
            layoutChangeListener?.invoke(it.type, requireViewModel().renderCount.value ?: 0)
            replaceFragment(
                    when (it.type) {
                        MixLayoutVM.MixLayoutType.Tiled -> TiledLayoutFrag(requireViewModel()).apply {
                            navigation = object : TiledLayoutFrag.TiledLayoutNavigation {
                                override fun navigateToLecturerLayout(view: View?, renderInfo: RenderInfo) {
                                    requireViewModel().changeLayout(MixLayoutVM.MixLayoutType.Lecturer, renderInfo)
                                }
                            }
                        }
                        MixLayoutVM.MixLayoutType.Lecturer -> LecturerLayoutFrag.createInstance(
                                requireViewModel(),
                                it.renderInfo?.id ?: -1
                        ).apply {
                            navigation = object : LecturerLayoutFrag.LecturerLayoutNavigation {
                                override fun navigateToTiledLayout(view: View?) {
                                    requireViewModel().changeLayout(MixLayoutVM.MixLayoutType.Tiled)
                                }

                                override fun navigateToWhiteboardPage(view: View?) {
                                    boardClickListener?.invoke(view)
                                }
                            }
                        }
                        MixLayoutVM.MixLayoutType.Audio -> AudioLayoutFrag()
                    }
            )
        }
        requireViewModel().renderCount.observe(requireLifecycleOwner()){
            layoutChangeListener?.invoke(requireViewModel().layoutInfo.value?.type ?: MixLayoutVM.MixLayoutType.Tiled, it)
        }

    }

    private fun replaceFragment(fragment: Fragment) {
        requireFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commitAllowingStateLoss()
    }


}