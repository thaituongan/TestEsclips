package view;

import java.awt.Color;
import java.util.Observable;
import java.util.Observer;

import mill.MillBoard;
import mill.MillGame;
import mill.Move;
import mill.ValidMove;

@SuppressWarnings("deprecation")
public class MoveParser extends Observable implements Observer {

	private static final int NONE = Move.NOWHERE;
	private static final long ERRORCIRCLE_DELAY = 700; // ms

	private boolean clickable;
	private GUIMillBoard guiBoard;
	private MillGame game;
	private GUIPlayer player;

	private int selectedFrom, selectedTo, selectedRemove;

	public MoveParser(GUIMillBoard guiBoard) {
		this.game = null;
		this.guiBoard = guiBoard;
		this.guiBoard.addObserver(this);
		this.player = null;
		this.clearSelections();

		this.clickable = false;
	}

	public void setGame(MillGame game) {
		this.game = game;
		this.clearSelections();
	}

	public void setClickable(boolean clickable) {
		this.clickable = clickable;
	}

	public void setPlayer(GUIPlayer player) throws IllegalArgumentException {
		if (player == null) {
			throw new IllegalArgumentException("Parameter 'GUIPlayer' can't be null!");
		}
		this.player = player;
	}

	public void clearSelections() {
		this.selectedFrom = NONE;
		this.selectedTo = NONE;
		this.selectedRemove = NONE;

		this.guiBoard.clearSelectedFromSquare();
		this.guiBoard.clearSelectedToSquare();

		if (player != null) {
			this.player.setInfoText(this.getHelpText());
		}
	}

	@SuppressWarnings("deprecation")
	public void update(Observable o, Object clickedSquare) {
		if (clickedSquare == null) {
			throw new IllegalArgumentException("clickedSquare can't be null!");
		}

		if (!clickable) {
			return;
		}
		int square = ((Integer) clickedSquare).intValue();

		if (square == this.selectedFrom) {
			this.clearSelections();
			this.guiBoard.repaint();
			return;
		} else if (square == this.selectedTo) {
			this.selectedTo = NONE;
			this.guiBoard.clearSelectedToSquare();
			this.guiBoard.repaint();
			return;
		}

		int[] legalFromSquares = ValidMove.getAllLegalFROMSquares(this.game);
		int[] legalToSquares = ValidMove.getAllLegalTOSquares(this.game, this.selectedFrom);
		int[] legalRemoveSquares = ValidMove.getAllLegalREMOVESquares(this.game, this.selectedFrom, this.selectedTo);

		MillBoard board = this.game.getMillBoard();
		int[] ownSquares = board.getColouredSquares(this.game.getActivePlayer());

		if (this.selectedFrom == NONE) {
			if (legalFromSquares.length > 0) { // laillisia 'from'-siirtoja�on ainakin yksi
				if (this.containsSquare(legalFromSquares, square)) { // onko ruutu yksi sallituista?
					this.selectedFrom = square;
					this.guiBoard.setSelectedFromSquare(square);
				} else {
					this.drawError(square);
				}
			} else if (this.selectedTo == NONE) { // pelataan k�dest�: valitaan 'to'
				if (legalToSquares.length > 0) {
					if (this.containsSquare(legalToSquares, square)) {
						this.selectedTo = square;
						this.guiBoard.setSelectedToSquare(square);
					} else {
						this.drawError(square);
					}
				} else {
					throw new IllegalStateException("This line should be unreachable! (1)");
				}
			} else if (this.selectedRemove == NONE) { // pelataan k�dest� ja valitaan sy�t�v� nappula
				if (legalRemoveSquares.length > 0) {
					if (this.containsSquare(legalRemoveSquares, square)) {
						this.selectedRemove = square;
					} else {
						this.drawError(square);
					}
				} else {
					throw new IllegalStateException("This line should be unreachable! (2)");
				}
			} else {
				throw new IllegalStateException("This line should be unreachable! (3)");
			}
		} else {
			if (this.selectedTo == NONE) {
				if (legalToSquares.length > 0) {
					if (this.containsSquare(legalToSquares, square)) {
						this.selectedTo = square;
						this.guiBoard.setSelectedToSquare(square);
					} else {

						if (this.containsSquare(legalFromSquares, square)) {
							this.selectedFrom = square;
							this.guiBoard.setSelectedFromSquare(square);
						} else {
							this.drawError(square);
						}
					}
				} else {
					throw new IllegalStateException("This line should be unreachable! (4)");
				}
			} else if (this.selectedRemove == NONE) { // pelataan k�dest� ja valitaan sy�t�v� nappula
				if (legalRemoveSquares.length > 0) {
					if (this.containsSquare(legalRemoveSquares, square)) {
						this.selectedRemove = square;
					} else {
						// siirrett�v�n nappulan vaihtaminen ennen nappulan poistamista
						if (this.containsSquare(legalFromSquares, square)) {
							this.selectedFrom = square;
							this.selectedTo = NONE;
							this.guiBoard.setSelectedFromSquare(square);
							this.guiBoard.setSelectedToSquare(NONE);
						} else {
							this.drawError(square);
						}
					}
				} else {
					throw new IllegalStateException("This line should be unreachable! (5)");
				}
			} else {
				throw new IllegalStateException("This line should be unreachable! (6)");
			}

		}

		this.player.setInfoText(this.getHelpText());
		this.guiBoard.repaint();

		// Siirto on muodostettu jos:
		// - remove on saanut juuri arvon
		// - to on juuri valittu, mutta laillisia remove-siirtoja ei ole.
		if ((this.selectedRemove != NONE) || (this.selectedTo != NONE
				&& ValidMove.getAllLegalREMOVESquares(this.game, this.selectedFrom, this.selectedTo).length == 0)) {
			super.setChanged();
			super.notifyObservers(new Move(this.selectedFrom, this.selectedTo, this.selectedRemove));

			this.clearSelections();
		}
	}

