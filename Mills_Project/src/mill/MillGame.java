package mill;

import java.util.Stack;

public class MillGame implements Cloneable {

	public static final int PHASE_BEGINNING = 1, PHASE_MIDGAME = 2, PHASE_ENDGAME = 3, PHASE_GAME_OVER = 4;

	private static final int PIECES_IN_HAND = 9;

	public static final int BLACK_PLAYER = BoardInfo.BLACK, WHITE_PLAYER = BoardInfo.WHITE;

	private int gameState;

	private int activePlayer;


	public void setWhitePiecesInHand(int whitePiecesInHand) {
		this.whitePiecesInHand = whitePiecesInHand;
	}

	public void setBlackPiecesInHand(int blackPiecesInHand) {
		this.blackPiecesInHand = blackPiecesInHand;
	}

	private int whitePiecesInHand;

	private int blackPiecesInHand;

	private MillBoard board;

	private Stack<MillGame> history;

	private Stack<MillGame> future;

	public MillGame() {
		this.newGame();
	}

	public void newGame() {
		this.gameState = PHASE_BEGINNING;
		this.activePlayer = BLACK_PLAYER;
		this.whitePiecesInHand = PIECES_IN_HAND;
		this.blackPiecesInHand = PIECES_IN_HAND;
		this.board = new MillBoard();
		this.history = new Stack<MillGame>();
		this.future = new Stack<MillGame>();
	}

	public int getTurnNumber() {
		return this.history.size() + 1;
	}

	// copy trang thai ban co
	public Object clone() {
		MillGame copy = new MillGame();
		copy.gameState = this.gameState;
		copy.activePlayer = this.activePlayer;
		copy.whitePiecesInHand = this.whitePiecesInHand;
		copy.blackPiecesInHand = this.blackPiecesInHand;
		copy.board = (MillBoard) this.board.clone();
		return copy;
	}

	public void setPiece(int square, int color) {
		this.board.setPiece(square, color);
		if (color == BoardInfo.BLACK) {
			this.blackPiecesInHand--;
		} else {
			this.whitePiecesInHand--;
		}

	}

	public void undo() throws IllegalStateException {
		if (this.history.empty()) {
			throw new IllegalStateException("Undo cannot be done during first turn!");
		}
		this.future.push((MillGame) this.clone());
		this.restoreGame((this.history.pop()));
	}

	public void redo() throws IllegalStateException {
		if (this.future.empty()) {
			throw new IllegalStateException("Redo cannot be done unless undo has been used first!");
		}
		this.history.push((MillGame) this.clone());
		this.restoreGame((this.future.pop()));
	}

	private void restoreGame(MillGame restored) {
		this.gameState = restored.gameState;
		this.activePlayer = restored.activePlayer;
		this.whitePiecesInHand = restored.whitePiecesInHand;
		this.blackPiecesInHand = restored.blackPiecesInHand;
		this.board = restored.board;

	}

	public boolean undoIsPossible() {
		return this.history.size() > 0;
	}

	public boolean redoIsPossible() {
		return this.future.size() > 0;
	}

	public MillBoard getMillBoard() {
		return (MillBoard) this.board.clone();
	}

	public int getBlackPiecesInHand() {
		return this.blackPiecesInHand;
	}

	public int getWhitePiecesInHand() {
		return this.whitePiecesInHand;
	}

	public int getActivePlayer() {
		return this.activePlayer;
	}

	public int getGameState() {
		return this.gameState;
	}

	public boolean makeMove(Move move) throws IllegalArgumentException, IllegalStateException {
		return this.move(move, true);
	}

	public boolean makeMove(Move move, boolean undoRedoEnabled) throws IllegalArgumentException, IllegalStateException {
		return this.move(move, undoRedoEnabled);
	}

	private boolean move(Move move, boolean undoRedoEnabled) throws IllegalArgumentException, IllegalStateException {
		if (move == null) {
			throw new IllegalArgumentException("makeMove(Move): Parameter 'Move' cannot be null. ");
		}

		if (!ValidMove.isValidMove(move, this)) {
			throw new IllegalArgumentException("makeMove(Move): Parameter 'Move' is an illegal move. ");
		}

		if (undoRedoEnabled) {
			this.history.push((MillGame) this.clone());
		} else {
			this.history = new Stack<MillGame>();
		}

		this.future = new Stack<MillGame>();

		switch (this.gameState) {
		case PHASE_BEGINNING:
			handleBeginningPhaseMove(move);
			break;
		case PHASE_MIDGAME:
			handleMidgamegameMove(move);
			break;
		case PHASE_ENDGAME:
			handleEndgamegameMove(move);
			break;
		default:
			throw new IllegalStateException("Game is over and no more moves can be played.");
		}

		this.activePlayer = this.getOpponent();
		return this.gameState == PHASE_GAME_OVER;
	}

	private void handleEndgamegameMove(Move move) {
		// di chuyển quân cờ
		this.board.movePiece(move.FROM, move.TO);
		// xóa quân cờ nếu có
		if (move.REMOVE != Move.NOWHERE) {
			this.board.removePiece(move.REMOVE);
		}

		if (isGameOver()) {
			this.gameState = PHASE_GAME_OVER;
		}

	}

	private void handleMidgamegameMove(Move move) {
		// di chuyển quân cờ
		this.board.movePiece(move.FROM, move.TO);
		// xóa quân cờ nếu có
		if (move.REMOVE != Move.NOWHERE) {
			this.board.removePiece(move.REMOVE);
		}

		// nếu có người chơi còn chỉ 3 quân cờ --> chuyển sang cuối game
		if (this.board.getColouredSquares(BoardInfo.WHITE).length == 3
				|| this.board.getColouredSquares(BoardInfo.BLACK).length == 3) {
			this.gameState = PHASE_ENDGAME;
		}

		if (isGameOver()) {
			this.gameState = PHASE_GAME_OVER;
		}

	}



