# maven_demo_github

把 Android 库以 **AAR + JAR** 形式发布到 **GitHub Packages** 的最小可运行示例。

- 发布方式：Gradle `maven-publish` 插件
- 仓库地址：`https://maven.pkg.github.com/<owner>/<repo>`
- 产物坐标：`com.example:mylibrary:1.0.0`

## 目录结构

```
.
├── settings.gradle.kts              # 根构建
├── build.gradle.kts
├── gradle.properties                # 坐标 + GitHub owner/repo（凭据不放这里）
├── gradlew / gradlew.bat            # Gradle 8.13 wrapper
├── mylibrary/                       # 要发布的 Android Library
│   ├── build.gradle.kts             # ★ AAR/JAR 打包 + 发布配置都在这里
│   └── src/main/java/com/example/mylibrary/{Greeter,Calculator}.java
├── consumer-app/                    # 独立的消费方 App（从本地目录仓库解析）
│   ├── settings.gradle.kts
│   └── build.gradle.kts
├── consumer-github/                 # 独立的消费方构建（从 GitHub Packages 解析）
│   ├── settings.gradle.kts          # https://maven.pkg.github.com/<owner>/<repo>
│   ├── probe/                       # 只下载不编译，用来单独确认包能不能拉下来
│   └── app/                         # Android App，真实编译并链接这个 AAR
├── scripts/
│   ├── setup-local.sh               # 生成 local.properties（sdk.dir）
│   ├── publish-local.sh             # 发布到本地目录仓库（不需要任何凭据）
│   ├── publish-github.sh            # 发布到 GitHub Packages
│   ├── verify-github.sh             # 验证 GitHub 上的包真的可用
│   └── verify-e2e.sh                # 端到端验证：发布 → 消费 → 解包检查
└── .github/workflows/publish.yml    # CI：打 tag 自动发布
```

## 一、本地验证（不需要 GitHub 账号）

```bash
./scripts/setup-local.sh "$ANDROID_HOME"   # 或 ./scripts/setup-local.sh /path/to/Android/Sdk
./scripts/verify-e2e.sh
```

只想发布不验证时用 `./scripts/publish-local.sh`；`verify-e2e.sh` 会依次做四件事：

1. 编译 + 跑单元测试，并发布到 `build/local-repo`（一个普通的 Maven 目录仓库）；
2. 打印仓库布局；
3. 用**独立的** Gradle 构建 `consumer-app` 从 `build/local-repo` 解析 `com.example:mylibrary:1.0.0` 并编译出 APK（真编译，不是只看文件在不在）；
4. 如果本机有 `mvn`，再用 Maven 解析同一坐标，确认 POM 对 Maven 用户同样有效。

发布出来的东西长这样：

```
build/local-repo/com/example/mylibrary/1.0.0/
├── mylibrary-1.0.0.aar            # Android 库主产物
├── mylibrary-1.0.0.jar            # 同坐标下的纯 class jar
├── mylibrary-1.0.0-sources.jar    # 源码
├── mylibrary-1.0.0.pom            # <packaging>aar</packaging>
└── mylibrary-1.0.0.module        # Gradle Module Metadata
```

## 二、发布到 GitHub Packages

### 1. 需要准备的东西

| 项 | 说明 |
| --- | --- |
| `owner` | GitHub 用户名或组织名，包会归属到它 |
| `repo` | 承载这个包的仓库名，例如 `maven-demo-github`（仓库可以先建好，空的也行） |
| PAT | Classic token 勾选 `write:packages`（读还要 `read:packages`，私有仓库再加 `repo`）；Fine-grained token 给仓库的 **Packages: Read and write** |
| `GITHUB_ACTOR` | 你的 GitHub 用户名 |

> GitHub Packages 的 Maven 仓库永远挂在 `owner/repo` 下：`https://maven.pkg.github.com/OWNER/REPO`。
> `groupId` 不需要和 owner 一致，示例里用的是 `com.example`。

### 2. 配置

改 `gradle.properties`：

```properties
github.owner=<你的用户名或组织>
github.repo=<仓库名>
```

凭据**不要**写进仓库里的文件。三种提供方式（优先级从高到低）：

1. 环境变量 `GITHUB_ACTOR` / `GITHUB_TOKEN`（推荐，CI 也是这个）
2. `-Pgithub.actor=... -Pgithub.token=...`
3. `~/.gradle/gradle.properties` 里的 `github.actor` / `github.token`

### 3. 发布

