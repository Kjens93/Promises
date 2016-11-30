package io.github.kjens93.promises;

import com.google.common.base.Throwables;

import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static io.github.kjens93.promises.Utils.threadPool;
import static io.github.kjens93.promises.Utils.verifyNotNull;

/**
 * Created by kjensen on 11/29/16.
 */
@FunctionalInterface
public interface Promise<T> extends Commitment {

    T get();

    default T get(long timeout, final TimeUnit unit) throws TimeoutException {
        verifyNotNull(unit);
        try {
            Future<T> future = threadPool.submit((Callable<T>) this::get);
            return future.get(timeout, unit);
        } catch (InterruptedException e) {
            Throwables.propagate(e);
        } catch (ExecutionException e) {
            Throwables.propagate(e.getCause());
        }
        return null;
    }

    @Override
    default Promise<T> async() {
        return async(Throwable::printStackTrace);
    }

    @Override
    default Promise<T> async(final Consumer<Throwable> uncaughtExceptionHandler) {
        Future<T> future = threadPool.submit((Callable<T>) this::get);
        return () -> {
            try {
                return future.get();
            } catch(ExecutionException e) {
                if(uncaughtExceptionHandler != null)
                    uncaughtExceptionHandler.accept(e.getCause());
            } catch (Throwable t) {
                if(uncaughtExceptionHandler != null)
                    uncaughtExceptionHandler.accept(t);
            }
            return null;
        };
    }

    default Promise<T> filter(final Predicate<? super T> predicate) {
        verifyNotNull(predicate);
        return () -> {
            T result = get();
            return predicate.test(result) ? result : null;
        };
    }

    default <R> Promise<R> map(final Function<? super T, ? extends R> mapper) {
        verifyNotNull(mapper);
        return () -> {
            T result = get();
            if(result != null)
                return mapper.apply(result);
            return null;
        };
    }

    default boolean matches(final Predicate<? super T> predicate) {
        verifyNotNull(predicate);
        return filter(predicate).get() != null;
    }

    default boolean isPresent() {
        return get() != null;
    }

    default Promise<T> ifPresent(final Consumer<? super T> action) {
        verifyNotNull(action);
        return () -> {
            T result = get();
            if(result != null)
                action.accept(result);
            return result;
        };
    }

    default T orElse(T t) {
        T result = get();
        return result != null ? result : t;
    }

    default T orElseGet(Supplier<T> supplier) {
        T result = get();
        return result != null ? result : supplier.get();
    }

    default <V extends Throwable> T orElseThrow(Supplier<V> supplier) throws V {
        T result = get();
        if(result != null)
            return result;
        throw supplier.get();
    }

    @Override
    default Promise<T> andThen(final Runnable runnable) {
        verifyNotNull(runnable);
        return () -> {
            T result = get();
            runnable.run();
            return result;
        };
    }

    default Promise<T> andThen(final Consumer<T> consumer) {
        verifyNotNull(consumer);
        return () -> {
            T result = get();
            consumer.accept(result);
            return result;
        };
    }

    @Override
    default void await() {
        get();
    }

    static Promise<?> infiniteLoop() {
        return () -> {
            while(true);
        };
    }

    static Promise<Boolean> doNothing() {
        return () -> true;
    }

    static Promise<?> throwException(Supplier<? extends RuntimeException> supplier) {
        return () -> {
            throw supplier.get();
        };
    }

}
