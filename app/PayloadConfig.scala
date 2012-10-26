package AppConfig

object PayloadConfig {

  lazy val defaultUrl = "mongodb://localhost:27017"

  def getMongoURL = {
    val mongoUrl = System.getenv("MONGOHQ_URL")

    if (mongoUrl.isEmpty) {
      println("mongoURL is empty: "+mongoUrl)
      defaultUrl
    }
    else {
      println("mongoURL is not empty: "+mongoUrl)
      mongoUrl
    }
  }
}
