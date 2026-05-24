# 宠物喂养系统架构图

## 总体架构

```mermaid
flowchart LR
  U[用户] --> W[浏览器 / Web 前端]

  subgraph P[表现层]
    W --> R[Vue Router]
    R --> V1[Home 控制台]
    R --> V2[登录 / 注册]
    R --> V3[宠物管理]
    R --> V4[智能喂养]
    R --> V5[健康监测]
    R --> V6[宠物日记]
    V1 --> C1[PetAvatar / PetTypeIcon / ECharts]
    V3 --> C2[表单 / 上传 / 列表]
    V4 --> C3[方案卡片 / 图表 / 打卡]
    V5 --> C4[健康表单 / 图表 / 预警]
    V6 --> C5[日历 / 编辑器 / 图片 / 收藏]
  end

  subgraph A[应用层]
    API[REST API Controller]
    SEC[Spring Security / JWT]
    API <--> SEC
    A1[AuthController]
    A2[PetController]
    A3[FeedingPlanController]
    A4[HealthRecordController]
    A5[HealthAlertController]
    A6[PetDiaryEntryController]
  end

  subgraph B[业务逻辑层]
    S1[UserService]
    S2[PetService]
    S3[FeedingPlanService]
    S4[HealthRecordService]
    S5[HealthAlertService]
    S6[PetDiaryEntryService]
  end

  subgraph D[数据访问层 / 持久层]
    R1[UserRepository]
    R2[PetRepository]
    R3[FeedingPlanRepository]
    R4[HealthRecordRepository]
    R5[HealthAlertRepository]
    R6[PetDiaryEntryRepository]
    DB[(MySQL / H2)]
  end

  C1 -->|axios| API
  C2 -->|axios| API
  C3 -->|axios| API
  C4 -->|axios| API
  C5 -->|axios| API

  API --> A1
  API --> A2
  API --> A3
  API --> A4
  API --> A5
  API --> A6

  A1 --> S1
  A2 --> S2
  A3 --> S3
  A4 --> S4
  A5 --> S5
  A6 --> S6

  S1 --> R1
  S2 --> R2
  S3 --> R3
  S4 --> R4
  S5 --> R5
  S6 --> R6

  R1 --> DB
  R2 --> DB
  R3 --> DB
  R4 --> DB
  R5 --> DB
  R6 --> DB
```

## 分层说明

### 1. 表现层
前端使用 Vue 3 + Vue Router + Axios，主要页面包括：
- `/` 控制台主页
- `/login` 登录
- `/register` 注册
- `/pets` 宠物管理
- `/feeding` 智能喂养
- `/health` 健康监测
- `/diary` 宠物日记

### 2. 应用层
Spring Boot Controller 负责：
- 接收前端请求
- 参数校验与错误返回
- 调用业务层服务
- 统一返回 JSON 响应

### 3. 业务逻辑层
Service 负责核心规则：
- 用户注册、登录
- 宠物 CRUD 与照片、备注保存
- 喂养方案生成
- 健康数据统计、预警标记
- 宠物日记保存、收藏、图片处理

### 4. 持久层
Repository 通过 JPA 访问数据库，当前支持：
- MySQL
- H2（本地临时模式）

## 主要接口清单

| 模块 | 接口 | 方法 | 作用 |
|---|---|---:|---|
| 认证 | `/api/auth/register` | POST | 用户注册 |
| 认证 | `/api/auth/login` | POST | 用户登录 |
| 宠物管理 | `/api/pets` | GET | 获取宠物列表 |
| 宠物管理 | `/api/pets` | POST | 新增宠物 |
| 宠物管理 | `/api/pets/{id}` | GET | 获取单只宠物 |
| 宠物管理 | `/api/pets/{id}` | PUT | 编辑宠物 |
| 宠物管理 | `/api/pets/{id}` | DELETE | 删除宠物 |
| 宠物管理 | `/api/pets/{id}/note` | POST/PATCH | 保存备注与备注图片 |
| 宠物管理 | `/api/pets/{id}/photo` | POST/PATCH | 保存宠物头像 |
| 智能喂养 | `/api/feeding-plan/{petId}` | GET | 获取或生成喂养方案 |
| 健康监测 | `/api/health-records` | POST | 新增健康记录 |
| 健康监测 | `/api/health-records/{petId}` | GET | 获取宠物健康记录 |
| 健康预警 | `/api/health-alerts/{petId}` | GET | 获取健康预警 |
| 健康预警 | `/api/health-alerts/{alertId}/handled` | POST/PATCH | 标记预警已处理 |
| 宠物日记 | `/api/pet-diaries/{petId}` | GET | 获取指定宠物日记 |
| 宠物日记 | `/api/pet-diaries` | POST | 保存日记、图片、收藏状态 |

## 数据表

- `users`
- `pets`
- `feeding_plans`
- `health_records`
- `health_alerts`
- `pet_diary_entries`

## 调用链

```mermaid
sequenceDiagram
  participant U as 用户
  participant F as 前端页面
  participant C as Controller
  participant S as Service
  participant R as Repository
  participant M as MySQL/H2

  U->>F: 操作表单 / 点击按钮
  F->>C: axios 请求
  C->>S: 业务调用
  S->>R: 数据查询 / 保存
  R->>M: SQL 读写
  M-->>R: 返回数据
  R-->>S: 实体对象
  S-->>C: 业务结果
  C-->>F: JSON 响应
```
