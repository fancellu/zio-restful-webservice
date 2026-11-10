# 🌀 HelloWorldTwirlApp

Source: [HelloTwirlApp.scala](../src/main/scala/com/felstar/restfulzio/hellotwirl/HelloTwirlApp.scala), template: [hellotwirl.scala.html](../src/main/twirl/hellotwirl.scala.html)

Renders HTML with a [Twirl](https://github.com/playframework/twirl) template. sbt-twirl compiles the template into a Scala object, `html.hellotwirl`, and the handler calls `render(param)`.

---

## GET /hellotwirl

Renders the template with the default parameter `defaultstring`.

- **Response:** `200`, `text/html`: a page that says *Just a twirl template with a passed parameter defaultstring*

```bash
curl http://localhost:8080/hellotwirl
```

---

## GET /hellotwirl/:string

Renders the template with the value from the path.

- **Path params:** `string`
- **Response:** `200`, `text/html`

```bash
curl http://localhost:8080/hellotwirl/Dino
```
