# Provider and model selection – 0.1.2

## Usage

In the DHU test app on the phone, first choose the provider and then the model. The selection is
saved locally right away. When you switch back to a provider, its most recently selected model
appears. The selection is kept when the app is opened again. The car template shows provider/model
below "Ready for the UI test." or "Started · Test N". Changes are updated while the template is
visible; when you return to it, it reads the current state. "Start" remains a pure UI test.

The continued assistant app also uses this selection. There, the provider/model switches are
disabled while a voice conversation is running. Its existing voice command for switching providers
uses the most recently selected model of the target provider and still creates a new chat without
the previous history.

## Storage and module boundaries

`core` contains `Provider`, `ModelOption`, `ModelCatalog` and `ModelSelection`.
A `ModelSelection` object only accepts a model that belongs to the provider.
`settings` depends on `core` and implements `ModelSelectionStore` with app-private
SharedPreferences (`model_selection_v1`). It stores `provider` as the enum name and
`model.openai` / `model.anthropic` as the model ID. Changes to both values become visible together.
Invalid or corrupted settings fall back to a matching default without rewriting archives.

The phone activity and the CarAppService of the DHU APK run in the same process and use this store.
Both unregister their observers when the visible UI is left.
The separately installed packages `de.azentrius.lautwerk`, `de.azentrius.lautwerk.prototype.phone`
and `de.azentrius.lautwerk.prototype.dhu` each have their own storage.
There is no exchange of data or keys between these APKs.

`ChatApi`, `ConversationStore` and `SecretStore` use the extracted types.
Enum names (`OPENAI`, `CLAUDE`), key identifiers (`openai`, `anthropic`), archive file, JSON fields
and Keystore aliases do not change.
A selected historical chat is only continued with exactly its own provider/model;
an unknown archive model is not silently replaced by another one.

## Deliberately kept starting catalog

| Provider | Model ID | Display name | Default |
|---|---|---|---|
| OpenAI | `gpt-5.4-mini` | GPT-5.4 mini | Yes |
| OpenAI | `gpt-5.4` | GPT-5.4 | |
| Claude / Anthropic | `claude-sonnet-4-6` | Claude Sonnet 4.6 | Yes |
| Claude / Anthropic | `claude-opus-4-6` | Claude Opus 4.6 | |
| Claude / Anthropic | `claude-haiku-4-5` | Claude Haiku 4.5 | |

This catalog takes over the IDs from 0.1.0; it is not a complete or automatically updated model
list. The IDs were checked against the official model pages on 2026-10-11. This does not test
account access or API behavior. No model migration and no paid call took place.

- [OpenAI GPT-5.4 mini](https://developers.openai.com/api/docs/models/gpt-5.4-mini)
- [OpenAI GPT-5.4](https://developers.openai.com/api/docs/models/gpt-5.4)
- [Claude Sonnet 4.6](https://platform.claude.com/docs/en/models/sonnet-4-6/overview)
- [Claude Opus 4.6](https://platform.claude.com/docs/en/models/opus-4-6/overview)
- [Claude Haiku 4.5](https://platform.claude.com/docs/en/models/haiku-4-5/overview)
- [Claude model IDs and aliases](https://platform.claude.com/docs/en/about-claude/models/model-ids-and-versions)

## Checking in the real DHU

1. Install 0.1.2 with `adb install -r` over the existing DHU app;
   [full Windows steps](START_WINDOWS.md).
2. On the phone, in exactly this app, choose Claude / Anthropic and Claude Haiku 4.5.
3. Close and reopen the app: the selection must be kept.
4. Open Lautwerk in the connected DHU: check the title, provider and model.
5. Tap "Start": "Started · Test 1" must appear together with the selection.
6. On the phone, choose OpenAI / GPT-5.4: the selection visible in the DHU should change.
7. Switch back to Claude: Claude Haiku 4.5 must be selected again.

The category/sideload limits of 0.1.1 still apply unchanged. The diagnostic POI entry, the active
HostValidator and the disabled DHU release variant were not changed. No officially supported AI car
app category is claimed.
