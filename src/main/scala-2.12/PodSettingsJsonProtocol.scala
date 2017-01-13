import spray.json.DefaultJsonProtocol
/**
  * Created by Nenyi on 12/01/2017.
  */
object PodSettingsJsonProtocol extends DefaultJsonProtocol {
  implicit val podSettingsFormat = jsonFormat3(PodSettings)
}
