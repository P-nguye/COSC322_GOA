package ubc.cosc322.engine.core;

/**
 * In our implemenation, a turn is split into two moves, a queen move, and an
 * arrow move. We previously also consided the queen pick to e third move type.
 */
public enum MoveType {

	QUEEN, ARROW;

	/** The number of move types. */
	public static final int COUNT = 2;

	/** The next move type in the sequence. */
	public MoveType next() {
		switch (this) {
			case QUEEN:
				return ARROW;
			case ARROW:
				return QUEEN;
		}
		return null;
	}

	/** The previous move type in the sequence. */
	public MoveType previous() {
		switch (this) {
			case QUEEN:
				return ARROW;
			case ARROW:
				return QUEEN;
		}
		return null;
	}

}
