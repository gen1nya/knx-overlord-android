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

package me.dmdev.rxpm.delegate

import com.nhaarman.mockitokotlin2.*
import io.reactivex.rxjava3.disposables.CompositeDisposable
import me.dmdev.rxpm.PmView
import me.dmdev.rxpm.PresentationModel
import me.dmdev.rxpm.navigation.NavigationMessageDispatcher
import me.dmdev.rxpm.util.SchedulersRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class CommonDelegateTest {

    @get:Rule val schedulers = SchedulersRule()

    private lateinit var pm: PresentationModel
    private lateinit var compositeDisposable: CompositeDisposable
    private lateinit var view: PmView<PresentationModel>
    private lateinit var navigationMessagesDispatcher: NavigationMessageDispatcher
    private lateinit var delegate: CommonDelegate<PresentationModel, PmView<PresentationModel>>

    @Before fun setUp() {
        pm = spy()
        compositeDisposable = mock()
        view = mockView()
        navigationMessagesDispatcher = mock()
        delegate = CommonDelegate(view, navigationMessagesDispatcher)
    }

    private fun mockView(): PmView<PresentationModel> {
        return mock {
            on { providePresentationModel() } doReturn pm
        }
    }

    @Test fun callViewMethods() {

        verify(view, never()).providePresentationModel()
        delegate.onCreate(null)
        verify(view).providePresentationModel()
        assertEquals(pm, delegate.presentationModel)

        verify(view, never()).onBindPresentationModel(pm)
        delegate.onBind()
        verify(view).onBindPresentationModel(pm)

        delegate.onResume()
        delegate.onPause()

        verify(view, never()).onUnbindPresentationModel()
        delegate.onUnbind()
        verify(view).onUnbindPresentationModel()

        delegate.onDestroy()

        verify(view, times(1)).onBindPresentationModel(pm)
        verify(view, times(1)).onUnbindPresentationModel()
        verify(view, times(1)).onUnbindPresentationModel()
    }

    @Test fun changePmLifecycle() {

        val testObserver = pm.lifecycleObservable.test()

        delegate.onCreate(null)
        delegate.onBind()
        delegate.onResume()
        delegate.onPause()
        delegate.onUnbind()
        delegate.onDestroy()

        testObserver.assertValuesOnly(
            PresentationModel.Lifecycle.CREATED,
            PresentationModel.Lifecycle.BINDED,
            PresentationModel.Lifecycle.RESUMED,
            PresentationModel.Lifecycle.PAUSED,
            PresentationModel.Lifecycle.UNBINDED,
            PresentationModel.Lifecycle.DESTROYED
        )
    }

    @Test fun filterRepeatedLifecycleCalls() {

        val testObserver = pm.lifecycleObservable.test()

        delegate.onCreate(null)
        delegate.onCreate(null)
        delegate.onBind()
        delegate.onBind()
        delegate.onResume()
        delegate.onResume()
        delegate.onPause()
        delegate.onPause()
        delegate.onUnbind()
        delegate.onUnbind()
        delegate.onDestroy()
        delegate.onDestroy()

        testObserver.assertValuesOnly(
            PresentationModel.Lifecycle.CREATED,
            PresentationModel.Lifecycle.BINDED,
            PresentationModel.Lifecycle.RESUMED,
            PresentationModel.Lifecycle.PAUSED,
            PresentationModel.Lifecycle.UNBINDED,
            PresentationModel.Lifecycle.DESTROYED
        )
    }
}