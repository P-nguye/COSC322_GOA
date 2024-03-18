package ubc.cosc322.engine.heuristics;

import ubc.cosc322.engine.core.Color;
import ubc.cosc322.engine.core.Board;
import ubc.cosc322.engine.players.Player;
import ubc.cosc322.engine.util.IntList;

/**
 * A modified rollout heuristic that detects and returns an endgame heuristic
 * once an endgame is reached.
 */
public class HybridRolloutHeuristic implements Heuristic {

	private Player player;
	private IntList simulationBuffer;
	private EndgameHeuristic endgame;

	public HybridRolloutHeuristic(Player player) {
		this.player = player;
		this.simulationBuffer = new IntList(4);
		this.endgame = new EndgameHeuristic();
	}

	@Override
	public int evaluate(Board board) {
		player.useBoard(board);
		do {
			int endgameEvaluation = endgame.evaluate(board);
			if (endgameEvaluation != 0) {
				return endgameEvaluation;
			}
			simulationBuffer.clear();
			player.suggestAndDoMoves(simulationBuffer.capacity(), simulationBuffer);
		} while (simulationBuffer.size() != 0);
		if (board.getColorToMove() == Color.WHITE) {
			return -1;
		} else {
			return 1;
		}
	}
	
}
