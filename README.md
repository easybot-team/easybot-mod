<p align="center">
  <img src="src/main/resources/assets/easybot/icon.png" alt="EasyBot" width="150" height="150">
</p>

<h1 align="center">EasyBot-Mod</h1>

<p align="center">EasyBot 的 Fabric / NeoForge / Forge 桥接模组：把 Minecraft 服务器接入 EasyBot 主程序，实现群服消息互通、账号绑定、玩家事件播报和统计变量查询。</p>

<p align="center">
  <a href="https://github.com/easybot-team/easybot-mod/actions/workflows/pr-build.yml"><img src="https://github.com/easybot-team/easybot-mod/actions/workflows/pr-build.yml/badge.svg" alt="PR Build"></a>
  <img src="https://img.shields.io/badge/Loader-Fabric%20%7C%20NeoForge%20%7C%20Forge-green" alt="Fabric / NeoForge / Forge">
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-Apache--2.0-blue" alt="Apache-2.0"></a>
</p>

<p align="center">
  <a href="#功能">功能</a> ·
  <a href="#工作方式">工作方式</a> ·
  <a href="#运行环境">运行环境</a> ·
  <a href="#可选联动">可选联动</a> ·
  <a href="#安装">安装</a> ·
  <a href="#命令与权限">命令与权限</a> ·
  <a href="#配置说明">配置说明</a> ·
  <a href="#播报冷却">播报冷却</a> ·
  <a href="#消息转发过滤">消息转发过滤</a> ·
  <a href="#从源码构建">从源码构建</a> ·
  <a href="#项目结构">项目结构</a> ·
  <a href="#相关链接">相关链接</a>
</p>

---

## 功能

- **消息互通**：群消息与游戏聊天双向同步，支持富文本、@ 提醒和 ChatImage 图片显示；可用 `/easybot say` 主动发送消息。
- **账号绑定**：生成绑定验证码、确认绑定、查询已绑定的社交账号，并配合主程序进行玩家登录检查。
- **玩家事件**：同步加入、退出和死亡消息，分别配置播报冷却，减少重复消息。
- **转发过滤**：按长度、关键词过滤消息，游戏到群还可按玩家名和 UUID 过滤；不影响游戏内聊天。
- **命令权限**：命令开关、原版权限等级及可选 LuckPerms 节点共同控制执行、帮助和补全。
- **变量查询**：提供玩家统计、数学运算和玩家数据查询能力，供主程序使用。
- **多端适配**：通过 Stonecutter 维护 Fabric、NeoForge 和 Forge 的多个 Minecraft 构建目标。

## 工作方式

```mermaid
flowchart LR
    Player["玩家"] <--> Server["Minecraft 服务端<br/>EasyBot 模组"]
    Server <-->|"WebSocket 桥接<br/>ws + token"| Main["EasyBot 主程序"]
    Main <-->|"收发消息"| Group["QQ 等社交平台"]
```

模组负责游戏内事件、命令和玩家数据，通过 WebSocket 与主程序通信；机器人连接、群同步规则和账号管理由主程序处理。

## 运行环境

安装时须同时匹配 **Minecraft 版本与加载器**。当前开发分支在 [settings.gradle.kts](settings.gradle.kts) 中定义以下构建目标：

| 加载器 | Minecraft 构建目标 |
| --- | --- |
| Fabric | 1.20.1、1.20.2、1.20.4、1.20.6、1.21、1.21.6、1.21.8、1.21.9、1.21.10、1.21.11、26.1、26.1.1、26.1.2、26.2、26.3 |
| NeoForge | 1.20.4、1.20.6、1.21、1.21.6、1.21.8、1.21.9、1.21.10、1.21.11、26.1、26.1.1、26.1.2、26.2、26.3 |
| Forge | 1.20.1（项目中称为 `legacyforge`） |

一个构建产物可能声明兼容多个小版本，具体以下载产物及对应 [versions/](versions/) 配置为准；构建目标列表不代表所有版本均经过实服测试。

