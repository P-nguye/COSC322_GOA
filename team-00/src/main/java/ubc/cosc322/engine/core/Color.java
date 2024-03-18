package ubc.cosc322.engine.core;

/** Distinguishes players in the Game of Amazons. */
public enum Color {

	WHITE, BLACK;

	/** Inverts the color. WHITE -> BLACK, BLACK -> WHITE */
	public Color opposite() {
		switch (this) {
			case WHITE:
				return BLACK;
			case BLACK:
				return WHITE;
		}
		return null;
	}

}
