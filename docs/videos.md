# 🎬 VideoApp

Source: [VideoApp.scala](../src/main/scala/com/felstar/restfulzio/videos/VideoApp.scala), model: [Video.scala](../src/main/scala/com/felstar/restfulzio/videos/Video.scala)

A small CRUD-style API over a `VideoRepo` from the environment (`Routes[VideoRepo, Throwable]`). There are two implementations:

- [InmemoryVideoRepo](../src/main/scala/com/felstar/restfulzio/videos/InmemoryVideoRepo.scala): a `Ref[Map[String, Video]]`. This is the default in `MainApp`, and the data is lost on restart.
- [PersistentVideoRepo](../src/main/scala/com/felstar/restfulzio/videos/PersistentVideoRepo.scala): H2 + Quill. To use it, swap `PersistentVideoRepo.layer` into `MainApp`. The schema is in [h2-schema.sql](../src/main/resources/h2-schema.sql).

The JSON form of a video is:

```json
{"name": "one", "uuid": "8f0e7a5c-2d1b-4c55-9b44-0a6b7f1e2c3d"}
```

---

## POST /videos/:name

Registers a video with the given name and a random UUID.

- **Path params:** `name`
- **Response:** `200`, `text/plain`: the new UUID

```bash
curl -X POST http://localhost:8080/videos/myvideo
```

---

## GET /videos/loadup

Registers four videos, `one`, `two`, `three` and `four`. Use it to get some test data quickly.

- **Response:** `200`, `text/plain`: `bulk load: <uuid1>,<uuid2>,<uuid3>,<uuid4>`
- **Notes:** This is a GET that changes state, purely for convenience in a browser. Each call adds four more videos.

```bash
curl http://localhost:8080/videos/loadup
```

---

## GET /videos

Lists all videos.

- **Response:** `200`, `application/json`: an array of videos (`[]` when empty)

```bash
curl http://localhost:8080/videos
```

---

## GET /videos/:uuid

Looks up one video by its UUID.

- **Path params:** `uuid` (the string returned by `POST /videos/:name`)
- **Response:**
  - `200`, `application/json`: the video
  - `404` if there's no video with that id

```bash
curl http://localhost:8080/videos/8f0e7a5c-2d1b-4c55-9b44-0a6b7f1e2c3d
```
