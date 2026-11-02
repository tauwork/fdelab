# FDELab

FDELab 业务系统代码框架。官网、微信小程序和管理后台共用一个后端。

| 模块 | 说明 | 本地地址 |
| --- | --- | --- |
| fdelab-server | Spring Boot 3 API | http://localhost:8080 |
| fdelab-admin-web | Vue 3 + Element Plus 管理后台 | http://localhost:5173 |
| fdelab-site-web | Vue 3 展示网站 | http://localhost:5174 |
| fdelab-miniapp | uni-app（Vue 3）微信小程序 | 微信开发者工具 / H5 |

协作仓库：https://github.com/tauwork/fdelab.git

## 技术栈

- 后端：Spring Boot 3.5、MyBatis-Plus、MySQL 8、Sa-Token、SpringDoc
- 管理后台：Vue 3、Vite、Element Plus、Pinia、Vue Router
- 展示网站：Vue 3、Vite
- 小程序：uni-app（Vue 3）
- 部署：Docker Compose

## 本地启动

需要 JDK 17、Maven、Node.js 20+、MySQL 8。

1. 初始化数据库：

```bash
mysql -uroot -p < sql/init.sql
```

默认数据源是 `127.0.0.1:3306/fdelab`，用户 `root`，密码 `root`。不一致时改 `fdelab-server/src/main/resources/application-dev.yml`。

2. 启动后端：

```bash
cd fdelab-server
mvn spring-boot:run
```

接口文档：http://localhost:8080/swagger-ui.html

3. 启动管理后台：

```bash
cd fdelab-admin-web
npm install
npm run dev
```

4. 启动展示网站：

```bash
cd fdelab-site-web
npm install
npm run dev
```

5. 启动小程序：

```bash
cd fdelab-miniapp
npm install
npm run dev:mp-weixin
```

用微信开发者工具打开 `fdelab-miniapp/dist/dev/mp-weixin`。H5 预览用 `npm run dev:h5`。小程序请求地址在 `fdelab-miniapp/src/utils/config.js`，正式环境改成已配置的 https 域名。`manifest.json` 里的微信 `appid` 留空，接入时再填。

## 初始账号

| 用户名 | 密码 | 角色 |
| --- | --- | --- |
| admin | admin123 | 管理员 |
| editor | editor123 | 编辑 |

只用于本地开发。上线前改掉数据库密码和这两个账号。

登录后请求头为 `Authorization: Bearer <token>`。令牌目前存在服务进程内存里，多实例部署前再接入 Redis。

## Docker Compose

本机装好 Docker 后，在仓库根目录执行：

```bash
docker compose up -d --build
```

| 服务 | 地址 |
| --- | --- |
| MySQL | localhost:3306 |
| API | http://localhost:8080 |
| 管理后台 | http://localhost:8081 |
| 展示网站 | http://localhost:8082 |

`sql/init.sql` 只在 MySQL 数据卷第一次初始化时执行。改了脚本后需要删掉数据卷再启动。

## 人才与技能库

每位 FDE 有一条人才档案，并分配子域名主页，例如蒋利文是 `https://jiangliwen.fdelab.io`。本地没有泛域名时，打开 `http://localhost:5174/t/jiangliwen`，或把 `jiangliwen.localhost` 指到展示站。

小程序底部是首页、人才、AI、方案、我的。人才列表一位一行，按点赞从高到低。管理后台的「人才」可以新建、编辑、下架和删除。

WorkBuddy 或其他代理调用技能库：

```bash
curl http://localhost:8080/api/public/skills
curl http://localhost:8080/api/public/skills/fde-profile
```

上传档案时带请求头 `X-Fdelab-Skill-Key`。本地默认密钥是 `fdelab-dev-skill-key`，生产环境用环境变量 `FDELAB_TALENT_SKILL_KEY` 覆盖。仓库里的技能说明在 `.cursor/skills/fde-profile/SKILL.md`。

已有数据库执行：

```bash
mysql -uroot -p < sql/talent.sql
```

线上把 `*.fdelab.io` 解析到展示站。展示站根据主机名切到个人主页。

## 往后加业务

- 表结构追加到 `sql/`
- 后端模块放在 `com.fdelab.module` 下，按 `entity`、`mapper`、`service`、`controller` 拆。MyBatis-Plus 3.5.18 的 `ServiceImpl` 在 `com.baomidou.mybatisplus.spring.service.impl`
- 需要登录的接口放在 `/api/**`，公开接口放在 `/api/public/**`
- 统一返回 `{ code, message, data }`，成功时 `code` 为 `0`
