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

package me.dmdev.rxpm

import android.annotation.SuppressLint
import com.jakewharton.rxrelay3.BehaviorRelay
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers
import me.dmdev.rxpm.util.BufferSingleValueWhileIdleOperator

/**
 * Reactive property for the [view's][PmView] state.
 * Can be observed and changed in reactive manner with it's [observable] and [PresentationModel.consumer].
 *
 * Use to represent a view state. It can be something simple, like some widget's text, or complex,
 * like inProgress or data.
 *
 * @param [initialValue] initial value.
 * @param [diffStrategy] diff strategy.
 *
 * @see Action
 * @see Command
 */
class State<T> internal constructor(
    internal val pm: PresentationModel,
    initialValue: T? = null,
    private val diffStrategy: DiffStrategy<T>? = null
) {

    private val behaviorRelay: BehaviorRelay<T>

    internal val relay =
        if (initialValue != null) {
            behaviorRelay = BehaviorRelay.createDefault<T>(initialValue)
            behaviorRelay.toSerialized()
        } else {
            behaviorRelay = BehaviorRelay.create<T>()
            behaviorRelay.toSerialized()
        }

    /**
     * Observable of this [State].
     */
    val observable: Observable<T>
        get() {
            return if (diffStrategy != null) {
                if (diffStrategy.computeAsync()) {
                    relay
                        .observeOn(Schedulers.computation())
                        .distinctUntilChanged(diffStrategy::areTheSame)
                } else {
                    relay.distinctUntilChanged(diffStrategy::areTheSame)
                }
            } else {
                relay.asObservable()
            }
        }

    /**
     * Returns a current value.
     * @throws UninitializedPropertyAccessException if there is no value and [State] was created without `initialValue`.
     */
    val value: T
        get() {
            return behaviorRelay.value
                ?: throw UninitializedPropertyAccessException("The State has no value yet. Use valueOrNull() or pass initialValue to the constructor.")
        }

    /**
     * Returns a current value or null.
     */
    val valueOrNull: T? get() = behaviorRelay.value


    /**
     * Returns true if the [State] has any value.
     */
    fun hasValue() = behaviorRelay.hasValue()
}

private val UNPROCESSED_ERROR_CONSUMER = Consumer<Throwable> { throwable ->
    throw IllegalStateException(
        "Unprocessed error encountered in the State. " +
                "State accepts only emitted items, so you need to process errors yourself.",
        throwable
    )
}

/**
 * Creates the [State].
 * Optionally subscribes to the provided [state source][stateSource] and
 * unsubscribes from it ON [DESTROY][PresentationModel.Lifecycle.DESTROYED].
 *
 * @param [initialValue] initial value.
 * @param [diffStrategy] diff strategy.
 * @param [stateSource] source of the state.
 */
@SuppressLint("CheckResult")
@Suppress("UNCHECKED_CAST")
fun <T> PresentationModel.state(
    initialValue: T? = null,
    diffStrategy: DiffStrategy<T>? = DiffByEquals as DiffStrategy<T>,
    stateSource: (() -> Observable<T>)? = null
): State<T> {

    val state = State(pm = this, initialValue = initialValue, diffStrategy = diffStrategy)

    if (stateSource != null) {
        lifecycleObservable
            .filter { it == PresentationModel.Lifecycle.CREATED }
            .take(1)
            .subscribe {
                stateSource.let { source ->
                    source()
                        .subscribe(state.relay, UNPROCESSED_ERROR_CONSUMER)
                        .untilDestroy()
                }
            }
    }

    return state
}

/**
 * Subscribes to the [State][State] and adds it to the subscriptions list
 * that will be CLEARED ON [UNBIND][PresentationModel.Lifecycle.UNBINDED],
 * so use it ONLY in [PmView.onBindPresentationModel].
 */
infix fun <T> State<T>.bindTo(consumer: Consumer<in T>) {
    with(pm) {
        this@bindTo.observable
            .observeOn(AndroidSchedulers.mainThread())
            .lift(BufferSingleValueWhileIdleOperator(paused))
            .subscribe(consumer)
            .untilUnbind()
    }
}

/**
 * Subscribes to the [State][State] and adds it to the subscriptions list
 * that will be CLEARED ON [UNBIND][PresentationModel.Lifecycle.UNBINDED],
 * so use it ONLY in [PmView.onBindPresentationModel].
 */
infix fun <T> State<T>.bindTo(consumer: (T) -> Unit) {
    with(pm) {
        this@bindTo.observable
            .observeOn(AndroidSchedulers.mainThread())
            .lift(BufferSingleValueWhileIdleOperator(paused))
            .subscribe(consumer)
            .untilUnbind()
    }
}

/**
 * Describes a strategy for comparing old and new values.
 *
 * It is used to optimize updates of the state[State] to avoid unnecessary UI redrawing.
 *
 * @see [DiffByEquals]
 * @see [DiffByReference]
 */
interface DiffStrategy<T> {

    /**
     * Compares the old and the new values.
     * @return true if both values ​​are identical or false if they are different.
     */
    fun areTheSame(new: T, old: T): Boolean

    /**
     * Defines a diff calculation on [the main thread][AndroidSchedulers.mainThread] or on [a computation thread][Schedulers.computation].
     */
    fun computeAsync(): Boolean
}

object DiffByEquals : DiffStrategy<Any> {

    override fun areTheSame(new: Any, old: Any) = new == old

    override fun computeAsync() = false
}

object DiffByReference : DiffStrategy<Any> {

    override fun areTheSame(new: Any, old: Any) = new === old

    override fun computeAsync() = false
}