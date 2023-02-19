package de.canyumusak.androiddrop.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import de.canyumusak.androiddrop.extension.PreferenceKeys
import de.canyumusak.androiddrop.extension.dataStore
import kotlinx.coroutines.flow.first

@Composable
fun rememberOnboardingState(page: OnboardingPage): OnboardingState {
    val context = LocalContext.current
    return OnboardingStateImpl(context.dataStore, page)
}

@Composable
fun rememberOnboardingState(): OnboardingState {
    val context = LocalContext.current
    val state = remember {
        OnboardingStateImpl(context.dataStore)
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
    private val dataStore: DataStore<Preferences>,
    show: Boolean = false,
    currentIndex: Int = 0,
) : OnboardingState {

    private val _show = mutableStateOf(show)
    override val show: State<Boolean> get() = _show

    private val onboardingPages = enumValues<OnboardingPage>()
    private val index = mutableStateOf(currentIndex)

    override val currentPage: State<OnboardingPage> = derivedStateOf {
        onboardingPages[index.value]
    }

    constructor(
        dataStore: DataStore<Preferences>,
        page: OnboardingPage = OnboardingPage.Welcome,
    ) : this(dataStore, true, enumValues<OnboardingPage>().indexOf(page))

    suspend fun loadInitialState() {
        _show.value = dataStore.data.first()[PreferenceKeys.showOnboardingKey] ?: true
    }

    override suspend fun next() {
        val newIndex = index.value + 1
        if (newIndex < onboardingPages.size) {
            index.value = newIndex
        } else {
            complete()
        }
    }

    override fun restart() {
        index.value = 0
        _show.value = true
    }

    override suspend fun skip() {
        complete()
    }

    private suspend fun complete() {
        dataStore.edit {
            it[PreferenceKeys.showOnboardingKey] = false
        }
        _show.value = false
    }
}
