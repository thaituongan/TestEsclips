package mill;

import java.util.ArrayList;

public final class ValidMove {

	public static boolean isValidMove(Move move, MillGame game) {
		return legal(move, game, game.getMillBoard());

	}

	/*
	 * đặt cờ hoặc di chuyển cờ để tạo thành các mill sau khi 1 mill được tạo thành
	 * ==> được xóa 1 quân cờ của đối thủ không được xóa quân cờ đang nằm trong mill
	 * của đối thủ nếu người chơi không còn khả năng tạo ra mill nữa(còn dưới 3 quân
	 * cờ) ==> thua người chơi còn nước đi nào hợp lệ nữa ==> thua
	 */

	// kiểm tra xem một nước đi có hợp lệ hay không
	private static boolean legal(Move move, MillGame game, MillBoard board) {
		int activePlayer = game.getActivePlayer();// người chơi hiện tại
		int opponent = game.getOpponent();// đối thủ
		int gameState = game.getGameState();// trạng thái game

		// vị trí đích để di chuyển đến không rỗng ==> nước đi không hợp lệ
		if (board.get(move.TO) != BoardInfo.EMPTY) {
			return false;
		}

		// ván game đã kết thúc ==> nước đi không hợp lệ
		if (gameState == MillGame.PHASE_GAME_OVER) {
			return false;
		}
		// ván game vừa bắt đầu ==> chưa thể di chuyển các quân cờ
		else if (gameState == MillGame.PHASE_BEGINNING) {
			if (move.FROM != Move.NOWHERE) {
				return false;
			}
		}
		// trạng thái giữa game
		else if (gameState == MillGame.PHASE_MIDGAME) {
			// from = nowhere ==> ko hợp lệ
			if (move.FROM == Move.NOWHERE) {
				return false;
			}
			// giá trị form không phải quân cờ của người chơi ==> không hợp lệ
			if (board.get(move.FROM) != activePlayer) {
				return false;
			}
			// 2 vị trí di chuyển từ from tới to không kề nhau ==> không hợp lệ
			if (!board.areNeighbours(move.FROM, move.TO)) {
				return false;
			}
		}
		// trạng thái cuối game: được bay
		else if (gameState == MillGame.PHASE_ENDGAME) {

			if (move.FROM == Move.NOWHERE) {
				return false;
			}
			if (board.get(move.FROM) != activePlayer) {
				return false;
			}
		}

		// không có quân bị loại bỏ vì không có mill mới được tạo thành sau nước đi
		if (move.REMOVE == Move.NOWHERE) {
			if (!board.createsNewMill(move, activePlayer)) {
				return true;
			}
		} else {
			if (board.get(move.REMOVE) == opponent && board.createsNewMill(move, activePlayer)) {
				return true;
			}
		}

		// nếu có quân cờ bị loại bỏ
		// kiểm tra đó có phải quân cờ của đối thủ hay không
		/*
		 * else if (board.get(move.REMOVE) == opponent) { // nếu mill mới được tạo thành
		 * if (board.createsNewMill(move, activePlayer)) { // quân cờ bị remove phải
		 * không nằm trong mill if (!board.isSquareInMill(move.REMOVE)) { return true; }
		 * else if (board.allPiecesInMills(opponent)) { return true; } } }
		 */

		return false;
	}

	public static Move[] getValidMoves(MillGame game) {
		int gameState = game.getGameState();

		if (gameState == MillGame.PHASE_BEGINNING) {
			return getValidMoves_beginning(game);
		} else if (gameState == MillGame.PHASE_MIDGAME) {
			return getValidMoves_middlegame(game);
		} else if (gameState == MillGame.PHASE_ENDGAME) {
			return getValidMoves_endgame(game);
		} else {
			return new Move[0];
		}
	}

