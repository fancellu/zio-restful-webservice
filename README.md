# 🚀 ZIO Restful webservice example

A collection of small, independent example apps (one per package under `src/main/scala/com/felstar/restfulzio`),
all composed together and served by `MainApp` on http://localhost:8080, plus a second server on port 8082.

Using zio 2.1, zio-http 3, zio-json, quill, H2, twirl, zio-logging, zio-cache, zio-actors, zio-spark, Gemini, openai

Now targeting zio 2.1.27 and zio-http 3.11.6 (the `Routes` / `Handler` API)

## 🧩 Apps

📚 Full per-endpoint documentation (params, responses, curl examples) is in [docs/](docs/README.md).

### 🌱 NoEnvApp
📖 [Docs](docs/noenv.md)

(no use of Environment)
  - http://localhost:8080/noenv
  - http://localhost:8080/headers (shows headers and use of request altering middleware, note "Seen" header)
  - http://localhost:8080/simplepost (POST some json, e.g. ```{"string": "hello"}```)
  - http://localhost:8080/simplepost2 (The same but using an implicit class to clean up parsing a bit)
  - http://localhost:8080/simplepostcodec (The same, but the route is a plain `SimplePost => String` function, wrapped by a generic json handler. Bad json is a 400)

### 👋 HelloWorldApp
📖 [Docs](docs/helloworld.md)

(Takes a String param via serviceAt Map as app root name)
  - http://localhost:8080/hello
  - http://localhost:8080/hello/Dino
  - http://localhost:8080/hello?name=Dino&name=Milo

### 🌀 HelloWorldTwirlApp
📖 [Docs](docs/hellotwirl.md)

(Takes a String param and uses Twirl for HTML rendering)
  - http://localhost:8080/hellotwirl
  - http://localhost:8080/hellotwirl/Dino

### 📥 DownloadApp
📖 [Docs](docs/download.md)

(Downloads a file in one go, and streaming, slowly)
  - http://localhost:8080/download
  - http://localhost:8080/download/stream

### 🔢 CounterApp
📖 [Docs](docs/counter.md)

(Stateful use of Ref)
  - http://localhost:8080/up
  - http://localhost:8080/up/ (You can put slashes at the end of each endpoint here)
  - http://localhost:8080/down
  - http://localhost:8080/get
  - http://localhost:8080/reset 

### 🎬 VideoApp
📖 [Docs](docs/videos.md)

(In memory and H2+Quill)
  - POST http://localhost:8080/videos/:name
  - http://localhost:8080/videos
  - http://localhost:8080/videos/loadup
  - http://localhost:8080/videos/:uuid 

### ⏳ DelayApp
📖 [Docs](docs/delay.md)

(Semantically sleeps for a while, no thread blocking, useful for emulating slow queries)
- http://localhost:8080/delay
- http://localhost:8080/delay/2
- http://localhost:8080/bang 💥 (always fails with a 500)
- http://localhost:8080/bangrandomly 🎲 (fails with a 500 about 9 times out of 10)

### 🌊 StreamApp
📖 [Docs](docs/stream.md)

