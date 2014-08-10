package com.server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
	
}
