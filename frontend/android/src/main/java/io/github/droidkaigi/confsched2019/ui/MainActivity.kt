package io.github.droidkaigi.confsched2019.ui

import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.android.support.DaggerAppCompatActivity
import io.github.droidkaigi.confsched2019.R
import io.github.droidkaigi.confsched2019.about.ui.AboutFragment
import io.github.droidkaigi.confsched2019.about.ui.AboutFragmentModule
import io.github.droidkaigi.confsched2019.announcement.ui.AnnouncementFragment
import io.github.droidkaigi.confsched2019.announcement.ui.AnnouncementFragmentModule
import io.github.droidkaigi.confsched2019.databinding.ActivityMainBinding
import io.github.droidkaigi.confsched2019.di.PageScope
import io.github.droidkaigi.confsched2019.ext.android.changed
import io.github.droidkaigi.confsched2019.floormap.ui.FloorMapFragment
import io.github.droidkaigi.confsched2019.floormap.ui.FloorMapFragmentModule
import io.github.droidkaigi.confsched2019.model.ErrorMessage
import io.github.droidkaigi.confsched2019.session.di.SessionAssistedInjectModule
import io.github.droidkaigi.confsched2019.session.ui.SearchFragment
import io.github.droidkaigi.confsched2019.session.ui.SearchFragmentModule
import io.github.droidkaigi.confsched2019.session.ui.SessionDetailFragment
import io.github.droidkaigi.confsched2019.session.ui.SessionDetailFragmentModule
import io.github.droidkaigi.confsched2019.session.ui.SessionPagesFragment
import io.github.droidkaigi.confsched2019.session.ui.SessionPagesFragmentModule
import io.github.droidkaigi.confsched2019.session.ui.SpeakerFragment
import io.github.droidkaigi.confsched2019.session.ui.SpeakerFragmentModule
import io.github.droidkaigi.confsched2019.sponsor.ui.SponsorFragment
import io.github.droidkaigi.confsched2019.sponsor.ui.SponsorFragmentModule
import io.github.droidkaigi.confsched2019.system.store.SystemStore
import io.github.droidkaigi.confsched2019.user.actioncreator.UserActionCreator
import io.github.droidkaigi.confsched2019.user.store.UserStore
import javax.inject.Inject

class MainActivity : DaggerAppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        DataBindingUtil.setContentView<ActivityMainBinding>(
            this,
            R.layout.activity_main
        )
    }

    @Inject lateinit var userActionCreator: UserActionCreator
    @Inject lateinit var systemStore: SystemStore
    @Inject lateinit var userStore: UserStore

    private val navController: NavController by lazy {
        findNavController(R.id.root_nav_host_fragment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
            setupNavigation()

        systemStore.errorMsg.changed(this) { message ->
            val messageStr = when (message) {
                is ErrorMessage.ResourceIdMessage -> getString(message.messageId)
                is ErrorMessage.Message -> message.message
            }
            Snackbar.make(binding.root, messageStr, Snackbar.LENGTH_LONG).show()
        }
        userStore.registered.changed(this) { registered ->
            if (!registered) {
                userActionCreator.load()
            }
        }
    }

    private fun setupNavigation() {
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.main, R.id.about, R.id.announce, R.id.setting),
            binding.drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val config = PageConfiguration.getConfiguration(destination.id)
            binding.logo.isVisible = config.isShowLogoImage
            if (!config.hasTitle) supportActionBar?.title = ""

            binding.isWhiteTheme = config.isWhiteTheme
            if (23 <= Build.VERSION.SDK_INT) {
                window.decorView.systemUiVisibility = if (config.isWhiteTheme) {
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else {
                    0
                }
            }
            val toolbarContentsColor = ContextCompat.getColor(
                this, if (config.isWhiteTheme) android.R.color.black else R.color.white
            )
            binding.toolbar.navigationIcon?.setColorFilter(
                toolbarContentsColor,
                PorterDuff.Mode.SRC_ATOP
            )
            binding.toolbar.setTitleTextColor(toolbarContentsColor)

            // Support display cutouts
            val navHeaderOffsetView =
                binding.navView.getHeaderView(0).findViewById<View>(R.id.offset_view)
            ViewCompat.setOnApplyWindowInsetsListener(navHeaderOffsetView) { view, windowInsets ->
                view.layoutParams.apply {
                    height = windowInsets.systemWindowInsetTop
                }

                windowInsets
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(item, navController) ||
            super.onOptionsItemSelected(item)
    }
}

@Module
abstract class MainActivityModule {
    @Binds abstract fun providesActivity(mainActivity: MainActivity): FragmentActivity

    @PageScope
    @ContributesAndroidInjector(
        modules = [SessionPagesFragmentModule::class, SessionAssistedInjectModule::class]
    )
    abstract fun contributeSessionPagesFragment(): SessionPagesFragment

    @PageScope
    @ContributesAndroidInjector(
        modules = [SessionDetailFragmentModule::class, SessionAssistedInjectModule::class]
    )
    abstract fun contributeSessionDetailFragment(): SessionDetailFragment

    @PageScope
    @ContributesAndroidInjector(
        modules = [SpeakerFragmentModule::class, SessionAssistedInjectModule::class]
    )
    abstract fun contributeSpeakerFragment(): SpeakerFragment

    @PageScope
    @ContributesAndroidInjector(
        modules = [SearchFragmentModule::class, SessionAssistedInjectModule::class]
    )
    abstract fun contributeSearchFragment(): SearchFragment

    @PageScope
    @ContributesAndroidInjector(
        modules = [AboutFragmentModule::class]
    )
    abstract fun contributeAboutFragment(): AboutFragment

    @PageScope
    @ContributesAndroidInjector(modules = [AnnouncementFragmentModule::class])
    abstract fun contributeAnnouncementFragment(): AnnouncementFragment

    @PageScope
    @ContributesAndroidInjector(modules = [FloorMapFragmentModule::class])
    abstract fun contributeFloorMapFragment(): FloorMapFragment

    @PageScope
    @ContributesAndroidInjector(modules = [SponsorFragmentModule::class])
    abstract fun contributeSponsorFragment(): SponsorFragment

    @Module
    companion object {
        @JvmStatic @Provides fun provideNavController(mainActivity: MainActivity): NavController {
            return Navigation
                .findNavController(mainActivity, R.id.root_nav_host_fragment)
        }
    }

    @Module
    abstract class MainActivityBuilder {
        @ContributesAndroidInjector(modules = [MainActivityModule::class])
        abstract fun contributeMainActivity(): MainActivity
    }
}
