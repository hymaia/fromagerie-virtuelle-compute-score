package fr.hymaia.fromagerie

import org.apache.spark._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{SaveMode, SparkSession}
import com.amazonaws.services.glue.GlueContext
import com.amazonaws.services.glue.util.GlueArgParser
import com.amazonaws.services.glue.util.Job
import com.amazonaws.services.glue.util.JsonOptions
import scala.collection.JavaConverters._
import fr.hymaia.fromagerie.ComputeScore

object Runner {
  def main(args: Array[String]): Unit = {
    val sc: SparkContext = new SparkContext()
    val glueContext: GlueContext = new GlueContext(sc)

    val param_names = Array(
      "JOB_NAME",
      "COMMAND_FILE",
      "OUTPUT_FILE",
    )
    val params = GlueArgParser.getResolvedOptions(args, param_names)
    Job.init(params("JOB_NAME"), glueContext, params.asJava)

    ComputeScore.ComputeScoreReference(param_names.map(params(_)))

    Job.commit()
  }
}
