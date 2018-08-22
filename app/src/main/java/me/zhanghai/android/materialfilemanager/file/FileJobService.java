/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.file;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.zhanghai.android.materialfilemanager.filesystem.File;

public class FileJobService extends Service {

    private static FileJobService sInstance;

    private static List<FileJob> sPendingJobs = new ArrayList<>();

    private List<FileJob> mRunningJobs = new ArrayList<>();

    private ExecutorService mExecutorService = Executors.newCachedThreadPool();

    private static void startJob(FileJob job, Context context) {
        if (sInstance != null) {
            sInstance.mRunningJobs.add(job);
            sInstance.startJob(job);
        } else {
            sPendingJobs.add(job);
            context.startService(new Intent(context, FileJobService.class));
        }
    }

    public static void delete(List<File> files, Context context) {
        startJob(new FileJobs.Delete(files), context);
    }

    public static void createFile(File file, Context context) {
        startJob(new FileJobs.CreateFile(file), context);
    }

    public static void createDirectory(File file, Context context) {
        startJob(new FileJobs.CreateDirectory(file), context);
    }

    public static void rename(File file, String name, Context context) {
        startJob(new FileJobs.Rename(file, name), context);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sInstance = this;

        Iterator<FileJob> iterator = sPendingJobs.iterator();
        while (iterator.hasNext()) {
            FileJob job = iterator.next();
            iterator.remove();
            mRunningJobs.add(job);
            startJob(job);
        }
    }

    private void startJob(FileJob job) {
        // TODO
        job.run(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        sInstance = null;

        Iterator<FileJob> iterator = mRunningJobs.iterator();
        while (iterator.hasNext()) {
            FileJob job = iterator.next();
            stopJob(job);
            iterator.remove();
        }

        mExecutorService.shutdownNow();
    }

    private void stopJob(FileJob job) {
        // TODO
    }
}