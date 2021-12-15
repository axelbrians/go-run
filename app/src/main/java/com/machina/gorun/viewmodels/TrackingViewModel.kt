package com.machina.gorun.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.machina.gorun.core.MyTimeHelper
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

    private val _timeElapsed : MutableLiveData<String> = MutableLiveData()
    val timeElapsed : LiveData<String> = _timeElapsed


    fun getLivePoint() {
        goRunRepositories.getPoints()
            .onEach { points ->
                if (points.isNotEmpty()) {
                    _currentPoint.postValue(points.last())

                    val temp = MyTimeHelper.formatMillisToMMSS(
                        points.last().time -
                                points.first().time)
                    _timeElapsed.postValue(temp)
                } else {
                    _timeElapsed.postValue("00:00")
                }
            }.launchIn(viewModelScope)
    }


}