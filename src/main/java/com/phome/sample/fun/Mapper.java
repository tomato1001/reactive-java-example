package com.phome.sample.fun;

/**
 * @author zw
 */
public interface Mapper<T, R> {

    R map(T input);

}
