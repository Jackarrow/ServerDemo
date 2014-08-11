package com.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.SparseArray;

/**
 * 可以用广播来更新多个线程下载的进度
 * @author Jack
 *
 */
public class MyServer extends Service {
	private RemoteCallbackList<DownCallBack> mCallback = new RemoteCallbackList<DownCallBack>();
	private List<String> downs = new ArrayList<String>();
	private ReentrantLock lock = new ReentrantLock();
	private MyThreadPool pool = null;
	private SparseArray<DownLoadThread> tasks = new SparseArray<DownLoadThread>();
	private boolean isRunningTask = false;
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			String finshTask = (String) msg.obj;
			System.out.println("接收到消息"+tasks.size());
			tasks.put(downs.indexOf(finshTask), null);
			tasks.size();
			isRunningTask = false;
			
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setDataAndType(Uri.parse("file://"+msg.getData().getString("file")), "application/vnd.android.package-archive");
			startActivity(intent);
			
			executorTask();
		}
	};
	@Override
	public void onCreate() {
		super.onCreate();
		pool = MyThreadPool.getInstance();
//		pThread = new ProgressThread(lock, mCallback);
//		pThread.flag = true;
//		pThread.start();
	}
	
	class FunctionImpl extends SupportFunction.Stub{
		
		@Override
		public String getAppListUrl() throws RemoteException {
			return "http://aus.bjaishua.com/AUS/pub.action?fetch";
		}

		@Override
		public void startDown(String url) throws RemoteException {
			if(!downs.contains(url)){
				downs.add(url);
				tasks.append(downs.indexOf(url), new DownLoadThread(url,mCallback,lock,handler));//添加任务
				executorTask();
			}
		}

		@Override
		public void regCallback(DownCallBack callBack) throws RemoteException {
//			if(pThread==null){
//				pThread = new ProgressThread(lock, mCallback);
//				pThread.flag = true;
//				pThread.start();
//			}
			if(callBack!=null){
				mCallback.register(callBack);
			}
		}

		@Override
		public void unregCallback(DownCallBack callBack) throws RemoteException {
			if(callBack!=null){
				mCallback.unregister(callBack);
			}
		}
	
	}
	
	private void executorTask(){
		if(isRunningTask){
			return;
		}
		for(int i = 0;i<tasks.size();i++){
			DownLoadThread t = tasks.get(i);
			if(t!=null){//当前有任务，执行当前任务并且中断当前循环
				pool.exector(t);
				isRunningTask = true;
				break;
			}
		}
	}
	
	private FunctionImpl function = new FunctionImpl();
	
	@Override
	public IBinder onBind(Intent intent) {
		System.out.println("bind server");
		return function;
	}

}
