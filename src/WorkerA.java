
public class WorkerA extends Thread {
	private final HPList list;
	
	public WorkerA(String name, HPList list){
		super(name);
		this.list = list;
	}
	
	public void run(){
		//System.out.println("Found U?: "+list.find("u", true));
		list.insert("a");
		list.insert("a");
		list.insert("b");
		list.insert("z");
		//list.insert("g");
		//list.insert("j");
		
	}

}

	