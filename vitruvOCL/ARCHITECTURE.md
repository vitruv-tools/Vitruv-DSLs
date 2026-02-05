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

## External Integration
- **ANTLR4**: Parser generation
- **EMF**: Metamodel and instance handling (EPackage, EObject)
- **Vitruvius**: VSUM integration (planned)