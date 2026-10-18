# Azentrius design reference

The active reference is the complete, unmodified tree in **upstream-f2c9df4/**,
from [Azentrius/design-system](https://github.com/Azentrius/design-system/tree/f2c9df4a4c099cf5175ad29c1e783e432f6d1b73),
the design-system commit that renames OpenAuto AI to Lautwerk.
`UPSTREAM.json` records its origin. `UPSTREAM.sha256` contains SHA-256 checksums of its 151 files,
with paths relative to this directory. Every file was also checked against its Git blob.
No upstream script was executed during the import.

The design audit in [docs/DESIGN_SYSTEM.md](../../docs/DESIGN_SYSTEM.md) was made against
`upstream-edd4c67/` for 0.1.18. Between edd4c67 and f2c9df4 the design system changed more than the
name: among other things the Android section, the Android Auto section, the tokens and the licence
headers of the `ic_az_*` icons. Those changes are not yet audited for the application; the rename
to Lautwerk adopts only the parts that concern the name, the wordmark and the app icon files.

Older snapshots are evidence, not the active design specification: `upstream-edd4c67/` (active
until the rename), and `upstream-9ed4032/`, `upstream-eb827c8/`, `upstream-ab59a45/` and the original
package files, which were restored unchanged from application commit `394bc23`.
`PACKAGE.sha256` continues to describe the original package.

Upstream text, licenses, assets and source files retain their original language and bytes.
They are not authored application documentation and are not relicensed as Apache-2.0.
See each snapshot's LICENSE and LICENSES, and the application NOTICE.
Do not edit snapshots: report gaps to the maintainer for resolution in the design system.
