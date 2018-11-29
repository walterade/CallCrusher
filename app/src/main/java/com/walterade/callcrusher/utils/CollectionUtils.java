package com.walterade.callcrusher.utils;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Walter on 2/3/18.
 */

public class CollectionUtils {
    public static boolean isEmpty(Collection c) {
        return c == null || c.isEmpty();
    }
    public static <T> List<T> unique(List<T> list, Comparator<? super T> c) {
        Set<T> set = new TreeSet<>(c);
        set.addAll(list);
        list.clear();
        list.addAll(set);
        return list;
    }
}
