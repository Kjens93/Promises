package io.github.kjens93.promises;

import com.google.common.base.Throwables;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.github.kjens93.promises.Utils.verifyNotNull;

/**
 * Created by kjensen on 11/29/16.
 */
@FunctionalInterface
public interface Commitment {

    void await();

    default Commitment async() {
        return async(Throwable::printStackTrace);
    }

    default Commitment async(final Consumer<Throwable> uncaughtExceptionHandler) {
        Promise<?> promise = () -> {
            this.await();
            return true;
        };
        return promise.async(uncaughtExceptionHandler);
    }

    default void await(long timeout, final TimeUnit unit) throws TimeoutException {
        Promise<?> promise = () -> {
            this.await();
            return true;
        };
        promise.get(timeout, unit);
    }

    default Commitment andThen(final Runnable runnable) {
        verifyNotNull(runnable);
        return () -> {
            await();
            runnable.run();
        };
    }

    static Commitment doNothing() {
        return () -> {};
    }

    static Commitment infiniteLoop() {
        return () -> {
            while(true);
        };
    }

    static Commitment sleepFor(long timeout, final TimeUnit unit) {
        verifyNotNull(unit);
        return () -> {
            try {
                Thread.sleep(unit.toMillis(timeout));
            } catch (InterruptedException e) {
                Throwables.propagate(e);
            }
        };
    }

    static Commitment throwException(Supplier<? extends RuntimeException> supplier) {
        return () -> {
            throw supplier.get();
        };
    }

}
