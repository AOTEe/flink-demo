package com.aootz;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.util.Collector;


public class StreamingDemo {

    /**
     * 把实时的数据流进行分流，分成even和odd两个流
     * 进行join条件是名称相同，把两个流的join结果输出
     */
    public static void main(String[] args) throws Exception {

        EnvironmentSettings bsSettings = EnvironmentSettings.newInstance().useBlinkPlanner().inStreamingMode().build();
        StreamExecutionEnvironment bsEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment bsTableEnvironment = StreamTableEnvironment.create(bsEnvironment, bsSettings);
        SingleOutputStreamOperator<MyStreamingSource.Item> source = bsEnvironment.addSource(new MyStreamingSource()).map(new MapFunction<MyStreamingSource.Item, MyStreamingSource.Item>() {
            @Override
            public MyStreamingSource.Item map(MyStreamingSource.Item item) throws Exception {
                return item;
            }
        });


        DataStream<MyStreamingSource.Item> even = source.process(new ProcessFunction<MyStreamingSource.Item, MyStreamingSource.Item>() {

            @Override
            public void processElement(MyStreamingSource.Item value, Context ctx, Collector<MyStreamingSource.Item> out) throws Exception {
                if (value.getId() % 2 == 0) {
                    out.collect(value);
                }
            }
        });
        even.print("even");


        DataStream<MyStreamingSource.Item> odd = source.process(new ProcessFunction<MyStreamingSource.Item, MyStreamingSource.Item>() {

            @Override
            public void processElement(MyStreamingSource.Item value, Context ctx, Collector<MyStreamingSource.Item> out) throws Exception {
                if (value.getId() % 2 != 0) {
                    out.collect(value);
                }
            }
        });
        even.print("odd");

        bsTableEnvironment.createTemporaryView("evenTable", even, "name,id");

        bsTableEnvironment.createTemporaryView("oddTable", odd, "name,id");

        Table table = bsTableEnvironment.sqlQuery("select a.id,a.name,b.id,b.name from evenTable as a join oddTable as b " +
                "on a.name = b.name");

        //打印
        table.printSchema();
        bsTableEnvironment.toRetractStream(table, TypeInformation.of(new TypeHint<Tuple4<Integer, String, Integer, String>>() {
        })).print();

        bsTableEnvironment.execute("streaming sql job");

        //旧版本写法

//        DataStream<MyStreamingSource.Item> odd = source.split(new OutputSelector<MyStreamingSource.Item>() {
//            @Override
//            public Iterable<String> select(MyStreamingSource.Item item) {
//                List<String> output = new ArrayList<>();
//                if (item.getId() % 2 == 0) {
//                    output.add("even");
//
//                } else {
//                    output.add("odd");
//                }
//                return output;
//            }
//        }).select("odd");

    }
}
