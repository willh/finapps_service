package AppConfig

object PayloadConfig {

  lazy val mongoEnvProp = "MONGOHQ_URL"
  lazy val defaultUrl = "mongodb://localhost:27017"

  def getMongoURL = {
    Option(System.getenv(mongoEnvProp)) getOrElse defaultUrl
  }
}
