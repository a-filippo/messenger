package ru.afilippo.messenger.net;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPool {
    private static ExecutorService executor;
    public static void init(){
        executor = Executors.newFixedThreadPool(100);
    }

    public static void addWork(Runnable work){
        executor.execute(work);
    }
}
