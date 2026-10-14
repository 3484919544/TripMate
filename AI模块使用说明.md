# AI 模块使用说明

> 路径均相对仓库根目录 `TripMate/`。本模块**后端**代码位于
> `tripmate-server/src/main/java/com/tripmate/ai/`，配置类位于
> `tripmate-server/src/main/java/com/tripmate/config/AiConfig.java`，
> 配置文件为 `tripmate-server/src/main/resources/application.yml` 与
> `tripmate-server/src/main/resources/application-local.yml`。

## 一、模块职责

AI 模块负责「用户输入 → 大模型调用 → 结构化行程」的完整链路，并提供安全防护与调用日志。

## 二、涉及文件清单

| 文件路径 | 职责 |
|---|---|
| `tripmate-server/src/main/java/com/tripmate/ai/AiClient.java` | 组装请求、HTTP 调用（OkHttp）、超时与重试、串联解析与日志 |
| `tripmate-server/src/main/java/com/tripmate/ai/PromptTemplate.java` | 系统提示词（含 JSON Schema）、用户提示词、日志提示词 |
| `tripmate-server/src/main/java/com/tripmate/ai/AiResultParser.java` | 将模型返回的 JSON 文本解析为 `Trip`（含 `dayList`）并校验 |
| `tripmate-server/src/main/java/com/tripmate/ai/ContentSafetyFilter.java` | 输入脱敏/注入拦截、输出基础过滤 |
| `tripmate-server/src/main/java/com/tripmate/ai/AiLogService.java` | 成功/失败调用日志落库（失败不影响主流程） |
| `tripmate-server/src/main/java/com/tripmate/config/AiConfig.java` | 读取 `ai.*` 配置（base-url/api-key/model/timeout/retry/json-mode 等） |
| `tripmate-server/src/main/java/com/tripmate/service/TripService.java` | 调用入口 `generate()`，先做安全过滤再调用 AiClient |
| `tripmate-server/src/main/java/com/tripmate/controller/TripController.java` | 对外接口 `POST /api/trip/generate` |
| `tripmate-server/src/main/resources/application.yml` | `ai.*` 默认配置 |
| `tripmate-server/src/main/resources/application-local.yml` | 存放真实 `ai.api-key`（不入库） |
| `tripmate-app/pages/create-trip/create-trip.vue` | 前端发起生成请求 |
| `tripmate-app/pages/result/result.vue` | 前端展示生成结果 |
| `tripmate-app/pages/ai-logs/ai-logs.vue` | 前端查看 AI 调用日志 |
| `tripmate-app/api/trip.js` | 前端接口封装 `generateTrip()` |

## 三、调用流程

```
前端 create-trip.vue → api/trip.js generateTrip()
        │  POST /api/trip/generate
        ▼
TripController.generate()
        ▼
TripService.generate()
  1. ContentSafetyFilter.sanitizeInput()   // 对 origin/destination/preference/notes 逐字段脱敏校验
  2. PromptTemplate.buildUserPrompt()       // 组装用户提示
  3. PromptTemplate.buildLogPrompt()        // 组装写入 ai_log 的精简输入
  4. AiClient.generateTrip(system, user, log, userId, budget)
       └─ 循环(最多 retry+1 次,默认 2 次):
            callModel()                      // OkHttp POST {base-url}{endpoint}
            AiResultParser.parse()           // 解析 + Schema 校验 + 预算兜底
            AiLogService.logSuccess()        // 成功日志
       └─ 全部失败: AiLogService.logFailure()
  5. 返回 Trip(未落库)
```

## 四、提示词与输出格式

- **系统提示词**：`PromptTemplate.SYSTEM_PROMPT`（常量），规定角色（资深旅游行程规划师）、交通费计算规则与**固定 JSON Schema**，并要求只输出 JSON、`dayList` 长度等于 `days`、`dayIndex` 从 1 连续递增。
- **交通费规则**：若提供出发地，按出发地到目的地距离估算往返交通费（机票/高铁/长途大巴取中间价），计入某天 `cost` 与 `tips`；出发地=目的地时记为 0。
- **输出 Schema**：

```json
{
  "title": "行程标题",
  "origin": "出发地(可空)",
  "destination": "目的地",
  "days": 3,
  "budget": 3000,
  "totalCost": 2800,
  "overBudget": false,
  "dayList": [
    { "dayIndex": 1, "theme": "主题", "morning": "上午安排",
      "afternoon": "下午安排", "evening": "晚上安排", "cost": 900, "tips": "提示" }
  ]
}
```

## 五、容错与校验（`AiResultParser`）

| 场景 | 处理 | 返回码 |
|---|---|---|
| 模型返回非合法 JSON | 抛异常 | 1002 |
| 顶层非对象 / `dayList` 缺失或非数组 | 抛异常 | 1002 |
| `dayList` 长度 ≠ `days` | 以实际长度为准修正 `days`（容错，不报错） | — |
| 字段缺失 | 使用默认值（`title` → “未命名行程”，数值 → 0 等） | — |
| `totalCost > budget` | 强制 `overBudget=true`（后端兜底） | — |
| HTTP 非 2xx / 超时 / 异常 | 进入重试，最终失败 | 1001 |

