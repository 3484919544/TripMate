# 智游 TripMate —— 基于大模型的个性化旅游行程规划平台

TripMate（智游）是一个前后端分离的旅游行程规划平台。用户只需输入出发地、目的地、天数、预算与偏好，后端调用大语言模型（兼容 OpenAI 协议的服务商均可接入）自动生成结构化、可执行的每日行程；平台还提供行程管理、用户与管理员管理、AI 调用日志、意见反馈等完整功能。

> 本仓库根目录为 `TripMate/`，下文所有路径均相对该目录。

## 一、功能特性

- **AI 智能生成行程**：根据目的地/天数/预算/偏好生成包含「上午/下午/晚上/花费/提示」的结构化每日行程，并自动估算往返交通费用。
- **行程管理**：保存、列表（搜索 + 天数筛选 + 分页）、详情、编辑、删除、复制为文本分享。
- **预算管控**：生成与编辑时实时校验是否超预算，超支高亮预警。
- **用户与认证**：图形验证码登录、注册、手机号重置密码、资料（昵称/头像/签名）编辑、注销账号。
- **管理员功能**：用户管理（增删改查、角色分配）、意见反馈处理（回复/删除）、查看 AI 调用日志。
- **AI 安全防护**：输入提示词注入过滤、输入长度限制、输出结构严格校验、调用失败自动重试、全量调用日志落库。
- **个性化体验**：深色模式、三档字号、消息通知偏好等设置。

## 二、技术栈

| 层 | 技术 |
|---|---|
| 前端 | uni-app (Vue 3) + Pinia + SCSS，可编译为 H5 / App |
| 后端 | Spring Boot 3.2.5 + Java 17 + MyBatis-Plus 3.5.5 |
| 鉴权 | JJWT 0.12.5（JWT）+ BCrypt |
| AI 调用 | OkHttp 4.12 + 大模型 API（兼容 OpenAI 协议，服务商可配置） |
| 数据库 | MySQL 8（utf8mb4） |

## 三、项目结构

