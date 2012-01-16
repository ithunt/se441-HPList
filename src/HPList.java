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
    public static final boolean DEBUG = true;
    

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
    	if (DEBUG) System.out.println("inserting: "+s);
        Node current = head;
        try {
        	//get lock
        	if (DEBUG) System.out.println(s + " is getting a lock on "+current.value);
        	current.lock.lock();
        	if (DEBUG) System.out.println(s + " got a lock on "+current.next.value);
        	
            while(current.next.value != DUMMY_NODE_VALUE) {
            	
            	if (DEBUG) System.out.println(s + " is getting a lock on "+current.next.value);
                current.next.lock.lock();
                if (DEBUG) System.out.println(s + " got a lock on "+current.next.value);
                

                if(current.next.value.compareTo(s) >= 0) {
                	if (DEBUG) System.out.println("Next node is too far.");
                    break;
                }
                
                
                if (DEBUG) System.out.println(s + " is trying to unlock "+current.value);
                current.lock.unlock();
                if (DEBUG) System.out.println(s + " unlocked "+current.value+" and is incrementing to "+current.next.value);
                current = current.next;
            }
            if(!current.next.value.equals(s)) {
                current.next = new Node(s, current.next);
                current.next.next.lock.unlock();
                if (DEBUG) System.out.println(s + " has been inserted.");
                current.nextChanged.signal();
            }else{
            	
            	if (DEBUG) System.out.println(s + " already in HPList");
            	current.next.lock.unlock();
            }
        }catch (Exception e){
        	System.out.println(e.getMessage());
        } finally {
        	if (DEBUG) System.out.println("Releasing all locks");
            current.lock.unlock();
            if (DEBUG) System.out.println("All locks released");
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

        Node current = head.next;
        
        //No locking needed, as this is a reader
        
        //check if in list
        while(current.value != DUMMY_NODE_VALUE) { //
        		System.out.println("Find Checking(1st pass): "+current.value);
                if(current.value.equals(s)) {
                    return true;
                } 
                Node temp = current.next;
                current = temp;
        }
        
        //if blocking, go through again to find position
        if (block){
        	current = head;
        	while(true){
        		if(current.value.equals(s)) { //just in case it slipped through
                    return true;
                }else if(s.compareTo(current.next.value) > 0 || current.next.value == DUMMY_NODE_VALUE) {
                    try {
                        current.nextChanged.await();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
        		current=current.next;
        	}
        	
        }
        
        //if not blocking and not in list
        return false;
        
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