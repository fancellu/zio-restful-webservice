# 🛡️ OpenAIModerationApp

Sources: [OpenAIModerationApp.scala](../src/main/scala/com/felstar/restfulzio/openai/OpenAIModerationApp.scala), shared helpers in [package.scala](../src/main/scala/com/felstar/restfulzio/openai/package.scala)

Calls the OpenAI moderation API (`https://api.openai.com/v1/moderations`) with the zio-http `Client`. The shared `postJson` sends JSON with `Authorization: Bearer <key>`. The request and response types come from the `openai-scala-models` library.

The moderation endpoint is free, so it works even if the OpenAI account has no credits. The OpenAI completion and DALL·E examples that used to sit alongside it have been replaced by the [Gemini apps](gemini.md), because OpenAI retired the models and APIs they used.

🔑 **API key:** read from the `OPENAI_API_KEY` JVM system property first, then from the `OPENAI_API_KEY` environment variable. If neither is set, the placeholder `ENTER_OPENAI_API_KEY` is sent, and OpenAI replies with a 401 `invalid_api_key` error. That error is passed through.

---

## GET /openai/moderation/:input

Checks `input` against OpenAI's content policy.

- **Path params:** `input` (spaces as `+` or `%20`)
- **Response:** `application/json`, with the upstream status. On success it's the pretty-printed moderation result (`flagged`, `categories`, `category_scores`). If the reply can't be decoded, the raw upstream JSON is returned instead.

```bash
curl http://localhost:8080/openai/moderation/we+must+kill+all+the+elves   # flagged: true (violence)
curl http://localhost:8080/openai/moderation/we+must+kiss+all+the+elves   # flagged: false
```
