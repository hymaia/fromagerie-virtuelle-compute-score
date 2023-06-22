package fr.hymaia.fromagerie

import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{FloatType, IntegerType}
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}

case class Command(cheese: String, quantity: String, year: String, month: String, day: String, bill: String)

object ComputeScore {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder.master("local[*]").appName("CompteScore").getOrCreate()
    ComputeScoreReference(Array("", "src/main/resources/commands.json", "output3"))
  }

  def ComputeScoreReference(args: Array[String]): Unit = {
    val spark = SparkSession.builder.appName("CompteScore").getOrCreate()

    val COMMAND_FILE: String = args(1)
    val OUTPUT_FILE: String = args(2)

    import spark.implicits._

    val commandDf = spark.read.json(COMMAND_FILE).as[Command].toDF()

    val res = commandDf.groupBy(col("cheese"), col("year"), col("month"))
      .agg(sum(col("quantity").cast(IntegerType)).as("quantity_ordered"), sum(col("bill").cast(FloatType)).as("bill"))

    res.coalesce(1).write.mode(SaveMode.Overwrite).partitionBy("year", "month").json(OUTPUT_FILE)
  }
}
