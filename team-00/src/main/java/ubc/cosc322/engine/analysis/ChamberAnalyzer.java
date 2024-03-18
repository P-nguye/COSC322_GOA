package ubc.cosc322.engine.analysis;

import ubc.cosc322.engine.core.Dimensions;
import ubc.cosc322.engine.util.IntList;

/**
 * This class is an efficient way to detect whether one position on the board
 * can be reached in any way from another position on the board. It also allows
 * the efficient calculation of the sizes of the territory reachable from a
 * given position. The output of the class can be used to detect endgames and
 * estimate territory control.
 */
public class ChamberAnalyzer {

	// the dimension object will the same as that used in the Board class
	private final Dimensions dimensions;

	// one integer in this bitmap is represents a board row
	// as such the maximum supported board width is 32
	private int[] arrowBitmap;

	// a matrix of mappings of position to groups/chambers
	private int[] groupBoard;

	// a vector of mapping between connected groups/chambers
	private int[] groupMappings;

	// the sizes of groups/chambers
	private int[] groupSizes;

	// binary masks, specific to group derived from arrow bitmap rows
	private IntList groupMasks;
	
	/** Creates chamber analyzer, placeArrow should be called after this. */
	public ChamberAnalyzer(Dimensions dimensions) {
		this.dimensions = dimensions;
		this.arrowBitmap = new int[dimensions.boardHeight];
		this.groupBoard = new int[dimensions.arraySize];
		this.groupMappings = new int[dimensions.boardSize];
		this.groupSizes = new int[dimensions.boardSize];
		this.groupMasks = new IntList(dimensions.boardSize);
	}

	/** Copy constructor. */
	public ChamberAnalyzer(ChamberAnalyzer other) {
		this.dimensions = other.dimensions;
		this.arrowBitmap = other.arrowBitmap.clone();
		this.groupBoard = other.groupBoard.clone();
		this.groupMappings = other.groupMappings.clone();
		this.groupSizes = other.groupSizes.clone();
		this.groupMasks = other.groupMasks.clone();
	}

	/** Places a 1 into the arrow bitmap at a given position. */
	public void placeArrow(int position) {
		arrowBitmap[dimensions.y(position)] |= 1 << dimensions.x(position);
	}

	/** Updates all the group mappings and sizes based on current arrow bitmap. */
	public void update() {
		groupMasks.clear();
		int previousBase = 0;
		computeGroupMasks(arrowBitmap[0], 0);
		// loop through bitmap rows
		for (int y = 1; y < arrowBitmap.length; y++) {
			int nextBase = groupMasks.size();
			computeGroupMasks(arrowBitmap[y], y);
			// loop through groups in row
			for (int i = nextBase; i < groupMasks.size(); i++) {
				int parentMask = groupMasks.get(i);
				int connectionMask = connectionMask(parentMask);
				// loop through previous groups in row
				for (int j = previousBase; j < nextBase; j++) {
					int childMask = groupMasks.get(j);
					// check if groups connected via bitwise and
					if ((connectionMask & childMask) != 0) {
						if (groupMappings[j] == j) {
							// if a group is mapped to itself
							// update its mapping to this new connected group
							// and attribute its size to the new group
							groupMappings[j] = groupMappings[i];
							groupSizes[i] += groupSizes[j];
						} else {
							// if the group is not mapped to itself
							// it must mapped to another established group
							// point the new group to that established group 
							groupMappings[i] = groupMappings[groupMappings[j]];
						}
					}
				}
				if (groupMappings[i] != i) {
					// attribute all size gained to established group if needed
					groupSizes[groupMappings[i]] += groupSizes[i];
				}
			}
			previousBase = nextBase;
		}
	}

	// converts a row to individual groups
	// eg. 0001100100
    // becomes
	//     0000000011
	//     0000011000
	//     1110000000
	// group board assignments are so made in this function
	// along with initial group sizes, but those are subject to change
	private void computeGroupMasks(int row, int y) {
		int group = 0;
		int shift = 0;
		int yOffset = y*dimensions.arrayWidth;
		do {
			while (((row >> shift) & 1) != 0 && shift < 10) {
				shift++;
			}
			int size = 0;
			while (((row >> shift) & 1) == 0 && shift < 10) {
				group = group | (1 << shift);
				groupBoard[shift+yOffset] = groupMasks.size();
				shift++;
				size++;
			}
			if (group != 0) {
				int groupIndex = groupMasks.size();
				groupMasks.push(group);
				groupMappings[groupIndex] = groupIndex;
				groupSizes[groupIndex] = size;
				group = 0;
			}
		} while (shift < 10);
	}

	// creates bitmask that can determine if two rows are connected
	// essentially bits are widened in both direction by one
	// eg. 0000111000
	// becomes
	//     0001111100
	private int connectionMask(int row) {
		return (row | (row << 1) | (row >> 1)) & ((1 << dimensions.boardWidth) - 1);
	}

	/**
	 * Gets the chamber mapping of a position.
	 * WARNING THIS MIGHT GO INTO AN INFINITE LOOP IF THE POSITION IS AN ARROW
	 * OR INVALID UNDEFINED BEHAVIOUR, DON'T EVEN TRY IT NOT CHECKING FOR
	 * PERFORMANCE REASONS.
	 */
	public int getPositionChamber(int position) {
		int mapping = groupBoard[position];
		while (groupMappings[mapping] != mapping) {
			mapping = groupMappings[mapping];
		}
		return mapping;
	}

	/** Gets the size of chamber in board tiles. */
	public int getChamberSize(int chamber) {
		return groupSizes[chamber];
	}

	/** Copies the analyzer. */
	public ChamberAnalyzer clone() {
		return new ChamberAnalyzer(this);
	}

}
