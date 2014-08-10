package com.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.locks.ReentrantLock;

import android.os.Handler;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

public class DownLoadThread implements Runnable {

	private String urlString;
	private RemoteCallbackList<DownCallBack> mCallLists;
	private ReentrantLock lock;
	private Handler handler;
	public DownLoadThread(String url,RemoteCallbackList<DownCallBack> mCallLists,ReentrantLock lock,Handler handler) {
		this.urlString = url;
		this.mCallLists = mCallLists;
		this.lock = lock;
		this.handler = handler;
	}

	@Override
	public void run() {
		try {
			URL url = new URL(urlString);
			URLConnection conn = url.openConnection();
			String name = getFileName();
			File file = new File("/mnt/sdcard/" + name);
			System.out.println("下载文件长度" + conn.getContentLength());
			int fileLength = conn.getContentLength();
			System.out.println(file.toString());
			conn.setConnectTimeout(6000);
			InputStream is = conn.getInputStream();

			FileOutputStream fos = new FileOutputStream(file);
			byte[] buf = new byte[1024*8];
			int len = 0;
			int count = 0;
			lock.lock();
			while ((len = is.read(buf)) != -1) {
					fos.write(buf, 0, len);
					float progress = (float)(count+=len)/fileLength;
		
//					if(progress==1f){
//						pThread.setData(progress, urlString);
//					}else{
//						pThread.setData(progress, urlString);
//					}
					sendProgress(progress, fileLength);
					if(progress==1){
						System.out.println("完成，需要发送消息");
						Message msg = handler.obtainMessage();
						msg.obj = urlString;
						handler.sendMessage(msg);
					}
			}
			fos.close();
			is.close();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			lock.unlock();
		}

	}

	public void sendProgress(float len,int fileLength){

		int i = mCallLists.beginBroadcast();
//		while(i >0){
//			i--;
			try {
				mCallLists.getBroadcastItem(0).progress(urlString,len);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
//		}
		mCallLists.finishBroadcast();
	}
	
	private String getFileName() {
		String temp = urlString.split("//")[1];
		String[] names = temp.split("/");
		return names[names.length - 1];
	}
}
