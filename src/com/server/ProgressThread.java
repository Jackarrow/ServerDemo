package com.server;

import java.util.concurrent.locks.ReentrantLock;

import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;

/**
 * 统计进度线程
 * 
 * @author Jack
 *
 */
public class ProgressThread extends Thread {
	public boolean flag = false;
	private ReentrantLock lock;
	private RemoteCallbackList<DownCallBack> mCallbacks;

	public ProgressThread(ReentrantLock lock,
			RemoteCallbackList<DownCallBack> mCallbacks) {
		this.lock = lock;
		this.mCallbacks = mCallbacks;
	}

	private float progress;
	private String urlString;

	public void setData(float progress, String urlString) {
		// System.out.println(progress+"--"+urlString);
		this.progress = progress;
		this.urlString = urlString;
	}

	public void sendProgress() {
		lock.lock();
		try {
			int i = mCallbacks.beginBroadcast();
			while (i > 0) {
				i--;
				try {
					mCallbacks.getBroadcastItem(i)
							.progress(urlString, progress);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			mCallbacks.finishBroadcast();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void run() {
		while (flag) {
			SystemClock.sleep(100);
			sendProgress();
		}
	}
}
