package fr.hymaia.fromagerie

import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}

case class Command(cheese: String, quantity: BigInt, year: String, month: String, day: String, bill: Double)

case class Answer(cheese: String, month: String, quantity_ordered: BigInt, player: String)

case class ScoreReference(cheese: String, month: String, quantity_produced: BigInt)

object ComputeScore {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder.master("local[*]").appName("ComputeScoreReference").getOrCreate()
    //computeScoreReference(Array("", "src/main/resources/commands.json", "output3"))
    val res = computeScore(Array("", "src/main/resources/score-reference.json", "src/main/resources/answers.json", "output/scores"))
    res.write.mode(SaveMode.Overwrite).json("output/dynamo-scores")
  }

  def computeScoreReference(args: Array[String]): Unit = {
    val spark = SparkSession.builder.appName("ComputeScoreReference").getOrCreate()

    val COMMAND_FILE: String = args(1)
    val OUTPUT_FILE: String = args(2)

    import spark.implicits._

    val commandDf = spark.read.json(COMMAND_FILE).as[Command].toDF()

    val res = commandDf.groupBy(col("cheese"), col("year"), col("month"))
      .agg(sum(col("quantity")).as("quantity_produced"), sum(col("bill")).as("bill"))

    res.coalesce(1).write.mode(SaveMode.Overwrite).partitionBy("year", "month").json(OUTPUT_FILE)
  }

  def computeScore(args: Array[String]): DataFrame = {
    val spark = SparkSession.builder.appName("ComputeScore").getOrCreate()

    val SCORE_REFERENCE_FILE: String = args(1)
    val ANSWER_FILE: String = args(2)
    val OUTPUT_FILE: String = args(3)

    import spark.implicits._
    val dfScoreRef = spark.read.json(SCORE_REFERENCE_FILE).as[ScoreReference].where(col("year") === "2024")
    val dfAnswer = spark.read.json(ANSWER_FILE).as[Answer]

    val res = dfAnswer.join(dfScoreRef, Seq("cheese", "month"))
      .withColumn("score", abs(col("quantity_produced") - col("quantity_ordered")) * (-5))
      .groupBy(col("player"), col("month")).agg(sum("score").as("score"))
      .withColumn("rank", rank() over Window.orderBy(col("score").desc)).cache()

    res.write.mode(SaveMode.Overwrite).parquet(OUTPUT_FILE)

    res.withColumn("PK", concat(lit("PLAYER#"), col("player")))
      .withColumn("SK", lit("1"))
      .withColumn("GSI1PK", lit("TOP"))
      .withColumn("GSI1SK", col("rank"))
  }
}
