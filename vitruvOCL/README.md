# VitruvOCL

A type-safe OCL variant for cross-metamodel consistency checking in the Vitruvius framework.

## About

VitruvOCL is a domain-specific language based on OCL (Object Constraint Language) designed for checking constraints across multiple metamodels.

## Installation

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Build from source

```bash
git clone <repository-url>
cd vitruvocl
mvn clean install
```

## Architecture

The compiler uses a 3-pass architecture:

1. **Symbol Table Construction**: Builds type system from metamodels
2. **Type Checking**: Validates all expressions and operations
3. **Evaluation**: Executes constraint against model instances

Built with:
- ANTLR4 for parsing
- Java 17
- Eclipse Modeling Framework (EMF)
- Vitruvius framework integration
