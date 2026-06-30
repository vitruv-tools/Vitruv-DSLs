# vitruvocl

A domain-specific language for cross-metamodel OCL constraint evaluation in the Vitruvius framework.

## Overview

vitruvocl implements OCL# semantics for type-safe constraint evaluation across multiple EMF metamodels. It features a three-pass compiler architecture with smart metamodel loading, type checking, and support for cross-metamodel references.

**Key Features:**
- **OCL# Semantics**: Null-safe, everything-is-a-collection value model
- **Cross-Metamodel Constraints**: Reference entities across different metamodels in one expression
- **Type Safety**: Full static type checking before evaluation
- **EMF Integration**: Native support for Ecore metamodels and XMI instances
- **Vitruvius Framework Integration**: First-class VSUM support via `VSUMWrapper`
- **Language Server**: LSP-based language server with diagnostics, completion, hover, and signature help
- **VSCode Extension**: Syntax highlighting, real-time error reporting, constraint explorer

Based on: Steinmann, F., Clarisó, R., Gogolla, M. (2025). ["Meet OCL#, a relational object constraint language"](https://link.springer.com/article/10.1007/s10270-025-01286-1).
Uses: [Vitruvius Framework](https://github.com/vitruv-tools)

---

## Quick Start

### VS Code Extension (Recommended)

The extension provides syntax highlighting, real-time diagnostics, code completion, and hover documentation.

1. Navigate to `vitruvocl-vscode-extension/` in the repository.
2. Locate the latest `.vsix` file.
3. In VS Code, open the **Extensions** view (`Ctrl+Shift+X`).
4. Click `···` → **Install from VSIX...** and select the file.

### Java Library (API)

To use vitruvocl in your own Java project, build and install it:

```bash
# Clone the parent repository
git clone https://github.com/vitruv-tools/Vitruv-DSLs.git
cd Vitruv-DSLs/vitruvocl

# Build everything (JARs + VSCode extension)
mvn clean package
```

The compiler JAR is then at `language/target/vitruvocl.jar`.

Add it to your `pom.xml`:

```xml
<dependency>
    <groupId>tools.vitruv.dsls</groupId>
    <artifactId>vitruvocl-language</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

See [Methodologist-Template](https://github.com/vitruv-tools/Methodologist-Template/tree/vitruviusOCL) for complete working examples.

---

## Syntax

vitruvocl extends standard OCL with cross-metamodel support and follows OCL# semantics.

### Key Differences from Standard OCL

| Standard OCL | vitruvocl |
|---|---|
| `->` for collection navigation | `.` for all navigation |
| `<>` for inequality | `!=` |
| Null concept | Empty collection `[]` |
| 0-based indexing | 1-based indexing |

### Constraint Format

```ocl
-- Comments start with double dash
context MetamodelName::ClassName inv invariantName:
  expression
```

The invariant name is optional:

```ocl
context spaceMission::Spacecraft inv:
  self.mass > 0
```

### Constraint Annotations

Annotations appear between the `:` and the constraint body. Each annotation is optional and may appear at most once per constraint.

```ocl
context spaceMission::Spacecraft inv massCheck:
  @severity WARNING
  @message "Spacecraft mass must be positive"
  self.mass > 0
```

**`@severity`** — sets the severity level reported when the constraint is violated.

| Value | Meaning |
|---|---|
| `CRITICAL` | Blocker-level violation |
| `WARNING` | Default if omitted |
| `MAJOR` | Significant issue |
| `MINOR` | Minor issue |
| `INFO` | Informational only |

**`@message`** — custom violation message (string literal). If omitted, a default message is generated.

### Supported Operations

**Collection:**
- `select(x | cond)`, `reject(x | cond)`, `collect(x | expr)`
- `forAll(x | cond)`, `exists(x | cond)`
- `size()`, `isEmpty()`, `notEmpty()`
- `includes(v)`, `excludes(v)`, `including(v)`, `excluding(v)`
- `union(c)`, `intersection(c)`, `append(v)`, `flatten()`
- `first()`, `last()`, `reverse()`, `at(i)` (1-based)
- `sum()`, `avg()`, `min()`, `max()`
- `allInstances()` — all EMF instances of a type

**Arithmetic:** `+`, `-`, `*`, `/`, `%`, unary `-`

**Math:** `abs()`, `floor()`, `ceil()`, `round()`

**Comparison:** `<`, `<=`, `>`, `>=`, `==`, `!=`

**Boolean:** `and`, `or`, `xor`, `not`, `implies`

**String:** `concat(s)`, `size()`, `length()`, `toUpper()`, `toLower()`,
`substring(start, end)`, `indexOf(s)`, `equalsIgnoreCase(s)`

**Control Flow:**
```ocl
if condition then expr1 else expr2 endif
let x = expr in body
let x = 1, y = 2 in x + y    -- multiple bindings
```

**Type Operations:**
```ocl
oclIsKindOf(pkg::Type)   -- isinstance check
oclIsTypeOf(pkg::Type)   -- exact type check
oclAsType(pkg::Type)     -- cast
```

### Cross-Metamodel Constraints

Reference types from multiple metamodels in one expression using fully qualified names:

```ocl
context spaceMission::Spacecraft inv:
  satelliteSystem::Satellite.allInstances()
    .collect(sat | sat.massKg)
    .sum() > self.mass
```

### Correspondence Operator (`~`)

The `~` operator checks whether two objects are related by a Vitruvius correspondence. It always refers to `self` on one side and is used inside collection operations:

| Syntax | Meaning |
|---|---|
| `Collection.select(~)` | Elements that correspond to `self` |
| `Collection.reject(~)` | Elements that do NOT correspond to `self` |
| `Collection.exists(~)` | True if any element corresponds to `self` |
| `Collection.select(~, Tag = "x")` | Filter by correspondence tag |
| `Collection.select(~, Type = pkg::Class)` | Filter by type of corresponding object |
| `Collection.select(~, Tag = "x", Type = pkg::Class)` | Combined tag + type filter |

Requires a `correspondence.ecore` metamodel and at least one `.correspondence` XMI file.

```ocl
context family::Member inv:
  self.firstName == "Homer" implies
    persons::Person.allInstances().select(~, Tag = "Husband").notEmpty()
```

---

## Building from Source

### Prerequisites

- Java 17+
- Maven 3.6+
- Node.js + npm (for VSCode extension packaging)

### Full Build

```bash
cd Vitruv-DSLs/vitruvocl
mvn clean package
```

This builds all three modules in order:
1. `language/` → `language/target/vitruvocl.jar` (compiler + evaluator)
2. `vitruvocl-language-server/` → `vitruvocl-language-server/target/language-server.jar`
3. `vitruvocl-vscode-extension/` → copies both JARs into `lib/`, then runs `vsce package` → `.vsix`

### Build without VSCode Extension

```bash
mvn clean package -pl language,vitruvocl-language-server
```

### Run Tests

```bash
mvn clean test
```

Coverage report: `language/target/site/jacoco/index.html`

---

## Java API

```java
import tools.vitruv.dsls.vitruvocl.pipeline.VitruvOCLCompiler;
import tools.vitruv.dsls.vitruvocl.pipeline.BatchValidationResult;
import java.nio.file.Path;

// Evaluate all constraints in a project directory
BatchValidationResult result = VitruvOCLCompiler.evaluateProject(Path.of("."));

System.out.println("Satisfied: " + result.getSatisfiedCount());
System.out.println("Violated:  " + result.getViolatedCount());
```

See `tools.vitruv.dsls.vitruvocl.pipeline.VitruvOCLCompiler` for the full API.

---

## Architecture

See [ARCHITECTURE.md](ARCHITECTURE.md) for the detailed design documentation.

---

## License

Eclipse Public License 2.0 — see [LICENSE](LICENSE).

See [NOTICE](NOTICE) for third-party dependency licenses.

## Acknowledgments

- Grammar derived from DeepOCL implementation by Ralph Gerbig and Arne Lange (University of Mannheim)
- Based on OCL# semantics by Steinmann, F., Clarisó, R., Gogolla, M.
