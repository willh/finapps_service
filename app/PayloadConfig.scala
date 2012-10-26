package AppConfig

object PayloadConfig {

  lazy val defaultUrl = "mongodb://localhost:27017"

  def getMongoURL = {
    val mongoUrl = System.getenv("MONGOHQ_URL")

    if (mongoUrl.isEmpty)
      defaultUrl
    else
      mongoUrl
  }
}
