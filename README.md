![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![Apache Maven](https://img.shields.io/badge/Apache%20Maven-C71A36?style=for-the-badge&logo=Apache%20Maven&logoColor=white)
![ChatGPT](https://img.shields.io/badge/chatGPT-74aa9c?style=for-the-badge&logo=openai&logoColor=white)
![Jinja](https://img.shields.io/badge/jinja-white.svg?style=for-the-badge&logo=jinja&logoColor=black)
![JSON](https://img.shields.io/badge/JSON-000?logo=json&logoColor=fff)
![GitHub Actions](https://img.shields.io/badge/github%20actions-%232671E5.svg?style=for-the-badge&logo=githubactions&logoColor=white)
![SonarQube](https://img.shields.io/badge/SonarQube-black?style=for-the-badge&logo=sonarqube&logoColor=4E9BCD)
![IntelliJ IDEA](https://img.shields.io/badge/IntelliJIDEA-000000.svg?style=for-the-badge&logo=intellij-idea&logoColor=white)

<img align="left" src="https://github.com/embabel/agent-api/blob/main/images/315px-Meister_der_Weltenchronik_001.jpg?raw=true" width="180">

&nbsp;&nbsp;&nbsp;&nbsp;

# Embabel Common

Foundational libraries and utilities for Embabel platform development.

## Overview

The Embabel Common repository provides core components, utilities, and shared dependencies used across Embabel's microservices and applications. It serves as the foundation upon which specific domain services are built.

## Modules

- **embabel-common-dependencies**: Dependency management (BOM) for maintaining consistent library versions across all Embabel projects
- **embabel-common-util**: Core utilities and domain primitives shared across all services

### Using in Your Project

Add Embabel Common BOM to your `pom.xml`:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.embabel.common</groupId>
            <artifactId>embabel-common-dependencies</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```
Add module(s) of interest as dependency to your `pom.xml`

```xml
<dependencies>
    <dependency>
        <groupId>com.embabel.common</groupId>
        <artifactId>embabel-common-core</artifactId>
    </dependency>

    <dependency>
        <groupId>com.embabel.common</groupId>
        <artifactId>embabel-common-util</artifactId>
    </dependency>

    <dependency>
        <groupId>com.embabel.common</groupId>
        <artifactId>embabel-common-ai</artifactId>
    </dependency>

    <dependency>
        <groupId>com.embabel.common</groupId>
        <artifactId>embabel-common-textio</artifactId>
    </dependency>
</dependencies>
```

## Repository

Binary Packages are located in github repository

```xml
<repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/embabel/embabel-common</url>
    <snapshots>
         <enabled>true</enabled>
    </snapshots>
</repository>
```

## License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.

---

© 2025 Embabel Software, Inc.
