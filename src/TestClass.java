
public class TestClass {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		HPList hpTestList = new HPList();
		hpTestList.insert("a");
		hpTestList.insert("b");
		hpTestList.insert("c");
		
		System.out.println("found a?" + hpTestList.find("a",false));

	}

}
