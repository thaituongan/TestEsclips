package mill;

public class AI {
	private static final short MAX_VALUE = Short.MAX_VALUE;
	private static final short MIN_VALUE = Short.MIN_VALUE;
	private int nodesOpened = 0;
	private int depthLimit = 1;
	private long searchStarted = 0;
	private int player;
	private int opponent = -1;

	public AI() {

	}

	public Move depthSearch(MillGame game, int depth) {
		if (depth < 0) {
			throw new IllegalArgumentException(
					"depthSearch(MillGame,byte): " + "depth:" + depth + " must be a positive number.");
		}
		this.nodesOpened = 0;

		this.depthLimit = depth;
		this.player = game.getActivePlayer();
		this.opponent = game.getOpponent();

		this.searchStarted = System.currentTimeMillis();

		Node best = maxValue(new Node(game, 0, null), MIN_VALUE, MAX_VALUE);
		if (best != null && best.PATH != null) {
			System.out
					.println("depth:" + depth + " move:" + best.PATH.PREVIOUS_MOVE + " value:" + best.VALUE + " nodes:"
							+ this.nodesOpened + " time:" + (System.currentTimeMillis() - this.searchStarted) + "ms");

		}
		this.printPath(best.PATH);
		if (best == null || best.PATH == null) {
			return null;
		}
		return best.PATH.PREVIOUS_MOVE;
	}

	public Node maxValue(Node currentNode, short alpha, short beta) {
		this.nodesOpened++;
		if (currentNode.DEPTH >= this.depthLimit) {
			currentNode.setValue(this.evluate(currentNode.GAME));
			currentNode.setPath(null);
			return currentNode;
		}
		short bestValue = alpha;
		Node bestPath = null;
		Move[] allMoves = ValidMove.getValidMoves(currentNode.GAME);

		MillGame copy;
		Move currentMove;
		for (Move moveIndex : allMoves) {
			// copy trạng thái game
			copy = (MillGame) currentNode.GAME.clone();
			currentMove = moveIndex;

			if (copy.makeMove(currentMove, false)) {
				if (copy.getActivePlayer() == this.player) {
					currentNode.setValue(MAX_VALUE);
				} else {
					currentNode.setValue(MIN_VALUE);
				}

				currentNode.setPath(new Node(copy, (int) (currentNode.DEPTH + 1), currentMove));
				return currentNode;
			}

			Node nextNode = this.minValue(new Node(copy, (int) (currentNode.DEPTH + 1), currentMove), alpha, beta);

			// nếu giá trị của node con lớn hơn best value hiện tại
			// cập nhật nhật bestvalue hiện tại
			if (nextNode.VALUE == MAX_VALUE) { 
				currentNode.setValue(MAX_VALUE);
				currentNode.setPath(nextNode);
				return currentNode;
			}
			if (nextNode.VALUE > bestValue) {
				bestValue = nextNode.VALUE;
				bestPath = nextNode;
			}
			if (bestValue >= beta) { // karsinta
				currentNode.setValue(bestValue);
				currentNode.setPath(bestPath);
				return currentNode;
			}
			if (bestValue > alpha) {
				alpha = bestValue;
			}
		}
		currentNode.setValue(bestValue);
		currentNode.setPath(bestPath);
		return currentNode;
	}

	public Node minValue(Node currentNode, short alpha, short beta) {

		this.nodesOpened++;

		if (currentNode.DEPTH >= this.depthLimit) {
			currentNode.setValue(this.evluate(currentNode.GAME));
			currentNode.setPath(null);
			return currentNode;
		}
		// giá trị min
		short worstValue = beta; // 32767
		Node worstPath = null;

		Move[] allMoves = ValidMove.getValidMoves(currentNode.GAME);

		MillGame copy;
		Move currentMove;
		for (Move moveIndex : allMoves) {
			copy = (MillGame) currentNode.GAME.clone();
			currentMove = moveIndex;

			if (copy.makeMove(currentMove, false)) {
				if (copy.getActivePlayer() == this.player) {
					currentNode.setValue(MAX_VALUE);
				} else {
					currentNode.setValue(MIN_VALUE);
				}
				// tạo đường đi
				currentNode.setPath(new Node(copy, (int) (currentNode.DEPTH + 1), currentMove));
				return currentNode;
			}
			// tạo node tiếp theo
			Node nextNode = this.maxValue(new Node(copy, (int) (currentNode.DEPTH + 1), currentMove), alpha, beta);
			if (nextNode.VALUE == MIN_VALUE) {
				currentNode.setValue(MIN_VALUE);
				currentNode.setPath(nextNode);
				return currentNode;
			}
			if (nextNode.VALUE < worstValue) {
				worstValue = nextNode.VALUE;
				worstPath = nextNode;
			}
			if (worstValue <= alpha) {
				currentNode.setValue(worstValue);
				currentNode.setPath(worstPath);
				return currentNode;
			}
			if (worstValue < beta) {
				beta = worstValue;
			}
		}
		// trả về node min tìm được
		currentNode.setValue(worstValue);
		currentNode.setPath(worstPath);
		return currentNode;

	}

