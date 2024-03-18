package ubc.cosc322.engine.heuristics;

import ubc.cosc322.engine.core.Board;

/** An interface to abstract heuristic evaluation for positions in the Game of Amazons. */
public interface Heuristic {
	
	/** Returns a evaluation of what color is winning. WHITE -> positive, BLACK -> negative */
	public int evaluate(Board board);

}
