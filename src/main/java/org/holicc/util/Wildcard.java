package org.holicc.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Wildcard {

    private List<Item> items;

    public Wildcard(List<Item> items) {
        this.items = items;
    }

    /**
     * types: 0 normal 1 all 2 any 3 []
     */
    static record Item(char c, Map<Character, Boolean> set, int type) {

        public boolean contains(char c) {
            return set != null && (set.containsKey(c) && set.get(c));
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
                if (items.get(j - 1).type == 1) {
                    table[i][j] = table[i][j - 1] || table[i - 1][j];
                } else {
                    table[i][j] = table[i - 1][j - 1] &&
                            (items.get(j - 1).type == 2 ||
                                    (items.get(j - 1).type == 0 && key.charAt(i - 1) == items.get(j-1).c) ||
                                    (items.get(j - 1).type == 3 && items.get(j - 1).contains(key.charAt(i - 1)))
                            );
                }
            }
        }
        return table[m][n];
    }

    public static Wildcard compile(String pattern) {
        List<Item> items = new ArrayList<>();
        boolean escape = false, inSet = false;
        Map<Character, Boolean> set = new HashMap<>();
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
                else set.put(c, true);
            } else if (c == ']') {
                if (inSet) {
                    inSet = false;
                    items.add(new Item(c, set, 3));
                } else items.add(new Item(c, null, 0));
            } else {
                if (inSet) set.put(c, true);
                else items.add(new Item(c, null, 0));
            }
        }
        return new Wildcard(items);
    }
}
