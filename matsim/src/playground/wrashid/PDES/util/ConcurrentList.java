package playground.wrashid.PDES.util;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

import playground.wrashid.DES.utils.Timer;
// optimized for single producer, single consumer
public class ConcurrentList<T> {
	private LinkedList<T> inputBuffer=new LinkedList<T>();
	private LinkedList<T> outputBuffer=new LinkedList<T>(); 
	public void add(T element){
		synchronized (inputBuffer){
			inputBuffer.add(element);
		}
	}
	
	// returns null, if empty, else the first element
	public T remove(){
		if (!outputBuffer.isEmpty()){
			return outputBuffer.poll();
		}
		if (!inputBuffer.isEmpty()){
			synchronized (inputBuffer){
				//swap buffers
				LinkedList<T> tempList=null; 
				tempList=inputBuffer;
				inputBuffer=outputBuffer;
				outputBuffer=tempList;
			}
			return outputBuffer.poll();
		}
		return null;
	}
	
	
	
	// this experiment effectivly demonstrates, why reimplement ConcurrentList
	// ------------
	// consumed Items:10000
	// time required for ConcurrentList (consumer): 27031
	// ----------
	// consumed Items:10000
	// time required for ConcurrentLinkedQueue (consumer): 60953
	// ----------
	// This experiment was done with adding 10000000 elements but only 10000 consumed
	// It shows, that ConcurrentList much better decouples the producer from the consumer
	// especially, when the consumer is slower than the producer (which was simulated by the sleep(1)
	

	public static void main(String[] args){
		
		// part 1
		
		ConcurrentList<Integer> cList=new ConcurrentList<Integer>();
		Thread t=new Thread(new ARunnable(cList));
		t.start();
		t=new Thread(new BRunnable(cList));
		t.start();
		
		/*
		// part 2
		ConcurrentLinkedQueue<Integer> cList=new ConcurrentLinkedQueue<Integer>();
		Thread t=new Thread(new CRunnable(cList));
		t.start();
		t=new Thread(new DRunnable(cList));
		t.start();
		*/
		
		
		
		
	}

static class ARunnable implements Runnable{
	ConcurrentList<Integer> cList=new ConcurrentList<Integer>();
	@Override
	public void run() {
		Timer timer=new Timer();
		timer.startTimer();
		for (int i=0;i<10000000;i++){
			cList.add(i);
		}
		timer.endTimer();
		timer.printMeasuredTime("time required for ConcurrentList (producer): ");
	}
	public ARunnable(ConcurrentList<Integer> cList){
		super();
		this.cList=cList;
	}
}

static class BRunnable implements Runnable{
	ConcurrentList<Integer> cList=new ConcurrentList<Integer>();
	@Override
	public void run() {
		Timer timer=new Timer();
		timer.startTimer();
		int count=0;
		while (count<10000){
			if (cList.remove()!=null){
				count++;
			}
			try {
				Thread.currentThread().sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("consumed Items:" + count);
		timer.endTimer();
		timer.printMeasuredTime("time required for ConcurrentList (consumer): ");
	}
	public BRunnable(ConcurrentList<Integer> cList){
		super();
		this.cList=cList;
	}
}

static class CRunnable implements Runnable{
	ConcurrentLinkedQueue<Integer> cList=new ConcurrentLinkedQueue<Integer>();
	@Override
	public void run() {
		Timer timer=new Timer();
		timer.startTimer();
		for (int i=0;i<10000000;i++){
			cList.add(i);
		}
		timer.endTimer();
		timer.printMeasuredTime("time required for ConcurrentLinkedQueue (producer): ");
	}
	public CRunnable(ConcurrentLinkedQueue<Integer> cList){
		super();
		this.cList=cList;
	}
}

static class DRunnable implements Runnable{
	ConcurrentLinkedQueue<Integer> cList=new ConcurrentLinkedQueue<Integer>();
	@Override
	public void run() {
		Timer timer=new Timer();
		timer.startTimer();
		int count=0;
		while (count<10000){
			if (cList.poll()!=null){
				count++;
				try {
					Thread.currentThread().sleep(1);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		System.out.println("consumed Items:" + count);
		timer.endTimer();
		timer.printMeasuredTime("time required for ConcurrentLinkedQueue (consumer): ");
	}
	public DRunnable(ConcurrentLinkedQueue<Integer> cList){
		super();
		this.cList=cList;
	}
}
	
	
}

