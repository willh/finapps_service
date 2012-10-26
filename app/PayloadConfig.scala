package AppConfig

object PayloadConfig {

  lazy val defaultUrl = "mongodb://localhost:27017"

  def getMongoURL = {
    Option(System.getenv("MONGOHQ_URL")) getOrElse defaultUrl
  }
}
