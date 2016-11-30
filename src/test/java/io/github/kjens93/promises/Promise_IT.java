package io.github.kjens93.promises;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by kjensen on 11/29/16.
 */
public class Promise_IT {


    @Test(timeout = 500)
    public void test_await() throws InterruptedException {

        assertThat(Promise.doNothing().get()).isTrue();

    }

    @Test(timeout = 500)
    public void test_fork() throws InterruptedException {

        Promise<Boolean> p = () -> {
            Commitment.sleepFor(250, TimeUnit.MILLISECONDS).await();
            return true;
        };

        p.async();

    }

    @Test(timeout = 500)
    public void test_fork_exception_handler() {

        AtomicReference<Throwable> caught = new AtomicReference<>(null);

        Object result = Promise.throwException(IllegalStateException::new)
                .async(caught::set)
                .get();

        assertThat(caught.get())
                .isNotNull()
                .isInstanceOf(IllegalStateException.class);

        assertThat(result).isNull();

    }

    @Test(timeout = 500)
    public void test_fork_null_exception_handler() {

        Promise.throwException(IllegalStateException::new)
                .async(null)
                .get();

    }

    @Test(timeout = 500)
    public void test_exception_propagation() {

        Promise<?> p = Promise.throwException(IllegalStateException::new);

        assertThatThrownBy(p::get)
                .isInstanceOf(IllegalStateException.class);

    }

    @Test(timeout = 5000)
    public void test_get_timeout() throws TimeoutException {

        Promise<?> p = Promise.infiniteLoop();

        assertThatThrownBy(()->p.get(100, TimeUnit.MILLISECONDS))
                .isInstanceOf(TimeoutException.class);

    }

    @Test(timeout = 5000)
    public void test_get_timeout_exception() {

        Promise<?> p = Promise.throwException(IllegalStateException::new);

        assertThatThrownBy(()->p.get(100, TimeUnit.MILLISECONDS))
                .isInstanceOf(IllegalStateException.class);

    }

    @Test(timeout = 5000)
    public void test_andThen() {

        AtomicBoolean callbackReached = new AtomicBoolean(false);

        Promise.doNothing()
                .andThen(() -> callbackReached.set(true))
                .await();

        assertThat(callbackReached.get())
                .isTrue();

    }

    @Test(timeout = 5000)
    public void test_ifPresent() {

        Promise<AtomicBoolean> p = AtomicBoolean::new;

        AtomicBoolean r = p.ifPresent(l -> l.set(true)).get();

        assertThat(r.get()).isTrue();

    }

    @Test(timeout = 5000)
    public void test_filter() {

        Promise<List<Integer>> p = () -> Arrays.asList(1,2,3,4,5);

        List<Integer> result1 = p.filter(List::isEmpty).get();

        List<Integer> result2 = p.filter(l -> l.size() == 5).get();

        assertThat(result1).isNull();

        assertThat(result2).containsExactly(1,2,3,4,5);

    }

    @Test(timeout = 5000)
    public void test_matches() {

        Promise<List<Integer>> p = () -> Arrays.asList(1,2,3,4,5);

        assertThat(p.matches(List::isEmpty)).isFalse();

        assertThat(p.matches(l -> l.size() == 5)).isTrue();

    }

    @Test(timeout = 5000)
    public void test_isPresent() {

        Promise<List<Integer>> p1 = () -> null;

        assertThat(p1.isPresent()).isFalse();

        Promise<List<Integer>> p2 = ArrayList::new;

        assertThat(p2.isPresent()).isTrue();

    }

    @Test(timeout = 5000)
    public void test_orElse() {

        Promise<List<Integer>> p = () -> null;

        List<Integer> list = new ArrayList<>();

        assertThat(p.orElse(list)).isEqualTo(list);

    }

    @Test(timeout = 5000)
    public void test_orElseGet() {

        Promise<List<Integer>> p = () -> null;

        List<Integer> list = new ArrayList<>();

        assertThat(p.orElseGet(() -> list)).isEqualTo(list);

    }

    @Test(timeout = 5000)
    public void test_orElseThrow() {

        Promise<List<Integer>> p = () -> null;

        assertThatThrownBy(() -> p.orElseThrow(IllegalStateException::new))
                .isInstanceOf(IllegalStateException.class);

    }

    @Test(timeout = 5000)
    public void test_map() {

        Promise<List<Integer>> p = () -> Arrays.asList(1,2,3,4,5);

        List<String> result = p.map(l -> {
                    return l.stream()
                            .map(i -> i + "")
                            .collect(toList());
                }).get();

        assertThat(result)
                .containsExactly("1","2","3","4","5");

    }

    @Test(timeout = 5000)
    public void test_ifPresent_withExceptionInMainCommitment() {

        Promise<AtomicBoolean> p = () -> {
            throw new IllegalStateException();
        };

        p = p.ifPresent(l -> l.set(true));

        assertThatThrownBy(p::get)
                .isInstanceOf(IllegalStateException.class);

    }

    @Test(timeout = 5000)
    public void test_ifPresent_withExceptionInIfPresent() {

        AtomicBoolean r = new AtomicBoolean(false);

        assertThatThrownBy(() -> {
            Promise<AtomicBoolean> p = AtomicBoolean::new;
            AtomicBoolean result = p.ifPresent(l -> {
                l.set(true);
                throw new IllegalStateException();
            }).get();
            r.set(result.get());
        }).isInstanceOf(IllegalStateException.class);

        assertThat(r.get()).isFalse();

    }

    @Test(timeout = 5000)
    public void test_ifPresent_andThen() {

        AtomicBoolean callbackReached = new AtomicBoolean(false);

        Promise<AtomicBoolean> p = AtomicBoolean::new;

        AtomicBoolean r = p.ifPresent(l -> l.set(true))
                .andThen(() -> callbackReached.set(true))
                .get();

        assertThat(r.get()).isTrue();

        assertThat(callbackReached.get())
                .isTrue();

    }

    @Test(timeout = 5000)
    public void test_andThen_withExceptionInMainCommitment() {

        AtomicBoolean callbackReached = new AtomicBoolean(false);

        Promise<?> p = Promise.throwException(IllegalStateException::new)
                .andThen(() -> callbackReached.set(true));

        assertThatThrownBy(p::await)
                .isInstanceOf(IllegalStateException.class);

        assertThat(callbackReached.get())
                .isFalse();

    }

    @Test(timeout = 5000)
    public void test_andThen_withExceptionInCallback() {

        AtomicBoolean callbackReached = new AtomicBoolean(false);

        assertThatThrownBy(() -> {
            Promise.doNothing()
                    .andThen(() -> {
                        callbackReached.set(true);
                        throw new IllegalStateException();
                    }).await();
        }).isInstanceOf(IllegalStateException.class);

        assertThat(callbackReached.get())
                .isTrue();

    }

}
