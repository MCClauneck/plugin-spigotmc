# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Primary Instruction Source

[`AGENTS.md`](./AGENTS.md) is the central, authoritative instruction set for **all** AI agents
(Claude Code, Windsurf, Codex, and others) operating in this repository.

Before and during any task, you **MUST** read and follow [`AGENTS.md`](./AGENTS.md). Its rules
take precedence over general knowledge and apply to every change you make here.

## Instruction Discovery Order

As described in [`AGENTS.md`](./AGENTS.md), resolve instructions in this order:

1. [`AGENTS.md`](./AGENTS.md) at the repository root (this is the primary instruction set).
2. The `.agents/` directory at the repository root — specific rules here override general knowledge.
3. `.agents/README.md` — the central index and routing hub; load the specialized instruction(s)
   that match the current task context.

Keeping this file thin is intentional: it exists only to route Claude Code to
[`AGENTS.md`](./AGENTS.md) so there is a single source of truth.
