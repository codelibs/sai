CodeLibs Sai
[![Java CI](https://github.com/codelibs/sai/actions/workflows/ant.yml/badge.svg)](https://github.com/codelibs/sai/actions/workflows/ant.yml)
=============

Sai is a runtime environment for programs written in ECMAScript 5.1 that runs on top of JVM.
This project forked from Nashorn.

## Usage

JAR file is in Maven repository.
You can add the following setting to pom.xml.

        <dependency>
            <groupId>org.codelibs</groupId>
            <artifactId>sai</artifactId>
            <version>0.2.0</version>
        </dependency>


## Build

```
ant -f make/build.xml
```
