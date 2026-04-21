#!/bin/bash
# Pull latest changes from origin/bionic, fast-forward only
cd "$(dirname "$0")"
git fetch origin
git merge --ff-only origin/bionic && echo "Synced to $(git log --oneline -1)" || echo "Sync failed - local changes present"
