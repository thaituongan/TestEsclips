package mill;

public class BoardInfo {
	public static final int SQUARES_ON_BOARD = 24, EMPTY = 0, BLACK = 1, WHITE = 2;

	//mảng chứa các ô kề
	private static final int[][] NEIGHBOUR_SQUARES = { { 1, 9 }, { 0, 2, 4 }, { 1, 14 }, { 4, 10 }, { 1, 3, 5, 7 },
			{ 4, 13 }, { 7, 11 }, { 4, 6, 8 }, { 7, 12 }, { 0, 10, 21 }, { 3, 9, 11, 18 }, { 6, 10, 15 }, { 8, 13, 17 },
			{ 5, 12, 14, 20 }, { 2, 13, 23 }, { 11, 16 }, { 15, 17, 19 }, { 12, 16 }, { 10, 19 }, { 16, 18, 20, 22 },
			{ 13, 19 }, { 9, 22 }, { 19, 21, 23 }, { 14, 22 } };
	//danh lấy danh số các ô kề của một ô trên bàn cờ
	public static int[] getNeighbours(int square) {
		int[] neighbours = new int[NEIGHBOUR_SQUARES[square].length];
		for (int neighbour = 0; neighbour < neighbours.length; neighbour++) {
			neighbours[neighbour] = NEIGHBOUR_SQUARES[square][neighbour];
		}
		return neighbours;
	}
	
	
	//tạo mảng đối tượng chứa các dòng mill
	private static final Object[] MILL_LINES;

	private static Object[] createMillLines() {
		//mỗi một ô có 2 trạng thái hình thành mill
		int[][] square_00 = { { 1, 2 }, { 9, 21 } }, square_01 = { { 0, 2 }, { 4, 7 } },
				square_02 = { { 0, 1 }, { 14, 23 } }, square_03 = { { 4, 5 }, { 10, 18 } },
				square_04 = { { 1, 7 }, { 3, 5 } }, square_05 = { { 3, 4 }, { 13, 20 } },
				square_06 = { { 7, 8 }, { 11, 15 } }, square_07 = { { 1, 4 }, { 6, 8 } },
				square_08 = { { 6, 7 }, { 12, 17 } }, square_09 = { { 0, 21 }, { 10, 11 } },
				square_10 = { { 3, 18 }, { 9, 11 } }, square_11 = { { 6, 15 }, { 9, 10 } },
				square_12 = { { 8, 17 }, { 13, 14 } }, square_13 = { { 5, 20 }, { 12, 14 } },
				square_14 = { { 2, 23 }, { 12, 13 } }, square_15 = { { 6, 11 }, { 16, 17 } },
				square_16 = { { 15, 17 }, { 19, 22 } }, square_17 = { { 8, 12 }, { 15, 16 } },
				square_18 = { { 3, 10 }, { 19, 20 } }, square_19 = { { 16, 22 }, { 18, 20 } },
				square_20 = { { 5, 13 }, { 18, 19 } }, square_21 = { { 0, 9 }, { 22, 23 } },
				square_22 = { { 16, 19 }, { 21, 23 } }, square_23 = { { 2, 14 }, { 21, 22 } };

		Object[] millLines = new Object[24];
		millLines[0] = square_00;
		millLines[1] = square_01;
		millLines[2] = square_02;
		millLines[3] = square_03;
		millLines[4] = square_04;
		millLines[5] = square_05;
		millLines[6] = square_06;
		millLines[7] = square_07;
		millLines[8] = square_08;
		millLines[9] = square_09;
		millLines[10] = square_10;
		millLines[11] = square_11;
		millLines[12] = square_12;
		millLines[13] = square_13;
		millLines[14] = square_14;
		millLines[15] = square_15;
		millLines[16] = square_16;
		millLines[17] = square_17;
		millLines[18] = square_18;
		millLines[19] = square_19;
		millLines[20] = square_20;
		millLines[21] = square_21;
		millLines[22] = square_22;
		millLines[23] = square_23;

		return millLines;
	}

	static {
		MILL_LINES = createMillLines();
	}

	   public static int[][] getMillLines(int square) {
	        int[][] millLines = {
	            { ((int[][])MILL_LINES[square])[0][0], ((int[][])MILL_LINES[square])[0][1]},
	            { ((int[][])MILL_LINES[square])[1][0], ((int[][])MILL_LINES[square])[1][1]}
	                             };
	        return millLines;
	    }
	   
	   //test
	   public static void main(String[] args) {
	        int[] neighbours = getNeighbours((int)5);
	        for (int neighbour=0; neighbour < neighbours.length; neighbour++) {
	            System.out.print(neighbours[neighbour]+", ");
	        }
	        System.out.println( " --> 4, 13 OK");

	        int[][] millLines = getMillLines((int)5);
	        for (int millLine=0; millLine < 2; millLine++) {
	            for (int index=0; index < 2; index++) {
	                System.out.print(millLines[millLine][index]+",");
	            }
	            System.out.println();
	        }
	        System.out.println( " --> (3,4) && (13,20) OK");

	    }
	

}
