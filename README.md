# Embabel Common

Foundational libraries and utilities for Embabel platform development.

## Overview

The Embabel Common repository provides core components, utilities, and shared dependencies used across Embabel's microservices and applications. It serves as the foundation upon which specific domain services are built.

## Modules

- **embabel-common-dependencies**: Dependency management (BOM) for maintaining consistent library versions across all Embabel projects
- **embabel-common-util**: Core utilities and domain primitives shared across all services

### Using in Your Project

Add the following to your `pom.xml`:

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

## License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.

---

© 2025 Embabel Software, Inc.