	// những nước đi hợp lệ ở cuối game:
	// người chơi cò 3 quân cờ được quyền fly
	private static Move[] getValidMoves_endgame(MillGame game) {
		int activePlayer = game.getActivePlayer();
		int opponent = game.getOpponent();
		MillBoard board = game.getMillBoard();

		ArrayList<Move> allMoves = new ArrayList<>();

		if (game.getMillBoard().getColouredSquares(activePlayer).length == 3) {
			for (int from = 0; from < BoardInfo.SQUARES_ON_BOARD; from++) {
				if (board.get(from) != activePlayer) {
					continue;
				}

				// được fly nên không cần lấy danh sách các vị trí kề của quân cờ
				for (int to = 0; to < BoardInfo.SQUARES_ON_BOARD; to++) {
					// nếu vị trí đích đã có quân cờ khác ==> bỏ qua
					if (board.get(to) != BoardInfo.EMPTY || to == from) {
						continue;
					}
					// Nếu nước đi từ from đến to tạo thành Mill mới
					if (board.createsNewMill(new Move(from, to, (int) 0), activePlayer)) {
						for (int remove = 0; remove < BoardInfo.SQUARES_ON_BOARD; remove++) {
							// kiểm tra remove có phải quân cờ của đôi thủ hay không để xóa
							if (board.get(remove) != opponent) {
								continue;
							}
							// nếu ô remove không nằm trong mill ==> add move
							if (!board.isSquareInMill(remove)) {
								System.out.println("From " + from + " To " + to + " Remove " + remove);
								allMoves.add(new Move(from, to, remove));
							}
						}
					} else {
						allMoves.add(new Move(from, to, Move.NOWHERE));
					}
				}
			}
		} else {
			return getValidMoves_middlegame(game);
		}
		// kiểm tra quân cờ from có phải của người đang chơi không

		return allMoves.toArray(new Move[0]);
	}

	// những nước đi hợp lệ ở trạng thái giữa game: di chuyển cờ để tạo thành mill
	private static Move[] getValidMoves_middlegame(MillGame game) {
		int activePlayer = game.getActivePlayer();
		int opponent = game.getOpponent();
		MillBoard board = game.getMillBoard();

		ArrayList<Move> allMoves = new ArrayList<>();
		// kiểm tra quân cờ from có phải của người đang chơi không
		for (int from = 0; from < BoardInfo.SQUARES_ON_BOARD; from++) {
			if (board.get(from) != activePlayer) {
				continue;
			}
			// lấy danh sách các vị trí kề của quân cờ
			int[] neighbours = BoardInfo.getNeighbours(from);
			for (int to : neighbours) {
				// nếu vị trí đích đã có quân cờ khác ==> bỏ qua
				if (board.get(to) != BoardInfo.EMPTY) {
					continue;
				}
				// Nếu nước đi từ from đến to tạo thành Mill mới
				if (board.createsNewMill(new Move(from, to, (int) 0), activePlayer)) {
					for (int remove = 0; remove < BoardInfo.SQUARES_ON_BOARD; remove++) {
						// kiểm tra remove có phải quân cờ của đôi thủ hay không để xóa
						if (board.get(remove) != opponent) {
							continue;
						}
						// nếu ô remove không nằm trong mill ==> add move
						if (!board.isSquareInMill(remove)) {
							allMoves.add(new Move(from, to, remove));
						}
					}
				} else {
					allMoves.add(new Move(from, to, Move.NOWHERE));
				}
			}
		}

		return allMoves.toArray(new Move[0]);
	}

	// những nước đi hợp lệ ở trạng thái đầu game: chỉ được đặt cờ để tạo thành mill
	private static Move[] getValidMoves_beginning(MillGame game) {
		int activePlayer = game.getActivePlayer();
		int opponent = game.getOpponent();
		MillBoard board = game.getMillBoard();

		ArrayList<Move> allMoves = new ArrayList<>();
		// Đầu game chỉ được đặt cờ
		int from = Move.NOWHERE;

		for (int to = 0; to < BoardInfo.SQUARES_ON_BOARD; to++) {
			// Kiểm tra xem ô đích có trống không
			if (board.get(to) != BoardInfo.EMPTY) {
				continue; // Nếu không trống, bỏ qua ô này và tiếp tục vòng lặp
			}

			// Kiểm tra xem nước đi mới có tạo thành Mill không
			if (board.createsNewMill(new Move(from, to, (int) 0), activePlayer)) {
				for (int remove = 0; remove < BoardInfo.SQUARES_ON_BOARD; remove++) {
					// Kiểm tra xem ô cần xóa có phải là quân đối thủ không
					if (board.get(remove) != opponent) {
						continue; // Nếu không phải, bỏ qua ô này và tiếp tục vòng lặp
					}
					// nếu ô remove không nằm trong mill ==> add move
					if (!board.isSquareInMill(remove)) {
						allMoves.add(new Move(from, to, remove));
					}
				}
			} else {
				allMoves.add(new Move(from, to, Move.NOWHERE));
			}
		}

		return allMoves.toArray(new Move[0]);
	}

	private static void printValidMoves(Move[] move) {
		for (Move m : move) {
			System.out.println(m);
		}
	}

