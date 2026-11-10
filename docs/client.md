# 🌐 ClientApp

Source: [ClientApp.scala](../src/main/scala/com/felstar/restfulzio/client/ClientApp.scala)

Uses the zio-http `Client` to call external services. Most endpoints proxy the fake REST API at https://jsonplaceholder.typicode.com. The routes need `Client` and a `Cache[Int, Throwable, Response]` from the environment.

Requests go through `Client.batched`, which reads the whole upstream body into memory and manages the connection. Network errors aren't handled, so they become a **500** through `MainApp.handleErrors`.

The JSON post type is:

```scala
case class Post(userId: Int, id: Int, title: String, body: String)
```

---

## GET /client/users

Fetches `/users` from jsonplaceholder and passes the JSON body and status straight through.

- **Response:** `application/json`, with the upstream status

```bash
curl http://localhost:8080/client/users
```

---

## GET /client/users/:id

Fetches one user through a [zio-cache](https://zio.dev/zio-cache/) `Cache`, built in `MainApp.userCache`:

- capacity 100
- time to live 15 seconds
- lookup function `ClientApp.getUser`

- **Path params:** `id` (integer)
- **Response:** `application/json`, with the upstream status (a missing user gives 404)
- **Notes:** The first call logs `Called https://jsonplaceholder.typicode.com/users/1 for cache`. Calls in the next 15 seconds are served from the cache and don't log that line.

```bash
curl http://localhost:8080/client/users/1
```

---

## GET /client/posts

Fetches all posts, decodes them as `List[Post]`, and re-encodes them as pretty-printed JSON.

- **Response:** `200`, `application/json`: an array of posts, or `null` if decoding fails

```bash
curl http://localhost:8080/client/posts
```

---

## GET /client/posts/:id

Fetches one post.

- **Path params:** `id` (integer)
- **Response:**
  - `200`, `application/json`: the post, pretty-printed
  - If the upstream status is an error (e.g. `404` for `/client/posts/9999`), the upstream status and body are passed through as text

```bash
curl http://localhost:8080/client/posts/1
curl -i http://localhost:8080/client/posts/9999
```

---

## GET /client/posts/userId/:userId

Fetches the posts by one user (`/posts?userId=...` upstream).

- **Path params:** `userId` (integer)
- **Response:** `200`, `application/json`: an array of posts

```bash
curl http://localhost:8080/client/posts/userId/1
```

---

## GET /client/dopost

Shows how to send a POST from the client. It sends this fixed JSON body to jsonplaceholder `/posts/` and returns the decoded `Post` that comes back:

```json
{"title": "Hello", "body": "World", "userId": "1"}
```

- **Response:** `200`, `application/json`: the created post with its new `id` (jsonplaceholder doesn't really store it). The response is `null` if decoding fails.

```bash
curl http://localhost:8080/client/dopost
```

---

## GET /client/dogs 🐶

Calls `https://dog.ceo/api/breeds/image/random/5` to get 5 random dog image URLs, then downloads each one into `src/main/resources/dogs`. The directory is created if it's missing.

- **Response:** `200`, `application/json`: `"Dogs are downloaded: 5"`
- **Notes:** The download code is quick and dirty. It writes each file with a blocking `FileOutputStream` inside `ZIO.attemptBlocking`, and strips any directory part from the file name, because the URL comes from a third party.

```bash
curl http://localhost:8080/client/dogs
```
