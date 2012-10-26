package AppConfig

object PayloadConfig {

  lazy val defaultUrl = "mongodb://127.0.0.1:27017"

  def getMongoURL = {
    Option(System.getenv("MONGOHQ_URL"))
  }
}
