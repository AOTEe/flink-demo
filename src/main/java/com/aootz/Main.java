package com.aootz;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;


public class Main {
    public static void main(String[] args) throws Exception {

        final ExecutionEnvironment environment = ExecutionEnvironment.getExecutionEnvironment();

        DataSet<String> text = environment.fromElements("one one tow", "to one one", "on to one");

        DataSet<Tuple2<String, Integer>> counts = text.flatMap(new LineSplitter()).groupBy(0).sum(1);

        counts.printToErr();
    }


    public static final class LineSplitter implements FlatMapFunction<String, Tuple2<String, Integer>> {

        @Override
        public void flatMap(String value, Collector<Tuple2<String, Integer>> collector) throws Exception {
            //文本分割
            String[] tokens = value.toLowerCase().split("\\W+");
            for (String token : tokens) {
                if (!token.isEmpty()) {
                    collector.collect(new Tuple2<String, Integer>(token, 1));
                }
            }
        }
    }
}