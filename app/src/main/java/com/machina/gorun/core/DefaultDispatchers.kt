package com.machina.gorun.core

import kotlinx.coroutines.Dispatchers

class DefaultDispatchers {
    val main = Dispatchers.Main
    val default = Dispatchers.Default
    val network = Dispatchers.IO
}