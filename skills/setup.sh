#!/usr/bin/env bash
set -euo pipefail

SKILLS_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SKILLS_DIR/.." && pwd)"

green()  { echo -e "\033[0;32m$*\033[0m"; }
yellow() { echo -e "\033[0;33m$*\033[0m"; }
blue()   { echo -e "\033[0;34m$*\033[0m"; }
red()    { echo -e "\033[0;31m$*\033[0m"; }

symlink_skill() {
  local src="$1"
  local dest="$2"
  mkdir -p "$(dirname "$dest")"
  [[ -L "$dest" ]] && rm "$dest"
  ln -s "$src" "$dest"
}

install_opencode() {
  blue "Installing skills for OpenCode..."
  green "  OpenCode reads AGENTS.md and skills directly"
}

install_claude() {
  blue "Installing skills for Claude Code (.claude/commands/)"
  local target="$ROOT_DIR/.claude/commands"
  mkdir -p "$target"

  cp "$ROOT_DIR/AGENTS.md" "$ROOT_DIR/CLAUDE.md"
  green "  Created CLAUDE.md from AGENTS.md"

  for skill_file in "$SKILLS_DIR"/*/SKILL.md; do
    local skill_name
    skill_name=$(basename "$(dirname "$skill_file")")
    local dest="$target/${skill_name}.md"
    symlink_skill "$skill_file" "$dest"
    green "  Linked $skill_name"
  done
}

install_gemini() {
  blue "Installing skills for Gemini CLI (.gemini/skills/)"
  local target="$ROOT_DIR/.gemini/skills"
  mkdir -p "$target"

  for skill_file in "$SKILLS_DIR"/*/SKILL.md; do
    local skill_name
    skill_name=$(basename "$(dirname "$skill_file")")
    local dest="$target/${skill_name}.md"
    symlink_skill "$skill_file" "$dest"
    green "  Linked $skill_name"
  done

  cp "$ROOT_DIR/AGENTS.md" "$ROOT_DIR/GEMINI.md"
  green "  Created GEMINI.md from AGENTS.md"
}

install_codex() {
  blue "Installing skills for Codex CLI (.codex/skills/)"
  local target="$ROOT_DIR/.codex/skills"
  mkdir -p "$target"

  for skill_file in "$SKILLS_DIR"/*/SKILL.md; do
    local skill_name
    skill_name=$(basename "$(dirname "$skill_file")")
    local dest="$target/${skill_name}.md"
    symlink_skill "$skill_file" "$dest"
    green "  Linked $skill_name"
  done
}

install_copilot() {
  blue "Installing skills for GitHub Copilot"
  local target="$ROOT_DIR/.github/copilot-instructions.md"
  mkdir -p "$ROOT_DIR/.github"

  {
    cat "$ROOT_DIR/AGENTS.md"
    echo ""
    echo "---"
    echo ""
    echo "# Project Skills"
    echo ""
    for skill_file in "$SKILLS_DIR"/*/SKILL.md; do
      local skill_name
      skill_name=$(basename "$(dirname "$skill_file")")
      echo "## Skill: $skill_name"
      echo ""
      cat "$skill_file"
      echo ""
    done
  } > "$target"

  green "  Created .github/copilot-instructions.md"
  yellow "  Copilot setup is generated, rerun after skill changes"
}

install_all() {
  install_opencode
  echo ""
  install_claude
  echo ""
  install_gemini
  echo ""
  install_codex
  echo ""
  install_copilot
}

show_menu() {
  echo ""
  blue "Recipe Cost Calculator - Skills Setup"
  echo "--------------------------------------"
  echo "1) opencode"
  echo "2) claude"
  echo "3) gemini"
  echo "4) codex"
  echo "5) copilot"
  echo "6) all"
  echo "q) quit"
  echo ""

  read -rp "Choice [1-6/q]: " choice
  case "$choice" in
    1|opencode) install_opencode ;;
    2|claude) install_claude ;;
    3|gemini) install_gemini ;;
    4|codex) install_codex ;;
    5|copilot) install_copilot ;;
    6|all) install_all ;;
    q|Q) echo "Aborted."; exit 0 ;;
    *) red "Invalid choice"; exit 1 ;;
  esac
}

if [[ $# -eq 0 ]]; then
  show_menu
else
  case "$1" in
    opencode) install_opencode ;;
    claude) install_claude ;;
    gemini) install_gemini ;;
    codex) install_codex ;;
    copilot) install_copilot ;;
    all) install_all ;;
    *) red "Unknown agent: $1"; exit 1 ;;
  esac
fi

echo ""
green "Setup complete"
echo "Next step: ./skills/skill-sync/sync.sh"
