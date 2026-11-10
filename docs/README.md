# 📚 Endpoint documentation

Every example app lives in its own package under [`src/main/scala/com/felstar/restfulzio`](../src/main/scala/com/felstar/restfulzio).
[`MainApp`](../src/main/scala/com/felstar/restfulzio/MainApp.scala) composes them into one `Routes` value and serves it on **port 8080**. A second, independent server runs on **port 8082**.

| App | Doc | Paths |
|-----|-----|-------|
| 🌱 NoEnvApp | [noenv.md](noenv.md) | `/noenv`, `/headers`, `/simplepost`, `/simplepost2`, `/simplepostcodec` |
| 👋 HelloWorldApp | [helloworld.md](helloworld.md) | `/hello`, `/hello/:name` |
| 🌀 HelloWorldTwirlApp | [hellotwirl.md](hellotwirl.md) | `/hellotwirl`, `/hellotwirl/:string` |
| 📥 DownloadApp | [download.md](download.md) | `/download`, `/download/stream` |
| 🔢 CounterApp | [counter.md](counter.md) | `/up`, `/down`, `/get`, `/reset` |
| 🎬 VideoApp | [videos.md](videos.md) | `/videos`, `POST /videos/:name`, `/videos/:uuid`, `/videos/loadup` |
| ⏳ DelayApp | [delay.md](delay.md) | `/delay`, `/delay/:n`, `/bang`, `/bangrandomly` |
| 🌊 StreamApp | [stream.md](stream.md) | `/stream/incrementing`, `/stream/randomInt`, `/stream/both`, `/stream/videos` |
| 🌐 ClientApp | [client.md](client.md) | `/client/users`, `/client/users/:id`, `/client/posts`, `/client/posts/:id`, `/client/posts/userId/:userId`, `/client/dopost`, `/client/dogs` |
| 📁 StaticApp | [static.md](static.md) | `/` and any other GET path |
| 🎭 ActorsApp | [actors.md](actors.md) | `/actors`, `/actors/:param`, `/actors/nodie`, `/actors/catchAll` |
| ✨ 🎨 Gemini apps | [gemini.md](gemini.md) | `/gemini/prompt/:prompt`, `/gemini/superhero/:animal`, `/gemini/image/:prompt` |
| 🛡️ OpenAIModerationApp | [openai.md](openai.md) | `/openai/moderation/:input` |
| 🍪 CookieAuthApp | [auth.md](auth.md) | `/login/:username/:password`, `/auth/:name/greet` |
| 📝 FormApp | [form.md](form.md) | `/form.html`, `POST /handle_form` |
| 🔀 Port 8082 server | [server-8082.md](server-8082.md) | `/hello`, `/world` |

## How errors are handled

All 8080 routes are wrapped by `MainApp.handleErrors`. Any failure or defect that a route doesn't handle itself gets logged (`ZIO.logError`) and turned into a plain **500 Internal Server Error** with an empty body. The client doesn't see a stack trace.

## Route order

Routes are combined with `++`. More specific routes win over the `StaticApp` catch-all (`GET /...`), so anything that isn't matched elsewhere is looked up as a file in `static/`. A path that matches nothing there returns **404**.
