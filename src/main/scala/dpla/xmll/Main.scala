package dpla.xmll

import java.io._
import java.util.Date
import java.util.zip.{GZIPInputStream, GZIPOutputStream}

import javax.xml.stream.XMLStreamConstants._
import javax.xml.stream._
import org.apache.commons.compress.compressors.bzip2.{BZip2CompressorInputStream, BZip2CompressorOutputStream}
import org.apache.tools.bzip2.CBZip2InputStream

import scala.util.{Failure, Success, Try}


object Main extends App {
  val start = new Date().getTime
  val documentElement = args(0) //"doc"
  val inFile = args(1) //"test.xml" ///Users/michael/NMNHBOTANY_DPLA" //args(1)
  val outFile = args(2) //"out.xmll" //args(2)


  val eventReader = getInputStream(new File(inFile)) match {
    case Some(stream) => XMLInputFactory.newFactory().createXMLEventReader(stream)
    case None => throw new RuntimeException(s"No valid stream from file $inFile")
  }

  val eventWriterFactory = XMLOutputFactory.newFactory()

  val out = getOutputStream(new File(outFile)) match {
    case Some(writer) => new PrintWriter(writer)
    case None => throw new RuntimeException(s"No valid output format for file $outFile")
  }

  //we need to skip past the intro matter (xml declaration, root element(s)
  // and dig to the first document element
  while (eventReader.hasNext) {
    Try { eventReader.peek() } match {
      case Success(event) =>
        if (event.isStartElement &&
          event.asStartElement().getName.getLocalPart == documentElement)
          processDocuments(eventReader, out) // that's one
        else eventReader.nextEvent() //skip it
      case Failure(f) =>
        println(s"Failure ${f.getMessage} ${eventReader.toString}")
        eventReader.nextEvent() //skip it
    }

  }

  out.close()

  val end = new Date().getTime
  println(end - start + " ms.")

  /**
    * Loads .gz, .tgz, .bz, and .tbz2, and plain old .tar files.
    *
    * @param file File to parse
    * @return TarInputstream of the tar contents
    */
  def getInputStream(file: File): Option[InputStream] =
    file.getName match {
      case xml if xml.endsWith(".xml") =>
        Some(new FileInputStream(xml))

      case gzName if gzName.endsWith("gz") =>
        Some(new GZIPInputStream(new FileInputStream(file)))

      case bz2name if bz2name.endsWith("bz2") =>
        val inputStream = new FileInputStream(file)
        inputStream.skip(2)
        Some(new org.apache.tools.bzip2.CBZip2InputStream(inputStream))

      case tarName if tarName.endsWith("tar") =>
        Some(new FileInputStream(file))

      case _ => None
    }

  /**
    * Write output to xml, gz or bz compression
    *
    * @param file
    * @return
    */
  def getOutputStream(file: File): Option[OutputStream] =
    file.getName match {
      case xml if xml.endsWith(".xml") =>
        Some(new FileOutputStream(xml))

      case gzName if gzName.endsWith("gz") =>
        Some(new GZIPOutputStream(new FileOutputStream(file)))

      case bz2name if bz2name.endsWith("bz2") =>
        Some(new BZip2CompressorOutputStream(new FileOutputStream(file)))

      case _ => None
    }


  def processDocuments(eventReader: XMLEventReader, out: PrintWriter): Unit = {
    while (eventReader.hasNext) {
      eventReader.peek().getEventType match {
        case START_ELEMENT =>
          val doc = subtreeAsString(eventReader)
          val line = doc.replaceAll("\n", " ")
          out.println(line)

        case _ => eventReader.nextEvent() //skip past
      }
    }
  }

  def subtreeAsString(eventReader: XMLEventReader): String = {
    val stringWriter = new StringWriter()
    val eventWriter = eventWriterFactory.createXMLEventWriter(stringWriter)
    eventWriter.add(eventReader.nextEvent()) //writes root of subtree
    recurse(eventReader, eventWriter)
    //closing root tag written during recurse call
    eventWriter.flush()
    eventWriter.close()
    stringWriter.toString
  }

  def recurse(eventReader: XMLEventReader, eventWriter: XMLEventWriter): Unit = {
    //write root of subtree
    while (eventReader.hasNext) {
      val event = eventReader.nextEvent()
      eventWriter.add(event)
      event.getEventType match {
        case END_ELEMENT => return //element done. move back up in tree/call stack
        case START_ELEMENT => recurse(eventReader, eventWriter) //new element. we have to go deeper
        case _ => () //leaf. keep loopin'
      }
    }
  }
}
