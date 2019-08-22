package dpla.xmll

import java.io.{FileInputStream, PrintWriter, StringWriter, Writer}
import java.util.Date

import javax.xml.stream._
import XMLStreamConstants._

object Main extends App {
  val start = new Date().getTime
  val documentElement = args(0) //"doc"
  val inFile = args(1) //"test.xml" ///Users/michael/NMNHBOTANY_DPLA" //args(1)
  val outFile = args(2) //"out.xmll" //args(2)

  val eventReader = XMLInputFactory.newFactory()
    .createXMLEventReader(new FileInputStream(inFile))
  val eventWriterFactory = XMLOutputFactory.newFactory()

  val out = new PrintWriter(outFile)

  //we need to skip past the intro matter (xml declaration, root element(s)
  // and dig to the first document element
  while (eventReader.hasNext) {
    val event = eventReader.peek()
    if (event.isStartElement &&
        event.asStartElement().getName.getLocalPart == documentElement)
      processDocuments(eventReader, out) // that's one
    else eventReader.nextEvent() //skip it
  }

  out.close()

  val end = new Date().getTime
  println(end - start + " ms.")

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
