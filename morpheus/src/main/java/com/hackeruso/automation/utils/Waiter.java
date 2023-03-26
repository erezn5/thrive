package com.hackeruso.automation.utils;

import org.apache.log4j.Logger;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.awaitility.core.Condition;

import java.util.concurrent.atomic.AtomicReference;

public class Waiter {

    private Waiter() {
    }

    private final static Logger Log = Logger.getLogger(Waiter.class);

    public static <R> R waitCondition(Duration timeout, final Condition<R> condition) {
        return waitCondition(timeout, condition, Duration.TWO_HUNDRED_MILLISECONDS);
    }

    public static <R> R waitCondition(Duration timeout, final Condition<R> condition, Duration interval) {
        final AtomicReference<R> returnValue = new AtomicReference<>();
        try {
            Awaitility.await().
                    ignoreExceptions().
                    pollDelay(Duration.ZERO).
                    atMost(timeout).
                    pollInterval(interval).until(() -> {
                        R result = condition.await();
                        returnValue.set(result);
                        if (result == null) {
                            return false;
                        } else if (result instanceof Boolean) {
                            return (boolean) result;
                        } else {
                            return true;
                        }
                    });
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
        return returnValue.get();
    }

}
