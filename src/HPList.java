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

        Node current = head;
        current.lock.lock();
        try {

            while(current.next.value != DUMMY_NODE_VALUE) {
                current.next.lock.lock();

                if(current.next.value.compareTo(s) >= 0) {
                    break;
                }

                Node temp = current.next;
                current.lock.unlock();
                current = temp;
            }
            if(!current.next.value.equals(s)) {
                current.next = new Node(s, current.next);
                //current.next.next.lock.unlock(); Why? :S 
                current.nextChanged.signal();
            }
        } finally {
            if(current.lock.tryLock()) current.lock.unlock();
            if(current.next.lock.tryLock()) current.next.lock.unlock();
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
        boolean found = false;
        current.lock.lock();
        try {
            while(current.value != DUMMY_NODE_VALUE && !found) {
                current.next.lock.lock();
                
                if(current.value.equals(s)) {
                    found = true;
                    break;
                    
                } else if (s.compareTo(current.next.value) > 0) {
                    if(block) {
                        try {
                            current.nextChanged.await();
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        break;
                    }
                }

                Node temp = current.next;
                current.lock.unlock();
                current = temp;
            } //end while
        } finally {
            if(current.lock.tryLock()) current.lock.unlock();
            if(current.next.lock.tryLock()) current.next.lock.unlock();
        }
        return found;


        
    }

    /**
     * Prints out the list for debugging purposes
     */
    public void printList(){
    	
    	Node current = head.next;
    
    	current.lock.lock();
    	
    	try{
    		while(current.value != DUMMY_NODE_VALUE){
    			current.next.lock.lock();
    			
    			System.out.println(current.value);
    			Node temp = current.next;
    			current.lock.unlock();
    			current = temp;
    		}
    	}
    	finally {
    		if(current.lock.tryLock()) current.lock.unlock();
    	}
    
    		
    }
}