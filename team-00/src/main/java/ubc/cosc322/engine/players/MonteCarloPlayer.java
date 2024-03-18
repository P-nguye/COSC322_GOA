package ubc.cosc322.engine.players;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.function.Supplier;

import ubc.cosc322.engine.core.Color;
import ubc.cosc322.engine.core.MoveType;
import ubc.cosc322.engine.core.Board;
import ubc.cosc322.engine.core.Turn;
import ubc.cosc322.engine.generators.MoveGenerator;
import ubc.cosc322.engine.heuristics.Heuristic;
import ubc.cosc322.engine.util.IntList;

/** A player that uses a lockless multi-threaded Monte Carlo Tree Search. */
public class MonteCarloPlayer extends Player implements AutoCloseable {

	// whether the worker threads should be running or not
	private volatile boolean running;

	// the number of threads to use while running
	private int threadCount;

	// a list of worker threads, so they can be closed later
	private ArrayList<Thread> workerThreads;

	// provides heuristic functions to the worker threads
	private Supplier<Heuristic> heuristicSupplier;

	// provides move generators to the worker threas
	private Supplier<MoveGenerator> moveGenSupplier;
	
	// a move generator for use by the main thread
	private MoveGenerator rootMoveGenerator;

	// the amount of time to spend think when suggesting a move
	private int thinkingMillis;

	// the exploration factor to be used in UCB1/UCT
	private double explorationFactor;

	// the root node of the tree
	// needs to volatile so worker threads can observe changes made by main
	private volatile Node root;

	// because stats are stored in arrays in the parent nodes
	// root stats need to stored separately
	private volatile RootStats rootStats;

	// a stats objet that can be retrieved by other code
	private Stats publicStats;

	public MonteCarloPlayer(Supplier<Heuristic> heuristicSupplier, Supplier<MoveGenerator> moveGenSupplier, int threadCount, int thinkingMillis, double explorationFactor) {
		this.running = false;
		this.thinkingMillis = thinkingMillis;
		this.explorationFactor = explorationFactor;
		this.threadCount = threadCount;
		this.heuristicSupplier = heuristicSupplier;
		this.moveGenSupplier = moveGenSupplier;
		this.rootMoveGenerator = moveGenSupplier.get();
		this.workerThreads = new ArrayList<>(threadCount);
	}

