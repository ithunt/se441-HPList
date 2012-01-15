
public class WorkerB extends Thread {
	
private final HPList list;
	
	public WorkerB(String name, HPList list){
		super(name);
		this.list = list;
	}
	
	public void run(){
		list.insert("a");
		list.insert("u");
		list.insert("y");
		list.insert("x");
		list.find("d", false);
		list.insert("d");
		list.insert("e");
		//list.find("e", true);
		list.insert("j");
		
	}

}