	private void handleBeginningPhaseMove(Move move) {
		// chỉ được đặt cờ
		if (this.activePlayer == WHITE_PLAYER) {
			this.whitePiecesInHand--;
			this.board.setPiece(move.TO, BoardInfo.WHITE);
		} else if (this.activePlayer == BLACK_PLAYER) {
			this.blackPiecesInHand--;
			this.board.setPiece(move.TO, BoardInfo.BLACK);
		} else {
			throw new IllegalStateException("Internal error --> Illegal activePlayer: " + this.activePlayer);
		}
		// xóa quân cờ nếu có
		if (move.REMOVE != Move.NOWHERE) {
			this.board.removePiece(move.REMOVE);
		}
		// nếu đặt hết cờ --> chuyển sang midgame
		if (this.whitePiecesInHand == 0 && this.blackPiecesInHand == 0) {
			this.gameState = PHASE_MIDGAME;
			if (isGameOver()) {
				this.gameState = PHASE_GAME_OVER;
			}
		}

	}

	private boolean isGameOver() {

		// nếu cờ bị kẹt hoặc còn dưới 3 quân cờ --> gameover
		if (this.board.allPiecesJammed(this.activePlayer)
				|| this.board.getColouredSquares(this.activePlayer).length < 3) {
			return true;
		}
		if (this.board.allPiecesJammed(this.getOpponent())
				|| this.board.getColouredSquares(this.getOpponent()).length < 3) {
			return true;
		}

		return false;

	}

	public int getOpponent() {
		if (this.activePlayer == WHITE_PLAYER)
			return BLACK_PLAYER;
		else
			return WHITE_PLAYER;
	}

	public String toString() {
		String tmp = "";
		tmp = tmp + this.board + "\n";
		if (this.gameState == PHASE_BEGINNING)
			tmp = tmp + "PHẦN ĐẦU TRÒ CHƠI!";
		else if (this.gameState == PHASE_MIDGAME)
			tmp = tmp + "PHẦN GIỮA TRÒ CHƠI!";
		else if (this.gameState == PHASE_ENDGAME)
			tmp = tmp + "PHẦN CUỐI TRÒ CHƠI!";
		else
			tmp = tmp + "TRÒ CHƠI KẾT THÚC!";
		if (this.activePlayer == WHITE_PLAYER)
			tmp = tmp + "  Lượt: TRẮNG";
		else
			tmp = tmp + "  Lượt: ĐEN";

		tmp = tmp + "  Số quân trắng còn lại: " + this.whitePiecesInHand + "  Số quân đen còn lại: "
				+ this.blackPiecesInHand;
		return tmp + "\nLượt: " + this.getTurnNumber()
				+ "  Định dạng nước đi: (FROM, TO, REMOVE), trong đó số từ 0--23 hoặc -1.";
	}

	public void generateMillGame(int newActivePlayer, int newGameState, int newWhitePiecesInHand,
			int newBlackPiecesInHand) {
		this.activePlayer = newActivePlayer;
		this.gameState = newGameState;
		this.whitePiecesInHand = newWhitePiecesInHand;
		this.blackPiecesInHand = newBlackPiecesInHand;
	}

	public static void main(String[] args) {
		/**
		 *
		 * <pre>
		 * 0 --------- 1 --------- 2
		 * |           |           |
		 * |   3 ----- 4 ----- 5   |
		 * |   |       |       |   |
		 * |   |   6 - 7 - 8   |   |
		 * |   |   |       |   |   |
		 * 9 - 10- 11      12- 13- 14
		 * |   |   |       |   |   |
		 * |   |   15- 16- 17  |   |
		 * |   |       |       |   |
		 * |   18----- 19----- 20  |
		 * |           |           |
		 * 21--------- 22--------- 23
		 * </pre>
		 *
		 */

		MillGame test = new MillGame();
		test.setPiece(0, BoardInfo.BLACK);
		test.setPiece(3, BoardInfo.BLACK);
		test.setPiece(4, BoardInfo.BLACK);
		test.setPiece(9, BoardInfo.BLACK);

		test.setPiece(10, BoardInfo.BLACK);
		test.setPiece(11, BoardInfo.BLACK);
		test.setPiece(18, BoardInfo.BLACK);
		test.setPiece(13, BoardInfo.BLACK);

		test.setPiece(2, BoardInfo.WHITE);
		test.setPiece(12, BoardInfo.WHITE);
		test.setPiece(14, BoardInfo.WHITE);
		/*
		 * test.setPiece(16, BoardInfo.WHITE); test.setPiece(19, BoardInfo.WHITE);
		 * test.setPiece(22, BoardInfo.WHITE); test.setPiece(23, BoardInfo.WHITE);
		 */
		test.setBlackPiecesInHand(0);
		test.setWhitePiecesInHand(0);
		test.setGameState(PHASE_MIDGAME);
		
		//System.out.println(test);
		// test.makeMove(new Move(-1, 13, 0));
		Move[] list = ValidMove.getValidMoves(test);
		for (Move move : list) {
			System.out.println(move);
		}

		AI ai = new AI();
		ai.depthSearch(test, 5);


		//test.makeMove(new Move(-1, 5, 2));

	}

	public void setGameState(int gameState) {
		this.gameState = gameState;
	}

}
