package mill;

public class Move {

	public static final int NOWHERE = -1;

	public final int FROM;

	public final int TO;

	public final int REMOVE;

	@SuppressWarnings("unused")
	private Move() {
		this.FROM = 0;
		this.TO = 0;
		this.REMOVE = 0;
	}

	public Move(int from, int to, int remove) throws IllegalArgumentException {
		if (from != NOWHERE && !(0 <= from && from <= 23))
			throw new IllegalArgumentException("public Move(from,to,remove): bad argument 'from': " + from);
		if (!(0 <= to && to <= 23))
			throw new IllegalArgumentException("public Move(from,to,remove): bad argument 'to': " + to);
		if (remove != NOWHERE && !(0 <= remove && remove <= 23))
			throw new IllegalArgumentException("public Move(from,to,remove): bad argument 'remove': " + remove);

		this.FROM = from;
		this.TO = to;
		this.REMOVE = remove;
	}

	

	public String toString() {
		return "(" + this.FROM + ", " + this.TO + ", " + REMOVE+")";
	}

	public static void main(String[] args) {
		Move move_1;
		Move move_2;
		//Move move_3;
		try {
			move_1 = new Move(1, 2, 3);
			System.out.println(move_1);
			move_2 = new Move(Move.NOWHERE, 2, Move.NOWHERE);
			System.out.println(move_2);
			//move_3 = new Move(7, Move.NOWHERE, 9);
		} catch (IllegalArgumentException e) {
			System.out.println(e + ", OK!");
		}

	}
}
