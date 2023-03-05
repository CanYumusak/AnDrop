package de.canyumusak.androiddrop.onboarding

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import de.canyumusak.androiddrop.analytics.Analytics
import de.canyumusak.androiddrop.extension.PreferenceKeys
import de.canyumusak.androiddrop.extension.dataStore
import kotlinx.coroutines.flow.first

@Composable
fun rememberOnboardingState(page: OnboardingPage): OnboardingState {
    val context = LocalContext.current
    return OnboardingStateImpl(context, page)
}

@Composable
fun rememberOnboardingState(): OnboardingState {
    val context = LocalContext.current
    val state = remember {
        OnboardingStateImpl(context)
    }

    LaunchedEffect(true) {
        state.loadInitialState()
    }
    return state
}

interface OnboardingState {

    val show: State<Boolean>
    val currentPage: State<OnboardingPage>

    suspend fun next()
    fun restart()
    suspend fun skip()
}

private class OnboardingStateImpl(
    private val context: Context,
    show: Boolean = false,
    currentIndex: Int = 0,
) : OnboardingState {

    private val dataStore: DataStore<Preferences> = context.dataStore

    private val _show = mutableStateOf(show)
    override val show: State<Boolean> get() = _show

    private val onboardingPages = enumValues<OnboardingPage>()
    private val index = mutableStateOf(currentIndex)

    override val currentPage: State<OnboardingPage> = derivedStateOf {
        onboardingPages[index.value]
    }

    constructor(
        context: Context,
        page: OnboardingPage = OnboardingPage.Welcome,
    ) : this(context, true, enumValues<OnboardingPage>().indexOf(page))

    suspend fun loadInitialState() {
        _show.value = dataStore.data.first()[PreferenceKeys.showOnboardingKey] ?: true
    }

    override suspend fun next() {
        showNextPage(index.value + 1)
    }

    override suspend fun skip() {
        OnboardingEvents.skip(currentPage.value)
        when {
            index.value >= OnboardingPage.PushPermission.ordinal -> {
                next()
            }

            else -> {
                showNextPage(OnboardingPage.PushPermission.ordinal)
            }
        }
    }

    private suspend fun showNextPage(startIndex: Int) {
        val nextIndex = (startIndex until onboardingPages.size)
            .firstOrNull { shouldShow(it) }

        if (nextIndex != null) {
            index.value = nextIndex
        } else {
            complete()
        }
    }

    private fun shouldShow(index: Int): Boolean {
        return when (val page = onboardingPages[index]) {
            OnboardingPage.Welcome,
            OnboardingPage.Install,
            OnboardingPage.CheckSetup -> true

            OnboardingPage.PushPermission -> askForPushPermission()
            OnboardingPage.AnalyticsPermission -> !Analytics.hasConsent()
        }
    }

    override fun restart() {
        OnboardingEvents.restart()
        index.value = 0
        _show.value = true
    }

    private fun askForPushPermission(): Boolean {
        return !NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    private suspend fun complete() {
        OnboardingEvents.complete()
        dataStore.edit {
            it[PreferenceKeys.showOnboardingKey] = false
        }
        _show.value = false
    }
}
