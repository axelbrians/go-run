package com.machina.gorun.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.machina.gorun.data.models.JoggingResult
import com.machina.gorun.data.repositories.GoRunRepositories
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repositories: GoRunRepositories
): ViewModel() {

    private val _joggingResults : MutableLiveData<List<JoggingResult>> = MutableLiveData()
    val joggingResults : LiveData<List<JoggingResult>> = _joggingResults

    fun getJoggingResults() {
        repositories.getJoggingResults()
            .onEach { _joggingResults.postValue(it) }
            .launchIn(viewModelScope)
    }


}