package com.udacity.webcrawler;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class that sorts the map of word counts.
 *
 * 这段代码定义了一个名为 WordCounts 的实用类，用于对一个包含单词及其出现次数的
 * Map<String, Integer> 进行排序，并返回只包含最流行单词的结果
 *
 * <p>TODO: Reimplement the sort() method using only the Stream API and lambdas and/or method
 *          references.
 */
final class WordCounts {

  /**
   * Given an unsorted map of word counts, returns a new map whose word counts are sorted according
   * to the provided {@link WordCountComparator}, and includes only the top
   * {@param popluarWordCount} words and counts.
   *
   * <p>TODO: Reimplement this method using only the Stream API and lambdas and/or method
   *          references.
   *
   * @param wordCounts       the unsorted map of word counts.
   *                         一个未排序的 wordCounts 单词统计
   * @param popularWordCount the number of popular words to include in the result map.
   *                         popularWordCount（指定要返回的最流行单词数）
   * @return a map containing the top {@param popularWordCount} words and counts in the right order.
   */
  static Map<String, Integer> sort(Map<String, Integer> wordCounts, int popularWordCount) {
    // 使用Stream API，传入自定义的WordCountComparator
    return wordCounts.entrySet()
            .stream()
            .sorted(new WordCountComparator())  // 使用自定义比较器进行排序
            .limit(Math.min(wordCounts.size(),popularWordCount))            // 限制结果数量
            .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (e1, e2) -> e1,             // 处理重复键的情况
                    LinkedHashMap::new          // 使用LinkedHashMap来保持顺序
            ));
  }

  /**
   * A {@link Comparator} that sorts word count pairs correctly:
   *
   * WordCountComparator 是一个静态内部类，用于比较两个 Map.Entry<String, Integer>，即单词及其出现次数
   * <p>
   * <ol>
   *   <li>First sorting by word count, ranking more frequent words higher.</li>
   *   优先按频率排序：词频越高，优先级越高（降序）
   *   <li>Then sorting by word length, ranking longer words higher.</li>
   *   然后按单词长度排序：单词越长，优先级越高（降序）
   *   <li>Finally, breaking ties using alphabetical order.</li>
   *   最后按字母顺序排序：如果频率和长度都相同，按字典序进行排序
   * </ol>
   */
  private static final class WordCountComparator implements Comparator<Map.Entry<String, Integer>> {
    @Override
    public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
      if (!a.getValue().equals(b.getValue())) {
        return b.getValue() - a.getValue();
      }
      if (a.getKey().length() != b.getKey().length()) {
        return b.getKey().length() - a.getKey().length();
      }
      return a.getKey().compareTo(b.getKey());
    }
  }

  private WordCounts() {
    // This class cannot be instantiated
  }
}