	@Override
	public void useBoard(Board board) {
		try {
			close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.useBoard(board);
		root = new Node(board, rootMoveGenerator);
		rootStats = new RootStats();
		running = true;
		workerThreads.clear();
		for (int i = 0; i < threadCount; i++) {
			Thread thread = new Thread(new Worker());
			// set the thread priority to low so it doesn't ruin main thread
			// responsivness, which can cause problems with turn timings
			thread.setPriority(Thread.MIN_PRIORITY);
			thread.start();
			workerThreads.add(thread);
		}
	}

	@Override
	public void doMove(int move) {
		publicStats = new Stats(rootStats);
		super.doMove(move);
		if (!rebase(move)) {
			this.root = new Node(board, rootMoveGenerator);
			this.rootStats = new RootStats();
		}
	}

	@Override
	public void doTurn(Turn turn) {
		publicStats = new Stats(rootStats);
		super.doTurn(turn);
		if (!rebase(turn.queenMove) || !rebase(turn.arrowMove)) {
			this.root = new Node(board, rootMoveGenerator);
			this.rootStats = new RootStats();
		}
	}

	// rebasing allows the search tree to reuse what is has already learned
	private boolean rebase(int move) {
		int index = root.moves.search(move);
		if (index == -1) {
			return false;
		}
		Node child = root.children[index];
		if (child == null) {
			return false;
		}
		RootStats newStats = new RootStats();
		newStats.maxDepth = rootStats.maxDepth - 1;
		newStats.evaluations.set(root.childrenEvaluations.get(index));
		int rewards;
		if (root.color != child.color) {
			rewards = root.childrenEvaluations.get(index) - root.childrenRewards.get(index);
		} else {
			rewards = root.childrenRewards.get(index);
		}
		newStats.rewards.set(rewards);
		rootStats = newStats;
		root = child;
		return true;
	}

	// pick nodes with the highest reward ratio for both teams
	@Override
	public void suggestAndDoMoves(int maxMoves, IntList output) {
		try {
			Thread.sleep(thinkingMillis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// TODO: find a better way to handle these stats instead of caching here
		Stats stats = new Stats(rootStats);
		Node node = root;
		while (maxMoves-- > 0) {
			double maxReward = 0.0;
			int maxIndex = -1;
			for (int i = 0; node != null && i < node.size(); i++) {
				double reward = (double) node.childrenRewards.get(i) / node.childrenEvaluations.get(i);
				if (reward >= maxReward) {
					maxReward = reward;
					maxIndex = i;
				}
			}
			if (maxIndex == -1) {
				break;
			}
			int move = node.moves.get(maxIndex);
			doMove(move);
			output.push(move);
			node = node.children[maxIndex];
		}
		publicStats = stats;
	}

	/** Get some stats that are useful to humans. */
	public Stats getStats() {
		return publicStats;
	}

	/** Sets the amount of time the player spends thinking. */
	public void setThinkingTime(int thinkingMillis) {
		this.thinkingMillis = thinkingMillis;
	}

	// each thread is a worker
	private class Worker implements Runnable {

		// each worker gets its own heuristic and generators from suppliers
		private Heuristic heuristic;
		private MoveGenerator moveGenerator;

		// a trace of the node indices explored in a search
		private IntList indexTrace;

		// a trace of all the nodes explored in a search
		private ArrayList<Node> nodeTrace;

		public Worker() {
			this.heuristic = heuristicSupplier.get();
			this.moveGenerator = moveGenSupplier.get();
			this.indexTrace = new IntList(board.dimensions.boardSize * MoveType.COUNT);
			this.nodeTrace = new ArrayList<>(board.dimensions.boardSize * MoveType.COUNT);
		}

		private void search(Board searchState) {

			// get a reference to the stats object now
			// if we got it later, it might have changed
			// and stats would be added to the wrong object!
			RootStats cachedRootStats = rootStats;

			// selection
			nodeTrace.clear();
			indexTrace.clear();
			double parentEvaluations = cachedRootStats.evaluations.doubleValue();
			Node previousNode = null;
			Node selectedNode = root;
			int selectedChild = -1;
			do {
				// this if boardment is needed to prevent inconsistent views when multithreading
				if (selectedNode.moveCount != searchState.getMoveCount()) {
					// System.out.println("MISMATCH PREVENTED");
					return;
				}
				selectedChild = select(parentEvaluations, selectedNode);
				if (selectedChild == -1) {
					break;
				}
				indexTrace.push(selectedChild);
				nodeTrace.add(selectedNode);
				searchState.doMove(selectedNode.moves.get(selectedChild));
				parentEvaluations = selectedNode.childrenEvaluations.get(selectedChild);
				previousNode = selectedNode;
				selectedNode = selectedNode.children[selectedChild];
			} while (selectedNode != null);

			int depth = indexTrace.size();
			if (depth > cachedRootStats.maxDepth) {
				cachedRootStats.maxDepth = depth;
			}

			Color winner;
			if (selectedChild == -1) {

				// terminal state
				winner = searchState.getColorToMove().opposite();

				// DEBUG ASSERTION
				// check for bad terminal boards
				// IntList moves = new IntList(searchState.getMaxMovesAbsolute());
				// searchState.generateMoves(moves);
				// if (moves.size() != 0) {
				// 	System.out.println("FALSE TERMINAL STATE");
				// 	System.out.println(searchState);
				// }

			} else {

				// expansion
				Node expandedNode = new Node(searchState, moveGenerator);
				// simulation
				winner = evaluate(searchState);
				// attach expanded node
				previousNode.children[selectedChild] = expandedNode;

			}

			backpropogate(cachedRootStats, winner);

		}

		// performs a single iteration of uct selection
		private int select(double parentEvaluations, Node node) {
			if (node.needsExpansion()) {
				return node.nextToExpand++;
			}
			double logParentEvaluations = Math.log(parentEvaluations);
			double maxScore = 0.0;
			int maxScoreIndex = -1;
			for (int i = 0; i < node.size(); i++) {
				if (node.children[i] == null) {
					return i;
				}
				double score = ucb1(node.childrenRewards.getPlain(i), node.childrenEvaluations.getPlain(i), logParentEvaluations);
				// TODO: fix this so it isn't needed
				if (Double.isNaN(score)) {
					return i;
				}
				if (score >= maxScore) {
					maxScore = score;
					maxScoreIndex = i;
				}
			}
			return maxScoreIndex;
		}

		// evaluates a state with heuristic and returns a winner.
		private Color evaluate(Board board) {
			int eval = heuristic.evaluate(board);
			if (eval > 0) {
				return Color.WHITE;
			} else if (eval < 0) {
				return Color.BLACK;
			} else {
				return null;
			}
		}

		// backpropogation step of uct
		private void backpropogate(RootStats cachedRootStats, Color winner) {
			cachedRootStats.evaluations.incrementAndGet();
			if (board.getColorToMove() == winner) {
				cachedRootStats.rewards.incrementAndGet();
			}
			for (int i = 0; i < indexTrace.size(); i++) {
				Node node = nodeTrace.get(i);
				int selectedChild = indexTrace.get(i);
				node.childrenEvaluations.getAndIncrement(selectedChild);
				if (node.color == winner) {
					node.childrenRewards.getAndIncrement(selectedChild);
				}
			}
		}

		// the famous ucb1 formula from the multi-arm bandit problem
		private double ucb1(double childRewards, double childEvaluations, double logParentEvaluations) {
			double exploitation = childRewards / childEvaluations;
			double exploration = Math.sqrt(logParentEvaluations / childEvaluations);
			return exploitation + explorationFactor * exploration;
		}

		// the worker loop
		@Override
		public void run() {
			while (running) {
				if (root != null) {
					// this try loop will catch any weird errors from threading
					// anomalies, just log them, there are few, but they are
					// mostly harmless in the grand scheme.
					try {
						search(board.clone());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		
	}

	// a mcts node
	// stats are stored in arrays in the parent to improve cpu cache hit ratio
	private class Node {

		public int moveCount;
		public Color color;
		public IntList moves;
		public Node[] children;
		public AtomicIntegerArray childrenEvaluations;
		public AtomicIntegerArray childrenRewards;
		public int nextToExpand;

		// move generator is passed here, because we need to use the worker's
		// generator not the root one, to be safe.
		public Node(Board board, MoveGenerator moveGenerator) {
			this.moveCount = board.getMoveCount();
			this.color = board.getColorToMove();
			this.moves = new IntList(board.getMaxMoves());
			moveGenerator.generateMoves(board, moves);
			this.children = new Node[moves.size()];
			this.childrenEvaluations = new AtomicIntegerArray(moves.size());
			this.childrenRewards = new AtomicIntegerArray(moves.size());
			this.nextToExpand = 0;
		}

		// whether the node needs expansion
		public boolean needsExpansion() {
			return nextToExpand < children.length && children[nextToExpand] == null;
		}

		// this could be returned from a multitude of the arrays in the node
		public int size() {
			return children.length;
		}

	}

	// root stats from internal use
	private class RootStats {

		public AtomicInteger rewards;
		public AtomicInteger evaluations;
		public int maxDepth;

		public RootStats() {
			rewards = new AtomicInteger();
			evaluations = new AtomicInteger();
		}

	}

	// public stats from external use
	public class Stats {

		public final double whiteWinRatio;
		public final int evaluations;
		public final int maxDepth;

		public Stats(RootStats stats) {
			this.evaluations = stats.evaluations.intValue();
			this.maxDepth = stats.maxDepth;
			double rewardRatio = stats.rewards.doubleValue() / stats.evaluations.doubleValue();
			rewardRatio = Math.max(0.0, Math.min(rewardRatio, 1.0));
			if (board.getColorToMove() == Color.WHITE) {
				this.whiteWinRatio = rewardRatio;
			} else {
				this.whiteWinRatio = 1.0 - rewardRatio;
			}
		}

	}

	@Override
	public void close() throws Exception {
		running = false;
		for (Thread thread : workerThreads) {
			thread.join();
		}
		workerThreads.clear();
	}
	
}
