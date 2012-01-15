
public class WorkerA extends Thread {
	private final HPList list;
	
	public WorkerA(String name, HPList list){
		super(name);
		this.list = list;
	}
	
	public void run(){
		list.insert("a");
		list.insert("b");
		list.insert("z");
		list.insert("g");
		list.find("d", false);
		//list.find("e", true);
		list.insert("j");
		
	}

}

	