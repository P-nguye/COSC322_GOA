package ubc.cosc322.engine.players;

import ubc.cosc322.engine.core.MoveType;
import ubc.cosc322.engine.core.Board;
import ubc.cosc322.engine.core.Turn;
import ubc.cosc322.engine.util.IntList;

/** An interface that abstracts a Game of Amazons player. */
public abstract class Player {
	
	protected Board board;

	/** Tells the player which baord object it will be using. */
	public void useBoard(Board board) {
		this.board = board;
	}

	/** Gets the board object back. */
	public Board getBoard() {
		return board;
	}

	/** Performs a turn from an opponent or external source. */
	public void doTurn(Turn turn) {
		board.doTurn(turn);
	}

	/** Performs a move from an opponent or external source. */
	public void doMove(int move) {
		board.doMove(move);
	}

	/** Performs and outputs up to a certain number of moves. */
	public abstract void suggestAndDoMoves(int maxMoves, IntList output);

	/** Performs and outputs exactly one turn. */
	public Turn suggestAndDoTurn() {
		if (board.getNextMoveType() != MoveType.QUEEN) {
			throw new IllegalStateException("when suggesting a turn, it must be the start of a turn");
		}
		IntList moves = new IntList(MoveType.COUNT);
		int lastSize = 0;
		while (moves.size() != MoveType.COUNT) {
			suggestAndDoMoves(MoveType.COUNT - lastSize, moves);
			int received = moves.size() - lastSize;
			if (received == 0) {
				return null;
			}
			lastSize = moves.size();
		}
		return new Turn(
			moves.get(0),
			moves.get(1)
		);
	}

}
