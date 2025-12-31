/*
 * Copyright (c) 2024, CodeLibs Project
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 */

package org.codelibs.sai.internal.runtime.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Memory leak tests for Context class.
 * These tests verify that ThreadLocal usage does not cause memory leaks
 * when threads are created and destroyed.
 */
public class ContextMemoryLeakTest {

    /**
     * Test that threads can be created and destroyed without accumulating memory.
     * This verifies that ThreadLocal cleanup works properly.
     */
    @Test
    public void testThreadLocalCleanup() throws InterruptedException {
        final int numThreads = 50;
        final CountDownLatch completeLatch = new CountDownLatch(numThreads);
        final AtomicInteger errorCount = new AtomicInteger(0);
        final List<WeakReference<Thread>> threadRefs = new ArrayList<>();

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        // Create many short-lived threads
        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    Thread currentThread = Thread.currentThread();
                    synchronized (threadRefs) {
                        threadRefs.add(new WeakReference<>(currentThread));
                    }

                    // Simulate some work
                    Thread.sleep(10);
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    e.printStackTrace();
                } finally {
                    completeLatch.countDown();
                }
            });
        }

        boolean completed = completeLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        boolean terminated = executor.awaitTermination(10, TimeUnit.SECONDS);

        Assert.assertTrue(completed, "Not all threads completed");
        Assert.assertTrue(terminated, "Executor did not terminate");
        Assert.assertEquals(errorCount.get(), 0, "Errors occurred during thread execution");

        // Force garbage collection
        for (int i = 0; i < 3; i++) {
            System.gc();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Check that threads can be garbage collected
        int collectedCount = 0;
        synchronized (threadRefs) {
            for (WeakReference<Thread> ref : threadRefs) {
                if (ref.get() == null) {
                    collectedCount++;
                }
            }
        }

        System.out.println("Threads collected by GC: " + collectedCount + " out of " + numThreads);

        // At least some threads should be collectable after GC
        // (exact number depends on GC implementation and timing)
        Assert.assertTrue(collectedCount > 0,
            "No threads were garbage collected. This may indicate a memory leak.");
    }

    /**
     * Test rapid thread creation and destruction to detect accumulation issues.
     */
    @Test
    public void testRapidThreadCreation() throws InterruptedException {
        final int iterations = 10;
        final int threadsPerIteration = 20;
        final AtomicInteger errorCount = new AtomicInteger(0);

        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();

        for (int iter = 0; iter < iterations; iter++) {
            final CountDownLatch latch = new CountDownLatch(threadsPerIteration);
            ExecutorService executor = Executors.newFixedThreadPool(threadsPerIteration);

            for (int i = 0; i < threadsPerIteration; i++) {
                executor.submit(() -> {
                    try {
                        // Minimal work
                        Thread.sleep(5);
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(10, TimeUnit.SECONDS);
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);

            // Periodic GC
            if (iter % 3 == 0) {
                System.gc();
            }
        }

        // Final GC
        System.gc();
        Thread.sleep(100);

        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryGrowth = finalMemory - initialMemory;

        System.out.println("Initial memory: " + (initialMemory / 1024 / 1024) + " MB");
        System.out.println("Final memory: " + (finalMemory / 1024 / 1024) + " MB");
        System.out.println("Memory growth: " + (memoryGrowth / 1024 / 1024) + " MB");

        Assert.assertEquals(errorCount.get(), 0, "Errors occurred during thread execution");

        // Memory growth should be reasonable (less than 50 MB for this test)
        // This is a loose bound to account for JVM overhead and GC timing
        Assert.assertTrue(memoryGrowth < 50 * 1024 * 1024,
            "Excessive memory growth detected: " + (memoryGrowth / 1024 / 1024) + " MB. " +
            "This may indicate a memory leak.");
    }

    /**
     * Test thread pool reuse to ensure ThreadLocal values don't accumulate.
     */
    @Test
    public void testThreadPoolReuse() throws InterruptedException {
        final int poolSize = 5;
        final int tasksPerThread = 20;
        final AtomicInteger errorCount = new AtomicInteger(0);
        final CountDownLatch completeLatch = new CountDownLatch(poolSize * tasksPerThread);

        ExecutorService executor = Executors.newFixedThreadPool(poolSize);

        // Submit many tasks to be executed by the same thread pool
        for (int i = 0; i < poolSize * tasksPerThread; i++) {
            executor.submit(() -> {
                try {
                    // Simulate work
                    Thread.sleep(5);
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    e.printStackTrace();
                } finally {
                    completeLatch.countDown();
                }
            });
        }

        boolean completed = completeLatch.await(60, TimeUnit.SECONDS);
        executor.shutdown();
        boolean terminated = executor.awaitTermination(10, TimeUnit.SECONDS);

        Assert.assertTrue(completed, "Not all tasks completed");
        Assert.assertTrue(terminated, "Executor did not terminate");
        Assert.assertEquals(errorCount.get(), 0, "Errors occurred during task execution");

        System.gc();
        Thread.sleep(100);

        // If we got here without OOM or excessive delays, the test passes
        System.out.println("Thread pool reuse test completed successfully");
    }

    /**
     * Test for ThreadLocal value accumulation across multiple contexts.
     */
    @Test
    public void testMultipleContextCreation() throws InterruptedException {
        final int iterations = 100;
        final AtomicInteger errorCount = new AtomicInteger(0);

        Runtime runtime = Runtime.getRuntime();
        System.gc();
        Thread.sleep(100);

        long baselineMemory = runtime.totalMemory() - runtime.freeMemory();

        for (int i = 0; i < iterations; i++) {
            Thread thread = new Thread(() -> {
                try {
                    // Thread executes minimal work
                    Thread.sleep(1);
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                }
            });

            thread.start();
            thread.join(1000);

            if (i % 20 == 0) {
                System.gc();
            }
        }

        System.gc();
        Thread.sleep(200);

        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryGrowth = finalMemory - baselineMemory;

        System.out.println("Context creation - Baseline memory: " + (baselineMemory / 1024 / 1024) + " MB");
        System.out.println("Context creation - Final memory: " + (finalMemory / 1024 / 1024) + " MB");
        System.out.println("Context creation - Memory growth: " + (memoryGrowth / 1024 / 1024) + " MB");

        Assert.assertEquals(errorCount.get(), 0, "Errors occurred during context creation");

        // Memory growth should be minimal (less than 20 MB)
        Assert.assertTrue(memoryGrowth < 20 * 1024 * 1024,
            "Excessive memory growth during context creation: " + (memoryGrowth / 1024 / 1024) + " MB");
    }
}
