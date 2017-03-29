package io.github.malvadeza.floatingcar


import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers

abstract class UseCase<Q : UseCase.RequestValues, P : UseCase.ResponseValue>(private val scheduler: Scheduler) {

    fun run(requestValues: Q?): Observable<P> {
        return execute(requestValues)
                .subscribeOn(scheduler)
                .observeOn(AndroidSchedulers.mainThread())
    }

    protected abstract fun execute(requestValues: Q?): Observable<P>


    interface RequestValues

    interface ResponseValue
}
