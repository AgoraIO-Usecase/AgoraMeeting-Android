package io.agora.meeting

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import io.agora.meeting.common.Constant
import io.agora.meeting.common.adapter.ExampleSection
import io.agora.meeting.common.model.ExampleBean
import io.agora.meeting.ui.module.root.RootUC

class MainActivity : AppCompatActivity(), ExampleSection.ItemClickListener {
    private var appBarConfiguration: AppBarConfiguration? = null

    private val rootUC by lazy {
        RootUC().apply {
            onFinishListener = { finish() }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration.Builder(navController.graph).build()
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration!!)

        rootUC.createViewBinding(this, this, null)
        rootUC.bindViewModel(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        rootUC.destroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        return (NavigationUI.navigateUp(navController, appBarConfiguration!!)
                || super.onSupportNavigateUp())
    }

    override fun onItemClick(item: ExampleBean) {
        val bundle = Bundle()
        bundle.putParcelable(Constant.DATA, item)
        Navigation.findNavController(this, R.id.nav_host_fragment)
                .navigate(R.id.action_mainFragment_to_Ready, bundle)
    }


}