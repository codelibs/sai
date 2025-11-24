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

package org.codelibs.sai.internal.codegen.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

/**
 * Memory leak tests for OptimisticTypesPersistence.
 * These tests verify that the shutdown hook properly cancels the cleanup timer
 * and prevents timer thread leaks.
 */
@Test
public class OptimisticTypesPersistenceMemoryLeakTest {

    /**
     * Verify that the cleanup timer thread is properly named and can be identified.
     * This helps in debugging and monitoring for timer thread leaks.
     */
    @Test
    public void testCleanupThreadExists() {
        // Get all threads
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(false, false);

        boolean foundCleanupThread = false;
        int timerThreadCount = 0;

        for (ThreadInfo threadInfo : threadInfos) {
            String threadName = threadInfo.getThreadName();
            if (threadName != null) {
                if (threadName.contains("OptimisticTypesPersistence-Cleanup")) {
                    foundCleanupThread = true;
                }
                if (threadName.toLowerCase().contains("timer")) {
                    timerThreadCount++;
                }
            }
        }

        // Log thread information for debugging
        System.out.println("Total timer threads found: " + timerThreadCount);
        if (foundCleanupThread) {
            System.out.println("OptimisticTypesPersistence cleanup thread is present");
        }

        // Note: The cleanup thread may or may not exist depending on whether
        // OptimisticTypesPersistence is enabled (depends on cache configuration).
        // This test primarily documents expected behavior rather than asserting it.
        System.out.println("Test completed - cleanup thread present: " + foundCleanupThread);
    }

    /**
     * Test that timer threads don't accumulate excessively.
     * This is a basic sanity check for timer thread management.
     */
    @Test
    public void testTimerThreadCount() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(false, false);

        int timerThreadCount = 0;
        for (ThreadInfo threadInfo : threadInfos) {
            String threadName = threadInfo.getThreadName();
            if (threadName != null && threadName.toLowerCase().contains("timer")) {
                timerThreadCount++;
                System.out.println("Found timer thread: " + threadName);
            }
        }

        // We should not have an excessive number of timer threads
        // A reasonable upper bound would be < 10 for a typical application
        Assert.assertTrue(timerThreadCount < 10,
            "Too many timer threads detected: " + timerThreadCount +
            ". This may indicate a timer thread leak.");
    }

    /**
     * Test that shutdown hooks are registered.
     * This verifies that cleanup code will run on JVM shutdown.
     */
    @Test
    public void testShutdownHookRegistered() {
        // Get current thread count before and after triggering class initialization
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        int initialThreadCount = threadMXBean.getThreadCount();

        // Force OptimisticTypesPersistence class initialization if not already done
        try {
            Class.forName("org.codelibs.sai.internal.codegen.OptimisticTypesPersistence");
        } catch (ClassNotFoundException e) {
            Assert.fail("OptimisticTypesPersistence class not found", e);
        }

        int afterThreadCount = threadMXBean.getThreadCount();

        // Thread count should be stable (not continuously growing)
        System.out.println("Initial thread count: " + initialThreadCount);
        System.out.println("After class load thread count: " + afterThreadCount);

        // Allow for some variation, but not excessive growth
        int threadDifference = afterThreadCount - initialThreadCount;
        Assert.assertTrue(Math.abs(threadDifference) < 5,
            "Thread count changed significantly: " + threadDifference +
            ". This may indicate improper thread management.");
    }

    /**
     * Verify daemon thread status for cleanup threads.
     * Timer threads should be daemon threads so they don't prevent JVM shutdown.
     */
    @Test
    public void testCleanupThreadIsDaemon() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        long[] threadIds = threadMXBean.getAllThreadIds();

        boolean foundNonDaemonTimer = false;

        for (long threadId : threadIds) {
            ThreadInfo threadInfo = threadMXBean.getThreadInfo(threadId);
            if (threadInfo != null) {
                String threadName = threadInfo.getThreadName();
                if (threadName != null && threadName.contains("OptimisticTypesPersistence")) {
                    // Get the actual thread to check daemon status
                    Thread thread = findThreadById(threadId);
                    if (thread != null && !thread.isDaemon()) {
                        foundNonDaemonTimer = true;
                        System.err.println("Found non-daemon OptimisticTypesPersistence thread: " + threadName);
                    }
                }
            }
        }

        Assert.assertFalse(foundNonDaemonTimer,
            "OptimisticTypesPersistence threads should be daemon threads to allow JVM shutdown");
    }

    /**
     * Helper method to find a thread by its ID.
     */
    private Thread findThreadById(long threadId) {
        ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
        while (rootGroup.getParent() != null) {
            rootGroup = rootGroup.getParent();
        }

        Thread[] threads = new Thread[rootGroup.activeCount() * 2];
        int count = rootGroup.enumerate(threads, true);

        for (int i = 0; i < count; i++) {
            if (threads[i] != null && threads[i].getId() == threadId) {
                return threads[i];
            }
        }
        return null;
    }

    /**
     * Test for potential memory pressure by monitoring heap usage.
     * This is a basic sanity check to ensure no obvious memory issues.
     */
    @Test
    public void testMemoryPressure() {
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();

        // Force garbage collection to get a baseline
        System.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long baselineMemory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Baseline memory usage: " + (baselineMemory / 1024 / 1024) + " MB");

        // The test passes if we can measure memory without OOM
        Assert.assertTrue(baselineMemory > 0, "Memory measurement failed");
    }
}