	private String getHelpText() {
		if (this.game.getGameState() == MillGame.PHASE_GAME_OVER) {
			if (this.game.getActivePlayer() == MillGame.WHITE_PLAYER) {
				return "Game is over. WHITE PLAYER WINS.";
			} else {
				return "Game is over. BLACK PLAYER WINS.";
			}
		} else if (this.game.getGameState() == MillGame.PHASE_BEGINNING) {
			if (this.selectedTo == NONE) {
				return "Click an empty square to place your piece on it.";
			} else {
				return "Click a piece your opponent owns in order to remove it."
						+ " Piece that is a part of mill can't be removed unless all"
						+ " opponent's pieces are in mills.";
			}
		} else {
			if (this.selectedFrom == NONE) {
				return "Click on the piece you want to move. You can only move your own pieces.";
			} else if (this.selectedTo == NONE) {
				return "Click an empty square to move your selected piece to it. If you have"
						+ " more than 3 pieces on the board, you can only move pieces to" + " adjacent squares.";
			} else {
				return "Click a piece your opponent owns in order to remove it."
						+ " Piece that is a part of mill can't be removed unless all"
						+ " opponent's pieces are in mills.";
			}
		}
	}

	private boolean containsSquare(int[] array, int square) {
		if (array == null) {
			return false;
		}
		for (int index = 0; index < array.length; index++) {
			if (array[index] == square) {
				return true;
			}
		}
		return false;
	}

	private void drawError(int square) {
		new ErrorCircle(square, ERRORCIRCLE_DELAY).start(); 
	}

	private class ErrorCircle extends Thread {
		private int square;
		private long delay;

		private ErrorCircle(int square, long delay) {
			this.square = square;
			this.delay = delay;
		}

		public void run() {
			guiBoard.drawSquareCircle(this.square, Color.red);
			try {
				Thread.sleep(this.delay);
			} catch (InterruptedException e) {
			}
			guiBoard.repaint();
		}

	}

}
