import java.util.concurrent.ExecutionException;
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
        try {
        	current.lock.lock();
        	
            while(current.next.value != DUMMY_NODE_VALUE &&
                    current.next.value.compareTo(s) < 0) {

                current.next.lock.lock();
                Node temp = current.next;
                current.lock.unlock();
                current = temp;
            }
            if(!current.next.value.equals(s)) {
            	current.next.lock.lock(); 
                current.next = new Node(s, current.next);
                current.next.next.lock.unlock();
                
                current.nextChanged.signal();
            }
        }catch (Exception ex){
        	ex.printStackTrace();
        } finally {
            current.lock.unlock();
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
        boolean found = false;
        Node current = head;
        current.lock.lock();
        try {
            do {
                while((current.next.value != DUMMY_NODE_VALUE) &&
                        (current.next.value.compareTo(s) <= 0) ) { //

                    current.next.lock.lock();
                    Node temp = current.next;
                    current.lock.unlock();
                    current = temp;
                }
                if(current.value.equals(s)) {
                    found = true;
                }
                if(!found && block) {
                    current.nextChanged.await();
                }
                
            } while (!found && block);

        } catch(InterruptedException ex) {
            ex.printStackTrace();
        } finally {
            current.lock.unlock();
        }

        return found;
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