# Fromagerie virtuelle : compute score

## compile

```shell
sbt compile
```

## test

Il n'y a pas encore de tests, mais sinon :
```shell
sbt test
```

## Run local

Il y a 2 jobs Spark :
* computeScore : calcule le score de chaque joueur
* computeScoreReference : calcule les réponses exactes nécessaires pour calculer le score

Via intelliJ en exécutant le `main` de la classe `ComputeScore`. Les fichiers dans `src/main/resources` servent actuellement de scénario de test pour l'exécution en local.

À vous de décommenter la ligne 17 si vous souhaitez tester le computeScoreReference.

## Deploy AWS

Une CI/CD déploie automatiquement l'infra, le jar et les scalascript pour les jobs Glues à chaque commit.

Pour les déployer à la main :
```bash
sbt package
aws s3 cp target/scala-2.12/fromagerie-virtuelle-compute-score_2.12-0.1.jar s3://fromagerie-virtuelle-datalake/jars/compute-score.jar
aws s3 cp RunnerComputeScoreReference.scala s3://fromagerie-virtuelle-datalake/scripts/RunnerComputeScoreReference.scala
aws s3 cp RunnerComputeScore.scala s3://fromagerie-virtuelle-datalake/scripts/RunnerComputeScore.scala
sam build
sam deploy
```

Attention si vous renommez un des scalascript à mettre à jour les commandes dans la CI/CD.
