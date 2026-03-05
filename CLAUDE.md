# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository..

## Project Overview

Flask-based web application displaying course information. MVC-style: routes in `app.py`, view logic in `views.py`, data in `models.py`.

## Commands

### Setup
```bash
python -m venv venv
venv\Scripts\activate  # Windows: venv\Scripts\activate | Unix: source venv/bin/activate
pip install -r requirements.txt
```

### Run
```bash
python src/app.py  # http://127.0.0.1:5000
```

### Test
```bash
# Run all tests
python -m unittest discover -s tests

# Run a specific test file
python -m unittest tests.test_app

# Run a single test case
python -m unittest tests.test_app.AppTestCase.test_index
```

## Architecture

- **src/app.py**: App entry point. Routes registered via `add_url_rule()` (not decorators) to keep views decoupled from Flask.
- **src/views.py**: Pure view functions — no Flask decorators. Imports `render_template` and `request` from Flask.
- **src/models.py**: In-memory `Course` objects in a module-level `courses` list. No ORM or database.
- **src/templates/**: Jinja2 templates. `layout.html` is the base; all others extend it.
- **src/static/css/**: Stylesheets.

### Data Model

`Course(title, description, instructor, duration, topics=[])` — plain Python class, no ORM.

All course data is hardcoded in `models.py`. Course URL IDs are **1-based**: `/course/1` maps to `courses[0]`.

## Routes

| Route | Methods | View |
|---|---|---|
| `/` | GET | `index` — lists all courses |
| `/course/<course_id>` | GET | `course` — 1-based index lookup; 404 if out of range |
| `/contact` | GET, POST | `contact` — form render (GET) or validation/processing (POST) |

Contact form validates: name (required), email (regex), address (required, min 10 chars). Errors re-render inline; success renders with `success=True`.

## Important Notes

- Run from repo root: `python src/app.py` — Flask resolves templates/static relative to `src/`
- Tests insert `src/` into `sys.path`; follow this pattern when adding new test files
- `.env` sets `FLASK_APP` and `FLASK_ENV` via `python-dotenv`
- `requirements.txt` includes `gunicorn` for production deployment

## Custom Commands & Agents

Available slash commands:
- `/implement_ui_user_story <story>` — Orchestrates Design → Implement → Verify workflow for UI features
- `/explain_this_file` — Explains a file in simple, non-technical terms

Agents in `.claude/agents/`:
- `ui-testing-agent` — Launch after implementing UI features to visually verify and screenshot
- `ux-design-planner` — Launch before coding UI features to plan layout and interactions

## GitHub Actions

CI is configured in `.github/workflows/claude.yml`. Claude can be triggered on issues and PRs by mentioning `@claude` in comments or issue bodies.

## Development Workflow

1. Add unit tests for any changes; run and ensure they pass before finishing
2. After implementing any feature, use the Playwright MCP tool to verify visually at `http://127.0.0.1:5000`
3. Save verification screenshots to `test-output/` with descriptive filenames (e.g., `feature-name-YYYY-MM-DD.png`)
