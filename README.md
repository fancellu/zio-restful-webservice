# ZIO Restful webservice example

Using zio 2.0, zio-http, zio-json, quill, H2, twirl, zio-logging, zio-cache, zio-actors, zio-spark, openai

Now targeting zio-http 0.0.3

## Apps

### NoEnvApp
(no use of Environment)
  - http://localhost:8080/noenv
  - http://localhost:8080/headers (shows headers and use of request altering middleware, note "Seen" header)
  - http://localhost:8080/simplepost (POST some json, e.g. ```{"string": "hello"}```)
  - http://localhost:8080/simplepost2 (The same but using an implicit class to clean up parsing a bit)
  - http://localhost:8080/simplepostcodec (The same but using MiddlewareCodec magic)

### HelloWorldApp
(Takes a String param via serviceAt Map as app root name)
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
  - http://localhost:8080/up/ (You can put slashes at the end of each endpoint here)
  - http://localhost:8080/down
  - http://localhost:8080/get
  - http://localhost:8080/reset 

### VideoApp
(In memory and H2+Quill)
  - POST http://localhost:8080/videos/:name
  - http://localhost:8080/videos
  - http://localhost:8080/videos/loadup
  - http://localhost:8080/videos/:uuid 

### DelayApp
(Semantically sleeps for a while, no thread blocking, useful for emulating slow queries)
- http://localhost:8080/delay
- http://localhost:8080/delay/2
- http://localhost:8080/bang
- http://localhost:8080/bangrandomly

### StreamApp
(Some streaming examples)
- http://localhost:8080/stream/incrementing
- http://localhost:8080/stream/randomInt
- http://localhost:8080/stream/videos
  - (Make sure you have some videos in DB, perhaps call http://localhost:8080/videos/loadup)

### ClientApp
(Client examples talking to external web service https://jsonplaceholder.typicode.com/)
- http://localhost:8080/client/users
- http://localhost:8080/client/users/1 (Is Cached, TTL 15 seconds)
- http://localhost:8080/client/posts
- http://localhost:8080/client/posts/1
- http://localhost:8080/client/posts/userId/1 
- http://localhost:8080/client/dopost
- 
Downloads anime from ```https://api.waifu.pics/many/sfw/waifu``` into resources/waifu dir
- http://localhost:8080/client/girls 

### StaticApp
(Static file server from static dir)
- http://localhost:8080
- http://localhost:8080/subdir
- http://localhost:8080/subdir/test3.txt 
- http://localhost:8080/test.txt
- http://localhost:8080/test2.txt

### ActorsApp
- http://localhost:8080/actors
- http://localhost:8080/actors/[int]
- http://localhost:8080/nodie
- http://localhost:8080/catchAll
- http://localhost:8080/randomString

[//]: # (### SparkApp)

[//]: # (&#40;Some Spark examples with zio-spark&#41;)

[//]: # ()
[//]: # (&#40;You can disable by setting enableSpark=false&#41;)

[//]: # (- http://localhost:8080/spark)

[//]: # (- http://localhost:8080/spark/person/Peter)

[//]: # (- http://localhost:8080/spark/job)

[//]: # (- http://localhost:8080/spark/wordcount)

### OpenAICompletionApp
(Modelled after https://github.com/openai/openai-quickstart-python.git)

To use it you'll have to fill in your OpenAI `OPENAI_API_KEY`

- http://localhost:8080/openai/superhero/Dog
- http://localhost:8080/openai/prompt/translate+I+like+kittens+to+French

### OpenAIDallEApp

- http://localhost:8080/openai/dalle/a+ginger+cat,+wearing+round+black+glasses,+riding+a+bike+and+smoking+a+pipe,+photorealistic

### OpenAIModerationApp

- http://localhost:8080/openai/moderation/we+must+kill+all+the+elves
- http://localhost:8080/openai/moderation/we+must+kiss+all+the+elves

### CookieAuthApp

- http://localhost:8080/login/dino/onid
- http://localhost:8080/login/dino/badpassword
- http://localhost:8080/auth/dino/greet
- http://localhost:8080/auth/wrongname/greet

### FormApp

- http://localhost:8080/form.html

### Parallel server on port 8082

- http://localhost:8082/hello => world
- http://localhost:8082/world => hello

## To run

```scala
sbt run
```

You should find it running on http://localhost:8080/
