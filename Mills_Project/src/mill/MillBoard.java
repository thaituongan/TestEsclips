package mill;

/*Lớp MillBoard đại diện cho bàn cờ trong trò chơi. 
Nó cung cấp các phương thức để thao tác với các ô và quân cờ trên bàn cờ*/

public class MillBoard {
	private int[] squares;

	public MillBoard() {
		this.squares = new int[BoardInfo.SQUARES_ON_BOARD];
	}

	public int[] getSquares() {
		return squares;
	}

	// tạo bản sao cho bàn cờ
	public Object clone() {
		MillBoard copy = new MillBoard();
		for (int index = 0; index < this.squares.length; index++) {
			copy.squares[index] = this.squares[index];
		}
		return copy;
	}

	// đặt một quân cờ vào bàn cờ
	public void setPiece(int square, int color) throws IllegalArgumentException, IllegalStateException {
		if ((color != BoardInfo.BLACK) && (color != BoardInfo.WHITE)) {
			throw new IllegalArgumentException("setPiece(int,int): unknown color value:" + color);
		}
		if (this.squares[square] != BoardInfo.EMPTY) {
			throw new IllegalStateException("setPiece(int,int): square " + square + " is occupied.");
		}
		this.squares[square] = color;
	}

	// xóa một quân cờ khỏi bàn cờ
	public void removePiece(int square) {
		this.squares[square] = BoardInfo.EMPTY;
	}

	public void movePiece(int fromSquare, int toSquare) throws IllegalArgumentException, IllegalStateException {
		this.setPiece(toSquare, this.squares[fromSquare]);
		this.removePiece(fromSquare);
	}

	// lấy giá trị của một ô cờ trên bàn cờ
	public int get(int square) {
		return this.squares[square];
	}

	// trả về một mảng chứa chỉ số của các ô trên bàn cờ có màu tương ứng với color.
	public int[] getColouredSquares(int color) throws IllegalArgumentException {
		if ((color != BoardInfo.BLACK) && (color != BoardInfo.WHITE) && (color != BoardInfo.EMPTY)) {
			throw new IllegalArgumentException("getSquares(int): unknown square value:" + color);
		}

		// Mảng tạm để lưu trữ tạm thời
		int[] tmp = new int[BoardInfo.SQUARES_ON_BOARD]; // Đủ 24 chỉ số
		int squareCounter = 0;
		for (int index = 0; index < this.squares.length; index++) {
			if (this.squares[index] == color) {
				tmp[squareCounter] = (int) index;
				squareCounter++;
			}
		}

		// Tạo mảng mới với kích thước thích hợp
		int[] array = new int[squareCounter];
		for (int index = 0; index < squareCounter; index++) {
			array[index] = tmp[index];
		}
		return array;
	}

	// kiểm tra 2 ô cờ có kề nhau không
	public boolean areNeighbours(int square1, int square2) {
		int[] neighbours = BoardInfo.getNeighbours(square1);
		for (int neighbour : neighbours) {
			if (neighbour == square2) {
				return true;
			}
		}
		return false;
	}

	// xác định xem một nước đi có tạo thành mills mới hay không
	public boolean createsNewMill(Move move, int color) throws IllegalArgumentException {
		if ((color != BoardInfo.BLACK) && (color != BoardInfo.WHITE)) {
			throw new IllegalArgumentException("createsNewMill(Move,int): unknown color value:" + color);
		}
		if ((move.FROM != Move.NOWHERE) && (this.squares[move.FROM] != color)) {
			throw new IllegalArgumentException("createsNewMill(Move,int): parameter color:" + color
					+ " doesn't match the color of piece being" + "moved:" + this.squares[move.FROM]);
		}
		int[][] millLines = BoardInfo.getMillLines(move.TO);
		int[] millLine_1 = millLines[0];
		int[] millLine_2 = millLines[1];

		if (((this.squares[millLine_1[0]] == color) && (millLine_1[0] != move.FROM))
				&& ((this.squares[millLine_1[1]] == color) && (millLine_1[1] != move.FROM))) {
			return true;
		}

		if (((this.squares[millLine_2[0]] == color) && (millLine_2[0] != move.FROM))
				&& ((this.squares[millLine_2[1]] == color) && (millLine_2[1] != move.FROM))) {
			return true;
		}
		return false;

	}