	public static void main(String[] args) {
		MillGame test = new MillGame();

		test.setPiece((int) 0, BoardInfo.BLACK);
		test.setPiece((int) 3, BoardInfo.BLACK);
		test.setPiece((int) 13, BoardInfo.BLACK);

		//test.setPiece((int) 18, BoardInfo.BLACK);
		// test.setPiece((int) 21, BoardInfo.BLACK);
		// test.setPiece((int) 23, BoardInfo.BLACK);

		test.setPiece((int) 1, BoardInfo.WHITE);
		test.setPiece((int) 2, BoardInfo.WHITE);
		test.setPiece((int) 4, BoardInfo.WHITE);

		test.setPiece((int) 5, BoardInfo.WHITE);

		test.setPiece((int) 21, BoardInfo.WHITE);
		test.setPiece((int) 19, BoardInfo.WHITE);
		test.setPiece((int) 23, BoardInfo.WHITE);
		test.setPiece((int) 10, BoardInfo.WHITE);

		test.setBlackPiecesInHand(1);
		test.setWhitePiecesInHand(0);

		// test.generateMillGame(MillGame.BLACK_PLAYER, MillGame.PHASE_MIDGAME);
		/*
		 * System.out.println(test.getMillBoard());
		 * System.out.println(test.getMillBoard().allPiecesInMills(MillGame.BLACK_PLAYER
		 * )); System.out.println(test.getMillBoard().isSquareInMill((int) 19)); Move
		 * move = new Move(13, 5, 19);
		 */
		/*
		 * move = new Move(13, 5, 19); System.out.println(isValidMove(move, test));
		 */
		test.makeMove(new Move(-1, 6, -1));
		test.makeMove(new Move(19, 22, 0));
		test.makeMove(new Move(3, 9, -1));
		System.out.println(test);
		printValidMoves(ValidMove.getValidMoves(test));
	}


	public static int[] getAllLegalFROMSquares(MillGame game) {
		Move[] allMoves = getValidMoves(game);
		int[] tmpSquares = new int[allMoves.length];
		for (int i = 0; i < tmpSquares.length; i++) {
			tmpSquares[i] = -2;
		}
		int counter = 0;
		for (int index = 0; index < allMoves.length; index++) {
			int from = allMoves[index].FROM;
			if (from != Move.NOWHERE) {
				if (!containsValue(tmpSquares, from)) {
					tmpSquares[counter] = from;
					counter++;
				}
			}
		}
		return compressArray(tmpSquares, counter);
	}

	public static int[] getAllLegalTOSquares(MillGame game, int fromSquare) {
		Move[] allMoves = getValidMoves(game);
		int[] tmpSquares = new int[allMoves.length]; // t�m� on maksimikoko
		for (int i = 0; i < tmpSquares.length; i++) {
			tmpSquares[i] = -2;
		}
		int counter = 0;
		for (int index = 0; index < allMoves.length; index++) {
			int from = allMoves[index].FROM;
			if (from != fromSquare) {
				continue;
			}
			int to = allMoves[index].TO;
			if (to != Move.NOWHERE) {
				if (!containsValue(tmpSquares, to)) {
					tmpSquares[counter] = to;
					counter++;
				}
			}
		}
		return compressArray(tmpSquares, counter);
	}

	// Jos palautettavan taulun koko on 0, vain 'remove == Move.NOWHERE' on sallittu
	public static int[] getAllLegalREMOVESquares(MillGame game, int fromSquare, int toSquare) {
		Move[] allMoves = getValidMoves(game);
		int[] tmpSquares = new int[allMoves.length]; // t�m� on maksimikoko
		for (int i = 0; i < tmpSquares.length; i++) {
			tmpSquares[i] = -2;
		}
		int counter = 0;
		for (int index = 0; index < allMoves.length; index++) {
			int from = allMoves[index].FROM;
			int to = allMoves[index].TO;
			if (from != fromSquare || to != toSquare) {
				continue;
			}
			int remove = allMoves[index].REMOVE;
			if (remove != Move.NOWHERE) {
				if (!containsValue(tmpSquares, remove)) {
					tmpSquares[counter] = remove;
					counter++;
				}
			}
		}
		return compressArray(tmpSquares, counter);
	}

	private static boolean containsValue(int[] array, int value) {
		for (int index = 0; index < array.length; index++) {
			if (array[index] == value) {
				return true;
			}
		}
		return false;
	}

	private static Move[] compressArray(Move[] array, int count) {
		Move[] compressed = new Move[count];
		for (int moveIndex = 0; moveIndex < count; moveIndex++)
			compressed[moveIndex] = array[moveIndex];
		return compressed;
	}

	private static int[] compressArray(int[] array, int count) {
		int[] compressed = new int[count];
		for (int moveIndex = 0; moveIndex < count; moveIndex++)
			compressed[moveIndex] = array[moveIndex];
		return compressed;
	}
}
