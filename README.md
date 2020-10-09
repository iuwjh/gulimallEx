简述
-
描述
-

|模块|功能|
|---|---|
|gulimall-product    |商品服务             |
|gulimall-coupon     |优惠卷服务            |
|gulimall-order      |订单服务             |
|gulimall-ware       |库存服务             |
|gulimall-member     |会员服务             |
|gulimall-third-party|第三方接口服务          |
|gulimall-search     |Elasticsearch检索服务|
|gulimall-auth-server|身份认证服务           |
|gulimall-cart       |购物车服务            |
|gulimall-seckill    |秒杀服b务             |
|gulimall-gateway    |API网关            |
|renren-fast         |管理后端             |
|gulimall-admin-vue  |管理前端             |

|链接                         |说明        |
|---                         |---       |
|gulimall.com                |主页        |
|auth.gulimall.com/login.html|登录页       |
|admin.gulimall.com          |后台管理系统    |
|zipkin.gulimall.com         |链路追踪监控    |
|rabbitmq.gulimall.com       |RabbiqMQ管理|
|consul.gulimall.com         |服务注册中心    |
|portainer.gulimall.com*     |容器可视化管理   |

\* _portainer容器要求创建后5分钟内配置admin账号，否则会自动退出_

环境搭建
-
|文件                       |说明                  |
|---                       |---                 |
|document/setup/hosts      |域名映射（IP为Docker虚拟机地址）|
|document/setup/docker-data|运行环境所需文件            |
|document/deploy/.env      |存储compose文件用到的环境变量  |

* 远程docker守护进程

虚拟机打开 `/lib/systemd/system/docker.service`
修改dockerd的启动参数，如下
```
[Service]
ExecStart=
ExecStart=/usr/bin/dockerd -H tcp://0.0.0.0:2375 -H unix://var/run/docker.sock
```
然后在主机设置全局环境变量
```
set DOCKER_HOST=tcp://{VM_IP}:2375
```
部署
-
* .env环境变量

|变量名        |说明                   |
|---          |---                    |
|HOST_IP      |主机可访问的Docker容器地址|
|HOST_DIR_ROOT|docker-data文件夹路径   |
|*_PORT       |容器在HOST_IP上绑定的端口|
|APP_JVM_FLAGS|Java服务的JVM参数       |

|部署文件                        |说明                    |
|---                            |---                   |
|document/deploy/compose-app.yml|服务部署文件                |
|document/deploy/compose-env.yml|运行环境部署文件              |
|document/deploy/.env           |存储compose文件用到的环境变量    |

|Gradle任务      |功能                          |
|---            |---                         |
|dockerBuild    |jib构建镜像、推送至Docker、Compose up|
|dockerDeployAll|构建所有项目并部署至Docker            |
|dockerCleanAll |清理所有容器和自建镜像                 |

完全Docker部署
```
gradlew dockerDeployAll
```
TODO
-

* Sentinel熔断降级
* Spring Security
* Https
* e2e test

FAQ
-

* 配置更改后没有生效

在使用Intellij Idea构建项目时，偶尔出现资源文件没有被拷贝到类路径的情况，导致以下错误。
```
class path resource [xxx] cannot be opened because it does not exist
```
可以先运行以下Gradle任务，再构建。
```
gradlew :gulimall-common:processResources
```
或者
```
gradlew :processResources
```

* Zipkin每隔2秒出现一个async追踪

Consul 服务发现的自动配置 `ConsulCatalogWatchAutoConfiguration` 里配置了定时任务 `catalogWatchTaskScheduler` 负责轮询获取服务状态。这个定时任务默认每隔1秒发送一次请求，每个请求阻塞2秒。通过观察 `ExecutorBeanPostProcessor`，发现 Sleuth 通过对 `ThreadPoolTaskExecutor` 进行包装来实现追踪。我们只需要在 Sleuth 的异步配置中将 `catalogWatchTaskScheduler` 排除在外即可。
```
spring.sleuth.async.ignored-beans=catalogWatchTaskScheduler
```
再查看日志，配置成功
```
o.s.c.s.i.a.ExecutorBeanPostProcessor    : Not instrumenting bean catalogWatchTaskScheduler
```

* Intellij Idea 远程调试 Docker 容器

`Run/Debug Configurations` 下新增 `Remote` 配置，`Debugger mode` 选 `Listen to remote JVM`，开始调试后调试器会监听localhost:5005。要配置Docker应用连接本地调试器，在 `document/deploy/.env` 里添加以下JVM参数
```
-agentlib:jdwp=transport=dt_socket,server=n,address=host.docker.internal:5005,suspend=y
```
`address` 修改为容器可以访问到的主机地址，然后利用 `document/deploy/compose-app.yml` 重新部署。部署成功后可以在调试器终端看到连接成功的日志。

* 如何查看Java容器的JVM参数

在 `.env` 文件中 `APP_JVM_FLAGS` 变量后面加上 `-XX:+PrintFlagsFinal`
