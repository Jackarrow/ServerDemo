package com.server;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MyThreadPool {
	private static MyThreadPool pool  = new MyThreadPool();
	private ExecutorService service;
	private MyThreadPool(){
		service =Executors.newFixedThreadPool(1);
	}
	
	public static MyThreadPool getInstance(){
		return pool;
	}
	
	public void exector(Runnable task){
		service.execute(task);
	}
	
	public void shutDownThread() throws InterruptedException{
		service.awaitTermination(5, TimeUnit.SECONDS);
		List<Runnable> tasks = service.shutdownNow();//执行任务后的剩下的未执行的任务
		if(tasks.size()==0){
			//当前没有执行中的任务
		}
	}
}
