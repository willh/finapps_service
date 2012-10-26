package AppConfig

object PayloadConfig {

  lazy val defaultUrl = "mongodb://localhost:27017"

  def getMongoURL = {
    val mongoUrl = System.getenv("MONGOHQ_URL")

    if (mongoUrl.isEmpty) {
      println("mongoURL is empty: "+mongoUrl)
      //defaultUrl
      "mongodb://heroku:d60e26f0adba335cfc4bb6af0820479f@alex.mongohq.com:10050/app8754822"
    }
    else {
      println("mongoURL is not empty: "+mongoUrl)
      mongoUrl
    }
  }
}
