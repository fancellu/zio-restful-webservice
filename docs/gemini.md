# ✨ Gemini apps

Sources: [GeminiTextApp.scala](../src/main/scala/com/felstar/restfulzio/gemini/GeminiTextApp.scala), [GeminiImageApp.scala](../src/main/scala/com/felstar/restfulzio/gemini/GeminiImageApp.scala), JSON types in [GeminiModels.scala](../src/main/scala/com/felstar/restfulzio/gemini/GeminiModels.scala), shared helpers in [package.scala](../src/main/scala/com/felstar/restfulzio/gemini/package.scala)

These apps call Google's Gemini API (`generateContent`) with the zio-http `Client`. The JSON types are small hand-written zio-json case classes covering only the fields used here. Unknown fields in responses are ignored.

| What | Model |
|------|-------|
| Text | `gemini-3.8-flash` |
| Images | `gemini-3.1-flash-image` |

The model names are constants in `package.scala`.

🔑 **API key:** read from the `GEMINI_API_KEY` JVM system property first, then from the `GEMINI_API_KEY` environment variable. You can get a free-tier key at https://aistudio.google.com/apikey. The key is sent in the `x-goog-api-key` header rather than in the URL, so it never shows up in the logged URLs.

```bash
export GEMINI_API_KEY=...
sbt run
```

Spaces in path parameters can be written as `+` or `%20`.

---

## ✨ GET /gemini/prompt/:prompt

Sends `prompt` to `gemini-3.8-flash` (temperature 0.6) and returns the generated text.

- **Path params:** `prompt`
- **Response:**
  - Success: `200`, `text/plain`, the generated text. Gemini often replies in Markdown.
  - An error from Gemini (e.g. a bad key, or quota exceeded) is passed through as `application/json`, with the upstream status.
- **Timing:** typically 2 to 5 seconds

```bash
curl "http://localhost:8080/gemini/prompt/translate+I+like+kittens+to+French"
```

Example output:

```text
"I like kittens" in French is:

**"J'aime les chatons."**
```

---

## ✨ GET /gemini/superhero/:animal

Builds a few-shot prompt that asks for three superhero names for `animal` (upper-cased), then sends it the same way as `/gemini/prompt/:prompt`.

- **Path params:** `animal`
- **Response:** the same as `/gemini/prompt/:prompt`

```bash
curl http://localhost:8080/gemini/superhero/Dog
# The Bark Knight, Mighty Mutt, Captain Canine
```

---

## 🎨 GET /gemini/image/:prompt

Asks `gemini-3.1-flash-image` for an image of `prompt`. Gemini returns the image bytes themselves, base64-encoded, rather than a link. The page therefore embeds the image as a `data:` URL.

- **Path params:** `prompt`
- **Response:** `text/html`, with the upstream status
  - Success: `<img src="data:image/jpeg;base64,..." alt="<prompt>">`. The page is about 1 MB.
  - Otherwise: `<div>Image not found</div>`. Gemini's reply is logged as a warning.
- **Timing:** about 10 seconds

Open it in a browser to see the image:

```text
http://localhost:8080/gemini/image/a+ginger+cat,+wearing+round+black+glasses,+riding+a+bike+and+smoking+a+pipe,+photorealistic
```
