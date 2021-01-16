#!/usr/bin/python3
import os

tests = [
  "SimpleRouter",
  "NDP",
  "NetPaxosAcceptor",
  "NetPaxosCoordinator",
  "NetCache",
  "Flowlet",
  "SwitchNoINT",
  "SwitchINT"
]

for t in tests:
  print(t, end=','),
  for _ in range(0,5):
    output = os.popen('''/home/tian/.jdks/corretto-1.8.0_265/bin/java \
      -javaagent:/home/tian/idea-IU-202.7660.26/lib/idea_rt.jar=43179:/home/tian/idea-IU-202.7660.26/bin \
      -Dfile.encoding=UTF-8 \
      -classpath \
      /home/tian/.local/share/JetBrains/IntelliJIdea2020.2/Scala/lib/runners.jar:/home/tian/.jdks/corretto-1.8.0_265/jre/lib/charsets.jar:/home/tian/.jdks/corretto-1.8.0_265/jre/lib/ext/cldrdata.jar:/home/tian/.jdks/corretto-1.8.0_265/jre/lib/ext/dnsns.jar:/home/tian/.jdks/corretto-1.8.0_265/jre/lib/ext/jaccess.jar:/home/tian/.jdks/corretto-1.8.0_265/jre/lib/ext/jfxrt.jar:/home/tian/.jdks/corretto-1.8.0_265/jre/lib/ext/localedata.jar:/home/tian/.jdks/corretto-1.8.0_265/jre/lib/ext/nashorn.jar:/home/tian/.jdks/corretto-1.8.0_265/jre/lib/ext/sunec.jar:/home/tian/.jdks/corretto-1.8.0_265/jre/lib/ext/sunjce_provider.jar:/home/tian/.jdks/corretto-1.8.0_265/jre/lib/ext/sunpkcs11.jar:/home/tian/.jdks/corretto-1.8.0_265/jre/lib/ext/zipfs.jar:/home/tian/.jdks/corretto-1.8.0_265/jre/lib/jce.jar:/home/tian/.jdks/corretto-1.8.0_265/jre/lib/jfr.jar:/home/tian/.jdks/corretto-1.8.0_265/jre/lib/jfxswt.jar:/home/tian/.jdks/corretto-1.8.0_265/jre/lib/jsse.jar:/home/tian/.jdks/corretto-1.8.0_265/jre/lib/management-agent.jar:/home/tian/.jdks/corretto-1.8.0_265/jre/lib/resources.jar:/home/tian/.jdks/corretto-1.8.0_265/jre/lib/rt.jar:/home/tian/oldvera/target/scala-2.11/test-classes:/home/tian/oldvera/target/scala-2.11/classes:/home/tian/.ivy2/cache/com.fasterxml.jackson.core/jackson-annotations/bundles/jackson-annotations-2.8.11.jar:/home/tian/.ivy2/cache/com.fasterxml.jackson.core/jackson-core/bundles/jackson-core-2.8.11.jar:/home/tian/.ivy2/cache/com.fasterxml.jackson.core/jackson-databind/bundles/jackson-databind-2.8.11.1.jar:/home/tian/.ivy2/cache/com.fasterxml.jackson.dataformat/jackson-dataformat-yaml/bundles/jackson-dataformat-yaml-2.8.3.jar:/home/tian/.ivy2/cache/com.fasterxml.jackson.datatype/jackson-datatype-jdk8/bundles/jackson-datatype-jdk8-2.8.11.jar:/home/tian/.ivy2/cache/com.fasterxml.jackson.datatype/jackson-datatype-jsr310/bundles/jackson-datatype-jsr310-2.8.11.jar:/home/tian/.ivy2/cache/com.fasterxml.jackson.module/jackson-module-paranamer/bundles/jackson-module-paranamer-2.8.3.jar:/home/tian/.ivy2/cache/com.fasterxml.jackson.module/jackson-module-scala_2.11/bundles/jackson-module-scala_2.11-2.8.3.jar:/home/tian/.ivy2/cache/com.github.nscala-time/nscala-time_2.11/jars/nscala-time_2.11-1.0.0.jar:/home/tian/.ivy2/cache/com.ibm.icu/icu4j/jars/icu4j-58.2.jar:/home/tian/.ivy2/cache/com.regblanc/scala-smtlib_2.11/jars/scala-smtlib_2.11-0.2.jar:/home/tian/.ivy2/cache/com.storm-enroute/scalameter-core_2.11/jars/scalameter-core_2.11-0.8.2.jar:/home/tian/.ivy2/cache/com.storm-enroute/scalameter_2.11/jars/scalameter_2.11-0.8.2.jar:/home/tian/.ivy2/cache/com.thoughtworks.paranamer/paranamer/bundles/paranamer-2.8.jar:/home/tian/.ivy2/cache/com.typesafe.play/play-functional_2.11/jars/play-functional_2.11-2.6.10.jar:/home/tian/.ivy2/cache/com.typesafe.play/play-json_2.11/jars/play-json_2.11-2.6.10.jar:/home/tian/.ivy2/cache/commons-io/commons-io/jars/commons-io-2.4.jar:/home/tian/.ivy2/cache/io.spray/spray-json_2.11/bundles/spray-json_2.11-1.3.2.jar:/home/tian/.ivy2/cache/joda-time/joda-time/jars/joda-time-2.9.9.jar:/home/tian/.ivy2/cache/junit/junit/jars/junit-4.12.jar:/home/tian/.ivy2/cache/org.abego.treelayout/org.abego.treelayout.core/bundles/org.abego.treelayout.core-1.0.3.jar:/home/tian/.ivy2/cache/org.antlr/ST4/jars/ST4-4.0.8.jar:/home/tian/.ivy2/cache/org.antlr/antlr-runtime/jars/antlr-runtime-3.5.2.jar:/home/tian/.ivy2/cache/org.antlr/antlr4-runtime/jars/antlr4-runtime-4.7.jar:/home/tian/.ivy2/cache/org.antlr/antlr4/jars/antlr4-4.7.jar:/home/tian/.ivy2/cache/org.apache.commons/commons-lang3/jars/commons-lang3-3.5.jar:/home/tian/.ivy2/cache/org.apache.commons/commons-math3/jars/commons-math3-3.2.jar:/home/tian/.ivy2/cache/org.glassfish/javax.json/bundles/javax.json-1.0.4.jar:/home/tian/.ivy2/cache/org.hamcrest/hamcrest-core/jars/hamcrest-core-1.3.jar:/home/tian/.ivy2/cache/org.joda/joda-convert/jars/joda-convert-1.2.jar:/home/tian/.ivy2/cache/org.mongodb/casbah-commons_2.11/jars/casbah-commons_2.11-3.1.1.jar:/home/tian/.ivy2/cache/org.mongodb/casbah-core_2.11/jars/casbah-core_2.11-3.1.1.jar:/home/tian/.ivy2/cache/org.mongodb/casbah-gridfs_2.11/jars/casbah-gridfs_2.11-3.1.1.jar:/home/tian/.ivy2/cache/org.mongodb/casbah-query_2.11/jars/casbah-query_2.11-3.1.1.jar:/home/tian/.ivy2/cache/org.mongodb/mongo-java-driver/jars/mongo-java-driver-3.2.2.jar:/home/tian/.ivy2/cache/org.ow2.asm/asm/jars/asm-5.0.4.jar:/home/tian/.ivy2/cache/org.scala-lang.modules/scala-parser-combinators_2.11/bundles/scala-parser-combinators_2.11-1.0.1.jar:/home/tian/.ivy2/cache/org.scala-lang.modules/scala-xml_2.11/bundles/scala-xml_2.11-1.0.1.jar:/home/tian/.ivy2/cache/org.scala-lang.modules/scala-xml_2.11/bundles/scala-xml_2.11-1.0.2.jar:/home/tian/.ivy2/cache/org.scala-lang/scala-library/jars/scala-library-2.11.11.jar:/home/tian/.ivy2/cache/org.scala-lang/scala-reflect/jars/scala-reflect-2.11.11.jar:/home/tian/.ivy2/cache/org.scala-tools.testing/test-interface/jars/test-interface-0.5.jar:/home/tian/.ivy2/cache/org.scalatest/scalatest_2.11/bundles/scalatest_2.11-2.2.4.jar:/home/tian/.ivy2/cache/org.slf4j/slf4j-api/jars/slf4j-api-1.6.0.jar:/home/tian/.ivy2/cache/org.typelevel/macro-compat_2.11/jars/macro-compat_2.11-1.1.1.jar:/home/tian/.ivy2/cache/org.yaml/snakeyaml/bundles/snakeyaml-1.15.jar:/home/tian/oldvera/lib/jSMTLIB.jar:/home/tian/oldvera/lib/scalaz3_2.11-2.1.jar \
      org.jetbrains.plugins.scala.testingSupport.scalaTest.ScalaTestRunner \
      -s parser.p4.test.AliTiming \
      -testName ''' + t, 'r')
    print(list(filter(lambda x:x.isdigit(), output.read().splitlines(False)))[0], end=',')
  print()
