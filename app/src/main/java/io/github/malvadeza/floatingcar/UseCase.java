package io.github.malvadeza.floatingcar;


import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;

public abstract class UseCase<Q extends UseCase.RequestValues, P extends UseCase.ResponseValue> {

    private final Scheduler scheduler;

    public UseCase(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public Observable<P> run(Q requestValues) {
        return execute(requestValues)
                .subscribeOn(scheduler)
                .observeOn(AndroidSchedulers.mainThread());
    }

    protected abstract Observable<P> execute(Q requestValues);


    public interface RequestValues {

    }

    public interface ResponseValue {

    }
}
