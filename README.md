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
