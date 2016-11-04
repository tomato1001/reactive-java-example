package com.phome.sample.fun;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zw
 */
public class MapperUtils {

    public static <T, R> List<R> map(List<T> inputs, Mapper<T, R> func) {
        List<R> results = new ArrayList<>();
        for (T input : inputs) {
            results.add(func.map(input));
        }
        return results;
    }

    public static <T, R> List<R> map(List<T> inputs, Function<T, R> func) {
        return inputs.stream().map(func).collect(Collectors.toList());
    }
}
