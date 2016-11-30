package io.github.kjens93.promises;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by kjensen on 11/29/16.
 */
interface Utils {

    ExecutorService threadPool = Executors.newCachedThreadPool();

    static void verifyNotNull(final Object o) {
        if(o == null) throw new NullPointerException();
    }

}
