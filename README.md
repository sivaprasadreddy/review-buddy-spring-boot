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
