# 🎭 ActorsApp

Source: [ActorsApp.scala](../src/main/scala/com/felstar/restfulzio/actors/ActorsApp.scala)

Sends messages to a [zio-actors](https://github.com/zio/zio-actors) stateful actor with `ask` (`?`). `ActorsApp.layer` creates one `ActorSystem` and one actor (`actor1`) for the whole app, and shuts them down when the app stops. The routes need an `ActorRef[Command]`.

The actor accepts two commands:

| Command | Behaviour |
|---------|-----------|
| `DoubleCommand(i)` | sleeps 3s, replies `i * 2` |
| `BangCommand("bang2")` | sleeps 2s (in the code), fails with `I went bang2` |
| `BangCommand(other)` | sleeps 1s (in the code), fails with `I went bang` |

In practice every endpoint below took about 3 seconds to respond, including the ones that fail.

The endpoints show three ways to deal with the actor's failures: recover with a fixed value, recover with `catchAll`, or let the failure through.

---

## GET /actors

Asks the actor to double 42.

- **Response:** `200`, `text/plain`: `Actor returned 84` (after about 3s)

```bash
curl http://localhost:8080/actors
```

---

## GET /actors/:param

- **Path params:** `param`
  - If it's an integer, the actor doubles it: `/actors/21` gives `200`, `Actor returned 42` (after about 3s)
  - If it's any other string (e.g. `randomString`), `BangCommand(param)` is sent and the failure isn't handled. The result is a `500` with an empty body, and the exception is logged by `MainApp.handleErrors`.

```bash
curl http://localhost:8080/actors/21
curl -i http://localhost:8080/actors/randomString   # 500
```

---

## GET /actors/nodie

Sends `BangCommand("nodie")`, which fails. The failure is replaced with a fixed value using `orElseSucceed`.

- **Response:** `200`, `text/plain`: `Actor returned fallback after error`

```bash
curl http://localhost:8080/actors/nodie
```

---

## GET /actors/catchAll

Sends `BangCommand("catchAll")`, which fails. `catchAll` recovers, and the reply includes the error.

- **Response:** `200`, `text/plain`: `Actor returned fallback because java.lang.Exception: I went bang`

```bash
curl http://localhost:8080/actors/catchAll
```
