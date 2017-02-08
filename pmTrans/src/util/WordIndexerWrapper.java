package util;

public class WordIndexerWrapper {

	public final int start;
	public final int end;

	public WordIndexerWrapper(int start, int end) {
		this.start = start;
		this.end = end;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + end;
		result = prime * result + start;
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WordIndexerWrapper other = (WordIndexerWrapper) obj;
		if (end != other.end)
			return false;
		if (start != other.start)
			return false;
		return true;
	}

}