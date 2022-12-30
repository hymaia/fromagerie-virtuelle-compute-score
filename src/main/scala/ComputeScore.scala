package fr.hymaia.fromagerie

import org.apache.spark.sql.functions._
import org.apache.spark.sql.{SaveMode, SparkSession}


case class PlayerSubmission(id: String, cheese: String, quantity: BigInt, month: String)

case class Order(cheese: String, quantity: BigInt, month: String, totalPrice: Double)

object ComputeScore {
  private val PLAYER_SUBMISSION_FILE: String = sys.env.getOrElse("PLAYER_SUBMISSION_FILE", "src/main/resources/player_submissions.json")
  private val ORDER_FILE: String = sys.env.getOrElse("COMMAND_FILE", "src/main/resources/orders.json")
  private val OUTPUT_FILE: String = sys.env.getOrElse("OUTPUT_FILE", "target/resources/output")

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder.master("local[*]").appName("ComputeScore").getOrCreate()
    import spark.implicits._

    val submissionDf = spark.read.json(PLAYER_SUBMISSION_FILE).as[PlayerSubmission].toDF().withColumnRenamed("quantity", "quantity_produced")
    val commandDf = spark.read.json(ORDER_FILE).as[Order].toDF()

    val res = commandDf.groupBy(col("cheese"), col("month"))
      .agg(sum(col("quantity")).as("quantity_ordered"), sum(col("totalPrice")).as("totalPrice"))
      .join(submissionDf, Seq("cheese", "month"))
      .withColumn("score", abs(col("quantity_produced") - col("quantity_ordered")) * (-5))
      .groupBy(col("id"), col("month")).agg(sum("score").as("score"))

    res.write.mode(SaveMode.Overwrite).parquet(OUTPUT_FILE)
  }
}
