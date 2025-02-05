package com.aootz.lecture05;

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

        // useBlinkPlanner() 适用于 Flink 1.11 及更早版本，Flink 1.12 之后 Blink 规划器已经成为默认的 Table API 规划器，并移除了 useBlinkPlanner() 方法。
        EnvironmentSettings bsSettings = EnvironmentSettings.newInstance().useBlinkPlanner().inStreamingMode().build();
        StreamExecutionEnvironment bsEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment bsTableEnvironment = StreamTableEnvironment.create(bsEnvironment, bsSettings);
        SingleOutputStreamOperator<MyStreamingSource.Item> source = bsEnvironment.addSource(new MyStreamingSource()).map(new MapFunction<MyStreamingSource.Item, MyStreamingSource.Item>() {
            @Override
            public MyStreamingSource.Item map(MyStreamingSource.Item item) throws Exception {
                return item;
            }
        });

        // 0.9版本中使用split方法（现已被弃用）
        DataStream<MyStreamingSource.Item> even = source.process(new ProcessFunction<MyStreamingSource.Item, MyStreamingSource.Item>() {

            @Override
            public void processElement(MyStreamingSource.Item value, Context ctx, Collector<MyStreamingSource.Item> out) throws Exception {
                if (value.getId() % 2 == 0) {
                    out.collect(value);
                }
            }
        });
        //even偶数
        even.print("even");


        DataStream<MyStreamingSource.Item> odd = source.process(new ProcessFunction<MyStreamingSource.Item, MyStreamingSource.Item>() {

            @Override
            public void processElement(MyStreamingSource.Item value, Context ctx, Collector<MyStreamingSource.Item> out) throws Exception {
                if (value.getId() % 2 != 0) {
                    out.collect(value);
                }
            }
        });
        //odd奇数
        odd.print("odd");

        bsTableEnvironment.createTemporaryView("evenTable", even, "id,name");

        bsTableEnvironment.createTemporaryView("oddTable", odd, "id,name");

        Table table = bsTableEnvironment.sqlQuery("select a.id,a.name,b.id,b.name from evenTable as a join oddTable as b " +
                "on a.name = b.name");

        //打印
        table.printSchema();
        bsTableEnvironment.toRetractStream(table, TypeInformation.of(new TypeHint<Tuple4<Integer, String, Integer, String>>() {
        })).print();


        //bsTableEnvironment.execute("streaming sql job");
        bsEnvironment.execute();
        //bsTableEnvironment.execute("streaming sql job");
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
