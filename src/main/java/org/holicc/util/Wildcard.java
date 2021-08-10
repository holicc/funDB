package org.holicc.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Wildcard {

    private List<Item> items;

    public Wildcard(List<Item> items) {
        this.items = items;
    }

    /**
     * types: 0 normal 1 all 2 any 3 [] 4 [a-b] 5 [^a]
     */
    static record Item(char c, Set<Character> set, int type) {

        public boolean contains(char c) {
            if (type == 4) { // range match
                if (set.contains(c)) return true;
                char min = Character.MAX_VALUE;
                char max = Character.MIN_VALUE;
                for (Character i : set) {
                    if (i < min) min = i;
                    if (i > max) max = i;
                }
                return c >= min && c <= max;
            } else if (type == 5) { // negative match
                return !set.contains(c);
            } else {
                return set.contains(c);
            }
        }
    }

    public boolean isMatch(String key) {
        int m = key.length(), n = items.size();
        boolean[][] table = new boolean[m + 1][n + 1];
        table[0][0] = true;
        //
        for (int i = 1; i <= n; i++) {
            table[0][i] = table[0][i - 1] && items.get(i - 1).type == 1;
        }
        //
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                Item item = items.get(j - 1);
                if (item.type == 1) table[i][j] = table[i][j - 1] || table[i - 1][j];
                else {
                    table[i][j] = table[i - 1][j - 1] &&
                            (
                                    item.type == 2
                                            || (item.type == 0 && key.charAt(i - 1) == item.c)
                                            || (item.type >= 3 && item.contains(key.charAt(i - 1)))
                            );
                }
            }
        }
        return table[m][n];
    }

    public static Wildcard compile(String pattern) {
        List<Item> items = new ArrayList<>();
        boolean escape = false, inSet = false;
        Set<Character> set = new HashSet<>();
        char[] chars = pattern.toCharArray();
        for (char c : chars) {
            if (escape) {
                items.add(new Item(c, null, 0));
                escape = false;
            } else if (c == '*') items.add(new Item(c, null, 1));
            else if (c == '?') items.add(new Item(c, null, 2));
            else if (c == '\\') escape = true;
            else if (c == '[') {
                if (!inSet) inSet = true;
                else set.add(c);
            } else if (c == ']') {
                if (inSet) {
                    inSet = false;
                    int type = 3;
                    if (set.contains('-')) {
                        type = 4;
                        set.remove('-');
                    } else if (set.contains('^')) {
                        type = 5;
                        set.remove('^');
                    }
                    items.add(new Item(c, set, type));
                } else items.add(new Item(c, null, 0));
            } else {
                if (inSet) {
                    set.add(c);
                } else items.add(new Item(c, null, 0));
            }
        }
        return new Wildcard(items);
    }
}
