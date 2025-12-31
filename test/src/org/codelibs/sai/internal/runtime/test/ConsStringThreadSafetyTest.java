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

import org.codelibs.sai.internal.runtime.ConsString;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread safety tests for ConsString class.
 * These tests verify that concurrent operations on ConsString do not cause
 * race conditions or data corruption.
 */
public class ConsStringThreadSafetyTest {

    /**
     * Test concurrent flattening of the same ConsString from multiple threads.
     * This verifies that the synchronized flattened() method prevents race conditions.
     */
    @Test
    public void testConcurrentFlattening() throws InterruptedException {
        final int numThreads = 10;
        final int iterations = 100;

        for (int iter = 0; iter < iterations; iter++) {
            // Create a deeply nested ConsString
            String part1 = "Hello";
            String part2 = "World";
            ConsString cs = new ConsString(part1, part2);

            // Create more nesting
            for (int i = 0; i < 5; i++) {
                cs = new ConsString(cs, Integer.toString(i));
            }

            final ConsString finalCs = cs;
            final CountDownLatch startLatch = new CountDownLatch(1);
            final CountDownLatch completeLatch = new CountDownLatch(numThreads);
            final List<String> results = new ArrayList<>();
            final AtomicInteger errorCount = new AtomicInteger(0);

            // Create threads that will all try to flatten at the same time
            ExecutorService executor = Executors.newFixedThreadPool(numThreads);

            for (int i = 0; i < numThreads; i++) {
                executor.submit(() -> {
                    try {
                        // Wait for signal to start
                        startLatch.await();

                        // Flatten the string
                        String result = finalCs.toString();

                        synchronized (results) {
                            results.add(result);
                        }
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                        e.printStackTrace();
                    } finally {
                        completeLatch.countDown();
                    }
                });
            }

            // Start all threads at once
            startLatch.countDown();

            // Wait for completion
            boolean completed = completeLatch.await(10, TimeUnit.SECONDS);
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);

            Assert.assertTrue(completed, "Test timed out waiting for threads to complete");
            Assert.assertEquals(errorCount.get(), 0, "Errors occurred during concurrent flattening");
            Assert.assertEquals(results.size(), numThreads, "Not all threads completed");

            // All results should be identical
            String expected = results.get(0);
            for (String result : results) {
                Assert.assertEquals(result, expected, "Flattened strings are not identical");
            }
        }
    }

    /**
     * Test concurrent calls to charAt() which internally calls flattened().
     */
    @Test
    public void testConcurrentCharAt() throws InterruptedException {
        final int numThreads = 10;
        final String left = "ABC";
        final String right = "DEF";
        final ConsString cs = new ConsString(left, right);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch completeLatch = new CountDownLatch(numThreads);
        final AtomicInteger errorCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    startLatch.await();

                    // Access characters from different positions
                    for (int j = 0; j < cs.length(); j++) {
                        char c = cs.charAt(j);
                        char expected = (left + right).charAt(j);
                        if (c != expected) {
                            System.err.println("Thread " + threadId + ": charAt(" + j + ") returned '" +
                                             c + "' but expected '" + expected + "'");
                            errorCount.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    e.printStackTrace();
                } finally {
                    completeLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean completed = completeLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        Assert.assertTrue(completed, "Test timed out");
        Assert.assertEquals(errorCount.get(), 0, "Errors occurred during concurrent charAt operations");
    }

    /**
     * Test concurrent calls to getComponents() and flattening.
     */
    @Test
    public void testConcurrentGetComponentsAndFlatten() throws InterruptedException {
        final int numThreads = 10;
        final String left = "Left";
        final String right = "Right";
        final ConsString cs = new ConsString(left, right);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch completeLatch = new CountDownLatch(numThreads);
        final AtomicInteger errorCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    startLatch.await();

                    if (threadId % 2 == 0) {
                        // Even threads call getComponents
                        CharSequence[] components = cs.getComponents();
                        Assert.assertNotNull(components, "getComponents returned null");
                        Assert.assertEquals(components.length, 2, "getComponents returned wrong length");
                    } else {
                        // Odd threads call toString (which flattens)
                        String result = cs.toString();
                        Assert.assertEquals(result, left + right, "toString returned wrong value");
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    e.printStackTrace();
                } finally {
                    completeLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean completed = completeLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        Assert.assertTrue(completed, "Test timed out");
        Assert.assertEquals(errorCount.get(), 0, "Errors occurred during concurrent operations");
    }

    /**
     * Test with deeply nested ConsStrings to ensure no stack overflow or deadlock.
     */
    @Test
    public void testDeeplyNestedConsString() throws InterruptedException {
        final int depth = 100;
        ConsString cs = new ConsString("0", "1");

        for (int i = 2; i < depth; i++) {
            cs = new ConsString(cs, Integer.toString(i));
        }

        final ConsString finalCs = cs;
        final int numThreads = 5;
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch completeLatch = new CountDownLatch(numThreads);
        final AtomicInteger errorCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();

                    // This should not cause stack overflow or deadlock
                    String result = finalCs.toString();
                    Assert.assertNotNull(result, "toString returned null");
                    Assert.assertTrue(result.length() > 0, "toString returned empty string");
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    e.printStackTrace();
                } finally {
                    completeLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean completed = completeLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        Assert.assertTrue(completed, "Test timed out");
        Assert.assertEquals(errorCount.get(), 0, "Errors occurred with deeply nested ConsString");
    }
}