	private short evluate(MillGame game) {
	    if (game.getGameState() == MillGame.PHASE_GAME_OVER) {
            System.out.println("Evaluating winning position!");
            if (game.getActivePlayer() == this.player) {
                return MAX_VALUE;
            }
            else {
                return MIN_VALUE;
            }
        }

        MillBoard board = game.getMillBoard();
        int value = 0;
		int[] playerPieces = board.getColouredSquares(this.player);
		int[] opponentPieces = board.getColouredSquares(this.opponent);
		int playerHandPieces;
		int opponentHandPieces;

		// đánh giá dựa trên số lương quân cờ
		if (this.player == MillGame.WHITE_PLAYER) {
			playerHandPieces = game.getWhitePiecesInHand();
			opponentHandPieces = game.getBlackPiecesInHand();
		} else {
			playerHandPieces = game.getBlackPiecesInHand();
			opponentHandPieces = game.getWhitePiecesInHand();
		}
		value += 200 * ((playerPieces.length + playerHandPieces) - (opponentPieces.length + opponentHandPieces));

		// đánh giá dựa trên tổng số ô trống có thể di chuyển
		if (game.getGameState() != MillGame.PHASE_ENDGAME) {
			for (int p : playerPieces) {
				value += 10 * this.getEmptyNeighbourCount(p, board);
				value += 30 * this.getPlayerNeighbours(p, board);

			}
			for (int p : opponentPieces) {
				value -= 10 * this.getEmptyNeighbourCount(p, board);
				value -= 30 * this.getOpponentNeighbours(p, board);

			}
		} else {

		}

		// đánh giá dựa trên số quân cờ có trong mill
		/*
		 * int playePieceInMill = countPiecesInMill(player, board); int
		 * opponentPieceInMill = countPiecesInMill(opponent, board); value +=
		 * (playePieceInMill - opponentPieceInMill) * 200;
		 */

		return (short) value;

	}

	// đếm số ô mà người chơi đang kề với chính người chơi
	private int getPlayerNeighbours(int square, MillBoard board) {
		int[] neighbours = BoardInfo.getNeighbours(square);
		int counter = 0;
		for (int n : neighbours) {
			if (board.get(n) == this.player) {
				counter++;
			}
		}

		return counter;
	}

	// đếm số ô mà đối thủ đang kề với chính người chơi
	private int getOpponentNeighbours(int square, MillBoard board) {
		int[] neighbours = BoardInfo.getNeighbours(square);
		int counter = 0;
		for (int n : neighbours) {
			if (board.get(n) == this.opponent) {
				counter++;
			}
		}

		return counter;
	}

	// đếm số quân cờ có trong mill
	private int countPiecesInMill(int currentPlayer, MillBoard board) {
		// TODO Auto-generated method stub
		int counter = 0;
		if (currentPlayer == BoardInfo.WHITE) {
			int[] whitePieces = board.getColouredSquares(BoardInfo.WHITE);
			for (int w : whitePieces) {
				board.isSquareInMill(w);
				counter++;
			}
		} else {
			int[] blackPieces = board.getColouredSquares(BoardInfo.WHITE);
			for (int b : blackPieces) {
				board.isSquareInMill(b);
				counter++;
			}
		}

		return counter;

	}

	// đếm số ô trống xung quanh quân cờ
	private int getEmptyNeighbourCount(int square, MillBoard board) {
		int[] neighbours = BoardInfo.getNeighbours(square);
		int counter = 0;
		for (int n : neighbours) {
			if (board.get(n) == BoardInfo.EMPTY) {
				counter++;
			}
		}

		return counter;
	}

	private void printPath(Node node) {
		Node tmp = node;
		while (tmp != null) {
			System.out.print(tmp);
			tmp = tmp.PATH;
		}
		System.out.println();
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
		// test.setPiece(13, BoardInfo.BLACK);

		test.setPiece(2, BoardInfo.WHITE);
		test.setPiece(12, BoardInfo.WHITE);
		test.setPiece(14, BoardInfo.WHITE);
		test.setPiece(16, BoardInfo.WHITE);
		test.setPiece(19, BoardInfo.WHITE);
		test.setPiece(22, BoardInfo.WHITE);
		test.setPiece(23, BoardInfo.WHITE);

		System.out.println(test);
//	test.makeMove(new Move(-1, 21, 2));
//	test.makeMove(new Move(-1, 1, -1));
//	test.makeMove(new Move(-1, 6, -1));
// test.makeMove(new Move(-1, 13, 6));

		Move[] list = ValidMove.getValidMoves(test);
		for (Move move : list) {
			System.out.println(move);
		}
		System.out.println(test);

		AI ai = new AI();
		ai.depthSearch(test, 3);
		System.out.println(ai.evluate(test));

		// test.makeMove(new Move(-1, 5, 2));

	}

}