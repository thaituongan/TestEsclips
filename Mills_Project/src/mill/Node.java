package mill;
public class Node {
    public final MillGame GAME;
    public final int DEPTH;
    public final Move PREVIOUS_MOVE;
    public Node PATH;
    public short VALUE;

    public Node(MillGame game, int depth, Move previousMove) {
        this.GAME = game;
        this.DEPTH = depth;
        this.PREVIOUS_MOVE = previousMove;
        this.PATH = null;
    }

    public void setPath(Node next) {
        this.PATH = next;
        
    }

    public void setValue(short value) {
        this.VALUE = value;
    }
    
    public String creatTree(int depth) {
    	String str = "||--";
    	String rs = str.repeat(depth);
    	return rs+">";
    }

    public String toString() {
    	if (this.PATH == null) {
    		 return creatTree(DEPTH)+"depth:"+DEPTH+" ,prevMove:"+PREVIOUS_MOVE+" , value:"+VALUE;
		}
    	
        return creatTree(DEPTH)+"depth:"+DEPTH+" ,prevMove:"+PREVIOUS_MOVE+" , value:"+VALUE+", path:\n"+PATH;
    }
}