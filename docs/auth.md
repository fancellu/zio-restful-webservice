# 🍪 CookieAuthApp

Source: [CookieAuthApp.scala](../src/main/scala/com/felstar/restfulzio/auth/CookieAuthApp.scala)

A toy login flow that keeps a signed JWT (via [jwt-scala](https://github.com/jwt-scala/jwt-scala)) in a cookie.

- The token is signed with HS512, using the hard-coded secret `secretKey!!`
- The claim content is `{"user": "<username>"}`
- The token expires 300 seconds (5 minutes) after it's issued

⚠️ This is a demo only. The secret is hard-coded, the password check is a joke, and the credentials travel in a GET URL.

---

## GET /login/:username/:password

Logs in. The login succeeds only when `password` is `username` reversed (e.g. `dino` / `onid`).

- **Path params:** `username`, `password`
- **Response:**
  - Success: `200`, `text/plain`: `Logged in`, plus a `Set-Cookie: jwt=<token>; Path=/` header
  - Failure: `401`, `text/plain`: `Invalid username or password.`

```bash
curl -i -c cookies.txt http://localhost:8080/login/dino/onid
curl -i http://localhost:8080/login/dino/badpassword   # 401
```

---

## GET /auth/:name/greet

Greets `name`, but only if the request has a valid `jwt` cookie for that same user. The token must decode with the secret, must not have expired, and its claim must be `{"user": "<name>"}`.

- **Path params:** `name`
- **Cookies:** `jwt`
- **Response:** always `200`, `text/plain`
  - Valid: `You are logged in! dino`
  - Missing, expired or wrong cookie, or a different name: `You are not logged in, sorry`

```bash
curl -b cookies.txt http://localhost:8080/auth/dino/greet       # You are logged in! dino
curl -b cookies.txt http://localhost:8080/auth/wrongname/greet  # You are not logged in, sorry
```

In a browser, open the login URL first. The browser stores the cookie and sends it with the greet URL.