| 项目 | 要求 |
| --- | --- |
| Java | 本仓库 1.20.1–1.20.4 构建目标使用 Java 17；1.20.6–1.21.x 使用 Java 21；26.x 使用 Java 25 |
| 主程序 | EasyBot 主程序，以及其中创建的服务器桥接地址和 token |
| Fabric API | Fabric 服务端安装与 Minecraft 版本对应的 Fabric API |

## 可选联动

| 项目 | 作用与适用范围 |
| --- | --- |
| LuckPerms | 在子服安装对应加载器版本后，使用 `easybot.command.*` 权限节点；未安装时使用原版权限等级 |
| Text Placeholder API | Fabric 1.20.x / 1.21.x 的文本占位符扩展；26.x 不走该解析分支 |
| EasyAuth | Fabric 端登录状态查询；需使用与服务器及当前 API 兼容的版本 |
| Geyser / Floodgate | 基岩版玩家身份与名称处理；需安装适配当前服务端的版本 |
| ChatImage | 客户端显示转发消息中的图片，模组端通过 `sync.chatImageSupport` 控制图片格式支持 |

## 安装

1. 按[使用文档](https://docs.inectar.cn/docs/easybot/intro/)准备 EasyBot 主程序，创建服务器并取得桥接地址和 token。
2. 下载与 Minecraft 版本、加载器匹配的 EasyBot 模组，放入服务端 `mods/` 目录；Fabric 端同时准备对应版本的 Fabric API。
3. 启动一次服务端，在生成的 `config/easybot/config.json` 中填写 `ws` 和 `token`。
4. 执行 `/easybot reload` 或等待自动重载，用 `/easybot status` 查看连接状态；在主程序中配置机器人、群聊及同步规则。

> [!IMPORTANT]
> `ignoreError` 默认为 `false`。连接或登录检查出错时，玩家可能被拒绝进入服务器；设为 `true` 可在这些异常情况下放行。它不会绕过主程序正常返回的拒绝登录结果。

## 命令与权限

`command.enabled` 可为下表中的配置键设置 `false`，禁用对应命令。`command.permissionLevels` 可设置未指定权限节点时使用的原版权限等级（`0`–`4`）。例如：

```json
"command": {
  "allowBind": true,
  "waitTime": 3,
  "enabled": { "say": false },
  "permissionLevels": { "status": 3, "reload": 3, "config": 3 }
}
```

| 命令 | 配置键 | LuckPerms 权限节点 | 默认等级 |
| --- | --- | --- | --- |
| `/easybot`、`/easybot help` | `help` | `easybot.command.help` | 0 |
| `/easybot say <消息>` | `say` | `easybot.command.say` | 0 |
| `/easybot status`（服务状态） | `status` | `easybot.command.status` | 3 |
| `/easybot reload` | `reload` | `easybot.command.reload` | 3 |
| `/easybot config <配置项> [秒数]` | `config` | `easybot.command.config` | 3 |
| `/easybot bind`、`/easybot bind confirm` | `bind` | `easybot.command.bind` | 0 |
| `/easybot confirm <code>` | `confirm` | `easybot.command.confirm`，同时要求 `bind` 权限 | 0 |
| `/easybot bind status`（绑定状态） | `bindStatus` | `easybot.command.bindstatus` | 0 |

关闭 `allowBind` 或禁用 `bind` 会同时阻止发起和确认绑定，绑定状态查询可独立使用。帮助和补全遵守开关与权限；若禁用了 `reload`，仍可修改配置文件，由自动重载恢复。

LuckPerms 为可选依赖，不随 Mod 打包，需在子服安装对应加载器版本。节点显式允许/拒绝优先于原版权限等级，显式拒绝对 OP 同样有效；节点未设置或未安装 LuckPerms 时使用上表等级。禁用的命令不受权限授予影响。

## 配置说明

配置文件为 `config/easybot/config.json`，默认内容见 [config.json](src/main/resources/config.json)。修改后会自动重载，也可执行 `/easybot reload`；无效配置不会替换正在使用的有效配置。

| 配置项 | 说明 |
| --- | --- |
| `ws` / `token` | 主程序提供的桥接地址与服务器 token |
| `ignoreError` | 连接或登录检查出错时是否放行玩家，默认 `false` |
| `debug` | 调试日志开关 |
| `message.*` | 绑定开始、成功及失败的提示模板 |
| `command.allowBind` | 是否允许发起和确认绑定 |
| `command.enabled` / `command.permissionLevels` | 命令开关及默认原版权限等级 |
| `skipOptions.*` | 跳过加入、退出、聊天或死亡消息同步 |
| `event.enableSuccessEvent` / `event.bindSuccess` | 绑定成功后执行的命令，支持 `$player`、`$account`、`$name` |
| `geyser.ignorePrefix` / `geyser.useRealUuid` | Floodgate 玩家名称与 UUID 的取值方式 |
| `sync.chatImageSupport` | 转发消息中的 ChatImage 图片格式支持 |
| `sync.*CooldownSeconds` | 进服、退服及死亡播报冷却，见下文 |
| `chatFilter.gameToGroup` / `chatFilter.groupToGame` | 两个方向的消息转发过滤规则 |

下方 JSON 示例为配置片段，需合并到现有文件的根对象中。

## 播报冷却

管理员可用 `/easybot config` 查看冷却配置，或执行以下命令保存并即时生效。默认权限等级为 3，与 reload 相同；支持命令开关、权限等级和 LuckPerms 节点配置。

```text
/easybot config sync.joinCooldownSeconds 60
/easybot config sync.quitCooldownSeconds 60
/easybot config sync.deathCooldownSeconds 30
```

省略秒数时查看该项；支持配置项和常用秒数补全。秒数须为 32 位整数，`0` 或负数关闭冷却。命令只开放上述三个冷却项，保存失败时保留原配置。

在 `config/easybot/config.json` 的 `sync` 中配置冷却秒数，例如：

```json
"sync": {
  "chatImageSupport": true,
  "joinCooldownSeconds": 60,
  "quitCooldownSeconds": 60,
  "deathCooldownSeconds": 60
}
```

- 三项默认均为 `0`，`0` 或负数表示关闭；旧配置无需修改。
- 进服、退服按玩家 UUID 分别计时；死亡按玩家 UUID 和伤害类型分别计时。
- 首次正常播报，冷却中的重复事件不延长时间；切换死因不会清除原死因的冷却。
- 只跳过发往主程序的对应播报，不取消游戏事件或改变登录验证。`skipOptions` 仍优先生效。
- 修改某项时长并重载后，该类事件下次发生时清空原冷却；冷却不写入磁盘，重启后重新计时。

## 消息转发过滤

在 `config/easybot/config.json` 中配置，省略新字段时保持原有行为：

```json
"chatFilter": {
  "gameToGroup": {
    "maxLength": 100,
    "blockedKeywords": ["广告词"],
    "blockedPlayerNames": ["PlayerName"],
    "blockedPlayerUuids": []
  },
  "groupToGame": {
    "maxLength": 200,
    "blockedKeywords": ["广告词"]
  }
}
```

- 默认长度为 `0`（不限）、列表为空；超限或命中规则后整条消息不转发，不截断，也不取消游戏内聊天。
- 关键词是普通文本包含匹配，忽略大小写；颜色代码不参与匹配或计数。长度按 Unicode 码点计算（组合 emoji 可能占多个码点）。
- 游戏到群使用发送包里的 `player_name_raw` 和 UUID，不使用显示昵称；名称忽略大小写，UUID 使用带连字符的完整格式。Floodgate 玩家沿用现有身份转换规则。
- 群到游戏同时检查主程序传来的纯文本与富文本段的文本表示，可能包含昵称、模板前缀、图片说明等，不读取图片内容。连续文本段不能绕过关键词检查。现有协议未提供发言人身份，因此此方向不支持玩家名单。
- `/easybot say` 也受过滤限制，拒绝时提示“未转发”；控制台只检查消息内容和长度。`skipOptions.skipChat` 仍仅控制自动聊天同步。
- 配置保存后自动重载，也可使用 `/easybot reload`；无效配置保留上一份有效配置并在后台报告错误。

## 从源码构建

使用 **JDK 25** 和仓库自带的 Gradle Wrapper，与当前 PR 构建工作流保持一致。`easybot-bridge` 和 `ez-statistic` 从 GitHub Packages 获取，构建前通过环境变量提供 `USERNAME` 和具有 `read:packages` 权限的 `TOKEN`。

```bash
# 构建全部目标并收集产物
./gradlew buildAndCollect
# 产物目录：build/libs/<mod.version>/

# 只构建指定目标，例如 1.21 Fabric
./gradlew :1.21-fabric:buildAndCollect
```

Windows PowerShell 中使用 `.\gradlew.bat` 执行同名任务。版本号来自 [gradle.properties](gradle.properties)，各目标的依赖版本位于 `versions/<版本>-<加载器>/gradle.properties`。

在 IDE 的 Gradle 任务列表中使用 `Set active project to ...` 切换当前源码对应的 Minecraft 版本与加载器。详细机制见 [Stonecutter 指南](https://stonecutter.kikugie.dev/wiki/start/)。

> [!IMPORTANT]
> 不要对整个项目运行 IDE 的全局代码优化或删除“未使用”代码。Stonecutter 根据目标切换条件代码，当前未启用的代码可能被其他版本或加载器使用。

## 项目结构

```text
src/main/java/com/springwater/easybot/
├── api/           扩展接口
├── commands/      命令注册与处理
├── config/        配置加载、校验与重载
├── impl/          Bridge 行为与消息组件实现
├── mixin/         游戏行为注入与版本适配
├── nbt/           玩家数据读取
├── placeholder/   统计及数学占位符
├── platforms/     Fabric、NeoForge、Forge 入口与事件
└── utils/         权限、过滤、冷却等工具

src/main/resources/  模组元数据、默认配置及图标
versions/            各版本与加载器的构建配置
buildSrc/            共享 Gradle 构建逻辑
```

当前 [PR Build](.github/workflows/pr-build.yml) 对面向 `develop` 的 PR 执行 `buildAndCollect` 检查，也支持手动触发。

## 相关项目

| 项目 | 说明 |
| --- | --- |
| [easybot-bridge](https://github.com/easybot-team/easybot-bridge) | 主程序与服务器的通信协议 |
| [ez-statistic](https://github.com/easybot-team/ez-statistic) | Minecraft 统计数据解析 |
| [easybot-bukkit](https://github.com/easybot-team/easybot-bukkit) | Bukkit / Spigot / Paper 插件端 |
| [Easybot-Velocity](https://github.com/easybot-team/Easybot-Velocity) | Velocity 代理端 |
| [easybot-mcdr](https://github.com/easybot-team/easybot-mcdr) | MCDR 群服同步插件 |
| [easybot-legacyforge](https://github.com/easybot-team/easybot-legacyforge) | 社区维护的 Forge 1.12.2 实现 |

## 相关链接

> [!TIP]
> 遇到 Bug 或想提功能建议，请到 [easybot-issues](https://github.com/easybot-team/easybot-issues/issues) 提交。
> 请附上 Minecraft、加载器、Java 和 EasyBot 版本，以及复现步骤和服务端日志。需要调试信息时可开启 `debug`，分享日志前请隐藏 token。

- 使用文档：<https://docs.inectar.cn/>
- 上游仓库：<https://github.com/easybot-team/easybot-mod>
- 问题反馈：[easybot-team/easybot-issues](https://github.com/easybot-team/easybot-issues/issues)
- 许可证：[Apache License 2.0](LICENSE)

---

<p align="center"><sub>EasyBot-Mod · 使用文档 <a href="https://docs.inectar.cn/">docs.inectar.cn</a> · 问题反馈 <a href="https://github.com/easybot-team/easybot-issues/issues">easybot-issues</a> · 由 <a href="https://github.com/easybot-team">easybot-team</a> 维护</sub></p>
