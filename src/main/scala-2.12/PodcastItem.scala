import java.io.File
import org.jaudiotagger.tag.Tag
/**
  * Created by Nenyi on 12/01/2017.
  */
case class PodcastItem (podcastFile: File, podcastTag: Tag, fileName: String, destDir: String)
case class BBCTags (artist: String, title: String, album: String, albumArtist: String,  genre: String)
case class PodSettings(source: String, destination: String, extensions: Array[String])
