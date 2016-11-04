package com.phome.samples.func;

import com.phome.sample.fun.Mapper;
import com.phome.sample.fun.MapperUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author zw
 */
public class FuncTest {

    @Test
    public void mapper() {
        List<String> inputs = new ArrayList<>();
        inputs.add("a");
        inputs.add("b");
        inputs.add("d");


        Mapper<String, String> mapperFunc = input -> input + "_func";
//        List<String> results = MapperUtils.map(inputs, input -> input + "_func");
        List<String> results = MapperUtils.map(inputs, mapperFunc);
        System.out.println(results);

        Function<String, String> func = input -> input + "_stream";
        System.out.println(MapperUtils.map(inputs, func));

    }
}
