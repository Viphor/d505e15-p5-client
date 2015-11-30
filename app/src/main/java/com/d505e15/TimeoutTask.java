package com.d505e15;

import java.util.concurrent.*;

/**
 * Created by Klostergaard on 04/11/15.
 */
public class TimeoutTask<T> {
    ExecutorService executer;
    Future<T> future;
    Callable<T> task;

    TimeoutTask(Callable<T> task) {
        this.task = task;

        executer = Executors.newSingleThreadExecutor();
        future = executer.submit(this.task);
    }



    T execute(long runtime, TimeUnit unit) throws TimeoutException {
        try {
            return future.get(runtime, unit);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            // Lazy ass solution
            e.printStackTrace();
            throw new TimeoutException("Could not finish task: " + task);
        }
    }
}
