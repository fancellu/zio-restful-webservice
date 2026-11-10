# 📝 FormApp

Source: [FormApp.scala](../src/main/scala/com/felstar/restfulzio/form/FormApp.scala), page: [static/form.html](../static/form.html)

Handles a plain HTML form post. `StaticApp` serves the page itself. `FormApp` only provides the POST handler.

---

## GET /form.html

A form with `username` and `age` fields that posts to `/handle_form`. `StaticApp` serves it from `static/`.

```text
http://localhost:8080/form.html
```

---

## POST /handle_form

Logs the raw request body and echoes it back. The body isn't parsed.

- **Request body:** `application/x-www-form-urlencoded`, e.g. `username=dino&age=42`
- **Response:** `200`, `text/plain`: `post is username=dino&age=42`

```bash
curl -X POST -d "username=dino&age=42" http://localhost:8080/handle_form
```