```
TripMate/
├── README.md                              项目总览(本文件)
├── 需求文档.md                              需求说明
├── 数据库设计文档.md                         数据库设计
├── 接口文档.md                               REST 接口文档
├── 部署运行说明.md                           部署、配置与排错(含所有文件路径)
├── AI模块使用说明.md                         AI 生成模块说明
├── 用户与认证模块使用说明.md                  用户与认证模块说明
├── 行程规划模块使用说明.md                   行程模块说明
├── 反馈管理模块使用说明.md                   反馈模块说明
├── 管理员模块使用说明.md                     管理员模块说明
├── .gitignore                             忽略密钥与构建产物
├── sql/
│   └── 01_schema.sql                      建表脚本(5 张表)
├── tripmate-server/                       后端(Spring Boot)
│   ├── pom.xml                            Maven 依赖与构建配置
│   └── src/main/
│       ├── java/com/tripmate/
│       │   ├── TripMateApplication.java   启动类(@MapperScan)
│       │   ├── ai/                        AI 模块
│       │   │   ├── AiClient.java          模型调用(OkHttp+超时+重试)
│       │   │   ├── AiLogService.java      AI 调用日志落库
│       │   │   ├── AiResultParser.java    结果解析与 Schema 校验
│       │   │   ├── ContentSafetyFilter.java 输入/输出安全过滤
│       │   │   └── PromptTemplate.java    提示词与 JSON Schema
│       │   ├── common/                    统一返回/异常/分页
│       │   │   ├── Result.java            统一返回包装
│       │   │   ├── ResultCode.java        返回码枚举
│       │   │   ├── PageResult.java        分页结果
│       │   │   ├── BusinessException.java 业务异常
│       │   │   └── GlobalExceptionHandler.java 全局异常处理
│       │   ├── config/                    配置类
│       │   │   ├── AiConfig.java          读取 ai.* 配置
│       │   │   ├── DataSeeder.java        启动初始化管理员/补账号
│       │   │   ├── MybatisPlusConfig.java 分页插件
│       │   │   └── WebMvcConfig.java      uploads/ 静态资源映射
│       │   ├── controller/                REST 控制器
│       │   │   ├── AuthController.java    认证:验证码/注册/登录
│       │   │   ├── UserController.java    用户:信息/改密/资料/注销/重置密码
│       │   │   ├── TripController.java    行程:生成/保存/列表/详情/修改/删除
│       │   │   ├── DestinationController.java 热门目的地
│       │   │   ├── FeedbackController.java 反馈:用户提交 + 管理员处理
│       │   │   ├── AdminController.java   管理:用户管理 + AI 日志
│       │   │   └── UploadController.java  头像上传
│       │   ├── dto/
│       │   │   ├── request/               请求体
│       │   │   │   ├── LoginRequest.java
│       │   │   │   ├── RegisterRequest.java
│       │   │   │   ├── GenerateTripRequest.java
│       │   │   │   ├── FeedbackRequest.java
│       │   │   │   ├── AdminCreateUserRequest.java
│       │   │   │   └── AdminUpdateUserRequest.java
│       │   │   └── response/              响应体
│       │   │       ├── LoginResponse.java
│       │   │       └── UserInfoVO.java
│       │   ├── entity/                    数据库实体
│       │   │   ├── User.java
│       │   │   ├── Trip.java
│       │   │   ├── TripDay.java
│       │   │   ├── Feedback.java
│       │   │   └── AiLog.java
│       │   ├── mapper/                    MyBatis-Plus Mapper
│       │   │   ├── UserMapper.java
│       │   │   ├── TripMapper.java        (含热门目的地统计 SQL)
│       │   │   ├── TripDayMapper.java
│       │   │   ├── FeedbackMapper.java
│       │   │   └── AiLogMapper.java
│       │   ├── security/                  鉴权
│       │   │   ├── JwtFilter.java         JWT 过滤器(白名单+解析)
│       │   │   ├── JwtUtil.java           JWT 生成/校验
│       │   │   └── UserContext.java       当前用户上下文(ThreadLocal)
│       │   └── service/                   业务服务
│       │       ├── AuthService.java       注册/登录
│       │       ├── UserService.java       用户资料/注销
│       │       ├── TripService.java       行程业务
│       │       ├── FeedbackService.java   反馈业务
│       │       ├── AdminService.java      用户管理/AI 日志
│       │       └── CaptchaService.java    图形验证码
│       └── resources/
│           ├── application.yml            主配置
│           └── application-local.yml.example 本地配置模板(复制为 application-local.yml)
└── tripmate-app/                          前端(uni-app)
    ├── package.json                       依赖与脚本
    ├── package-lock.json
    ├── index.html                         H5 入口页
    ├── main.js                            入口(注册 Pinia)
    ├── App.vue                            根组件(全局样式/主题/字号变量)
    ├── pages.json                         页面注册(12 个页面)
    ├── manifest.json                      应用信息/H5 端口等
    ├── uni.scss                           全局 SCSS 变量
    ├── vite.config.js                     构建与开发代理配置
    ├── api/                               接口封装
    │   ├── request.js                     统一请求(前缀/鉴权/错误处理)
    │   ├── auth.js                        认证与用户
    │   ├── trip.js                        行程与热门目的地
    │   ├── feedback.js                    反馈
    │   └── admin.js                       用户管理/AI 日志
    ├── components/                        公共组件
    │   ├── TripCard.vue                   行程卡片
    │   ├── EmptyState.vue                 空状态
    │   ├── DayPlanCard.vue                单日行程卡
    │   ├── BudgetAlert.vue                超预算预警
    │   └── AiBadge.vue                    AI 生成标识
    ├── pages/                             页面
    │   ├── login/login.vue                登录/注册/找回密码
    │   ├── home/home.vue                  首页
    │   ├── create-trip/create-trip.vue    创建行程
    │   ├── result/result.vue              生成结果
    │   ├── detail/detail.vue              行程详情
    │   ├── list/list.vue                  我的行程
    │   ├── profile/profile.vue            个人中心
    │   ├── settings/settings.vue          设置
    │   ├── ai-logs/ai-logs.vue            AI 使用记录
    │   ├── feedback/feedback.vue          反馈(用户)
    │   ├── admin-users/admin-users.vue    用户管理(管理员)
    │   └── admin-feedback/admin-feedback.vue 反馈管理(管理员)
    ├── store/                             Pinia 状态
    │   ├── user.js                        登录态与用户信息
    │   ├── trip.js                        跨页暂存生成结果
    │   └── settings.js                    主题/字号/通知偏好
    ├── utils/
    │   ├── format.js                      金额/日期格式化、行程转文本
    │   └── validator.js                   表单校验
    └── static/                            静态资源目录(预留)
```

