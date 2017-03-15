package io.github.malvadeza.floatingcar;


public interface BaseView<T extends BasePresenter> {

    void setPresenter(T presenter);

}
