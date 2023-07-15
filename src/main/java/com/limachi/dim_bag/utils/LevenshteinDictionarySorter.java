package com.limachi.dim_bag.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class LevenshteinDictionarySorter {
    public static final class Builder {
        private ArrayList<String> dictionary = new ArrayList<>();
        private int delete = 1;
        private int insert = 1;
        private int replace = 1;
        private int maximumInputLength = 50;

        public Builder() {}

        public Builder addEntries(String ... entries) {
            dictionary.addAll(List.of(entries));
            return this;
        }

        public Builder addEntries(Collection<String> entries) {
            dictionary.addAll(entries);
            return this;
        }

        public Builder setMaximumInputLength(int length) {
            maximumInputLength = Math.max(length, 1);
            return this;
        }

        public Builder setDeletionCost(int cost) {
            delete = cost;
            return this;
        }

        public Builder setInsertionCost(int cost) {
            insert = cost;
            return this;
        }

        public Builder setReplaceCost(int cost) {
            replace = cost;
            return this;
        }

        public Builder setCosts(int delete, int insert, int replace) {
            this.delete = delete;
            this.insert = insert;
            this.replace = replace;
            return this;
        }

        public LevenshteinDictionarySorter build() {
            return new LevenshteinDictionarySorter(maximumInputLength, delete, insert, replace, dictionary);
        }
    }
    protected static final class Entry {
        String value;
        int score = 0;
        int longest = 0;

        Entry(String value) { this.value = value; }
    }

    protected final int delete;
    protected final int insert;
    protected final int replace;
    protected final int[][] distance;
    protected List<String> sortedDictionary;
    protected ArrayList<Entry> dictionary;

    protected int levenshteinDistance(String input, String dictionaryEntry) {
        int l = Math.min(input.length(), distance.length - 1);
        int dl = dictionaryEntry.length();
        if (l > 0)
            for (int i = 1; i <= l; ++i)
                for (int d = 1; d <= dl; ++d) {
                    char in = input.charAt(i - 1);
                    char dic = dictionaryEntry.charAt(d - 1);
                    if (in == dic)
                        distance[i][d] = distance[i - 1][d - 1];
                    else
                        distance[i][d] = replace + Math.min(Math.min(distance[i - 1][d], distance[i][d - 1]), distance[i - 1][d - 1]);
                }
        if (l == dl)
            return distance[l][l];
        else if (l < dl)
            return distance[l][dl] + insert * (dl - l);
        return distance[l][dl] + delete * (l - dl);
    }

    protected int longestMatch(String input, String dictionaryEntry) {
        int l = Math.min(input.length(), dictionaryEntry.length());
        for (int i = 0; i < l; ++i)
            if (!dictionaryEntry.contains(input.substring(0, i + 1)))
                return i * replace;
        return l * replace;
    }
    protected LevenshteinDictionarySorter(int maxInput, int delete, int insert, int replace, Collection<String> dictionary) {
        this.delete = delete;
        this.insert = insert;
        this.replace = insert;
        sortedDictionary = new ArrayList<>(dictionary);
        this.dictionary = new ArrayList<>(dictionary.size());
        int len = 0;
        for (String e : dictionary) {
            int l = e.length();
            if (l > len)
                len = l;
            this.dictionary.add(new Entry(e));
        }
        distance = new int[maxInput + 1][len + 1];
        for (int i = 0; i < maxInput; ++i)
            distance[i][0] = i;
        for (int i = 0; i < distance[0].length; ++i)
            distance[0][i] = i;
    }

    public LevenshteinDictionarySorter sortAgainst(String input) {
        for (Entry e : dictionary) {
            e.score = levenshteinDistance(input, e.value);
            e.longest = longestMatch(input, e.value);
        }
        dictionary.sort((e1, e2)->{
            if (e1.score == e2.score) {
                if (e1.longest == e2.longest)
                    return e1.value.compareTo(e2.value);
                return e2.longest - e1.longest;
            }
            if (e1.longest != e2.longest && Math.abs(e1.score - e2.score) <= Math.abs(e1.longest - e2.longest))
                return e2.longest - e1.longest;
            return e1.score - e2.score;
        });
        sortedDictionary = dictionary.stream().map(e->e.value).collect(Collectors.toList());
        return this;
    }

    public List<String> getSortedDictionary() { return sortedDictionary; }

    public int size() { return dictionary.size(); }
}
