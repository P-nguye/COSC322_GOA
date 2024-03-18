package ubc.cosc322.engine.core;

/** A single turn in the Game of Amazons. A turn has two moves, a queen move and an arrow move. */
public class Turn {
	
	/** The two moves of a turn. */
	public final int queenMove, arrowMove;

	/** Construct a turn directly from the two moves. */
	public Turn(int queenMove, int arrowMove) {
		this.queenMove = queenMove;
		this.arrowMove = arrowMove;
	}

	/** Construct a turn from a triplet of positions. */
	public Turn(int queenSource, int queenDestination, int arrowMove) {
		this.queenMove = Move.encodeQueenMove(queenSource, queenDestination);
		this.arrowMove = arrowMove;
	}

	/** Gets the position the queen is moving from. */
	public int getQueenSource() {
		return Move.decodeQueenSource(queenMove);
	}

	/** Gets the position the queen is moving to. */
	public int getQueenDestination() {
		return Move.decodeQueenDestination(queenMove);
	}

	/** Gets the position of the arrow being shot. */
	public int getArrowDestination() {
		return arrowMove;
	}

}
