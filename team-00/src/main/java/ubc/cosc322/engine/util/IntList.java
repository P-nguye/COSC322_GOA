package ubc.cosc322.engine.util;

import java.util.Arrays;

/** A class very similar to ArrayList<Integer>, but without all the needless allocations. */
public final class IntList {
	
	private int[] array;
	private int size;
	
	public IntList(int capacity) {
		this.array = new int[capacity];
		this.size = 0;
	}

	public IntList(int[] array, int size) {
		this.array = array;
		this.size = size;
	}

	public int size() {
		return size;
	}

	public int capacity() {
		return array.length;
	}

	public void push(int n) {
		array[size] = n;
		size++;
	}

	public int pop() {
		int n = array[size-1];
		size--;
		return n;
	}

	public int get(int i) {
		// DEBUG ASSERTION
		// if (i < 0 || i >= size) {
		// 	throw new IndexOutOfBoundsException();
		// }
		return array[i];
	}

	public void set(int i, int n) {
		// DEBUG ASSERTION
		// if (i < 0 || i >= size) {
		// 	throw new IndexOutOfBoundsException();
		// }
		array[i] = n;
	}

	public void removeIndex(int i) {
		// DEBUG ASSERTION
		// if (i < 0 || i >= size) {
		// 	throw new IndexOutOfBoundsException();
		// }
		int copyLength = size - 1 - i;
		System.arraycopy(array, i+1, array, i, copyLength);
		size--;
	}

	public boolean removeValue(int n) {
		for (int i = 0; i < size; i++) {
			if (array[i] == n) {
				removeIndex(i);
				return true;
			}
		}
		return false;
	}

	public int search(int n) {
		for (int i = 0; i < size; i++) {
			if (array[i] == n) {
				return i;
			}
		}
		return -1;
	}

	public boolean contains(int n) {
		for (int i = 0; i < size; i++) {
			if (array[i] == n) {
				return true;
			}
		}
		return false;
	}

	public void sort() {
		Arrays.sort(array, 0, size);
	}

	public boolean empty() {
		return size == 0;
	}

	public int count(int n) {
		int count = 0;
		for (int i = 0; i < size; i++) {
			if (array[i] == n) {
				count++;
			}
		}
		return count;
	}

	public void clear() {
		size = 0;
	}

	public IntList clone() {
		return new IntList(array.clone(), size);
	}

	@Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
		builder.append('[');
		if (size >= 1) {
			builder.append(array[0]);
		}
		for (int i = 1; i < size; i++) {
			builder.append(',');
			builder.append(array[i]);
		}
		builder.append(']');
		return builder.toString();
    }

}
