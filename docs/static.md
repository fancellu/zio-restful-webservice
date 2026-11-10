# 📁 StaticApp

Source: [StaticApp.scala](../src/main/scala/com/felstar/restfulzio/staticserver/StaticApp.scala)

A static file server for the [`static/`](../static) directory. Its single route is `Method.GET / trailing`, which matches any GET path, so it's added last in `MainApp`. Every other route takes precedence over it.

How a path is handled:

- **Directory:** an HTML "File Explorer" listing, sorted by name, with a `..` link to the parent
- **File:** the file itself (`Handler.fromFile`)
- **Missing, or outside `static/`:** `404`. The path is canonicalised first, so `..` segments can't escape the directory.

Every request prints `StaticApp called on <path>` to the console.

---

## GET /

Lists the contents of `static/`.

```bash
curl http://localhost:8080/
```

## GET /subdir

Lists the contents of `static/subdir`.

```bash
curl http://localhost:8080/subdir
```

## GET /:file

Serves a file, e.g. `/test.txt`, `/test2.txt`, `/subdir/test3.txt` or `/form.html`.

```bash
curl http://localhost:8080/test.txt          # Hello from static dir file test.txt
curl http://localhost:8080/subdir/test3.txt
curl -i http://localhost:8080/nope.txt       # 404
```
