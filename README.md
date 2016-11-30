# Promises
[![Build Status](https://travis-ci.org/Kjens93/promises-java.svg?branch=master)](https://travis-ci.org/Kjens93/promises-java)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kjens93.promises/promises.svg)](https://mvnrepository.com/artifact/io.github.kjens93.promises/promises)

A Java library for streaming future tasks.

## Installation
```xml
<dependency>
    <groupId>io.github.kjens93.promises</groupId>
    <artifactId>promises</artifactId>
    <version>LATEST</version>
</dependency>
```

## Usage
```java

class Example {
    
    public void commitment() {
        
        Commitment c = () -> {
            //Perform some long-running task, like taking out the garbage.
        };
        
        c.async();
        
        //Do some other stuff while we're waiting
        
        c.await(); //Blocks until the commitment is fulfilled.
        
    }
    
    public void promise() {
        
        Promise<Boolean> p = () -> {
            //Perform some long running task, like downloading an image.
            return true;
        };
        
        p.async();
        
        //Do some other stuff while we're waiting
        
        boolean result = p.get(); //Blocks until the promise is fulfilled.
        
    }
    
    public void callbacks() {
            
        Promise<Boolean> p = () -> {
            //Perform some long running task, like downloading an image.
            return true;
        };
        
        int i = p.andThen((b) -> {
            //Do something awesome here.
        }).map((b) -> {
            return 1;
        }).get();
        
    }
    
}



```