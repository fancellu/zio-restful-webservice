# 🌱 NoEnvApp

Source: [NoEnvApp.scala](../src/main/scala/com/felstar/restfulzio/noenv/NoEnvApp.scala)

The simplest routes: they don't need anything from the ZIO environment and can't fail (`Routes[Any, Nothing]`).
They show plain text responses, reading request headers, request-altering middleware, and three ways to decode a JSON body.

In `MainApp` these routes, and only these, get `requestMiddleWare`. It adds a `Seen` header (current local date-time) to every incoming request before the handler runs.

The JSON body type for the POST endpoints is:

```scala
final case class SimplePost(string: String)
```

---

## GET /noenv

Returns a fixed greeting.

- **Response:** `200`, `text/plain`: `Hello from noenv`

```bash
curl http://localhost:8080/noenv
```

---

## GET /headers

Lists the request headers that the handler sees, one per line, as `(name,value)`.

- **Response:** `200`, `text/plain`
- **Notes:** The list includes a `Seen` header that the client never sent. The middleware added it, which shows that a middleware can change the request before the handler gets it.

```bash
curl http://localhost:8080/headers
```

Example output:

```text
headers are (host,localhost:8080)
(user-agent,curl/8.15.0)
(accept,*/*)
(content-length,0)
(seen,2026-11-10T15:03:43.2302596)
```

Header names come out in lower case, so `Seen` is listed as `seen`.

---

## POST /simplepost

Reads the body as a string, then decodes it as `SimplePost` by hand.

- **Request body:** JSON, e.g. `{"string": "hello"}`
- **Response:** `200`, `text/plain`
  - Valid JSON: `post is SimplePost(hello)`
  - Invalid JSON: `post is ` (still a 200, because the decode error is thrown away)

```bash
curl -X POST -d '{"string": "hello"}' http://localhost:8080/simplepost
```

---

## POST /simplepost2

Behaves the same as `/simplepost`. The decoding moves into an implicit class (`SimplePost.SimplePostRequest`), which adds `req.simplePostOptionZIO` to `Request`.

- **Request body:** JSON, e.g. `{"string": "hello"}`
- **Response:** `200`, `text/plain`: `post is SimplePost(hello)`, or `post is ` for invalid JSON

```bash
curl -X POST -d '{"string": "hello"}' http://localhost:8080/simplepost2
```

---

## POST /simplepostcodec

The route logic is a plain `SimplePost => String` function. No `Request` or `Response` appears in it. The generic `jsonHandler[In, Out]` decodes the body and encodes the result as JSON.

- **Request body:** JSON, e.g. `{"string": "hello"}`
- **Response:**
  - `200`, `application/json`: `"string is hello"`
  - `{"string": "Dino"}` gets a special reply: `"hello Dino!!!!"`
  - `400`: `Invalid JSON!!` if the body isn't a valid `SimplePost`

```bash
curl -X POST -d '{"string": "Dino"}' http://localhost:8080/simplepostcodec
curl -i -X POST -d 'not json' http://localhost:8080/simplepostcodec   # 400
```
