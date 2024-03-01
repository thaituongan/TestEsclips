package view;

import java.util.Observable;
import java.util.Observer;

import mill.AI;
import mill.MillGame;
import mill.Move;

@SuppressWarnings("deprecation")
public class GUIControl implements Observer {
	private MillGame game;
	private GUIMillBoard guiBoard;
	private GUI gui;
	private GUIPlayer whitePlayer, blackPlayer;
	private MoveParser moveParser;
	private AI ai;

	private boolean highlightLegalSquares;
	private boolean gameRunning;
	private boolean thinking;
	private Thread infoThread;
	private Thread cpuThread;
	private Thread cpuMatchThread;

	public GUIControl(GUIMillBoard guiBoard, GUI gui) {
		if (guiBoard == null) {
			throw new IllegalArgumentException("GUIControl(GUIMillBoard,GUI): Parameter 'GUIMillBoard' can't be null.");
		}
		if (gui == null) {
			throw new IllegalArgumentException("GUIControl(GUIMillBoard,GUI): Parameter 'GUI' can't be null.");
		}
		this.game = null;
		this.guiBoard = guiBoard;
		this.gui = gui;
		this.whitePlayer = null;
		this.blackPlayer = null;

		this.moveParser = new MoveParser(this.guiBoard);
		((Observable) this.moveParser).addObserver((Observer) this);
		this.ai = new AI();

		this.highlightLegalSquares = true;
		this.gameRunning = false;
		this.thinking = false;
		this.infoThread = null;
		this.cpuThread = null;
		this.cpuMatchThread = null;
	}

	public void setHighlightLegalSquares(boolean value) {
		if (this.highlightLegalSquares != value) {
			this.highlightLegalSquares = value;

			if (this.gameRunning && !this.CPUIsActive()) {
				this.guiBoard.setHighlightLegalSquares(value);
				this.guiBoard.repaint();
			}
		}
	}

	public void newGame(MillGame game, GUIPlayer whitePlayer, GUIPlayer blackPlayer) {
		this.stopGame();
		this.game = game;
		this.continueGame(whitePlayer, blackPlayer);
	}

	public void continueGame(GUIPlayer whitePlayer, GUIPlayer blackPlayer) {

		if (this.thinking) {
			System.out.println("Estetty!");
			return;
		}
		this.whitePlayer = whitePlayer;
		this.blackPlayer = blackPlayer;

		if (this.game.getGameState() == MillGame.PHASE_GAME_OVER) {
			System.out.println("Can't continue: Game is over");
			return;
		}

		this.gameRunning = true;
		this.moveParser.setGame(this.game);
		this.guiBoard.setGame(this.game);
		this.setPlayerChoicesActive(false);
		this.guiBoard.setHighlightLegalSquares(this.highlightLegalSquares);
		this.setActivePlayer();

		if (whitePlayer.getPlayerChoice() == GUIPlayer.CPU && blackPlayer.getPlayerChoice() == GUIPlayer.CPU) {
			this.moveParser.setClickable(false);
			this.cpuMatchThread = new GUIControl.CPUMatchThread();
			this.cpuMatchThread.start();
		} else {

			if (this.game.getActivePlayer() == MillGame.WHITE_PLAYER) {
				if (this.whitePlayer.getPlayerChoice() == GUIPlayer.CPU) {
					this.computerMoves();
					this.moveParser.setPlayer(this.blackPlayer);
				} else {
					this.moveParser.setPlayer(this.whitePlayer);
					this.moveParser.setClickable(true);
				}
			} else {
				if (this.blackPlayer.getPlayerChoice() == GUIPlayer.CPU) {
					this.computerMoves();
					this.moveParser.setPlayer(this.whitePlayer);
				} else {
					this.moveParser.setPlayer(this.blackPlayer);
					this.moveParser.setClickable(true);
				}
			}
		}
		this.guiBoard.repaint();
		this.gui.refreshButtons();
	}

	public void stopGame() {
		this.gameRunning = false;
		this.moveParser.setClickable(false);
		this.guiBoard.setHighlightLegalSquares(false);
		this.clearActivePlayer();
		this.setPlayerChoicesActive(true);

		/*
		 * this.ai.stopSearch(); if (this.infoThread != null)
		 * this.infoThread.interrupt(); if (this.cpuThread != null)
		 * this.cpuThread.interrupt(); if (this.cpuMatchThread != null)
		 * this.cpuMatchThread.interrupt();
		 */

		this.gui.refreshButtons();
		this.guiBoard.repaint();
	}

	public void undo() {
		this.stopGame();
		this.moveParser.clearSelections();
		this.game.undo();
		this.guiBoard.repaint();
		this.gui.refreshButtons();
	}

	public void redo() {
		this.stopGame();
		this.moveParser.clearSelections();
		this.game.redo();
		this.guiBoard.repaint();
		this.gui.refreshButtons();
	}

	public boolean gameIsRunning() {
		return this.gameRunning;
	}

	public void update(Observable o, Object move) {
		int activePlayer = this.game.getActivePlayer();
		if (this.CPUIsActive()) {
			return;
		}

		boolean victory = this.game.makeMove((Move) move);
		GUIControl.this.gui.refreshButtons();
		if (victory) {
			this.victory();
		} else {
			if (this.CPUIsActive()) {
				this.computerMoves();
			} else {
				this.setActivePlayer();
				if (this.game.getActivePlayer() == MillGame.WHITE_PLAYER) {
					this.moveParser.setPlayer(this.whitePlayer);
				} else {
					this.moveParser.setPlayer(this.blackPlayer);
				}
			}
		}
	}

