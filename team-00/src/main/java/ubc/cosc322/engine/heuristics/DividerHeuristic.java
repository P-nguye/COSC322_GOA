package ubc.cosc322.engine.heuristics;

import ubc.cosc322.engine.core.Board;
import ubc.cosc322.engine.util.IntList;

/**
 * An experimental heuristic that divides of the territory in chambers by the
 * number of queens that reside in them.
 */
public class DividerHeuristic implements Heuristic {

	@Override
	public int evaluate(Board board) {

		board.computeChambers();
		IntList allChambers = board.getQueenChambers();
		IntList whiteChambers = board.getWhiteChambers();
		IntList blackChambers = board.getBlackChambers();

		int territory = 0;

		int lastChamber = -1;
		for (int i = 0; i < allChambers.size(); i++) {
			int chamber = allChambers.get(i);
			if (chamber == lastChamber) {
				continue;
			}
			int chamberSize = board.getChamberSize(chamber);
			int whiteCount = whiteChambers.count(chamber);
			int blackCount = blackChambers.count(chamber);
			int territoryPerQueen = chamberSize / (whiteCount + blackCount);
			territory += territoryPerQueen * whiteCount;
			territory -= territoryPerQueen * blackCount;
			lastChamber = chamber;
		}

		return territory;

	}
	
}