```bash
export GITHUB_OWNER=<你的用户名或组织>
export GITHUB_REPO=<仓库名>
export GITHUB_ACTOR=<你的用户名>
export GITHUB_TOKEN=<ghp_xxx>

./scripts/publish-github.sh                 # 默认版本 1.0.0
LIBRARY_VERSION=1.0.1 ./scripts/publish-github.sh
```

成功后在这里能看到：`https://github.com/OWNER/REPO/pkgs`

### 4. 在别的项目里消费

```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/OWNER/REPO")
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("com.example:mylibrary:1.0.0")   // Android 工程拿到 .aar
}
```

> 注意：GitHub Packages **即使包是 public，读也要带 token**。没有 token 会直接 401。

### 5. CI 自动发布

`.github/workflows/publish.yml` 在 push `v*` tag、发布 Release 或手动触发时执行，
用 Actions 自带的 `GITHUB_TOKEN`（workflow 里已声明 `packages: write`）发布，不需要额外配置 secret。

```bash
git tag v1.0.0 && git push origin v1.0.0
```

## 三、验证 GitHub 上的包真的可用

`consumer-github/` 是一个独立的 Gradle 构建，**只从 GitHub Packages 拉取**，用来回答
"包到底能不能用" 这个问题：

```bash
export GITHUB_ACTOR=<你的用户名>
export GITHUB_TOKEN=<有 read:packages 权限的 token>

./scripts/verify-github.sh
```

它做两件事：

1. `:probe:verifyArtifact` —— 只解析、不编译，把 `mylibrary-1.0.0.aar` / `.jar` / `-sources.jar`
   下载到本地，打印文件大小和内部条目，并断言 AAR 里的 `classes.jar` 含有
   `com/example/mylibrary/Greeter.class`。这一步不碰 Android SDK。
2. `:app:assembleDebug` —— 一个真实的 Android App，`implementation("com.example:mylibrary:1.0.0")`
   然后编译出 APK。编译通过就说明这个包是可用的，而不只是"能下载"。

只想跑第一步：

```bash
./scripts/verify-github.sh :probe:verifyArtifact
```

> 没有 token 时会在下载 `.pom` 时直接 401，这是预期行为——GitHub Packages 的 Maven
> 仓库即使包是 public 也不允许匿名读取。凭据可以用环境变量，也可以写进
> `~/.gradle/gradle.properties` 的 `github.actor` / `github.token`。

## 四、实现要点

都在 `mylibrary/build.gradle.kts` 里：

- **AAR**：`from(components["release"])`，配合 `android { publishing { singleVariant("release") { withSourcesJar() } } }`。
- **JAR**：AGP 的 `bundleLibRuntimeToJarRelease` 产出编译好的 classes jar，用 `androidJar` 任务重新打包成 `mylibrary-1.0.0.jar`，再 `artifact(androidJar)` 加到同一个 publication。
- **packaging 修正**：一个 publication 里同时有 `.aar` 和 `.jar` 时，Gradle 会把 POM 的 `<packaging>` 退化成 `pom`，Maven 用户就拉不到 `aar` 了，所以用 `pom.withXml { }` 显式改回 `aar`（示例已验证 `mvn dependency:get` 能解析）。
- **离线友好**：`localRepo` 仓库永远注册；GitHub 仓库只在 `github.owner`/`github.repo` 非空时才注册。
- **快速失败**：跑 `publish*ToGitHubRepository` 时如果没凭据，直接给出可操作的报错，而不是等一个 HTTP 401。

## 五、常见问题

- **401 / 403**：token 没设置或权限不够。Classic PAT 需要 `write:packages`；fine-grained PAT 需要该仓库的 Packages 读写权限。发布用 `GITHUB_ACTOR`+`GITHUB_TOKEN` 两个都要有。
- **409 / `Cannot publish over existing version`**：GitHub Packages 不允许覆盖同一个版本，改 `LIBRARY_VERSION` 或删掉旧的包再发。
- **`<packaging>pom</packaging>`**：说明 POM 被 Gradle 退化了，检查 `pom.withXml` 那段是否还在。
- **找不到 Android SDK**：`./scripts/setup-local.sh /path/to/Android/Sdk`，或者导出 `ANDROID_HOME`。
- **`Could not find com.example:mylibrary`**：确认 `github.owner`/`github.repo` 与包实际所在的仓库一致；GitHub Packages 的 URL 必须指向**发布它的那个仓库**。
