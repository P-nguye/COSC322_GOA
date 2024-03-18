package ubc.cosc322.engine.core;

/** The 8 directions pieces can move in the Game of Amazons. */
public enum Direction {

	UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0),
	UP_LEFT(-1, -1), UP_RIGHT(1, -1), DOWN_RIGHT(1, 1), DOWN_LEFT(-1, 1);

	/** Coordinate offsets. One of (-1, 0, 1). */
	public final int x, y;

	/** The number of directions. */
	public static final int COUNT = 8;

	private Direction(int x, int y) {
		this.x = x;
		this.y = y;
	}

}
