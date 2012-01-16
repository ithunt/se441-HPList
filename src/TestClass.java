
public class TestClass {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		HPList hpTestList = new HPList();
		
		WorkerA workerA = new WorkerA("WorkerA", hpTestList);
		WorkerB workerB = new WorkerB("WorkerB", hpTestList);
		
		
		hpTestList.printList();
		
		workerA.start();  //RUN WON'T WORK, USE START!
		workerB.start();
		
		workerA.join();
		workerB.join();
		
		hpTestList.printList();
	
		System.exit(0);
		
	

	}

}
