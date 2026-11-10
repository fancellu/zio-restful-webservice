# ⏳ DelayApp

Source: [DelayApp.scala](../src/main/scala/com/felstar/restfulzio/delay/DelayApp.scala)

Slow and failing endpoints, for testing how clients handle timeouts and errors. `ZIO.sleep` suspends the fiber without blocking any thread, so many requests can wait at the same time cheaply.

Failures aren't handled here (`Routes[Any, Throwable]`). They go to `MainApp.handleErrors`, which logs them and returns a plain **500**.

---

## GET /delay

Sleeps for 3 seconds, then responds.

- **Response:** `200`, `text/plain`: `Hello from delay, slept for 3 seconds, random int: 4821`

```bash
curl http://localhost:8080/delay
```

---

## GET /delay/:n

Sleeps for `n` seconds, then responds.

- **Path params:** `n` (integer). A path segment that isn't an integer doesn't match this route. It falls through to `StaticApp`, so `/delay/abc` returns `404`.
- **Response:** `200`, `text/plain`: `Hello from delay, slept for 2 seconds, random int: 1234`

```bash
curl http://localhost:8080/delay/2
```

---

## GET /bang 💥

Sleeps for 3 seconds (the same as `/delay`), then always fails with `Exception("bang!!!")`.

- **Response:** `500`, empty body. The exception appears in the server log.

```bash
curl -i http://localhost:8080/bang
```

---

## GET /bangrandomly 🎲

Picks a random float. If it's above 0.1 the request fails, so it fails about 9 times out of 10.

- **Response:**
  - About 90% of the time: `500`, empty body (logs `went bang <float>`)
  - About 10% of the time: `200`, `text/plain`: `OK`

```bash
curl -i http://localhost:8080/bangrandomly
```
