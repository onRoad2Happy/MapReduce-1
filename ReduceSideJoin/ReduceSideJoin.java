package wordcount;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class ReduceSideJoin
{

    public static class TokenizerMapper extends Mapper<Object, Text, IntWritable, Text>
    {

        private IntWritable primaryKey = new IntWritable();
        private Text otherValues = new Text();


        public void map(Object key, Text value, Context context) throws IOException, InterruptedException
        {
            StringTokenizer itr = new StringTokenizer(value.toString(),",");
            int i=0;
            StringBuilder allValues=new StringBuilder();

            while (itr.hasMoreTokens())
            {
                String data= itr.nextToken();
                if(i==0)
                    primaryKey.set(Integer.parseInt(data));
                else
                {
                    if(i==1)
                        allValues.append(data);
                    else
                    {
                        allValues.append(", ");
                        allValues.append(data);
                    }
                 }
                i++;
            }
            otherValues.set(allValues.toString());
            context.write(primaryKey, otherValues);

        }
    }

    public static class RelationalReducer extends Reducer<IntWritable, Text, IntWritable, Text>
    {
        private Text result = new Text();

        public void reduce(IntWritable key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException
        {
            String combination = "";
            boolean firstTime=true;
            for (Text val : values)
            {
                if(firstTime)
                    combination += val;
                else
                    combination +=", "+ val;
                firstTime=false;
            }
            result.set(combination);
            context.write(key, result);
        }
    }

    public static void main(String[] args) throws Exception
    {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "ReduceSideJoin");
        job.setJarByClass(ReduceSideJoin.class);
        
        job.setMapperClass(TokenizerMapper.class);
        job.setReducerClass(RelationalReducer.class);
        
        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(Text.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
    

}
