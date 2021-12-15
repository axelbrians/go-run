package com.machina.gorun.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.machina.gorun.core.DefaultDispatchers
import com.machina.gorun.data.models.JoggingResult
import com.machina.gorun.data.models.JoggingResultDto
import com.machina.gorun.data.models.PastMonthResult
import com.machina.gorun.data.repositories.GoRunRepositories
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.hypot

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repositories: GoRunRepositories,
    private val dispatchers: DefaultDispatchers
): ViewModel() {

    private val _todayJoggingResult: MutableLiveData<PastMonthResult> = MutableLiveData()
    val todayJoggingResult: LiveData<PastMonthResult> = _todayJoggingResult

    private val _joggingResults : MutableLiveData<List<JoggingResult>> = MutableLiveData()
    val joggingResults : LiveData<List<JoggingResult>> = _joggingResults

    private var rawJoggingResult: List<JoggingResult> = listOf()

    fun getTodayJoggingResult() {
        repositories.getTodayJoggingResult()
            .onEach { _todayJoggingResult.postValue(it) }
            .launchIn(viewModelScope)
    }

    fun getJoggingResults() {
        repositories.getJoggingResults()
            .onEach {
                _joggingResults.postValue(it)
                rawJoggingResult = it
            }
            .launchIn(viewModelScope)
    }

    fun searchJoggingResults(query: String) {
        viewModelScope.launch(dispatchers.default) {
            if (query.isBlank()) {
                _joggingResults.postValue(rawJoggingResult)
                return@launch
            }

            val temp = mutableListOf<JoggingResult>()

            rawJoggingResult.forEach { jog ->
                if (jog.timeStamp.lowercase().contains(query)) {
                    temp.add(jog)
                }
            }

            _joggingResults.postValue(temp)
        }
    }

}