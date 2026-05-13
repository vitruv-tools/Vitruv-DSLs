# VitruvOCL Architecture

## Overview
3-pass compiler: Parse → Type Check → Evaluate

## Directory Structure
- `grammar/` - ANTLR4 grammar (VitruvOCL.g4)
- `symboltable/` - Symbol table, scopes, variable bindings
- `typechecker/` - Pass 2: Type checking visitor
- `evaluator/` - Pass 3: Runtime evaluation, Value system
- `pipeline/` - Public API, metamodel loading, compilation orchestration
- `common/` - Shared types, error handling, visitor base classes

## Compilation Pipeline

### Pass 1: Symbol Table Construction
- Visits `let` expressions and iterator variables
- Builds scope hierarchy (GlobalScope, LocalScope)
- No type information yet

### Pass 2: Type Checking
- Input: Parse tree + symbol table
- Output: `nodeTypes` map (ParseTree → Type)
- Validates: operations, iterators, attribute access
- Reports type errors

### Pass 3: Evaluation
- Input: Parse tree + nodeTypes + model instances
- Output: `Value` (collection of `OCLElement`)
- Executes constraints against EMF models
- Uses metamodel wrapper for EObject access

## Key Design Patterns

**Visitor Pattern**: TypeCheckVisitor, EvaluationVisitor extend AbstractPhaseVisitor
**Type System**: Unified Type hierarchy (IntType, StringType, CollectionType, etc.)
**Value System**: Value wraps List<OCLElement>, everything is a collection
**Smart Loading**: Only loads metamodels referenced in constraints

## Correspondence Operator

The `~` operator is resolved during Pass 3 (Evaluation) via `MetamodelWrapper.getCorrespondingObjects()`. It reads loaded `.correspondence` XMI files and matches `leftEObjects`/`rightEObjects` references to determine if two objects are in the same correspondence.

Supported forms in collection operations (`select`, `reject`, `exists`):
- `select(~)` — objects corresponding to `self` (any tag/type)
- `select(~, Tag = "x")` — filter by `tag` attribute on the correspondence
- `select(~, Type = pkg::Class)` — filter by concrete EClass of the corresponding object
- `select(~, Tag = "x", Type = pkg::Class)` — combined filter

## External Integration
- **ANTLR4**: Parser generation
- **EMF**: Metamodel and instance handling (EPackage, EObject)
- **Vitruvius VSUM**: Integration via `VSUMWrapper` — wraps a `VirtualModel` and exposes its view-source models as `MetamodelWrapperInterface`; registered via `VitruvOCL.registerVSUM()`