package com.aootz;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.Random;

public class MyStreamingSource implements SourceFunction<MyStreamingSource.Item> {

    private boolean isRunning = true;

    @Override
    public void run(SourceContext sourceContext) throws Exception {

        while (isRunning) {

            Item item = generateItem();
            sourceContext.collect(item);
            Thread.sleep(1000);

        }

    }

    @Override
    public void cancel() {

    }

    public static class Item {
        private Integer id;
        private String name;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "Item{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    private Item generateItem() {
        int i = new Random().nextInt(100);
        Item item = new Item();
        item.setId(i);
        item.setName("name" + i);
        return item;
    }


    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();
        //获取数据源
        DataStreamSource text = environment.addSource(new MyStreamingSource()).setParallelism(1);

        DataStream<Item> map = text.map((MapFunction<Item, Item>) value -> value);

        map.print().setParallelism(1);


        String jobName = "my streaming source";
        environment.execute(jobName);
    }
}
