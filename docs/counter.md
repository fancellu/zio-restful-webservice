# 🔢 CounterApp

Source: [CounterApp.scala](../src/main/scala/com/felstar/restfulzio/counter/CounterApp.scala)

Keeps state in a `Ref[Int]` that comes from the environment (`Routes[Ref[Int], Nothing]`). `MainApp` provides it with `ZLayer.fromZIO(Ref.make(0))`, so all requests share one counter that starts at 0 and is lost when the server restarts.

Each endpoint returns the counter value after the operation, as `text/plain`. Changes are logged at INFO level. A trailing slash also works, e.g. `/up/`.

---

## GET /up

Adds 1 to the counter and returns the new value.

```bash
curl http://localhost:8080/up      # 1
```

## GET /down

Subtracts 1 from the counter and returns the new value. The counter can go below zero.

```bash
curl http://localhost:8080/down    # 0
```

## GET /get

Returns the current value and doesn't change it.

```bash
curl http://localhost:8080/get
```

## GET /reset

Sets the counter to 0 and returns `0`.

```bash
curl http://localhost:8080/reset
```
