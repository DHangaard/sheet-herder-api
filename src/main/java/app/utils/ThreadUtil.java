package app.utils;

import app.exceptions.ConcurrentExecutionException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public final class ThreadUtil
{
    private ThreadUtil()
    {
    }

    public static <T> List<T> fetchConcurrently(List<Callable<T>> tasks)
    {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<T> result = new ArrayList<>();

        List<Future<T>> futures = tasks.stream()
                .map(executorService::submit)
                .toList();

        try
        {
            for (Future<T> future : futures)
            {
                result.add(future.get());
            }
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new ConcurrentExecutionException("Failed to fetch data concurrently", e);
        }
        catch (ExecutionException e)
        {
            throw new ConcurrentExecutionException("Failed to fetch data concurrently", e);
        }
        finally
        {
            executorService.shutdown();
        }

        return result;
    }
}
