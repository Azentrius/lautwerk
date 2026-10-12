# Security Policy

## Reporting a vulnerability

Please do **not** open a public issue for security problems.

Report them privately through GitHub instead:
[Report a vulnerability](https://github.com/Azentrius/openauto-ai/security/advisories/new)
(tab **Security → Report a vulnerability** in this repository).

Please include:

- what is affected (module, version or commit),
- steps to reproduce,
- the impact you expect.

We aim to acknowledge reports within 7 days. This is a small project, so a fix
may take longer; we will keep you informed and credit you in the advisory
unless you prefer to stay anonymous.

## Supported versions

Only the latest state of the `main` branch receives security fixes.

## Especially relevant

- leaking or insecurely storing API keys for AI providers (OpenAI, Anthropic),
- sending conversation content anywhere other than the provider the user selected,
- anything that lets a remote party trigger actions in the app or on the car display.
