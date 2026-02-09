---
description: Creates a sample test issue and assigns it to Copilot.

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
    title-prefix: "[test] "
    assignees: [copilot]
    labels: [test]
---
# Create Sample Issue

Create a test issue in this repository assigned to Copilot. The issue should contain:

- **Title:** `Sample test issue`
- **Body:** `This is an automated test issue created by the create-sample-issue workflow. Feel free to close it.`

| `.github/scripts/upstream-sync/` | Helper scripts used by the merge prompt |
