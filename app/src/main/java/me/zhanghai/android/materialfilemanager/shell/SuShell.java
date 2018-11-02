/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.shell;

import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import eu.chainfire.libsuperuser.Debug;
import me.zhanghai.android.materialfilemanager.BuildConfig;
import me.zhanghai.android.materialfilemanager.util.Holder;
import me.zhanghai.android.materialfilemanager.util.StringCompat;

public class SuShell {

    @Nullable
    private static HandlerThread sHandlerThread;
    @Nullable
    private static Shell sShell;
    private static final Object sLock = new Object();

    static {
        if (BuildConfig.DEBUG) {
            Debug.setDebug(true);
        }
    }

    private SuShell() {}

    private static boolean isOpenLocked() {
        return sHandlerThread != null && sShell != null;
    }

    @WorkerThread
    private static void ensureOpenLocked() {
        if (isOpenLocked()) {
            return;
        }
        sHandlerThread = new HandlerThread("SuShellHandler");
        sHandlerThread.start();
        Handler handler = new Handler(sHandlerThread.getLooper());
        sShell = new Shell.Builder()
                .useSU()
                .setHandler(handler)
                .open();
    }

    private static int runLocked(@NonNull String command, @Nullable Holder<String> stdout,
                                 @Nullable Holder<String> stderr) {
        Holder<Integer> exitCodeHolder = new Holder<>();
        sShell.addCommand(command, 0, (commandCode, exitCode, stdoutLines, stderrLines) -> {
            if (stdout != null) {
                stdout.value = stdoutLines != null ? StringCompat.join("\n", stdoutLines) : null;
            }
            if (stderr != null) {
                stderr.value = stderrLines != null ? StringCompat.join("\n", stderrLines) : null;
            }
            exitCodeHolder.value = exitCode;
        });
        sShell.waitForIdle();
        return exitCodeHolder.value;
    }

    @WorkerThread
    public static int run(@NonNull String command, @Nullable Holder<String> stdout,
                          @Nullable Holder<String> stderr) {
        synchronized (sLock) {
            ensureOpenLocked();
            return runLocked(command, stdout, stderr);
        }
    }

    // Actually, we can just leave the shell open while app is running. When app is killed, stdin
    // for the shell will be closed so the shell will exit automatically.
    @WorkerThread
    private static void ensureClosedLocked() {
        if (!isOpenLocked()) {
            return;
        }
        sShell.close();
        sShell = null;
        sHandlerThread.quitSafely();
        sHandlerThread = null;
    }
}
