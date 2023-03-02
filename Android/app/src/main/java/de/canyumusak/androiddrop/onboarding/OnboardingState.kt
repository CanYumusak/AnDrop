package de.canyumusak.androiddrop.onboarding

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
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

@Composable
fun requestNotificationPermission() {

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
        val newIndex = index.value + 1
        when {
            newIndex < onboardingPages.size -> {
                index.value = newIndex
            }

            else -> {
                complete()
            }
        }
    }

    override fun restart() {
        index.value = 0
        _show.value = true
    }

    override suspend fun skip() {
        when {
            askForPushPermission() -> {
                index.value = OnboardingPage.PushPermission.ordinal
            }

            else -> {
                index.value = OnboardingPage.values().size - 1
            }
        }
    }

    private fun askForPushPermission(): Boolean {
        return !NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    private suspend fun complete() {
        dataStore.edit {
            it[PreferenceKeys.showOnboardingKey] = false
        }
        _show.value = false
    }
}
