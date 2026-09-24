#!/usr/bin/env bash
# Verifies the fork's upstream hooks survived a merge. See FORK.md.
# Fails if any hook listed in FORK.md is missing its `// PASSGEN:` line, or if any
# tracked file still contains merge conflict markers.
set -euo pipefail
root="${1:-$(git rev-parse --show-toplevel)}"
cd "$root"
status=0

if [[ ! -f FORK.md ]]; then
  echo "MISSING FORK.md: cannot verify hooks without it"
  exit 1
fi

hook_count=0
while IFS= read -r line; do
  hook_count=$((hook_count + 1))
  path="$(sed -E 's/^- `([^`]+)` :: `.*`$/\1/' <<<"$line")"
  anchor="$(sed -E 's/^- `[^`]+` :: `(.*)`$/\1/' <<<"$line")"
  if [[ ! -f "$path" ]]; then
    echo "MISSING FILE: $path"; status=1; continue
  fi
  if ! grep -F -- "$anchor" "$path" | grep -qF '// PASSGEN:'; then
    echo "MISSING HOOK: $path :: $anchor"; status=1
  fi
done < <(grep -E '^- `[^`]+` :: `.+`$' FORK.md || true)

if [[ $hook_count -eq 0 ]]; then
  echo "NO HOOKS PARSED: FORK.md has zero lines matching the hook format; is the Hooks section missing?"
  status=1
fi

# Only the unambiguous markers; a bare ======= line is also valid content in many files.
set +e
conflicts="$(git grep -nE '^(<<<<<<< |>>>>>>> )' -- .)"
grep_status=$?
set -e
if [[ $grep_status -eq 0 ]]; then
  echo "CONFLICT MARKERS:"; echo "$conflicts"; status=1
elif [[ $grep_status -gt 1 ]]; then
  echo "ERROR: git grep failed while checking for conflict markers (exit $grep_status)"
  status=1
fi

[[ $status -eq 0 ]] && echo "fork hooks OK"
exit $status
