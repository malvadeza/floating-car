package io.github.malvadeza.floatingcar;


public class UseCaseHandler {
    private static UseCaseHandler INSTANCE;

    private final UseCaseScheduler useCaseScheduler;

    public UseCaseHandler(UseCaseScheduler useCaseScheduler) {
        this.useCaseScheduler = useCaseScheduler;
    }

    public <T extends UseCase.RequestValues, R extends UseCase.ResponseValue> void execute(
            final UseCase<T, R> usecase, T values, UseCase.UseCaseCallback<R> callback) {

    }

    public <V extends UseCase.ResponseValue> void notifyResponse(final V response,
            final UseCase.UseCaseCallback<V> useCaseCallback) {
        useCaseScheduler.notifyResponse(response, useCaseCallback);
    }

    public <V extends UseCase.ResponseValue> void notifyError(
            final UseCase.UseCaseCallback<V> useCaseCallback) {
        useCaseScheduler.onError(useCaseCallback);
    }

    public static UseCaseHandler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UseCaseHandler(new UseCaseThreadPoolScheduler());
        }

        return INSTANCE;
    }

}
