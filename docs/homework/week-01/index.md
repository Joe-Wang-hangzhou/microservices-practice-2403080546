## 环境检查
➜  ~ java --version
openjdk 21.0.12.1 2026-08-18 LTS
OpenJDK Runtime Environment Microsoft-14941480 (build 21.0.12.1+1-LTS)
OpenJDK 64-Bit Server VM Microsoft-14941480 (build 21.0.12.1+1-LTS, mixed mode, sharing)
➜  ~ mvn --version
Apache Maven 3.9.4 (dfbb324ad4a7c8fb0bf182e6d91b0ae20e3d2dd9)
Maven home: /Users/Zhuanz/Java_BackEnd_Learning/Tools/apache-maven-3.9.4
Java version: 21.0.12.1, vendor: Microsoft, runtime: /Users/Zhuanz/Library/Java/JavaVirtualMachines/ms-21.0.12.1/Contents/Home
Default locale: en_US, platform encoding: UTF-8
OS name: "mac os x", version: "27.0", arch: "aarch64", family: "mac"
➜  ~ git --version
git version 2.54.0 (Apple Git-157)
➜  ~ docker --version
Docker version 28.4.0, build d8eb465
➜  ~ docker compose version
Docker Compose version v2.39.2-desktop.1

## 概念回答

1. 什么是微服务架构？
微服务架构是一种将大型应用按照业务功能拆分成多个独立服务的设计方式。每个服务可以独立开发、部署和扩展，通过接口进行通信，提高系统的灵活性和可维护性。

2. 微服务和单体架构的主要区别是什么？
单体架构将所有功能集中在一个项目中，而微服务将系统拆分成多个独立服务。微服务支持独立部署和扩展，但会增加服务通信、管理和维护的复杂度。

3. 为什么本课程先实现单体系统，再逐步拆分为微服务？
先实现单体系统可以帮助理解完整业务流程和模块关系，再根据实际问题进行拆分。这样能理解微服务解决的问题，而不是为了使用微服务而拆分。

4. 为什么作业需要提供可重复运行的测试或验证脚本？
测试脚本可以快速验证系统功能，减少人工操作，提高测试效率。同时方便其他人复现运行结果，保证代码修改后系统仍然能够正常工作。


## 问题记录
