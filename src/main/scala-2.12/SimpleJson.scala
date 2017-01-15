import PodSettingsJsonProtocol._
import spray.json._
/**
  * Created by Nenyi on 12/01/2017.
  */
class SimpleJson {
  def createSettingJson(doesExist: Boolean, filename: String): String = {

    doesExist match {
      case false =>
        val simpleFile = new SimpleFileTools
        val settings = new PodSettings(simpleFile.getCurrentDirectory + simpleFile.getSeparator + "podcasts",
          simpleFile.getCurrentDirectory + simpleFile.getSeparator + "podcast", Array("mp4", "m4a")).toJson
        simpleFile.CreateFile(filename, settings.toString)
        filename
      case _ => filename
    }
  }

  def ConvertJsonToPodcastSettings(content: String): PodSettings = {
    content.parseJson.convertTo[PodSettings]
  }


}
