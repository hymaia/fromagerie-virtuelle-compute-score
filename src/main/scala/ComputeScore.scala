package fr.hymaia.fromagerie

import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}


case class PlayerSubmission(playerId: String, cheese: String, quantity: BigInt, month: String)

case class Command(cheese: String, quantity: BigInt, month: String, bill: Double)

object ComputeScore {

  def main(args: Array[String]): DataFrame = {
    val spark = SparkSession.builder.appName("CompteScore").getOrCreate()

    val PLAYER_SUBMISSION_FILE: String = args(1)
    val COMMAND_FILE: String = args(2)
    val OUTPUT_FILE: String = args(3)

    import spark.implicits._

    val submissionDf = spark.read.json(PLAYER_SUBMISSION_FILE).as[PlayerSubmission].toDF().withColumnRenamed("quantity", "quantity_produced")
    val commandDf = spark.read.json(COMMAND_FILE).as[Command].toDF()

    val res = commandDf.groupBy(col("cheese"), col("month"))
      .agg(sum(col("quantity")).as("quantity_ordered"), sum(col("bill")).as("bill"))
      .join(submissionDf, Seq("cheese", "month"))
      .withColumn("score", abs(col("quantity_produced") - col("quantity_ordered")) * (-5))
      .groupBy(col("playerId"), col("month")).agg(sum("score").as("score"))

    res.write.mode(SaveMode.Overwrite).parquet(OUTPUT_FILE)

    res.withColumn("PK", concat(lit("PLAYER#"), col("playerId")))
      .withColumn("SK", lit("1"))
      .withColumn("GSI1PK", lit("TOP"))
      .withColumn("GSI1SK", concat(lit("SCORE#"), col("score")))
  }
}
