#!/usr/bin/env bash
# Tests for check-fork-hooks.sh using a throwaway fixture repo.
set -euo pipefail
here="$(cd "$(dirname "$0")/.." && pwd)"
tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT

make_fixture() {
  rm -rf "$tmp/repo" && mkdir -p "$tmp/repo/src"
  cat >"$tmp/repo/FORK.md" <<'EOF'
## Hooks
- `src/A.kt` :: `PASSGEN(labelRes`
EOF
  echo 'PASSGEN(labelRes = x), // PASSGEN:' >"$tmp/repo/src/A.kt"
  git -C "$tmp/repo" init -q && git -C "$tmp/repo" add -A
}

make_fixture
"$here/check-fork-hooks.sh" "$tmp/repo" >/dev/null || { echo "FAIL: clean fixture rejected"; exit 1; }

make_fixture
echo 'PASSGEN(labelRes = x),' >"$tmp/repo/src/A.kt"
if "$here/check-fork-hooks.sh" "$tmp/repo" >/dev/null; then echo "FAIL: dropped marker accepted"; exit 1; fi

make_fixture
printf '<<<<<<< HEAD\na\n=======\nb\n>>>>>>> upstream/main\n' >>"$tmp/repo/src/A.kt"
git -C "$tmp/repo" add -A
if "$here/check-fork-hooks.sh" "$tmp/repo" >/dev/null; then echo "FAIL: conflict markers accepted"; exit 1; fi

make_fixture
rm "$tmp/repo/FORK.md"
if "$here/check-fork-hooks.sh" "$tmp/repo" >/dev/null; then echo "FAIL: missing FORK.md accepted"; exit 1; fi

make_fixture
echo '# no hooks here' >"$tmp/repo/FORK.md"
if "$here/check-fork-hooks.sh" "$tmp/repo" >/dev/null; then echo "FAIL: zero parsed hooks accepted"; exit 1; fi

make_fixture
rm "$tmp/repo/FORK.md"
cat >"$tmp/repo/FORK.md" <<'EOF'
## Hooks
- `src/s.xml` :: `PASSGEN: Passgen shortcut`
EOF
echo '<!-- PASSGEN: Passgen shortcut (see FORK.md) -->' >"$tmp/repo/src/s.xml"
git -C "$tmp/repo" add -A
"$here/check-fork-hooks.sh" "$tmp/repo" >/dev/null || { echo "FAIL: XML marker with <!-- PASSGEN: rejected"; exit 1; }

make_fixture
rm "$tmp/repo/FORK.md"
cat >"$tmp/repo/FORK.md" <<'EOF'
## Hooks
- `src/s.xml` :: `Passgen shortcut`
EOF
echo '<!-- Passgen shortcut -->' >"$tmp/repo/src/s.xml"
git -C "$tmp/repo" add -A
if "$here/check-fork-hooks.sh" "$tmp/repo" >/dev/null; then echo "FAIL: XML marker without PASSGEN marker accepted"; exit 1; fi

echo "PASS"
