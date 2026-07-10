# vitruvocl Architecture

## Module Structure

vitruvocl is a Maven multi-module project with three modules, built in order:

```
vitruvocl/                          (root aggregator POM)
├── language/                       → vitruvocl.jar (compiler + evaluator)
├── vitruvocl-language-server/      → language-server.jar (LSP server)
└── vitruvocl-vscode-extension/     → vitruvocl-0.0.1.vsix (editor extension)
```

Dependencies flow in one direction only:
```
vscode-extension → language-server → language
```

---

## Compiler Pipeline (language/)

The compiler runs in three sequential passes over the ANTLR parse tree:

```
Source text
    │
    ▼
[ANTLR4 Lexer + Parser]  →  Parse Tree (VitruvOCLParser)
    │
    ▼
[Pass 1 — SymbolTableBuilder]
    Visits: let-expressions, iterator variables
    Produces: SymbolTable (GlobalScope, LocalScope hierarchy)
    │
    ▼
[Pass 2 — TypeCheckVisitor]
    Visits: all expressions
    Input:  parse tree + symbol table
    Produces: ParseTreeProperty<Type> (nodeTypes map)
    Validates: operations, iterator types, attribute access, annotation syntax
    Reports: type errors via ErrorCollector
    │
    ▼
[Pass 3 — EvaluationVisitor]
    Visits: all expressions
    Input:  parse tree + nodeTypes + EMF model instances
    Produces: Value (collection of OCLElement) per constraint
    Executes: constraints against EObjects from MetamodelWrapper
```

Orchestrated by `VitruvOCLCompiler` in the `pipeline` package.

---

## Package Overview (language/src/main/java)

```
tools.vitruv.dsls.vitruvocl/
├── common/          Shared infrastructure
│   ├── AbstractPhaseVisitor   Base class for all three pass visitors
│   ├── ErrorCollector         Accumulates CompileErrors across passes
│   ├── CompileError           Single error with location + severity
│   └── ErrorSeverity          ERROR / WARNING enum
│
├── symboltable/     Pass 1
│   ├── SymbolTableBuilder     ANTLR visitor, builds scope tree
│   ├── SymbolTable            Scope lookup facade
│   ├── GlobalScope / LocalScope
│   └── ScopeAnnotator         Writes scope annotations onto parse tree nodes
│                              (read by Pass 2 to resolve iterator variables)
│
├── typechecker/     Pass 2
│   ├── TypeCheckVisitor       Core type checking visitor (~6400 LOC)
│   ├── TypeResolver           Shared type resolution logic (reused by evaluator)
│   └── Type                   OCL# type system
│                              Primitive: INTEGER, STRING, BOOLEAN, DOUBLE, FLOAT
│                              Collection: set(T), sequence(T), bag(T), orderedSet(T)
│                              Special: ANY, ERROR, optional(T) for null-safe ops
│
├── evaluator/       Pass 3
│   ├── EvaluationVisitor      Runtime evaluation visitor (~4400 LOC)
│   ├── Value                  Runtime value — wraps List<OCLElement>
│   │                          Implements Comparable<Value> (element-wise ordering)
│   ├── OCLElement             Sealed hierarchy of runtime element types:
│   │                          IntValue, StringValue, BoolValue, DoubleValue,
│   │                          FloatValue, EnumValue, ObjectRef, MetaclassValue,
│   │                          NestedCollection, CastedMetaclassValue
│   └── ViolationRecord        Single constraint violation with location + message
│
├── pipeline/        Public API + orchestration
│   ├── VitruvOCLCompiler      Main entry point
│   ├── MetamodelWrapper       EMF model access (loads .ecore + .xmi files)
│   ├── MetamodelWrapperInterface  Abstraction over MetamodelWrapper / VsumWrapper
│   ├── VsumWrapper            Vitruvius VSUM integration
│   ├── BatchValidationResult  Aggregate result for multiple constraints/files
│   ├── ConstraintResult       Result for a single constraint
│   ├── FileValidator          Validates one .ocl file against a model set
│   ├── DependencyAnalyzer     Determines which metamodels a constraint file uses
│   ├── FileError              Non-constraint errors (missing files, parse failures)
│   └── Warning                Non-fatal issues
│
└── cli/
    └── VitruvOclCli           Command-line interface
```

## Language Server (vitruvocl-language-server/)

LSP4J-based language server. Entry point: `LspMain`. Registered providers:

`DocumentAnalyzer`: Runs the full compiler pipeline on open documents, produces LSP `Diagnostic` list
`CompletionProvider`: Context-sensitive completion: packages, classes, `inv` keyword, features, `@severity` levels, annotation keywords, collection literals
`HoverProvider`. Hover documentation for OCL operations via `OclOperationDocs`
`SignatureHelpProvider`: Parameter hints for OCL operations (e.g., `select`, `substring`)
`SymbolProvider`: Document symbols (invariant names + ranges) for outline view
`InlayHintProvider`: Inlay hints for inferred types
`DefinitionProvider`: Go-to-definition for metamodel types
`NodeFinder`: Maps cursor position to ANTLR parse tree node

The server communicates with the VSCode extension over stdio via LSP4J's `Launcher`.

---

## VSCode Extension (vitruvocl-vscode-extension/)

TypeScript extension wrapping the language server. Provides:
- Syntax highlighting (TextMate grammar)
- Real-time diagnostics (via LSP)
- Completion, hover, signature help (via LSP)
- Constraint Explorer view (custom tree view)
- Auto-detection of bundled JARs in `lib/`

The extension is built as part of the Maven reactor (`mvn package`):
1. `maven-dependency-plugin` copies `vitruvocl.jar` → `lib/vitruvOCL.jar` and `language-server.jar` → `lib/language-server.jar`
2. `exec-maven-plugin` runs `npm install` then `npm run package` (`vsce package`)

---

## External Dependencies

| Library | Purpose |
|---|---|
| ANTLR4 4.13.2 | Parser generation and parse tree traversal |
| EMF Ecore | Metamodel access (`EPackage`, `EClass`, `EObject`) |
| EMF Ecore XMI | Loading `.ecore` and `.xmi` instance files |
| LSP4J 0.21.2 | Language Server Protocol implementation |
| Gson 2.8.9 | JSON serialization in CLI output |
| Vitruvius VSUM | VSUM integration via `VsumWrapper` |
