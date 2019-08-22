# xmll
Basic command line utility for shredding a big XML file full of records into a text file with one XML record on each line.

Sometimes you have a BIG XML, and you want, like, a lot of little XMLs? 

Which is funny, because the little XMLs each describe something different, and yet someone has herded them into one enormous tree like an infinite number of monkeys.

And SOMETIMES, that XML tree full of monkeys is SO BIG that it's bigger than your RAM. Someone should <a href="https://developers.google.com/protocol-buffers/">do</a> <a href="https://avro.apache.org">something</a> <a href="https://thrift.apache.org/">about</a> your XML getting so BIG all the time! 

Oh well, until that happens, you can use this command line tool. Just <a href="https://www.scala-sbt.org/1.x/docs/Setup.html">install sbt</a>, check out this project, go to the xmll directory and do:

```sbt "runMain dpla.xmll.Main <name of record element> <infile> <outfile>"```

The outfile will end up containing one row for every <name of record element> that is found at a sibling level as the first one found. Each line will contain the XML corresponding to that element and it's descendants. All newlines in the xml will be replaced with spaces.

If you'd like, you can package the project up into a portable JAR file using the command `sbt assembly`. The JAR will be saved at the path `target/cala-2.13/xmll-assembly-0.1.jar`, and then you can copy it to wherever a java install is handy, and run it with: 

```java -jar xmll-assembly-0.1.jar <name of record element> <infile> <outfile>```

Happy trees!
