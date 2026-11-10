# 📥 DownloadApp

Source: [DownloadApp.scala](../src/main/scala/com/felstar/restfulzio/download/DownloadApp.scala)

Serves files from the classpath (`src/main/resources`) as downloads. The body is a `ZStream` of bytes sent with chunked transfer encoding (`Body.fromStreamChunked`).

Both endpoints respond with these headers:

- `Content-Type: application/octet-stream`
- `Content-Disposition: attachment; filename="..."`

---

## GET /download

Downloads [file.txt](../src/main/resources/file.txt) all at once.

- **Response:** `200`, attachment `file.txt`

```bash
curl -OJ http://localhost:8080/download
```

---

## GET /download/stream

Downloads [bigfile.txt](../src/main/resources/bigfile.txt). The stream is deliberately slowed down: `.schedule(Schedule.spaced(10.millis))` waits 10ms between elements, so you can watch the download arrive.

- **Response:** `200`, attachment `bigfile.txt`, streamed slowly

```bash
curl -OJ http://localhost:8080/download/stream
```
