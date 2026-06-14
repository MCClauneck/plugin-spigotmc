# Global Instructions for AI Agents

All agents (Windsurf, Claude Code, Codex, and others) MUST read and adhere to the latest instructions from the local repository before and during execution.

**Note**: This file acts as the primary instruction set. Any specific rules found within the `.agents/` folder at the root of this repository override general knowledge.

Please refer to `.agents/README.md` for the central index and routing hub of all specialized instructions.

## Git: branching and commits (MANDATORY)

Before creating any branch or writing any commit, you MUST read and follow these two files:

- [`.agents/git/workflow.md`](.agents/git/workflow.md) — **how to create and name branches.** Branch names MUST follow the `{type}/{primary-noun}` (or `{type}/{primary-noun}-{secondary-noun}`) format. Do NOT use randomly generated branch names, verbs, or task IDs.
- [`.agents/git/commits.md`](.agents/git/commits.md) — **how to write commits.** Commits MUST follow the Conventional Commits specification.

These rules are not optional. In particular:

- Always create a new, descriptively named branch for each task scope. Never reuse or invent a random branch name.
- Do NOT squash all of your work into a single commit. Commit each logical change separately (grouping only closely related edits), and verify the diff before each commit, as described in [`.agents/git/workflow.md`](.agents/git/workflow.md).
