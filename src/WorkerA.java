
public class WorkerA extends Thread {
	private final HPList list;
	
	public WorkerA(String name, HPList list){
		super(name);
		this.list = list;
	}
	
	public void run(){
		list.insert("a");
		list.insert("a");
		//list.find("e", true);
		list.insert("b");
		list.insert("z");
		list.insert("g");
		//System.out.println("Found U?: "+list.find("u", false));
		list.insert("j");
		
	}

}

	