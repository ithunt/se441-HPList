import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author ian hunt
 * @date 1/9/12
 *
 * High performance, thread safe, singly linked list of Strings.
 * The list comprises Nodes, and as few nodes as possible are locked by any thread so as to permit
 * the greatest possible concurrency.
 */

@ThreadSafe
public class HPList {
    class Node {
        final String value ;          // String in this node; a null string ("") is a dummy node/
        Node next ;                   // The node following this one.
        final Lock lock ;             // Guards this node ;
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
        head = new Node("", new Node("", null)) ;
    }

    /*
    * Insert string s into the HPList. Simply returns if s is already
    * in the list, otherwise inserts it so as to keep the list in
    * ascending order of strings.
    */
    public void insert(String s) {

        head.lock.lock();
        head.next = new Node(s, head.next);
        head.lock.unlock();
    }

    /*
    * Returns true if String s is in the HPList, otherwise returns
    * false. If block is true, will wait until s is inserted and
    * unconditionally return true.
    */
    public boolean find(String s, boolean block) { 
        
        Node current = head;
        while(current.next != null) {
            if(s.equals(current.value)) return true;
            current = current.next;
        }
    
        if(block) {
            
        } else {
            return false;
        }
    
    
    }
    
}


