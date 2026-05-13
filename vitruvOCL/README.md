# VitruvOCL

A domain-specific language for cross-metamodel OCL constraint evaluation in the Vitruvius framework.

## Overview

VitruvOCL implements OCL# semantics for type-safe constraint evaluation across multiple EMF metamodels. It features a three-pass compiler architecture with smart metamodel loading, type checking, and support for cross-metamodel references.

**Key Features:**
- **OCL# Semantics**: Null-safe
- **Cross-Metamodel Constraints**: Reference entities across different metamodels
- **Type Safety**: Full static type checking before evaluation
- **EMF Integration**: Native support for Ecore metamodels and XMI instances
- **Vitruvius Framework Integration**: Native support for Ecore metamodels and XMI instances

Based on: Steinmann, F., Clarisó, R., Gogolla, M. (2025). ["Meet OCL{^\sharp }, a relational object constraint language"](https://link.springer.com/article/10.1007/s10270-025-01286-1).
Uses: [Vitruvius Framweork](https://github.com/vitruv-tools)

## Quick Start

### Download

VitruvOCL consists of a Java-based language engine and a VS Code extension for development support.

### VS Code Extension (Recommended)
The extension provides syntax highlighting, real-time error reporting, and Language Server support.

1. Navigate to the `vitruvocl-vscode-extension/` folder in the repository.
2. Locate the latest `.vsix` file.
3. In VS Code, go to the **Extensions** view (`Ctrl+Shift+X`).
4. Click the three dots (···) in the top right and select **Install from VSIX...**.
5. Select the downloaded `.vsix` file.

### Java Library (API)
To use VitruvOCL in your own Java project (e.g., to integrate it with a VSUM), you need to install the library to your local Maven repository:

1. Clone the repository:
   ```bash
   git clone [https://github.com/vitruv-tools/Vitruv-DSLs.git](https://github.com/vitruv-tools/Vitruv-DSLs.git)
Navigate to the language engine:

Bash
cd Vitruv-DSLs/vitruvOCL/language
Build and install locally:

Bash
mvn clean install


You can then add it to your `pom.xml`:
```xml
<dependency>
      <groupId>tools.vitruv</groupId>
      <artifactId>vitruvOCL</artifactId>
      <version>1.0.0</version>
</dependency>


### Example Constraints

**constraints.ocl:**
```ocl
-- Simple constraint
context spaceMission::Spacecraft inv:
  self.mass > 0

-- Cross-metamodel constraint
context spaceMission::Spacecraft inv:
  satelliteSystem::Satellite.allInstances().collect(sat |
    sat.massKg
  ).sum() > self.mass
```

**Java API:**
```java
import tools.vitruv.dsls.vitruvOCL.pipeline.*;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws Exception {
        // Evaluate project
        BatchValidationResult result = VitruvOCL.evaluateProject(
            Path.of(".")
        );
        
        System.out.println("Satisfied: " + result.getSatisfiedCount());
        System.out.println("Violated: " + result.getViolatedCount());
    }
}
```
See [Methodologist-Template](https://github.com/vitruv-tools/Methodologist-Template/tree/vitruviusOCL) for complete working examples.

## Syntax

VitruvOCL extends standard OCL with cross-metamodel support and follows OCL# semantics.

### Key Differences from Standard OCL

- **Unified dot notation**: Use `.` for all navigation (no `->` operator)
- **Inequality operator**: Use `!=` instead of `<>`
- **Everything is a collection**: Single values are singletons `[5]`, null is empty `[]`
- **1-based indexing**: Collections start at index 1
- **Fully qualified names**: `metamodelName::ClassName`

### Constraint Format
```ocl
-- Comments start with double dash
context MetamodelName::ClassName inv:
  expression
```
### Supported Operations

**Collection Operations:**
- `select(iterator | condition)` - Filter elements
- `reject(iterator | condition)` - Exclude elements
- `collect(iterator | expression)` - Transform elements
- `forAll(iterator | condition)` - Universal quantification
- `exists(iterator | condition)` - Existential quantification
- `size()`, `isEmpty()`, `notEmpty()`
- `includes(value)`, `excludes(value)`
- `including(value)`, `excluding(value)` - Add/remove elements
- `union(collection)`, `append(value)` - Combine collections
- `flatten()` - Flatten nested collections
- `sum()`, `avg()`, `min()`, `max()` - Numeric aggregations
- `abs()`, `floor()`, `ceil()`, `round()` - Numeric operations
- `first()`, `last()`, `reverse()` - Sequence operations
- `at(index)` - Access by index (1-based)
- `lift()` - Lift operation
- `allInstances()` - Get all instances of a type

**Arithmetic:**
- `+`, `-`, `*`, `/`, `%`
- Unary minus: `-expression`

**Comparison:**
- `<`, `<=`, `>`, `>=`, `==`, `!=`

**Boolean:**
- `and`, `or`, `xor`, `not`, `implies`

**String:**
- `concat(string)`, `size()`
- `toUpper()`, `toLower()`
- `substring(start, end)`
- `indexOf(substring)`
- `equalsIgnoreCase(string)`
- `length()`

**Control Flow:**
- `if condition then expr1 else expr2 endif`
- `let variable = expression in body`
- Multiple variables: `let x = 1, y = 2 in x + y`

**Type Checking:**
- `oclIsKindOf(Type)` - Check if instance is kind of type
- `oclIsTypeOf(Type)` - Check exact type
- `oclAsType(Type)` - Cast to type

**Correspondence Operator (`~`):**

The `~` operator checks whether two objects are related by a Vitruvius correspondence. It is used as a shorthand predicate inside collection operations — it always refers to `self` on one side:

| Syntax | Meaning |
|--------|---------|
| `Collection.select(~)` | Keep elements that correspond to `self` |
| `Collection.reject(~)` | Remove elements that correspond to `self` |
| `Collection.exists(~)` | True if any element corresponds to `self` |
| `Collection.select(~, Tag = "x")` | Filter by correspondence tag |
| `Collection.select(~, Type = pkg::Class)` | Filter by concrete type of the corresponding object |
| `Collection.select(~, Tag = "x", Type = pkg::Class)` | Combined tag + type filter |

Requires a `correspondence.ecore` metamodel and at least one `.correspondence` instance file to be loaded alongside the domain metamodels.

**Example:**
```ocl
-- Homer has exactly one corresponding Person
context family::Member inv:
  self.firstName == "Homer" implies
    persons::Person.allInstances().select(~).size() == 1

-- Only Husband correspondences
context family::Member inv:
  self.firstName == "Homer" implies
    persons::Person.allInstances().select(~, Tag = "Husband").notEmpty()

-- Filter by type and tag
context family::Member inv:
  persons::Person.allInstances()
    .select(~, Type = persons::Male, Tag = "Husband").size() == 1
```

## Architecture

VitruvOCL uses a three-pass compiler architecture:

1. **Pass 1 - Symbol Table Construction**: Builds scope hierarchy, registers variables
2. **Pass 2 - Type Checking**: Validates operations, produces type annotations
3. **Pass 3 - Evaluation**: Executes constraints against EMF model instances

See [ARCHITECTURE.md](ARCHITECTURE.md) for detailed design documentation.

## Building from Source

### Prerequisites

- Java 17+
- Maven 3.6+

### Build
```bash
mvn clean package
```

JAR will be in `language/target/vitruvOCL.jar`

### Run Tests
```bash
mvn clean test
```

Test coverage report: `language/target/site/jacoco/index.html`

## API Documentation

Full JavaDoc available in `tools.vitruv.dsls.vitruvOCL.pipeline.VitruvOCL` class.

### Main API Methods

**Project-based evaluation:**
```java
BatchValidationResult evaluateProject(Path projectDir)
```

**Single constraint:**
```java
ConstraintResult evaluateConstraint(
    String constraint, 
    Path[] ecoreFiles, 
    Path[] xmiFiles
)
```

**Multiple constraints from file:**
```java
BatchValidationResult evaluateConstraints(
    Path constraintsFile,
    Path[] ecoreFiles,
    Path[] xmiFiles
)
```

## License

This project is licensed under the Eclipse Public License 2.0 - see [LICENSE](LICENSE) file for details.

### Third-Party Licenses

See [NOTICE](NOTICE) for information about third-party dependencies and their licenses.


## Acknowledgments

- Grammar derived from DeepOCL implementation by Ralph Gerbig and Arne Lange (University of Mannheim)
- Based on OCL# semantics by Steinmann, F., Clarisó, R., Gogolla, M.