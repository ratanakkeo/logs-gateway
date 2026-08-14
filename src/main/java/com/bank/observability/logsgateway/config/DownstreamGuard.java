package com.bank.observability.logsgateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.core.functions.CheckedRunnable;
import io.github.resilience4j.retry.Retry;

public final class DownstreamGuard {

    private DownstreamGuard() {
    }

    public static void run(Retry retry, CircuitBreaker circuitBreaker, CheckedRunnable action) {
        CheckedRunnable decorated = Retry.decorateCheckedRunnable(
                retry, CircuitBreaker.decorateCheckedRunnable(circuitBreaker, action));
        try {
            decorated.run();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        } catch (Throwable ex) {
            throw new IllegalStateException(ex);
        }
    }
}
