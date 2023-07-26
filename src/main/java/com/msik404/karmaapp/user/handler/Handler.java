package com.msik404.karmaapp.user.handler;

/**
 * Chain of responsibility
 */
interface Handler<T> {

    public void setNext(Handler<T> updater);

    public void handle(T entity);

}