> 配置相关文件的说明见 [部署运行说明.md](./部署运行说明.md)；各目录职责详见对应模块使用说明。

## 四、快速开始

> 完整的配置项、环境变量与排错说明见 [部署运行说明.md](./部署运行说明.md)。

### 第 1 步：准备数据库

数据库名固定为 `TripMate`。执行脚本 `sql/01_schema.sql`：

```bash
mysql -u 你的数据库账号 -p -e "CREATE DATABASE TripMate DEFAULT CHARSET utf8mb4;"
mysql -u 你的数据库账号 -p TripMate < sql/01_schema.sql
```

### 第 2 步：配置后端

复制 `tripmate-server/src/main/resources/application-local.yml.example` 为同目录下的 `application-local.yml`，按模板填写你实际的数据库账号/密码，以及所选 AI 服务商的 Key、基址与模型名（该文件已被 `.gitignore` 忽略）。

### 第 3 步：启动后端

```bash
cd tripmate-server
mvn spring-boot:run
# 服务默认端口 8090,主配置见 tripmate-server/src/main/resources/application.yml
```

### 第 4 步：启动前端

```bash
cd tripmate-app
npm install
npm run dev:h5
# 默认访问 http://localhost:5173,接口代理到后端 8090(见 tripmate-app/vite.config.js)
```

## 五、角色与权限

| 角色 | 说明 |
|---|---|
| 普通用户 | 生成/管理自己的行程、编辑资料、提交反馈、查看自己的 AI 记录 |
| 管理员 | 拥有普通用户能力，额外可管理全部用户、处理全部反馈 |

管理员身份由 `user.role='ADMIN'` 决定。系统首次启动且配置了管理员密码时，若库中无管理员，会自动创建一个管理员。

## 六、文档索引

| 文档 | 内容 |
|---|---|
| [需求文档.md](./需求文档.md) | 项目背景、角色、功能与非功能需求、错误码 |
| [数据库设计文档.md](./数据库设计文档.md) | 表结构、字段、索引说明 |
| [接口文档.md](./接口文档.md) | 全部 REST 接口的请求/响应约定 |
| [部署运行说明.md](./部署运行说明.md) | 环境准备、配置、启动、生产部署与排错（含所有文件路径） |
| [AI模块使用说明.md](./AI模块使用说明.md) | AI 生成流程、提示词、安全与日志 |
| [用户与认证模块使用说明.md](./用户与认证模块使用说明.md) | 注册/登录/验证码/找回密码/资料 |
| [行程规划模块使用说明.md](./行程规划模块使用说明.md) | 行程生成、保存、管理全流程 |
| [反馈管理模块使用说明.md](./反馈管理模块使用说明.md) | 反馈提交与处理 |
| [管理员模块使用说明.md](./管理员模块使用说明.md) | 用户管理与 AI 日志 |

## 七、开源协议
本项目仅供学习交流使用。
