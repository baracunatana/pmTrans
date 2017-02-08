package util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WordIndexer {

	private String searchString;

	public WordIndexer(String searchString) {
		this.searchString = searchString;
	}

	public List<WordIndexerWrapper> findIndexesForKeyword(String keyword) {
		return findIndexesForKeyword(keyword, 0, keyword.length() - 1);
	}

	public List<WordIndexerWrapper> findIndexesForKeyword(String keyword,
			int beginIndex, int endIndex) {
		if (searchString == null || searchString.length() == 0
				|| beginIndex >= endIndex)
			return new LinkedList<WordIndexerWrapper>();

		String regex = keyword;
		String tempString = searchString.substring(beginIndex, endIndex);
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(tempString);

		List<WordIndexerWrapper> wrappers = new ArrayList<WordIndexerWrapper>();

		while (matcher.find() == true) {
			int end = matcher.end() + beginIndex;
			int start = matcher.start() + beginIndex;
			WordIndexerWrapper wrapper = new WordIndexerWrapper(start, end);
			wrappers.add(wrapper);
		}
		return wrappers;
	}
}
