# 🌊 StreamApp

Source: [StreamApp.scala](../src/main/scala/com/felstar/restfulzio/stream/StreamApp.scala)

Streams a `ZStream[..., String]` as a chunked response body (`Body.fromCharSequenceStreamChunked`). Each stream is throttled with `Schedule.spaced`, so you can watch the data arrive.

Every endpoint responds with these headers:

- `Content-Type: application/octet-stream`
- `Content-Disposition: attachment; filename="stream.txt"`

A browser will save the response as `stream.txt`. Use `curl -N` (no buffering) to watch it live instead.

---

## GET /stream/incrementing

Sends the numbers 0 to 49, one per line, every 100ms (about 5 seconds in total).

```bash
curl -N http://localhost:8080/stream/incrementing
```

---

## GET /stream/randomInt

Sends 10 lines, every 50ms. Random ints alternate with the fixed value `999` (`intersperse(999)`), then the stream is cut to 10 elements.

```bash
curl -N http://localhost:8080/stream/randomInt
```

---

## GET /stream/both

Sends `incrementing` and then `randomInt`, joined with `++`.

```bash
curl -N http://localhost:8080/stream/both
```

---

## GET /stream/videos

Streams every video in the `VideoRepo` as newline-delimited JSON, one every 500ms.

- **Notes:** The response body keeps running after the handler returns, so the handler gives the stream its `VideoRepo` up front with `provideEnvironment`. If there are no videos the body is empty. Call [`/videos/loadup`](videos.md#get-videosloadup) first to add some.

```bash
curl http://localhost:8080/videos/loadup
curl -N http://localhost:8080/stream/videos
```

Example output:

```text
{"name":"one","uuid":"..."}
{"name":"two","uuid":"..."}
```
