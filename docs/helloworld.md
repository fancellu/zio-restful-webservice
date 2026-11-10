# 👋 HelloWorldApp

Source: [HelloWorldApp.scala](../src/main/scala/com/felstar/restfulzio/helloworld/HelloWorldApp.scala)

This app shows that routes are ordinary values that you can build from the environment. `HelloWorldApp()` is a `URIO[Map[String, String], Routes[Any, Nothing]]`. It reads the `"helloworld"` key with `ZIO.serviceAt` and uses that value as the root path segment.

`MainApp` provides `Map("helloworld" -> "hello", ...)`, so the root is `/hello`. Without the `helloworld` key the app would contribute no routes (`Routes.empty`).

---

## GET /hello

Greets the world, or everyone named in the `name` query parameters.

- **Query params:** `name` (optional, may be repeated)
- **Response:** `200`, `text/plain`
  - No `name`: `Hello World!`
  - `?name=Dino`: `Hello Dino!`
  - `?name=Dino&name=Milo`: `Hello Dino and Milo!`

```bash
curl http://localhost:8080/hello
curl "http://localhost:8080/hello?name=Dino&name=Milo"
```

---

## GET /hello/:name

Greets the name given in the path.

- **Path params:** `name` (string)
- **Response:** `200`, `text/plain`: `Hello Dino!`

```bash
curl http://localhost:8080/hello/Dino
```