(Some streaming examples)
- http://localhost:8080/stream/incrementing
- http://localhost:8080/stream/randomInt
- http://localhost:8080/stream/both (the two above, one after the other)
- http://localhost:8080/stream/videos
  - (Make sure you have some videos in DB, perhaps call http://localhost:8080/videos/loadup)

### 🌐 ClientApp
📖 [Docs](docs/client.md)

(Client examples talking to external web service https://jsonplaceholder.typicode.com/)
- http://localhost:8080/client/users
- http://localhost:8080/client/users/1 (Is Cached, TTL 15 seconds)
- http://localhost:8080/client/posts
- http://localhost:8080/client/posts/1
- http://localhost:8080/client/posts/userId/1 
- http://localhost:8080/client/dopost

🐶 Downloads 5 random dog pictures from ```https://dog.ceo/api/breeds/image/random/5``` into `src/main/resources/dogs` (created if missing)
- http://localhost:8080/client/dogs 

### 📁 StaticApp
📖 [Docs](docs/static.md)

(Static file server from static dir)
- http://localhost:8080
- http://localhost:8080/subdir
- http://localhost:8080/subdir/test3.txt 
- http://localhost:8080/test.txt
- http://localhost:8080/test2.txt

### 🎭 ActorsApp
📖 [Docs](docs/actors.md)

- http://localhost:8080/actors
- http://localhost:8080/actors/[int]
- http://localhost:8080/actors/nodie
- http://localhost:8080/actors/catchAll
- http://localhost:8080/actors/randomString (deliberately fails with a 500, the exception is logged by the error middleware)

[//]: # (### SparkApp)

[//]: # (&#40;Some Spark examples with zio-spark&#41;)

[//]: # ()
[//]: # (&#40;You can disable by setting enableSpark=false&#41;)

[//]: # (- http://localhost:8080/spark)

[//]: # (- http://localhost:8080/spark/person/Peter)

[//]: # (- http://localhost:8080/spark/job)

[//]: # (- http://localhost:8080/spark/wordcount)

### ✨ GeminiTextApp
📖 [Docs](docs/gemini.md)

(Text generation with Google's `gemini-3.8-flash`)

🔑 To use it you'll have to set your `GEMINI_API_KEY` (free tier available at https://aistudio.google.com/apikey)

- http://localhost:8080/gemini/superhero/Dog
- http://localhost:8080/gemini/prompt/translate+I+like+kittens+to+French

### 🎨 GeminiImageApp
📖 [Docs](docs/gemini.md)

(Image generation with `gemini-3.1-flash-image`, same `GEMINI_API_KEY`)

- http://localhost:8080/gemini/image/a+ginger+cat,+wearing+round+black+glasses,+riding+a+bike+and+smoking+a+pipe,+photorealistic

### 🛡️ OpenAIModerationApp
📖 [Docs](docs/openai.md)

🔑 Needs your OpenAI `OPENAI_API_KEY` (the moderation endpoint is free)

- http://localhost:8080/openai/moderation/we+must+kill+all+the+elves
- http://localhost:8080/openai/moderation/we+must+kiss+all+the+elves

### 🍪 CookieAuthApp
📖 [Docs](docs/auth.md)


- http://localhost:8080/login/dino/onid
- http://localhost:8080/login/dino/badpassword
- http://localhost:8080/auth/dino/greet
- http://localhost:8080/auth/wrongname/greet

### 📝 FormApp
📖 [Docs](docs/form.md)


- http://localhost:8080/form.html

### 🔀 Parallel server on port 8082
📖 [Docs](docs/server-8082.md)


- http://localhost:8082/hello => world
- http://localhost:8082/world => hello

## 📋 Requirements

- JDK 17 or 21 recommended (it builds and runs on newer JDKs too, but if you re-enable the Spark code you'll need 17 or older, or the appropriate `--add-opens` flags)
- sbt 1.x (currently 1.13.0, see `project/build.properties`). ⚠️ **Not sbt 2.x**: the sbt-twirl plugin and the Scala 2 compiler plugin used here are sbt 1.x only
- Scala 2.13.18 (set in `build.sbt`)

## ▶️ To run

```
sbt run
```

You should find it running on http://localhost:8080/ (and http://localhost:8082/hello)

Press enter in the terminal to stop the servers.

## 🧪 To test

```
sbt test
```

Uses zio-test and the zio-http testkit.

## ⚙️ Configuration

- H2 database settings are in `src/main/resources/application.conf`, schema in `h2-schema.sql`
- `MainApp` uses the in-memory video repo by default. Swap in `PersistentVideoRepo.layer` to use H2 + Quill
- The Gemini apps read `GEMINI_API_KEY`, and the OpenAI moderation app reads `OPENAI_API_KEY`, from a system property or an environment variable
- Spark is currently disabled (see `enableSpark` in `MainApp`), as enabling it made the 8080 endpoints unreachable

## 🩺 Troubleshooting

- `bad constant pool index` / `Could not initialize class sbt.internal.parser.SbtParser$`: your sbt is too old for your JDK. Use sbt 1.10+ or an older JDK
- `sbt-bloop_sbt2_3 ... not found`: Metals has generated sbt 2 plugin files. Use sbt 1.x, and delete the auto-generated `project/project/` and `project/metals.sbt`
- `Create a new server?` / `ServerAlreadyBootingException`: another sbt instance is still running, stop it first
- `found version conflict(s)` for `scala-xml` or `zio-json`: handled by `libraryDependencySchemes` in `build.sbt`
