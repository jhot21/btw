#!/usr/bin/env bash
# Verifies the fork's upstream hooks survived a merge. See FORK.md.
# Fails if any hook listed in FORK.md is missing its `// PASSGEN:` line, or if any
# tracked file still contains merge conflict markers.
set -euo pipefail
root="${1:-$(git rev-parse --show-toplevel)}"
cd "$root"
status=0

while IFS= read -r line; do
  path="$(sed -E 's/^- `([^`]+)` :: `.*`$/\1/' <<<"$line")"
  anchor="$(sed -E 's/^- `[^`]+` :: `(.*)`$/\1/' <<<"$line")"
  if [[ ! -f "$path" ]]; then
    echo "MISSING FILE: $path"; status=1; continue
  fi
  if ! grep -F -- "$anchor" "$path" | grep -qF '// PASSGEN:'; then
    echo "MISSING HOOK: $path :: $anchor"; status=1
  fi
done < <(grep -E '^- `[^`]+` :: `.+`$' FORK.md)

# Only the unambiguous markers; a bare ======= line is also valid content in many files.
if conflicts="$(git grep -nE '^(<<<<<<< |>>>>>>> )' -- . || true)"; [[ -n "$conflicts" ]]; then
  echo "CONFLICT MARKERS:"; echo "$conflicts"; status=1
fi

[[ $status -eq 0 ]] && echo "fork hooks OK"
exit $status
