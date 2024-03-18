package ubc.cosc322.engine.heuristics;

import ubc.cosc322.engine.core.Color;
import ubc.cosc322.engine.core.Board;
import ubc.cosc322.engine.players.Player;
import ubc.cosc322.engine.util.IntList;

/**
 * An heuristic that performs full rollouts the end state of a game.
 * This is generally a good heuristic, but gets pretty delusional in some end
 * games. This is because naive rollout players will vlaue open areas over long
 * winding pathways, despite greater territory.
 **/
public class RolloutHeuristic implements Heuristic {

	private Player player;
	IntList simulationBuffer;

	public RolloutHeuristic(Player player) {
		this.player = player;
		this.simulationBuffer = new IntList(200);
	}

	@Override
	public int evaluate(Board board) {
		player.useBoard(board);
		do {
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
