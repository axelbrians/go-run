package com.machina.gorun.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.machina.gorun.core.DefaultDispatchers
import com.machina.gorun.data.models.JoggingPoint
import com.machina.gorun.data.models.JoggingResult
import com.machina.gorun.data.repositories.GoRunRepositories
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JoggingPathViewModel @Inject constructor(
    private val repositories: GoRunRepositories,
    private val dispatchers: DefaultDispatchers
): ViewModel() {

    private val _joggingPoints : MutableLiveData<List<JoggingPoint>> = MutableLiveData()
    val joggingPoints : LiveData<List<JoggingPoint>> = _joggingPoints

    private val _joggingResult : MutableLiveData<JoggingResult> = MutableLiveData()
    val joggingResult : LiveData<JoggingResult> = _joggingResult


    fun getJoggingPoints(id: Int) {
        viewModelScope.launch(dispatchers.network) {
            val res = repositories.getJoggingPoints(id)
            _joggingPoints.postValue(res)
        }
    }
}