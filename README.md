# Promises
[![Build Status](https://travis-ci.org/Kjens93/promises-java.svg?branch=master)](https://travis-ci.org/Kjens93/promises-java)
[![Coverage Status](https://coveralls.io/repos/github/Kjens93/promises-java/badge.svg?branch=master)](https://coveralls.io/github/Kjens93/promises-java?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.kjens93.promises/promises/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.kjens93.promises/promises)

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
        
        c.async(); //Promise is executed on an instance of Executors.cachedThreadPool()
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
        }).filter((i) -> {
            return i == 1;
        }).ifPresent((i) -> {
            //Do something with this integer
        }).get();
        
    }
    
}
```