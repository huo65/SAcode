# AGENTS.md

## Project Goal

This repository is a classroom-demo implementation of the food delivery order system described in the two course PPT files. The priority is a clear, runnable, explainable four-role business loop rather than production-level completeness.

Use these documents as the current source of truth:

- [README.md](README.md): project entry and document map.
- [docs/需求进度与后续计划.md](docs/需求进度与后续计划.md): requirements, current progress, and roadmap.
- [docs/课堂展示联调验收清单.md](docs/课堂展示联调验收清单.md): final demo verification checklist.
- [front/README.md](front/README.md): frontend commands and structure.
- [backend/DB-market/DEVELOPMENT.md](backend/DB-market/DEVELOPMENT.md): backend database, configuration, test, and run notes.

## Development Principles

- Prioritize features that can be demonstrated end to end in class.
- Keep the customer, merchant, driver, and admin paths coherent before adding production-grade enhancements.
- Prefer a stable local demo path for payment, storage, maps, and messaging when external integrations are not fully ready.
- Update `docs/需求进度与后续计划.md` when feature status or the roadmap changes.
- Update `docs/课堂展示联调验收清单.md` when verification steps or final demo evidence changes.

## Current Priority

1. Run one complete local verification round and fill in the checklist.
2. Confirm the `driver` role page, order pickup, and delivery completion path.
3. Verify upload, local storage, and image display.
4. Decide whether the class demo uses the stable balance payment path or also includes Alipay sandbox.
5. Keep later production-level work, such as pagination, cache, monitoring, backup, CI, and E2E, as roadmap items unless the main demo is already stable.
