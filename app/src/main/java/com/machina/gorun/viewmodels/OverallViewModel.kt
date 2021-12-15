package com.machina.gorun.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.machina.gorun.data.models.PastMonthResult
import com.machina.gorun.data.repositories.GoRunRepositories
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class OverallViewModel @Inject constructor(
    private val repositories: GoRunRepositories
): ViewModel() {

    private val _monthActivities : MutableLiveData<PastMonthResult> = MutableLiveData()
    val monthActivities : LiveData<PastMonthResult> = _monthActivities

    fun getPastMonthJoggingResult() {
        repositories.getPastMonthJoggingResult()
            .onEach { _monthActivities.postValue(it) }
            .launchIn(viewModelScope)
    }
}