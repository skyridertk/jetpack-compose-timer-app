/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androiddevchallenge

import android.os.Bundle
import android.os.CountDownTimer
import android.text.format.DateUtils
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.androiddevchallenge.ui.theme.MyTheme
import kotlin.math.roundToLong

class MainActivity : AppCompatActivity() {

    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyTheme {
                MyApp()
            }
        }
    }
}

class CounterViewModel : ViewModel() {

    private lateinit var mCountDownTimer: CountDownTimer

    private val _mTimeLeftInMillis = MutableLiveData<Long>(COUNT_TIME)

    private val _formattedTime = MutableLiveData<String>(formatDuration((COUNT_TIME.toFloat() / 1000.0).roundToLong()))
    val formattedTime: LiveData<String>
        get() = _formattedTime

    private val _startedTimer = MutableLiveData<Boolean>(false)
    val startedTimer: LiveData<Boolean>
        get() = _startedTimer

    private val _inPause = MutableLiveData<Boolean>(false)
    val inPause: LiveData<Boolean>
        get() = _inPause

    private fun customTimer(stopTimer: Long) {
        mCountDownTimer = object : CountDownTimer(stopTimer, 1) {

            override fun onTick(millisUntilFinished: Long) {

                _mTimeLeftInMillis.postValue(millisUntilFinished)

                _formattedTime.postValue(formatDuration((millisUntilFinished.toFloat() / 1000.0).roundToLong()))
            }

            override fun onFinish() {
                _mTimeLeftInMillis.postValue(COUNT_TIME)
                _formattedTime.postValue(formatDuration((COUNT_TIME.toFloat() / 1000.0).roundToLong()))
                _startedTimer.postValue(false)
            }
        }.start()
    }

    fun startTimer() {
        customTimer(_mTimeLeftInMillis.value!!)

        _startedTimer.postValue(true)
        _inPause.postValue(false)
    }

    fun pauseTimer() {
        if (_startedTimer.value == true) {
            mCountDownTimer.cancel()
        }

        _startedTimer.postValue(false)
        _inPause.postValue(true)
    }

    fun resetTimer() {
        if (_startedTimer.value == true) {
            mCountDownTimer.cancel()
        }

        _mTimeLeftInMillis.postValue(COUNT_TIME)
        _formattedTime.postValue(formatDuration((COUNT_TIME.toFloat() / 1000.0).roundToLong()))
        _startedTimer.postValue(false)
        _inPause.postValue(false)
    }

    fun formatDuration(seconds: Long): String = if (seconds < 60) {
        seconds.toString() + "s"
    } else {
        DateUtils.formatElapsedTime(seconds)
    }
}

@ExperimentalAnimationApi
@Composable
fun MyApp(counterViewModel: CounterViewModel = CounterViewModel()) {
    val startedTimer by counterViewModel.startedTimer.observeAsState()
    val formattedTime by counterViewModel.formattedTime.observeAsState()
    val inPause by counterViewModel.inPause.observeAsState()

    val infiniteTransition = rememberInfiniteTransition()

    MainContent(
        infiniteTransition = infiniteTransition,
        formattedTime = formattedTime,
        inPause = inPause,
        startedTimer = startedTimer,
        startTimer = { counterViewModel.startTimer() },
        pauseTimer = { counterViewModel.pauseTimer() },
        resetTimer = { counterViewModel.resetTimer() }
    )
}

// Suku

// Start building your app here!
@ExperimentalAnimationApi
@Composable
fun MainContent(
    infiniteTransition: InfiniteTransition,
    inPause: Boolean?,
    formattedTime: String?,
    startedTimer: Boolean?,
    startTimer: () -> Unit,
    pauseTimer: () -> Unit,
    resetTimer: () -> Unit,
) {

    val alpha = if (inPause == true) {
        val computedAplha by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0.4f,
            animationSpec = infiniteRepeatable(

                animation = keyframes {
                    durationMillis = 1000
                    0.7f at 500
                },

                repeatMode = RepeatMode.Reverse
            )
        )
        computedAplha
    } else {
        1f
    }

    Surface(color = MaterialTheme.colors.background) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "$formattedTime", color = MaterialTheme.colors.primaryVariant.copy(alpha = alpha), fontSize = 60.sp)
            Row() {
                AnimatedVisibility(startedTimer == false) {
                    Button(
                        onClick = {
                            startTimer()
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.onPrimary)
                    ) {
                        Text("Start", color = MaterialTheme.colors.primary)
                    }
                }
                AnimatedVisibility(startedTimer == true) {
                    Button(
                        onClick = {
                            pauseTimer()
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
                    ) {
                        Text("Stop", color = MaterialTheme.colors.primary)
                    }
                }

                Spacer(modifier = Modifier.size(20.dp))
                Button(
                    onClick = {
                        resetTimer()
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.onSecondary)
                ) {
                    Text("Reset", color = MaterialTheme.colors.primary)
                }
            }
        }
    }
}

// @ExperimentalAnimationApi
// @Preview("Light Theme", widthDp = 360, heightDp = 640)
// @Composable
// fun LightPreview() {
//    MyTheme {
//        MyApp()
//    }
// }

// @ExperimentalAnimationApi
// @Preview("Dark Theme", widthDp = 360, heightDp = 640)
// @Composable
// fun DarkPreview() {
//    MyTheme(darkTheme = true) {
//        MyApp()
//    }
// }
