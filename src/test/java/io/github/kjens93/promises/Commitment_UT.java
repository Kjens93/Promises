package io.github.kjens93.promises;

import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by kjensen on 11/29/16.
 */
public class Commitment_UT {

    private final Object lock = new Object();


    @Test(timeout = 500)
    public void test_await() {

        Commitment.doNothing().await();

    }

    @Test(timeout = 500)
    public void test_fork() throws InterruptedException {

        Commitment c = () -> {
            Commitment.sleepFor(250, TimeUnit.MILLISECONDS).await();
            synchronized (lock) {
                lock.notify();
            }
        };

        c.async();

        synchronized (lock) {
            lock.wait();
        }

    }

    @Test
    public void test_fork_exception_handler() {

        AtomicReference<Throwable> caught = new AtomicReference<>(null);

        Commitment.throwException(IllegalStateException::new)
                .async(caught::set)
                .await();

        assertThat(caught.get())
                .isNotNull()
                .isInstanceOf(IllegalStateException.class);

    }

    @Test(timeout = 500)
    public void test_fork_null_exception_handler() {

        Commitment.throwException(IllegalStateException::new)
                .async(null)
                .await();

    }

    @Test(timeout = 500)
    public void test_exception_propagation() {

        Commitment c = Commitment.throwException(IllegalStateException::new);

        assertThatThrownBy(c::await)
                .isInstanceOf(IllegalStateException.class);

    }

    @Test(timeout = 5000)
    public void test_await_timeout() {

        Commitment c = Commitment.infiniteLoop();

        assertThatThrownBy(()->c.await(100, TimeUnit.MILLISECONDS))
                .isInstanceOf(TimeoutException.class);

    }

    @Test(timeout = 5000)
    public void test_await_timeout_exception() {

        Commitment c = Commitment.throwException(IllegalStateException::new);

        assertThatThrownBy(()->c.await(100, TimeUnit.MILLISECONDS))
                .isInstanceOf(IllegalStateException.class);

    }

    @Test(timeout = 5000)
    public void test_andThen() {

        AtomicBoolean callbackReached = new AtomicBoolean(false);

        Commitment.doNothing()
                .andThen(() -> callbackReached.set(true))
                .await();

        assertThat(callbackReached.get())
                .isTrue();

    }

    @Test(timeout = 5000)
    public void test_andThen_withExceptionInMainCommitment() {

        AtomicBoolean callbackReached = new AtomicBoolean(false);

        assertThatThrownBy(() -> {
            Commitment.throwException(IllegalStateException::new)
                    .andThen(() -> callbackReached.set(true))
                    .await();
        }).isInstanceOf(IllegalStateException.class);

        assertThat(callbackReached.get())
                .isFalse();

    }

    @Test(timeout = 5000)
    public void test_andThen_withExceptionInCallback() {

        AtomicBoolean callbackReached = new AtomicBoolean(false);

        assertThatThrownBy(() -> {
            Commitment.doNothing()
                    .andThen(() -> {
                        callbackReached.set(true);
                        throw new IllegalStateException();
                    }).await();
        }).isInstanceOf(IllegalStateException.class);

        assertThat(callbackReached.get())
                .isTrue();

    }

}
