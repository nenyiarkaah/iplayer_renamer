import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.{FieldKey, Tag}
import org.jaudiotagger.tag.mp4.{Mp4FieldKey, Mp4Tag}
import java.io._

import scala.collection.immutable.Stream.Empty
/**
  * Created by Nenyi on 12/01/2017.
  */
class SimplePodcastTools {

  val simpleFileTools = new SimpleFileTools

  def processBBCPodcasts() = {
    val simpleJson = new SimpleJson
    val filename = simpleFileTools.getCurrentDirectory + simpleFileTools.getSeparator + "settings.json"
    println(filename)

    val content = simpleFileTools OpenAndReadFile(simpleJson createSettingJson(simpleFileTools checkIfFileExists(filename), filename))
    val settings = simpleJson.ConvertJsonToPodcastSettings(content)
    //copy from worksheet
    //get Podcasts from Podcast source
    val podcasts = getBBCPodcastFiles(settings.source, settings.extensions)

    //extract and rename podcasts
    val items = podcasts.map(p =>
       getPodcastDestination(
       extractAndRenamePodcastTags(
       getPodcastTags(p), p), settings.destination))
      .map(p => writeToPodcast(p))
      .map(p => simpleFileTools renameFile(p))

    //move podcasts to destination
     items.map(p => simpleFileTools movePodcast(mapPodcastDestination(p)))
  }

  def getPodcastTags(file: File): Tag = {
    AudioFileIO.read(file).getTag().asInstanceOf[Mp4Tag]
  }

  def isBBCPodcast(tag: Tag): Boolean = {
    val mp4tag = tag.asInstanceOf[Mp4Tag]
    mp4tag.getFirst(Mp4FieldKey.COMPOSER) == "BBC iPlayer" && mp4tag.getFirst(FieldKey.GENRE) != "Podcast"
  }

  def getBBCPodcastFiles(dir: String, extensions: Array[String]): List[File] = {
    simpleFileTools.getPodcastFiles(dir, extensions).map(p =>
      if(isBBCPodcast(getPodcastTags(p))) p
    ).toList.filter(_ != ()).asInstanceOf[List[File]]
  }

  def extractPodcastTags(tag: Tag, f: File): PodcastItem = {
    // this is the point where I have to find out which show I am searching for?
    val mp4tag = tag.asInstanceOf[Mp4Tag]
    val artist = mp4tag.getFirst(Mp4FieldKey.ARTIST)
    val title = mp4tag.getFirst(Mp4FieldKey.TITLE)
    val album = mp4tag.getFirst(Mp4FieldKey.ALBUM)
    val genre = mp4tag.getFirst(Mp4FieldKey.GENRE)
    val fileName = artist + "-" + title
    new PodcastItem(f, renamePodcastTags(mp4tag, new BBCTags(artist, title, album, album, genre)), fileName, "")
  }

  def extractAndRenamePodcastTags(tag: Tag, f: File): PodcastItem = {
    // this is the point where I have to find out which show I am searching for?
    val mp4tag = tag.asInstanceOf[Mp4Tag]
    val mp4Split = mp4tag.getFirst(Mp4FieldKey.TITLE).split(": ")
    val artist: String = mp4Split.headOption match {case null => "" case a => "(" ++ a.get ++ ")"}
    val title = mp4Split.lastOption match {case null => "" case t => t.get}
    val album = mp4Split.headOption match {case null => "" case a => "(BBC Radio 1)-" ++ a.get}
    val genre = "Podcast"
    val fileName = artist + "-" + title.replaceAll("[^a-zA-Z0-9\\._ ]+", "")
    new PodcastItem(f, renamePodcastTags(mp4tag, new BBCTags(artist, title, album, album, genre)), fileName, "")
  }

  def renamePodcastTags(tag: Tag, bbcTag: BBCTags): Tag = {
    val mp4tag = tag.asInstanceOf[Mp4Tag]
    mp4tag.setField(FieldKey.ARTIST, bbcTag.artist)
    mp4tag.setField(FieldKey.ORIGINAL_ARTIST, bbcTag.artist)
    mp4tag.setField(FieldKey.TITLE, bbcTag.title)
    mp4tag.setField(FieldKey.ALBUM, bbcTag.album)
    mp4tag.setField(FieldKey.ALBUM_ARTIST, bbcTag.albumArtist)
    mp4tag.setField(FieldKey.GENRE, bbcTag.genre)
    mp4tag
  }

  def writeToPodcast(item: PodcastItem): PodcastItem  = {
    val podcast = AudioFileIO.read(item.podcastFile)
    podcast.setTag(item.podcastTag)
    podcast.commit()
    item
  }

  def getPodcastDestination(item: PodcastItem, destDir: String): PodcastItem = {
    val mp4tag = item.podcastTag.asInstanceOf[Mp4Tag]
    val album = mp4tag.getFirst(Mp4FieldKey.ALBUM)
    new PodcastItem(item.podcastFile, item.podcastTag, item.fileName, destDir + simpleFileTools.getSeparator + album)
  }

  def mapPodcastDestination(item: PodcastItem): PodcastItem = {
    val destDir = item.destDir
    val size = item.podcastFile.length
    simpleFileTools.checkAndCreateParentDirectory(destDir)
    val dirs = simpleFileTools.getListOfSubDirectories(destDir).toList
    val validDirs =  dirs.map(d => if(simpleFileTools.convertToMB(simpleFileTools.getFileFolderSize(d) + size) > 4140 || d.list().length != 0) d)
      .filter(_ != ()).asInstanceOf[List[File]]

    new PodcastItem(item.podcastFile, item.podcastTag, item.fileName, simpleFileTools.matchValidDir(dirs, validDirs, destDir).get.getAbsolutePath)
  }
}
