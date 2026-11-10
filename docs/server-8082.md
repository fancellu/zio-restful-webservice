# 🔀 Parallel server on port 8082

Source: [MainApp.scala](../src/main/scala/com/felstar/restfulzio/MainApp.scala)

`MainApp` starts a second, independent `Server` on port 8082 (`Server.defaultWithPort(8082)`). It runs in parallel with the 8080 server (`zipPar`). Its routes are defined inline, have no middleware and need no environment.

If either server fails to start (for example, because its port is already in use), the whole app fails.

---

## GET /hello

- **Response:** `200`, `text/plain`: `world`

```bash
curl http://localhost:8082/hello
```

## GET /world

- **Response:** `200`, `text/plain`: `hello`

```bash
curl http://localhost:8082/world
```