> 前端对错误码处理：`1003` 超预算提示、`1001/1002` 生成失败、其余通用失败（见 `tripmate-app/pages/create-trip/create-trip.vue`）。

## 六、安全防护（`ContentSafetyFilter`）

- **输入过滤 `sanitizeInput`**：
  - 长度上限 200 字，超出抛 `1004`（`INVALID_INPUT`）。
  - 正则拦截常见提示词注入，命中抛 `1004`。包含特征：
    - `ignore (previous|above|all) instructions`
    - `disregard (the )?(system|previous)`
    - `you are (now|actually) a`
    - `system:`
    - `reveal|leak|print your prompt`
    - `<system|prompt|instruction` 标签
- **输出过滤 `sanitizeOutput`**：去除可能的 markdown 代码块包裹，并截断超长输出（>20000 字符，防撑爆解析）。
- **密钥安全**：API Key 仅用于请求头 `Authorization: Bearer`，不写库、不返回前端。

## 七、调用日志（`AiLogService`）

每次调用写入数据库表 `ai_log`（表结构见 [数据库设计文档.md](./数据库设计文档.md)）：

- 成功：`status=SUCCESS`，`requestTime`/`responseTime` 由耗时推算。
- 失败：`status=FAIL`，记录 `errorMsg`。
- `prompt` 记录精简输入，格式：`出发地=..|目的地=..|天数=..|预算=..|偏好=..|备注=..`。
- 日志写入使用独立 `try/catch`，**落库失败不影响行程生成**。

用户在「AI 使用记录」页查看本人日志：前端 `tripmate-app/pages/ai-logs/ai-logs.vue` → `tripmate-app/api/admin.js` 的 `listAiLogs()` → `GET /api/admin/ai-logs`（返回当前登录用户日志，分页）。

## 八、配置与切换模型

本模块通过 **OpenAI 兼容协议** 调用大模型，因此**换服务商/模型无需改代码，只改配置**。`ai:` 段位于 `tripmate-server/src/main/resources/application.yml`；私密项（Key、服务商地址、模型名）建议写在 `tripmate-server/src/main/resources/application-local.yml`（该文件被 `.gitignore` 忽略）或使用环境变量。

```yaml
# 文件: tripmate-server/src/main/resources/application.yml
ai:
  api-key: ${AI_API_KEY:}
  base-url: ${AI_BASE_URL:}
  model: ${AI_MODEL:}
  endpoint: /chat/completions
  timeout: 30000
  retry: 1
  temperature: 0.7
  json-mode: true
```

| 配置项 | 说明 | 修改位置 / 环境变量 |
|---|---|---|
| `ai.api-key` | 模型密钥（自行填写） | `application-local.yml` 或 `AI_API_KEY` |
| `ai.base-url` | 模型服务基址（自行填写，不含 `/chat/completions`） | `application-local.yml` 或 `AI_BASE_URL` |
| `ai.model` | 模型名（自行填写） | `application-local.yml` 或 `AI_MODEL` |
| `ai.endpoint` | 接口路径 | `application.yml`（兼容协议固定 `/chat/completions`） |
| `ai.timeout` | 读超时(ms) | `application.yml` |
| `ai.retry` | 失败重试次数 | `application.yml` |
| `ai.temperature` | 采样温度 | `application.yml` |
| `ai.json-mode` | 强制 JSON 输出 | `application.yml` |

### 8.1 配置示例（占位符，按需填写）

在 `tripmate-server/src/main/resources/application-local.yml` 中填写你所用服务商的值（代码会将 `base-url` 与 `endpoint` 拼接为完整请求地址）：

```yaml
# 文件: tripmate-server/src/main/resources/application-local.yml
ai:
  api-key: 你的APIKey
  base-url: 你的模型服务商基址        # 例如某兼容 OpenAI 协议服务商的接口基址(不含 /chat/completions)
  model: 你的模型名
```

只要服务商兼容 OpenAI 的 `/chat/completions` 接口即可接入。也可用环境变量 `AI_API_KEY` / `AI_BASE_URL` / `AI_MODEL` 覆盖。

### 8.2 注意事项

- **不是所有服务商都支持 `response_format: json_object`**。若调用报错或返回格式不稳定，将 `ai.json-mode` 设为 `false`（此时依赖提示词约束输出 JSON，解析器仍会校验）。
- 本地部署的模型通常无需真实 Key，`ai.api-key` 可填任意值。
- 切换模型后建议先用「创建行程」验证一次生成是否成功，并到「AI 使用记录」查看调用日志。
- `ai.timeout` 对推理较慢的模型可适当调大（如 `60000`）。

## 九、扩展建议

- 多轮流式输出：在 `AiClient.callModel` 基础上扩展。
- 更严格的输入合规：在 `ContentSafetyFilter` 增加词库或接入内容审核服务。
- 记录 token 消耗：解析响应 `usage` 字段后写入 `ai_log.tokens`（当前默认记 0）。
