import java.io._
import java.io.{BufferedWriter, File, FileWriter}
import java.nio.file.{Files, Paths, StandardCopyOption}
import java.nio.file._

import org.apache.commons.io.FileUtils

import scala.io.Source
import scala.util.{Failure, Success, Try}

/**
  * Created by Nenyi on 12/01/2017.
  */
class SimpleFileTools {

  val x = new SimpleJson
  def codeSource = x.getClass.getProtectionDomain().getCodeSource()
  def jarFile = new File(codeSource.getLocation().toURI().getPath())
  def jarDir = jarFile.getParentFile().getPath()
  def getCurrentDirectory = new File(jarDir).toString
  def getSeparator = File.separator


  def OpenAndReadFile(filename: String): String = {
    val stream = new FileInputStream(filename)
    ReadFile(stream) match {
      case Success(fileString) => fileString
      case Failure(f) => throw(f)
    }
  }

  def ReadFile(stream: FileInputStream): Try[String] =  {
    Try(Source.fromInputStream(stream).mkString)
  }

  def checkIfFileExists(filename: String): Boolean = {
    Files.exists(Paths.get(filename))
  }

  def CreateFile(filename: String, settings: String) = {
    val file = new File(filename)
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(settings)
    bw.close()
  }

  def getPodcastFiles(dir: String, extensions: Array[String]): Array[File] = {
    val casts = extensions.flatMap(ext => new File(dir).listFiles.filter(_.getName.endsWith(ext)))
    casts
  }

  def renameFile(item: PodcastItem): PodcastItem = {
    val file = item.podcastFile
    val extension = file.getName.split('.').drop(-1).lastOption
    val fileName = file.getParent + getSeparator + item.fileName + "." + extension.get
    val renamedFile = new File(fileName)
    file.renameTo(renamedFile)
    new PodcastItem(renamedFile, item.podcastTag, renamedFile.getName, item.destDir)
  }

  def checkAndCreateParentDirectory(dirPath: String) = {
    val dir = new File(dirPath)
    if (!dir.exists)
      dir.mkdir()
  }

  def matchValidDir(dirs: List[File], validDirs: List[File], parent: String): File = validDirs match {
//    case null => createNewPodcastDestination(dirs, parent)
    case Nil => createNewPodcastDestination(dirs, parent)
    case _ => validDirs.headOption match {case None => createNewPodcastDestination(dirs, parent) case Some(v) => v}
  }

  def createNewPodcastDestination(dirs: List[File], parent: String): File = {
    val file = dirs match {
      case List() => incrementAndCheckNewDirectory("", parent)
      case _ => incrementAndCheckNewDirectory(dirs.map(_.getName).sorted.last, dirs.map(_.getParent).sorted.last)
    }
    val dir = new File(file)
    dir
  }

  def incrementAndCheckNewDirectory(folderId: String, parent: String): String = {
    checkAndCreateParentDirectory(parent)

    def checkFolderId(folderId: String): String = {
      if (!Option(folderId).exists(_.trim.nonEmpty)) {
        checkAndCreateParentDirectory(parent + getSeparator +"0.1" )
        "0.1"
      }
      else {
        increment(folderId)
      }

    }
    def increment(id: String): String = {
      val splitId = id.split("\\.")
      val integer = splitId.headOption match {case None => 1 case Some(t) => t.toInt}
      val decimal = splitId.lastOption match {case None => 0 case Some(d) => d.toInt}
      val newId =
        if (decimal == 9) {
          (integer + 1).toString + "." + 0.toString
        }
        else {
          integer.toString + "." + (decimal + 1).toString
        }
      newId
    }
    parent + getSeparator + checkFolderId(folderId)
  }

  def convertToMB(size: Long): Long = {
    size / 1048576
  }

  def getListOfSubDirectories(directoryName: String): Array[File] = {
    (new File(directoryName)).listFiles.filter(_.isDirectory).map(f => f)
  }

  def getFileFolderSize(dir: File): Long = {
    dir.listFiles().map(_.length).sum
  }

  def movePodcastLegacy(item: PodcastItem) = {
    val source = item.podcastFile.toPath
    val extension = item.podcastFile.getName.split('.').drop(-1).lastOption match {case None => "Unknown" case Some(e) => e}
    val destination = new File(item.destDir + getSeparator + item.fileName).toPath
    Files.move(source, destination, StandardCopyOption.ATOMIC_MOVE)
  }

  def movePodcast(item: PodcastItem) = {
    val source = item.podcastFile.toString
    val destination = item.destDir
    FileUtils.moveFileToDirectory(
      FileUtils.getFile(source), FileUtils.getFile(destination), true)
  }

}
