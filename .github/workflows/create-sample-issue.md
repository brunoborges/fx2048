---
description: Creates a sample issue and assigns it to Copilot.

on:
  workflow_dispatch:

permissions:
  contents: read
  actions: read
  issues: read

tools:
  github:
    toolsets: [context, issues]

safe-outputs:
  create-issue:
    assignees: [copilot]
---
# Create Sample Issue

Create an issue in this repository assigned to Copilot. The issue should have:

- **Title:** `Sample issue for Copilot`
- **Body:** `This is a sample issue created by the create-sample-issue workflow to demonstrate GitHub Agentic Workflows.`
