package fr.hymaia.fromagerie

import org.apache.spark._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{SaveMode, SparkSession}
import com.amazonaws.services.glue.GlueContext
import com.amazonaws.services.glue.util.GlueArgParser
import com.amazonaws.services.glue.util.Job
import scala.collection.JavaConverters._
import fr.hymaia.fromagerie.ComputeScore
import com.amazonaws.services.glue.DynamoDbDataSink
import com.amazonaws.services.glue.util.JsonOptions
import com.amazonaws.services.glue.DynamicFrame

object Runner {
  def main(args: Array[String]): Unit = {
    val sc: SparkContext = new SparkContext()
    val glueContext: GlueContext = new GlueContext(sc)

    val param_names = Array(
      "JOB_NAME",
      "PLAYER_SUBMISSION_FILE",
      "COMMAND_FILE",
      "OUTPUT_FILE",
      "DYNAMO_TABLE",
    )
    val params = GlueArgParser.getResolvedOptions(args, param_names)
    Job.init(params("JOB_NAME"), glueContext, params.asJava)

    val df = ComputeScore.main(param_names.map(params(_)))
    df.show()
    df.printSchema()
    println("testestest")

    val dydf = DynamicFrame(df, glueContext)

    val dynamoDbSink: DynamoDbDataSink =  glueContext.getSinkWithFormat(
      connectionType = "dynamodb",
      options = JsonOptions(Map(
        "dynamodb.output.tableName" -> params("DYNAMO_TABLE"),
        "dynamodb.throughput.write.percent" -> "1.0"
      ))
    ).asInstanceOf[DynamoDbDataSink]

    dynamoDbSink.writeDynamicFrame(dydf)

    Job.commit()
  }
}
