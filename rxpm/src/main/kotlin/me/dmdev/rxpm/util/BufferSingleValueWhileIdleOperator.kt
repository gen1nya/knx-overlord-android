/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2021 Dmitriy Gorbunov (dmitriy.goto@gmail.com)
 *                     and Vasili Chyrvon (vasili.chyrvon@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.dmdev.rxpm.util

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableOperator
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.plugins.RxJavaPlugins


internal class BufferSingleValueWhileIdleOperator<T>(
    private val idleObserver: Observable<Boolean>
) : ObservableOperator<T, T> {

    override fun apply(observer: Observer<in T>): Observer<in T> {
        return ObserverWithBuffer(idleObserver, observer)
    }

    class ObserverWithBuffer<T>(
        private val idleObserver: Observable<Boolean>,
        private val downstream: Observer<in T>
    ) : Observer<T> {

        private val compositeDisposable = CompositeDisposable()
        private var done = false

        private var isIdle = false
        private var bufferedValue: T? = null

        override fun onSubscribe(disposable: Disposable) {

            compositeDisposable.addAll(
                disposable,
                idleObserver.subscribe {
                    if (it) {
                        isIdle = true
                    } else {
                        isIdle = false
                        bufferedValue?.let { value ->
                            onNext(value)
                        }
                        bufferedValue = null
                    }
                }
            )

            downstream.onSubscribe(compositeDisposable)
        }

        override fun onNext(v: T) {
            if (done) {
                return
            }

            if (isIdle) {
                bufferedValue = v
            } else {
                downstream.onNext(v)
            }
        }

        override fun onError(e: Throwable) {
            if (done) {
                RxJavaPlugins.onError(e)
                return
            }
            done = true
            compositeDisposable.dispose()
            downstream.onError(e)
        }

        override fun onComplete() {
            if (done) {
                return
            }

            done = true
            compositeDisposable.dispose()
            downstream.onComplete()
        }
    }
}