	public boolean allPiecesInMills(int color) throws IllegalArgumentException {
	    if ((color != BoardInfo.BLACK) && (color != BoardInfo.WHITE)) {
	        throw new IllegalArgumentException("allPiecesInMills(): unknown color value: " + color);
	    }
	    int[] colouredSquares = this.getColouredSquares(color);
	    for (int square : colouredSquares) {
	        if (this.squares[square] != color || !isSquareInMill(square)) {
	            return false;
	        }
	    }
	    return true;
	}

	public boolean isSquareInMill(int square) {
		 if (this.squares[square] == BoardInfo.EMPTY) {
	            return false;
	        }
	        int[][] millLines = BoardInfo.getMillLines(square);
	        int[] millLine_1 = millLines[0];
	        int[] millLine_2 = millLines[1];
	        int color = this.squares[square];

	        return ( ((this.squares[millLine_1[0]] == color) && (this.squares[millLine_1[1]] == color)) ||
	                 ((this.squares[millLine_2[0]] == color) && (this.squares[millLine_2[1]] == color))   );
	}
	

	// kiểm tra cờ của một player có bị kẹt hay không
	public boolean allPiecesJammed(int color) throws IllegalArgumentException {
	    if ((color != BoardInfo.BLACK) && (color != BoardInfo.WHITE)) {
	        throw new IllegalArgumentException("allPiecesInMills(int): unknown color value:" + color);
	    }
	    int[] colouredSquares = this.getColouredSquares(color);
	    for (int square : colouredSquares) {
	        if (!this.pieceJammed(square)) {
	            return false;
	        }
	    }
	    return true;
	}

	// kiểm tra một ô cờ có bị kẹt hay không
	private boolean pieceJammed(int square) {
	    int[] neighbours = BoardInfo.getNeighbours(square);
	    for (int neighbour : neighbours) {
	        if (this.squares[neighbour] == BoardInfo.EMPTY) {
	            return false;
	        }
	    }
	    return true;
	}

	public String toString() {
		char[] squarecharacters = new char[this.squares.length];
		for (int squareIndex = 0; squareIndex < this.squares.length; squareIndex++) {
			if (this.squares[squareIndex] == BoardInfo.EMPTY) {
				squarecharacters[squareIndex] = ' ';
			} else if (this.squares[squareIndex] == BoardInfo.WHITE) {
				squarecharacters[squareIndex] = 'w';
			} else if (this.squares[squareIndex] == BoardInfo.BLACK) {
				squarecharacters[squareIndex] = 'b';
			}
		}

		char[] s = squarecharacters;
		return "" + "  " + s[0] + " --------- " + s[1] + " --------- " + s[2]
				+ "                      0 --------- 1 --------- 2" + "\n" + "  |           |           |"
				+ "                      |           |           |" + "\n" + "  |   " + s[3] + " ----- " + s[4]
				+ " ----- " + s[5] + "   |" + "                      |   3 ----- 4 ----- 5   |" + "\n"
				+ "  |   |       |       |   |" + "                      |   |       |       |   |" + "\n"
				+ "  |   |   " + s[6] + " - " + s[7] + " - " + s[8] + "   |   |"
				+ "                      |   |   6 - 7 - 8   |   |" + "\n" + "  |   |   |       |   |   |"
				+ "                      |   |   |       |   |   |" + "\n" + "  " + s[9] + " - " + s[10] + " - " + s[11]
				+ "       " + s[12] + " - " + s[13] + " - " + s[14] + "                      9 - 10- 11      12- 13- 14"
				+ "\n" + "  |   |   |       |   |   |" + "                      |   |   |       |   |   |" + "\n"
				+ "  |   |   " + s[15] + " - " + s[16] + " - " + s[17] + "   |   |"
				+ "                      |   |   15- 16- 17  |   |" + "\n" + "  |   |       |       |   |"
				+ "                      |   |       |       |   |" + "\n" + "  |   " + s[18] + " ----- " + s[19]
				+ " ----- " + s[20] + "   |" + "                      |   18----- 19----- 20  |" + "\n"
				+ "  |           |           |" + "                      |           |           |" + "\n" + "  "
				+ s[21] + " --------- " + s[22] + " --------- " + s[23]
				+ "                      21--------- 22--------- 23";

	}

}
