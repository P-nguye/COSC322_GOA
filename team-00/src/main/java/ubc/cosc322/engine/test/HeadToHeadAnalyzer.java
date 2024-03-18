package ubc.cosc322.engine.test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import ubc.cosc322.engine.core.Color;
import ubc.cosc322.engine.core.Board;
import ubc.cosc322.engine.core.Turn;
import ubc.cosc322.engine.players.Player;

/** Pits two Players against eachother offline. */
public class HeadToHeadAnalyzer implements AutoCloseable {

	Board initialState;
	Player whitePlayer, blackPlayer;
	private Map<Color,Integer> winCounts;
	private List<Consumer<Board>> turnCallbacks;
	private List<Consumer<Board>> endCallbacks;
	
	public HeadToHeadAnalyzer(Board initialState, Player whitePlayer, Player blackPlayer) {
		if (whitePlayer == blackPlayer) {
			throw new IllegalArgumentException("seperate players required");
		}
		this.initialState = initialState;
		this.whitePlayer = whitePlayer;
		this.blackPlayer = blackPlayer;
		this.winCounts = new EnumMap<>(Color.class);
		winCounts.put(Color.WHITE, Integer.valueOf(0));
		winCounts.put(Color.BLACK, Integer.valueOf(0));
		this.turnCallbacks = new ArrayList<>();
		this.endCallbacks = new ArrayList<>();
	}

	public void play(int n) {
		for (int i = 0; i < n; i++) {
			Board playState = initialState.clone();
			whitePlayer.useBoard(initialState.clone());
			blackPlayer.useBoard(initialState.clone());
			while (true) {
				Player playerToMove, playerToWait;
				if (playState.getColorToMove() == Color.WHITE) {
					playerToMove = whitePlayer;
					playerToWait = blackPlayer;
				} else {
					playerToMove = blackPlayer;
					playerToWait = whitePlayer;
				}
				Turn turn = playerToMove.suggestAndDoTurn();
				if (turn == null) {
					break;
				}
				playerToWait.doTurn(turn);
				playState.doTurn(turn);
				for (Consumer<Board> callback : turnCallbacks) {
					callback.accept(playState);
				}
			}
			for (Consumer<Board> callback : endCallbacks) {
				callback.accept(playState);
			}
			Color winner = playState.getColorToMove().opposite();
			winCounts.put(
				winner,
				Integer.valueOf(
					winCounts.get(winner).intValue() + 1
				)
			);
		}
	}

	public float getWinRate(Color color) {
		return (float) getWinCount(color) / getGamesPlayed();
	}

	public int getWinCount(Color color) {
		return winCounts.get(color).intValue();
	}

	public int getGamesPlayed() {
		return winCounts.get(Color.WHITE).intValue() + winCounts.get(Color.BLACK).intValue();
	}

	public void onTurn(Consumer<Board> callback) {
		turnCallbacks.add(callback);
	}

	public void onEnd(Consumer<Board> callback) {
		endCallbacks.add(callback);
	}

	@Override
	public void close() throws Exception {
		if (whitePlayer instanceof AutoCloseable) {
			((AutoCloseable) whitePlayer).close();
		}
		if (blackPlayer instanceof AutoCloseable) {
			((AutoCloseable) blackPlayer).close();
		}
	}

}
