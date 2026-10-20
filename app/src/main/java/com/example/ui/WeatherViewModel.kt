package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.WeatherAiOverview
import com.example.data.ai.WeatherAiService
import com.example.data.model.*
import com.example.data.repository.WeatherRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "WeatherViewModel"

sealed interface AiOverviewUiState {
    data object Idle : AiOverviewUiState
    data object Loading : AiOverviewUiState
    data class Success(val overview: WeatherAiOverview) : AiOverviewUiState
    data class Error(val message: String) : AiOverviewUiState
}

sealed interface AiChatUiState {
    data object Idle : AiChatUiState
    data object Loading : AiChatUiState
    data class Streaming(val prompt: String, val answer: String) : AiChatUiState
    data class Success(val prompt: String, val answer: String) : AiChatUiState
    data class Error(val prompt: String, val message: String) : AiChatUiState
}

data class WeatherUiState(
    val selectedLocation: CityLocation = CityLocation(
        name = "New York",
        country = "United States",
        stateOrRegion = "New York",
        latitude = 40.7128,
        longitude = -74.0060
    ),
    val weatherReport: FullWeatherReport? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isAccuWeatherMapOpen: Boolean = false,
    val isSearchDialogOpen: Boolean = false,
    val isLanguageSheetOpen: Boolean = false,
    val currentLanguage: AppLanguage = AppLanguage.ENGLISH,
    val tempUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val searchQuery: String = "",
    val searchResults: List<CityLocation> = emptyList(),
    val isSearching: Boolean = false,
    val errorMessage: String? = null,
    val aiOverviewState: AiOverviewUiState = AiOverviewUiState.Idle,
    val aiChatState: AiChatUiState = AiChatUiState.Idle
)

class WeatherViewModel(
    private val repository: WeatherRepository = WeatherRepository(),
    private val aiService: WeatherAiService = WeatherAiService()
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var aiOverviewJob: Job? = null
    private var aiChatJob: Job? = null

    init {
        loadWeatherForCurrentLocation()
    }

    fun loadWeatherForCurrentLocation() {
        val location = _uiState.value.selectedLocation
        fetchWeather(location, isRefresh = false)
    }

    fun refreshWeather() {
        val location = _uiState.value.selectedLocation
        fetchWeather(location, isRefresh = true)
    }

    fun selectLocation(location: CityLocation) {
        _uiState.update {
            it.copy(
                selectedLocation = location,
                isSearchDialogOpen = false,
                searchQuery = "",
                searchResults = emptyList()
            )
        }
        fetchWeather(location, isRefresh = false)
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(350)
            _uiState.update { it.copy(isSearching = true) }
            val results = repository.searchPlaces(query)
            _uiState.update { it.copy(searchResults = results, isSearching = false) }
        }
    }

    fun openSearchDialog() {
        _uiState.update { it.copy(isSearchDialogOpen = true, searchQuery = "", searchResults = emptyList()) }
    }

    fun closeSearchDialog() {
        _uiState.update { it.copy(isSearchDialogOpen = false) }
    }

    fun openLanguageSheet() {
        _uiState.update { it.copy(isLanguageSheetOpen = true) }
    }

    fun closeLanguageSheet() {
        _uiState.update { it.copy(isLanguageSheetOpen = false) }
    }

    /**
     * Updates the app's current language and immediately regenerates the
     * Gemini AI overview in the newly selected language.
     */
    fun setLanguage(language: AppLanguage) {
        _uiState.update { it.copy(currentLanguage = language, isLanguageSheetOpen = false) }
        val report = _uiState.value.weatherReport
        if (report != null) {
            generateAiOverview(language = language)
        }
    }

    fun toggleTempUnit() {
        _uiState.update {
            it.copy(
                tempUnit = if (it.tempUnit == TemperatureUnit.CELSIUS) TemperatureUnit.FAHRENHEIT else TemperatureUnit.CELSIUS
            )
        }
    }

    fun openAccuWeatherMap() {
        _uiState.update { it.copy(isAccuWeatherMapOpen = true) }
    }

    fun closeAccuWeatherMap() {
        _uiState.update { it.copy(isAccuWeatherMapOpen = false) }
    }

    /**
     * Generates an atmospheric AI overview using Gemini 3.8 Flash
     * translated and localized to the specified language.
     */
    fun generateAiOverview(language: AppLanguage = _uiState.value.currentLanguage) {
        val report = _uiState.value.weatherReport ?: return
        val location = _uiState.value.selectedLocation
        val unit = _uiState.value.tempUnit

        aiOverviewJob?.cancel()
        aiOverviewJob = viewModelScope.launch {
            _uiState.update { it.copy(aiOverviewState = AiOverviewUiState.Loading) }
            try {
                val overview = aiService.generateOverview(
                    report = report,
                    location = location,
                    language = language,
                    tempUnit = unit
                )
                _uiState.update { it.copy(aiOverviewState = AiOverviewUiState.Success(overview)) }
                Log.d(TAG, "AI Overview generated successfully in ${language.displayName}")
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "AI Overview generation timed out", e)
                _uiState.update {
                    it.copy(aiOverviewState = AiOverviewUiState.Error("Generation timed out. Tap retry."))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to generate AI overview", e)
                _uiState.update {
                    it.copy(aiOverviewState = AiOverviewUiState.Error(e.localizedMessage ?: "Failed to generate AI overview"))
                }
            }
        }
    }

    /**
     * Sends an interactive question to Gemini with full weather telemetry
     * and streams the answer token-by-token in the user's selected language.
     */
    fun askAiQuestion(question: String) {
        val report = _uiState.value.weatherReport ?: return
        val location = _uiState.value.selectedLocation
        val language = _uiState.value.currentLanguage
        val unit = _uiState.value.tempUnit

        aiChatJob?.cancel()
        aiChatJob = viewModelScope.launch {
            _uiState.update { it.copy(aiChatState = AiChatUiState.Loading) }
            val accumulated = StringBuilder()
            try {
                aiService.askQuestionStream(
                    question = question,
                    report = report,
                    location = location,
                    language = language,
                    tempUnit = unit
                ).collect { chunk ->
                    accumulated.append(chunk)
                    _uiState.update {
                        it.copy(
                            aiChatState = AiChatUiState.Streaming(
                                prompt = question,
                                answer = accumulated.toString()
                            )
                        )
                    }
                }
                _uiState.update {
                    it.copy(
                        aiChatState = AiChatUiState.Success(
                            prompt = question,
                            answer = accumulated.toString()
                        )
                    )
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "AI Q&A streaming timed out", e)
                _uiState.update {
                    it.copy(aiChatState = AiChatUiState.Error(question, "Streaming timed out."))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI Q&A failed", e)
                _uiState.update {
                    it.copy(aiChatState = AiChatUiState.Error(question, e.localizedMessage ?: "Question failed"))
                }
            }
        }
    }

    private fun fetchWeather(location: CityLocation, isRefresh: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                if (isRefresh) it.copy(isRefreshing = true, errorMessage = null)
                else it.copy(isLoading = true, errorMessage = null)
            }

            try {
                val report = repository.getFullWeatherReport(location)
                _uiState.update {
                    it.copy(
                        weatherReport = report,
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = null
                    )
                }
                // Automatically generate AI overview in current language when weather is loaded
                generateAiOverview(_uiState.value.currentLanguage)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = e.message ?: "Failed to load atmospheric data"
                    )
                }
            }
        }
    }
}
