package com.machina.gorun.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.machina.gorun.data.models.Point
import com.machina.gorun.data.repositories.GoRunRepositories
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TrackingViewModel @Inject constructor(
    private val goRunRepositories: GoRunRepositories,
    
): ViewModel() {
    
    private val _currentPoint : MutableLiveData<Point> = MutableLiveData()
    val currentPoint : LiveData<Point> = _currentPoint

    fun getLivePoint() {
        goRunRepositories.getPoints()
            .onEach { points ->
                if (points.isNotEmpty()) {
                    _currentPoint.postValue(points.last())
                }
            }.launchIn(viewModelScope)
    }


}