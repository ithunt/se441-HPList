import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Ian Hunt
 * @author Christoffer Rosen
 * @author Patrick McAfee
 * @date 1/9/12
 *
 * High performance, thread safe, singly linked list of Strings.
 * The list comprises Nodes, and as few nodes as possible are locked by any thread so as to permit
 * the greatest possible concurrency.
 */

//@ThreadSafe
public class HPList {
    
    public static final String DUMMY_NODE_VALUE = "";
    public static final boolean DEBUG_INS = true;
    public static final boolean DEBUG_FND = false;
    

    class Node {
        final String value ;          // String in this node; a null string ("") is a dummy node/
        Node next ;                   // The node following this one.
        final Lock lock ;             // Guards this node ;
        // CONDITION PREDICATE: The next linked node is not current being accessed.
        final Condition nextChanged ; // Signaled when the next link for this node changes.

        Node(String value, Node next) {
            this.value = value ;
            this.next = next ;
            this.lock = new ReentrantLock() ;
            this.nextChanged = lock.newCondition() ;
        }
    }

    private Node head ;    // Head of list  - always points to something

    /*
    * An HP list always contains at least two nodes - a dummy head
    * and a dummy tail.
    */
    public HPList() {
        head = new Node(DUMMY_NODE_VALUE, new Node(DUMMY_NODE_VALUE, null)) ;
    }

    /**
    * Insert string s into the HPList. Simply returns if s is already
    * in the list, otherwise inserts it so as to keep the list in
    * ascending order of strings.
    * @param s The string element to insert into the collection
    */
    public void insert(String s) {
    	if (DEBUG_INS) System.out.println("inserting: "+s);
        Node current = head;
        try {
        	//get lock
        	if (DEBUG_INS) System.out.println(s + " is getting a lock on "+current.value);
        	current.lock.lock();
        	if (DEBUG_INS) System.out.println(s + " got a lock on "+current.next.value);
        	
            while(current.next.value != DUMMY_NODE_VALUE) {
            	
            	if (DEBUG_INS) System.out.println(s + " is getting a lock on "+current.next.value);
                current.next.lock.lock();
                if (DEBUG_INS) System.out.println(s + " got a lock on "+current.next.value);
                

                if(current.next.value.compareTo(s) >= 0) {
                	if (DEBUG_INS) System.out.println("Next node is too far or is same.");
                    break;
                }
                
                
                if (DEBUG_INS) System.out.println(s + " is trying to unlock "+current.value);
                current.lock.unlock();
                if (DEBUG_INS) System.out.println(s + " unlocked "+current.value+" and is incrementing to "+current.next.value);
                current = current.next;
            }
            if(!current.next.value.equals(s)) {
            	current.next.lock.lock(); 
                current.next = new Node(s, current.next);
                current.next.next.lock.unlock();  //lock position moved one further due to insert
                if (DEBUG_INS) System.out.println(s + " has been inserted.");
                current.nextChanged.signalAll();
            }else{
            	
            	if (DEBUG_INS) System.out.println(s + " already in HPList");
            	current.next.lock.unlock();
            }
        }catch (Exception e){
        	e.printStackTrace();
        } finally {
        	if (DEBUG_INS) System.out.println("Releasing core lock");
            current.lock.unlock();
            if (DEBUG_INS) System.out.println("Core lock released");
        }
    }


    /**
     * Returns true if String s is in the HPList, otherwise returns
     * false. If block is true, will wait until s is inserted and
     * unconditionally return true.
     * @param s The string you are searching for
     * @param block block thread until s is inserted
     * @return true if element is in the list
     */
    public boolean find(String s, boolean block) {

        Node current = head;
        
        //check if in list
        while(current.next.value != DUMMY_NODE_VALUE) { //
        		//System.out.println("Find Checking(1st pass): "+current.value);
                if(current.value.equals(s)) {
                    return true;
                }else if (block && (current.next.value.compareTo(s) > 0) || current.next.value.equals(DUMMY_NODE_VALUE)){
                	return waitOnBlocking(current, s);
                }
                Node temp = current.next;
                current = temp;
        }
        
        if (block){
        	return waitOnBlocking(current, s);
        }
        
        //if not blocking and not in list
        return false;
        
    }
    
    /**
     * Waits for an element to be added before returning true
     * 
     * @param current
     * @return
     */
    private boolean waitOnBlocking(Node current, String s){
    	while (true){
    		try {
    			current.lock.lock();
				current.nextChanged.await();
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    		if (current.next.value.equals(s)){
    			return true;
    		}else if (current.next.value.compareTo(s)<0 && !current.next.value.equals(DUMMY_NODE_VALUE)){
    			current = current.next;
    		}
    	}
    	
    }

    /**
     * Prints out the list for debugging purposes
     */
    public void printList(){
    	
    	Node current = head.next;
    	
    	while(current.value != DUMMY_NODE_VALUE){
			
			System.out.println(current.value);
			current = current.next;
		}

    
    		
    }
}