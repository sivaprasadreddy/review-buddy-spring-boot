# Review Buddy

## Prerequisites
* JDK 25
* Docker and Docker Compose

Install JDK, Maven, Gradle,  etc using [SDKMAN](https://sdkman.io/)

```shell
$ curl -s "https://get.sdkman.io" | bash
$ source "$HOME/.sdkman/bin/sdkman-init.sh"
$ sdk env install
```

## MCP Configuration

### Playwright MCP

* [Playwright MCP Docs](https://playwright.dev/docs/getting-started-mcp)

`claude mcp add playwright npx @playwright/mcp@latest`

Claude Code Config file: `'/Users/<username>/Library/Application Support/Claude-3p/claude_desktop_config.json'`

```json
{
  "mcpServers": {
    "playwright": {
      "command": "npx",
      "args": [
        "@playwright/mcp@latest"
      ]
    }
  }
}
```

Code Config file: `~/.codex/config.toml`

```toml
[mcp_servers.playwright]
args = ['@playwright/mcp@latest']
command = 'npx'
```

## Agent Skills

* [SivaLabs Agent Skills](https://github.com/sivaprasadreddy/sivalabs-agent-skills)
* [JetBrains Skills](https://github.com/JetBrains/skills)
* [Kotlin Agent Skills](https://github.com/Kotlin/kotlin-agent-skills)
* [Matt Pocock Skills](https://github.com/mattpocock/skills)
* [Addy Osmani Agent Skills](https://github.com/addyosmani/agent-skills)
* [Piotr Mińkowski Spring Boot Skills](https://github.com/piomin/claude-ai-spring-boot)
* [Rrezart Prebreza Spring Boot Skills](https://github.com/rrezartprebreza/spring-boot-skills)

## AI review summaries

Set `OPENAI_API_KEY` before starting the application. Optionally set `OPENAI_MODEL`
(default: `gpt-4o-mini`). Reviews are sent to OpenAI as feedback text only, without
reviewer identities. Tests use stubbed AI responses and require no OpenAI access.

After each review commits, a background task summarizes all reviews for that product
once it has at least three. Invalid structured output is retried twice (three total attempts) by Spring AI’s validation advisor.
Provider failures retain the previous summary without service-level retries.
If all attempts fail, the previous summary is retained; the next review triggers a
new generation attempt. Summaries are persisted per product, and an older snapshot
cannot overwrite one based on more reviews. Public display is tracked separately
under UC-007.
