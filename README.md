# ZIO Restful webservice example

Using zio 2.0, zio-http, zio-json, quill, H2, twirl

## Apps

### NoEnvApp
(no use of Environment)
  - http://localhost:8080/noenv

### HelloWorldApp
(Takes a String as app root name)
  - http://localhost:8080/hello
  - http://localhost:8080/hello/Dino
  - http://localhost:8080/hello?name=Dino&name=Milo

### HelloWorldTwirlApp
(Takes a String param and uses Twirl for HTML rendering)
  - http://localhost:8080/hellotwirl
  - http://localhost:8080/hellotwirl/Dino

### DownloadApp
(Downloads a file in one go, and streaming, slowly)
  - http://localhost:8080/download
  - http://localhost:8080/download/stream

### CounterApp
(Stateful use of Ref)
  - http://localhost:8080/up
  - http://localhost:8080/down
  - http://localhost:8080/get
  - http://localhost:8080/reset 

### VideoApp
(In memory and H2+Quill)
  - POST http://localhost:8080/videos/:name
  - http://localhost:8080/videos
  - http://localhost:8080/videos/loadup
  - http://localhost:8080/videos/:name 

### DelayApp
(Semantically sleeps for a while, no thread blocking, useful for emulating slow queries)
- http://localhost:8080/delay
- http://localhost:8080/delay/2
- http://localhost:8080/bang

## To run

```scala
sbt run
```

You should find it running on http://localhost:8080/