	private void computerMoves() {
		this.moveParser.setClickable(false);
		this.cpuThread = new GUIControl.CPUThread();
		this.thinking = true;
		this.cpuThread.start();
	}

	private class CPUThread extends Thread {
		public void run() {
			GUIControl.this.setActivePlayer();
			GUIControl.this.guiBoard.setHighlightLegalSquares(false);

			int level;
			if (game.getActivePlayer() == MillGame.WHITE_PLAYER) {
				whitePlayer.setInfoText("");
				infoThread = new GUIControl.InfoThread(whitePlayer);
				level = whitePlayer.getCPULevelChoice();
			} else {
				blackPlayer.setInfoText("");
				infoThread = new GUIControl.InfoThread(blackPlayer);
				level = blackPlayer.getCPULevelChoice();
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}

			if (!GUIControl.this.gameRunning) {
				GUIControl.this.thinking = false;
				GUIControl.this.clearActivePlayer();
				return;
			}

		

			Move move;
			GUIControl.this.infoThread.start();
			System.out.println(level);

			if (level == GUIPlayer.Easy)
				move = ai.depthSearch(GUIControl.this.game, 3);
			else if (level == GUIPlayer.Medium)
				move = ai.depthSearch(GUIControl.this.game, 4);
			else if (level == GUIPlayer.Hard)
				move = ai.depthSearch(GUIControl.this.game, 5);
			else
				move = ai.depthSearch(GUIControl.this.game, 1);

			System.out.println(level == GUIPlayer.Easy);
			GUIControl.this.infoThread.interrupt();
			if (GUIControl.this.gameRunning) {
				if (GUIControl.this.game.makeMove(move)) {
					GUIControl.this.victory();
				} else {
					GUIControl.this.moveParser.setClickable(!CPUIsActive());
					GUIControl.this.setActivePlayer();
				}
			}
			GUIControl.this.thinking = false;
			GUIControl.this.guiBoard.repaint();
			GUIControl.this.gui.refreshButtons();
			System.out.println("CPUThread kuoli...");
		}
	}

	private class InfoThread extends Thread {
		private GUIPlayer targetPlayer;

		public InfoThread(GUIPlayer player) {
			if (player == null) {
				throw new IllegalArgumentException("InfoThread(GUIPlayer): " + "Parameter GUIPlayer can't be null");
			}
			this.targetPlayer = player;
		}

		public void run() {
			try {
				while (!this.isInterrupted()) {
					this.targetPlayer.setInfoText(GUIControl.this.ai.toString());
					Thread.sleep(200);
				}
			} catch (InterruptedException e) {
			}

			this.targetPlayer.setInfoText(GUIControl.this.ai.toString());

		}
	}

	private class CPUMatchThread extends Thread {
		public void run() {
			Thread computerMove = new GUIControl.CPUThread();
			while (!this.isInterrupted() && gameRunning) {
				GUIControl.this.thinking = true;
				computerMove.run();
			}

		}
	}

	private boolean CPUIsActive() {
		int activePlayer = this.game.getActivePlayer();
		if (activePlayer == MillGame.WHITE_PLAYER && this.whitePlayer.getPlayerChoice() == GUIPlayer.CPU) {
			return true;
		} else if (activePlayer == MillGame.BLACK_PLAYER && this.blackPlayer.getPlayerChoice() == GUIPlayer.CPU) {
			return true;
		}
		return false;
	}

	private void setActivePlayer() {
		if (this.whitePlayer == null || this.blackPlayer == null) {
			return;
		}

		boolean highlightPossible = false;
		if (this.game.getActivePlayer() == MillGame.WHITE_PLAYER) {
			this.whitePlayer.setActive(true);
			this.whitePlayer.showInfoText(true);
			this.blackPlayer.setActive(false);
			if (this.whitePlayer.getPlayerChoice() == GUIPlayer.HUMAN) {
				highlightPossible = true;
			}
			if (this.blackPlayer.getPlayerChoice() == GUIPlayer.HUMAN) {
				this.blackPlayer.showInfoText(false);
			}
		} else {
			this.whitePlayer.setActive(false);
			this.blackPlayer.setActive(true);
			this.blackPlayer.showInfoText(true);
			if (this.blackPlayer.getPlayerChoice() == GUIPlayer.HUMAN) {
				highlightPossible = true;
			}
			if (this.whitePlayer.getPlayerChoice() == GUIPlayer.HUMAN) {
				this.whitePlayer.showInfoText(false);
			}
		}
		if (highlightPossible) {
			this.guiBoard.setHighlightLegalSquares(this.highlightLegalSquares);
		} else {
			this.guiBoard.setHighlightLegalSquares(false);
		}

	}

	private void clearActivePlayer() {
		if (this.whitePlayer != null) {
			whitePlayer.setActive(false);
		}
		if (this.blackPlayer != null) {
			blackPlayer.setActive(false);
		}
		this.guiBoard.setHighlightLegalSquares(false);
	}

	private void setPlayerChoicesActive(boolean value) {
		if (this.whitePlayer != null) {
			whitePlayer.setChoicesActive(value);
		}
		if (this.blackPlayer != null) {
			blackPlayer.setChoicesActive(value);
		}
	}

	private void victory() {
		this.gameRunning = false;
		System.out.println("WIN!");
	}
}
