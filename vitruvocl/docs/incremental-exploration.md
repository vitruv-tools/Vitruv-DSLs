# Exploration: Inkrementelle Constraint-Auswertung für VitruviusOCL

Stand: 2026-10-11 · Branch `vitruviusocl_parallel` (HEAD `39476b07`) · reine Exploration, kein Code geändert.

Legende für Belege: **[Code]** = im Code verifiziert · **[Paper n]** = aus Paper n · **[Annahme]** = eigene Annahme bzw. offen.
Pfade relativ zu `language/src/main/java/tools/vitruv/dsls/vitruvocl/`, sofern nicht anders angegeben.

---

## 0. Abweichungen vom Auftrag (zuerst)

1. **Branch-Namen:** Es gibt weder `optimized` noch `original`. Die Optimierungen liegen auf `vitruviusocl_parallel` (lokal und `origin`). Die Baseline ist vermutlich `main` bzw. `vitruvocl`. **[Code]** `git branch -a`
2. **„`allInstances()`-Caching“ cacht keine Instanzen.** Gecacht wird nur die *Analyse* (Call-Sites, Containment-Erreichbarkeit). Die Instanzmenge wird bei jedem Aufruf neu traversiert („compute() is always invoked against a freshly-read root list“). **[Code]** `pipeline/VsumWrapper.java:168` (`getAllInstances`), `:185` (`obtainEngine`); `vitruvocl-allinstances/.../AllInstancesEngine.java:77` (`compute`). Folge: Für den Cache ist heute keine Invalidierung durch Create/Delete nötig, siehe Frage 6.
3. **Parallelisierung arbeitet pro Constraint-Text, nicht pro (Constraint, Instanz).** Jede Task parst, typprüft und wertet *einen* Constraint-String vollständig aus. **[Code]** `pipeline/ConstraintListEvaluator.java:55-83`, `pipeline/VitruvOCL.java:394` → `compileAndEvaluate` (`:479`) → `new VitruvOCLCompiler(...)` (`:491`) → Parser neu (`pipeline/VitruvOCLCompiler.java:166`).
4. **Kein Transaktions-Hook in VitruvOCL.** Weder CLI noch VS-Code-Extension liefern eine Transaktion. Die Extension sagt das ausdrücklich: „no transaction context available“ (`vitruvocl-vscode-extension/src/extension.ts:466`). `VitruvOclCli.java` enthält keine Transaktions-/EChange-Logik. Transaktionen kommen nur über die API (`List<EChange<EObject>>`); in Tests werden sie per `ChangeRecorder` erzeugt. **[Code]** `test/.../vsum/VsumTransactionConstraintTest.java:115-124`. Commit `39476b07` („wire it through the CLI“) betrifft also nur die Skip-Hinweise.
5. **Paper-Zitate vertauscht:** **Def. 3.3.1** (`and`-Pruning) und **Prop. 4.2** (Vereinigung relevanter Instanzen) stehen in **Paper 2** (Cabot & Teniente 2006), nicht in Paper 3. **Abschnitt 5.5** („Execution Context“ mit Event-Simplification) steht in **Paper 3** (Fernández-Álvarez 2016), nicht in Paper 2 (Paper 2 hat keinen Abschnitt 5.5). **[Paper 2]** Abschn. 3.3 und 4, **[Paper 3]** Abschn. 5.5.
6. **Testzahl:** Statisch gezählt gibt es **2006** `@Test`/`@ParameterizedTest`-Annotationen in `language/src/test`. Die „2400+“ sind ohne Testlauf nicht verifiziert; parametrisierte Fälle können die Zahl erhöhen. **[Code]**
7. **Papers liegen nicht in `docs/papers/`.** Gelesen aus `C:\Users\maxoe\Downloads\` (Paper 1: `1-s2.0-S0164121209000508-main.pdf`, Paper 2: `Incremental_Evaluation_of_OCL_Constraints.pdf`, Paper 3: `978-3-319-30142-6_8.pdf`, Paper 4: `3837062.3839069.pdf`).
8. **Zeitbudget:** Heute ist der 11.10.2026. Bis „Mitte Oktober“ bleiben also **etwa 4–5 Arbeitstage**. Das verschärft die Empfehlung (siehe Frage 11).

## Kurzfassung

- **Machbar und sinnvoll:** eine **konservative PSE-Filterung pro Constraint** (Stufen 1/2) zusammen mit einem **Change-Listener** auf `ChangeableModelRepository.addChangePropagationListener`. Der ParseTree samt `nodeTypes` des TypeCheckers liefert alles, was zur Laufzeit-unabhängigen Ableitung der Lesemenge (Typ, Feature) nötig ist.
- **Größtes Risiko für den erhofften Boost:** Die Benchmark-Suite (Tier B, T3) nutzt fast überall `X.allInstances().select(~)`. Nach Paper 1 sind das **Class Events**: Relevante Instanzen bringen dort nichts, und jedes Create/Delete eines beteiligten Typs löst eine Vollauswertung aus. Den Effekt liefert dort nur der Constraint-Filter (Stufe 1/2), und auch nur, wenn eine Transaktion die beteiligten Typen gar nicht berührt.
- **Undefiniert-Semantik:** Der Evaluator ist zweiwertig mit „leer“ als drittem Zustand (`and`/`or` → `false`, `implies` → `true`, `not` → leer; ein leeres Ergebnis wird **weder** als Verletzung noch als Erfüllung verbucht). Pruning bleibt trotzdem sicher, weil es nur Determinismus voraussetzt (Frage 4).
- **Reverse-Index:** In keinem Ecore von VitruvOCL und im Graph-Benchmark gibt es `eOpposite` (0 von 70 Referenzen). Ohne eigenen Index sind relevante Instanzen nur über `eContainer()` und `~` (symmetrisch) möglich.
- **Empfehlung:** Bis Mitte Oktober **Stufe 1** (Feature-/Typ-Filter, polaritätsfrei), **Violation-Set** und **Differential-Tests** umsetzen; Stufe 2 (Polarität) nur, wenn Stufe 1 am 2. Tag grün ist. Relevante Instanzen (Stufe 3) ins **Future Work** verschieben, höchstens als Prototyp für Self-/Containment-Pfade.

---

## Frage 1: Codebasis kartieren

**Befund**

| Baustein | Ort | Belege |
|---|---|---|
| Grammatik / AST | ANTLR4, ParseTree als AST (kein eigenes AST-Modell) | `language/src/main/antlr4/.../VitruvOCL.g4` (520 Zeilen) |
| Pipeline-Phasen | Parse → SymbolTable → TypeCheck (`nodeTypes`) → Evaluation | `pipeline/VitruvOCLCompiler.java:144-238`, `typechecker/TypeCheckVisitor.java:148` |
| Evaluator | `EvaluationVisitor` (5127 Zeilen) | `evaluator/EvaluationVisitor.java` |
| Kontext-Iteration | `visitClassifierContextCS`: **holt immer die komplette Extension** des Kontexttyps | `EvaluationVisitor.java:314` (`specification.getAllInstances(eClass)`) |
| Ergebnisverbuchung | `recordConstraintResult`; leer → kein Eintrag | `EvaluationVisitor.java:432-449` |
| Einstieg Batch | `VitruvOCL.evaluateConstraints(...)` (viele Überladungen) → privat `:371` | `pipeline/VitruvOCL.java:181-412` |
| Parallelität | `ConstraintListEvaluator.evaluate`, `newFixedThreadPool`, eine Task pro Constraint | `pipeline/ConstraintListEvaluator.java:55-83` |
| Default-Threads | `availableProcessors()` | `pipeline/VitruvOCL.java` (Feld `DEFAULT_THREAD_POOL_SIZE`) |
| CLI | `eval-batch … --threads N` | `cli/VitruvOclCli.java:53`, `:161-185` |
| Lazy Evaluation | `evaluator/lazy/*`, vier Kategorien (lazy / early-termination / Buffer / strikt) | `evaluator/lazy/package-info.java` |
| allInstances | `AllInstancesEngine` (Wei & Kolovos), Analyse gecacht, Traversierung live | `VsumWrapper.java:168-205`, `MetamodelWrapper.java:600-630` |
| Transaktion | `TransactionModel` über `List<EChange<EObject>>`; Lifecycle-Prädikate und `@pre` | `evaluator/transaction/TransactionModel.java:45-148` |
| Korrespondenz | `VsumWrapper.getCorrespondingObjects` → `CorrespondenceModelView` | `VsumWrapper.java:261-277`; Evaluator `:4217`, `:4963` |

**Transaktionsgrenze und Hook [Code, Vitruvius-JARs]:** VitruvOCL selbst kennt keine Transaktionsgrenze. In Vitruvius gibt es aber:
- `ChangeableModelRepository.addChangePropagationListener(ChangePropagationListener)` sowie `propagateChange(...)` mit Rückgabe `List<PropagatedChange>`,
- `ChangePropagationListener.startedChangePropagation()` / `finishedChangePropagation(Iterable<PropagatedChange>)`,
- `PropagatedChange.getOriginalChange()` / `getConsequentialChanges()`, beide vom Typ `VitruviusChange<EObject>`.

(Geprüft per Bytecode-Signaturen in `~/.m2/.../tools.vitruv.change.composite-4.0.1-SNAPSHOT.jar`.)

**Vorgeschlagener Hook [Annahme]:** In `VsumWrapper` (Konstruktor, `:109`) einen Listener registrieren, der pro `finishedChangePropagation` die EChanges in der Reihenfolge *original, dann consequential* je `PropagatedChange` zu einer Liste konkateniert. Daraus entsteht ein `TransactionDelta`, das an eine neue API `VitruvOCL.evaluateConstraintsIncremental(...)` geht. Die bestehende `transaction`-Parameterkette (`VitruvOCL.java:371ff`) kann dieselbe Liste weiterreichen.

**Offen:**
- Ist eine „Transaktion“ ein `commitChanges()` (ein Listener-Callback) oder eine Folge davon? Der Benchmark-Replayer macht ein `commitChanges()` pro Delta-Schritt (`Measure-GraphGenerator/.../replay/DeltaSequenceReplayer.java:8`).
- Wird `addChangePropagationListener` von der `InternalVirtualModel`-Implementierung exponiert? Laut Bytecode erbt `InternalVirtualModel` von `ChangeableModelRepository`; das ist zu bestätigen.

## Frage 2: EChange-Mapping

**Befund [Code: `tools.vitruv.change.atomic`, Verwendung in `TransactionModel.java:20-27`; Paper 1 Abschn. 2.1]**

| Vitruvius EChange | Paper-1-Event | Bemerkung |
|---|---|---|
| `CreateEObject` | InsertET(T) | Objekt existiert danach meist noch *ohne* Container. Erst `InsertEReference` (Containment) bzw. `InsertRootEObject` hängt es ein. Für `allInstances` (Containment-Traversierung) zählt also das Einhängen. |
| `DeleteEObject` | DeleteET(T) | Folgt in Vitruvius nach dem Aushängen. Hängende Cross-Refs werden nur entfernt, wenn ein `Remove*`-EChange aufgezeichnet wurde. |
| `InsertRootEObject` / `RemoveRootEObject` | InsertET / DeleteET (Extension-Sicht) | `TransactionModel.wasCreated/wasDeleted` behandelt sie schon so (`:58-81`). |
| `ReplaceSingleValuedEAttribute` | UpdateAttribute(T, a) | inkl. set/unset (`null` als old/new) |
| `InsertEAttributeValue` / `RemoveEAttributeValue` | *fehlt in Paper 1* | Mehrwertige Attribute → wie InsertRT/DeleteRT auf Attributebene behandeln |
| `ReplaceSingleValuedEReference` | DeleteRT(old) + InsertRT(new) | Ein Event mit beiden Polaritäten |
| `InsertEReference` / `RemoveEReference` | InsertRT / DeleteRT | inkl. Containment |
| Containment-Verschiebung (`Remove` am alten Container + `Insert` am neuen, ohne Create/Delete) | DeleteRT + InsertRT | Kein Delete/InsertET. Für `allInstances` kann sich die Extension nur ändern, wenn das Objekt den Traversierungsbereich verlässt. **Konservativ:** Containment-Insert/Remove eines Typs T als Insert/DeleteET(T *und alle enthaltenen Typen*) werten. |
| Permutation / Indexverschiebung in geordneten Listen | *fehlt* | Ist bei uns Remove+Insert. Relevant für `first/last/at/indexOf/subSequence` (Paper 1 Tab. 3.3: `±`). |
| SpecializeET / GeneralizeET | — | **Nicht vorhanden**: EMF-Objekte ändern ihre Klasse nie. Entfällt; `oclIsTypeOf/KindOf/oclAsType` hängen nur noch von InsertET/DeleteET ab. |
| Korrespondenz-Änderungen | — | **Offen:** ob Änderungen am Korrespondenzmodell in `PropagatedChange` erscheinen. Siehe Frage 3/5. |

**Offen:** Erzeugt die Reactions-Runtime für Containment-Löschungen bei Cross-Referenzen `RemoveEReference`-Events (etwa via `EcoreUtil.delete`)? Davon hängt ab, ob „DeleteET ⇒ zugehörige DeleteRT beobachtet“ gilt.

## Frage 3: PSE-Berechnung

**Abdeckung durch Paper 1, Tab. 3.1–3.4 [Paper 1, Abschn. 3.1]:** Navigation, Attributzugriff, `self`, `oclAsType/IsTypeOf/IsKindOf/IsUndefined`, `= + − * / < ≤ > ≥ max min floor abs round and or not size concat toInt toReal div mod`, `forAll`, `select`, `iterate`, `sum`, `count`, `collect`, `∪ ∩ product`, Set-Differenz, `symmetricDifference`, `asX`, `flatten`, `at`, `indexOf`, `subSequence`, `allInstances`. Für `not` und `select` gelten Sonderregeln mit Gegen-Events. Weitere Operatoren werden „über Vereinfachungsregeln“ auf diese zurückgeführt (Cabot & Teniente 2007, nicht vorliegend).

**AST-Knoten in VitruvOCL ohne direkte Tabellenzeile [Code: `VitruvOCL.g4`]:**

| Konstrukt (Grammatikregel) | Vorschlag (konservativ) |
|---|---|
| `exists`, `reject`, `one`, `any`, `isUnique`, `sortedBy`, `collectNested` (`iteratorOpCS`) | `exists` ≡ `not forAll(not …)`, `reject` ≡ `select(not …)`. Für `one/any/isUnique/sortedBy`: **beide Polaritäten (`±`) an Quelle und Body**. |
| `includes/excludes/includesAll/excludesAll`, `including/excluding`, `isEmpty/notEmpty`, `first/last/reverse/append/prepend/insertAt`, `avg`, `ceil(ing)`, `lift` | `±` an alle Kinder (Polarität für Stufe 2 später verfeinerbar) |
| String-Ops (`substring`, `toUpper`, `matches`, `tokenize`, …) | `±` an alle Kinder (für Stufe 1 irrelevant, dort zählt nur das gelesene Feature) |
| `if … then … else` (`ifExpCS`), `let` (`letExpCS`), `implies`, `xor` | `±` an alle Kinder; `let`-Variablen wie Iteratorvariablen behandeln (PathVar folgt der Init-Expression) |
| Collection-Literale, Ranges | Blätter, keine PSE |
| `T::C.allInstances()` / `prefixedQualified` | InsertET(C*) und DeleteET(C*) für C und alle Subtypen (Tab. 3.4, Spalte allInstances), immer **Class Event** |
| **Cross-Metamodel-Navigation** | Bei uns nur über `allInstances` eines fremden Metamodells oder über `~`. Normale Features sind metamodell-lokal und werden wie oben behandelt. |
| **Korrespondenz `a ~ b`, `select(~)`, `reject(~)`, `exists(~)` inkl. `Type=`/`Tag=`** (`EvaluationVisitor.java:4181-4226`, `:4963`) | Neues Pseudo-Event **`CorrChanged`**. Solange nicht belegt ist, dass Korrespondenzänderungen im EChange-Strom stehen: **Constraint mit `~` bei *jeder* nicht-leeren Transaktion, die einen der beteiligten Typen (Kontexttyp, `Type=`-Filter, `allInstances`-Typ) berührt, auswerten.** Die Begründung ist eine Annahme: Reactions legen Korrespondenzen in derselben Propagation an, in der sie das Partnerelement erzeugen. Bis zur Bestätigung gilt im Zweifel „immer auswerten“. |
| **`@pre`** (`navigationChainCS: AT_PRE`, `EvaluationVisitor.java:3354`) | Hängt von der Transaktion selbst ab, nicht nur vom Zustand. **Nie überspringen und nie prunen.** Ein Constraint mit `@pre` ist Teil der Transaktionsprüfung, keine Invariante. |
| **`pre`/`post`-Blöcke**, `OCLisNew/Modified/Deleted` (`EvaluationVisitor.java:4367-4418`) | Die Annahme „vorher konsistent“ ist sinnlos, weil sie sich auf ein Zustandspaar beziehen. **Immer voll auswerten.** Optional lassen sie sich auf Instanzen mit `wasCreated/wasModified/wasDeleted` einschränken (Stufe 3). |
| Operation-Kontext mit Parametern (`operationParamListCS`) | Parameter werden leer gebunden (`EvaluationVisitor.java:340-361`), also keine PSE. Den Kontext normal behandeln. |
| Mehrere `specificationCS` pro `inv` | **Achtung:** `evaluateSpecs` gibt nur das **letzte** Ergebnis zurück (`EvaluationVisitor.java:606-612`). Die PSE-Analyse muss trotzdem *alle* Specs abdecken, weil frühere Specs Seiteneffekte auf Lazy-Werte haben könnten. Konservativ: Vereinigung über alle. |
| `unknown*`, `invalid*`, `message (^)` | Fehlerknoten, Constraint schlägt im Compiler fehl. Kein inkrementeller Pfad, immer voll auswerten. |
| Polymorphe Feature-Zugriffe (Feature per Name auf `instance.eClass()` aufgelöst, `EvaluationVisitor.java:3393ff`) | PSE-Schlüssel ist **(EClass-Menge, Feature-Name)**. Den statischen Empfängertyp aus `nodeTypes` nehmen und **alle Subklassen** einschließen. Bei `OclAny`/unbekanntem Typ gilt der Name in *allen* Klassen. |

**Kernvorschlag [Annahme]:** Die PSE-Analyse wird **zweistufig** implementiert:
- **Stufe 1, „Read-Set“ (polaritätsfrei):** Ein Visitor über den typgeprüften ParseTree sammelt (a) alle gelesenen (Typ, Feature)-Paare, (b) alle `allInstances`-Typen, (c) den Kontexttyp, (d) Flags `usesCorrespondence`, `usesPre`, `isPrePost`. Ein Constraint wird übersprungen, wenn **kein** EChange der Transaktion ein Paar aus (a) betrifft, **kein** Create/Delete/Containment-Event einen Typ aus (b) oder (c) betrifft und keines der Flags greift. Das ist trivial sound, solange die Lesemenge vollständig ist.
- **Stufe 2, Polarität (Tab. 3.1–3.4):** filtert zusätzlich nach Event-Richtung, zum Beispiel InsertRT unschädlich bei `−`-Knoten. Die Sonderfälle `not`/`select`/`iterate` erzeugen viel `±`; der Zusatzgewinn gegenüber Stufe 1 ist daher fraglich und sollte gemessen werden (Ablation).

Die Analyse wird **einmal pro Constraint-Text** gecacht. Dafür ist eine Trennung von Compile und Evaluate nötig, die es heute nicht gibt (Abweichung 3).

## Frage 4: Undefiniert-Semantik und Pruning

**Befund [Code]:**
- `and`/`or`/`xor`: Beide Seiten werden **eager** ausgewertet (kein Short-Circuit). Leer, nicht-singleton oder nicht-boolesch ergibt `false` (`EvaluationVisitor.java:2963-2993`).
- `implies`: dieselben Fälle ergeben `true` (`:3011-3035`).
- `not`: Leere Operanden werden übersprungen, das Ergebnis ist **leer** (`:3128-3149`).
- `recordConstraintResult`: leer ergibt **weder Verletzung noch Erfolg**, nicht-boolesch ergibt Verletzung (`:432-449`).

Der Evaluator ist damit nicht klassisch zweiwertig: `not (a and b)` ist bei leerem `a` gleich `true`, `not a` aber leer, also „stumm erfüllt“.

**Bewertung von Pruning (Paper 2, Def. 3.3.1):** Das Argument lautet: L2 war vorher `true`, ev berührt L2 nicht, also ist L2 weiterhin `true`. Es braucht **keine** klassische Logik, nur zwei Dinge:
1. **Determinismus:** Der Wert eines Teilausdrucks hängt nur von den in seiner Lesemenge erfassten Zustandsteilen ab. Bei uns gilt das, sofern `@pre`, Lifecycle-Prädikate, `~` und `allInstances` (Reihenfolge!) in der Lesemenge stehen.
2. **Vorher war der Wurzel-`and` `true`.** Da `and` bei uns *immer* ein Singleton-Boolean liefert, folgt aus „Constraint war für diese Instanz erfüllt“, dass beide Konjunkte singleton `true` waren. **Sicher.**

**Einschränkungen:**
- Pruning nur, wenn die Instanz vorher **nicht** im Violation-Set stand (siehe Frage 8).
- Pruning nur auf dem Pfad Wurzel→`and`, ohne `select` darüber (Def. 3.3.1), ohne `not`, `implies` oder `if` dazwischen. Unter `not` würde aus `and` ein `or`.
- **Nutzen gering:** Da `and` eager ist, spart Pruning genau die Auswertung des unberührten Konjunkts. Der eigentliche Gewinn käme erst, wenn wir auch das unberührte Konjunkt *nicht mehr evaluieren*. Das erfordert eine umgeschriebene oder maskierte Auswertung (ParseTree-Maske im Visitor). **[Annahme]** Aufwand mittel, Gewinn stark constraintabhängig. Priorität niedrig.
- Ein leer ausgewerteter Constraint (stumm erfüllt) bleibt bei unveränderter Lesemenge stumm erfüllt. Instanzweises Überspringen ist also ebenfalls sicher.

## Frage 5: Relevante Instanzen / Inverse-Navigation

**Befund [Code]:**
- `eOpposite`: **0** Vorkommen in allen `.ecore` des VitruvOCL-Repos (70 `EReference`s) und **0** im Benchmark-Metamodell (`Measure-GraphGenerator/model/src/main/ecore/model.ecore`: `Edge.nodes` ohne Gegenrichtung).
- Containment-Rückweg ist immer gratis über `eContainer()`/`eContainmentFeature()`.
- Korrespondenz ist symmetrisch, die Rückwärtsnavigation ist `getCorrespondingObjects` (`VsumWrapper.java:261`). `checkCorrespondence` prüft bereits beide Richtungen (`EvaluationVisitor.java:4217-4227`).
- `select(~)` filtert den Empfänger nach Korrespondenz mit `self` (`EvaluationVisitor.java:4963-4998`).

**Optionen für den Reverse-Index:**

| Option | Speicher | Pflege | Thread-Sicherheit | Bewertung |
|---|---|---|---|---|
| `ECrossReferenceAdapter` (EMF) auf dem ResourceSet der VSUM | O(#Cross-Refs) + Adapter pro Objekt | automatisch über Notifications. Muss *vor* dem Modellaufbau installiert sein, sonst initiale Vollindizierung. | Lesen während paralleler Auswertung ok, solange niemand schreibt. Schreiben passiert nur während der Propagation. | Schnell umsetzbar, indiziert aber **alle** Referenzen. Der Zugriff auf die interne ResourceSet der VSUM ist zu prüfen. **[Annahme]** |
| Eigener Index nur für Features aus Inverse-Pfaden | klein | inkrementell aus genau den EChanges der Transaktion (Insert/RemoveEReference) | wie oben | präziser, aber eigener Code und Pflege bei initialem Laden |
| Kein Index (Fallback) | 0 | — | — | Event wird **Class Event**: volle Extension des Kontexttyps |

**Rückwärts über Korrespondenz [Annahme]:** Das Idiom `T.allInstances().select(~)` lässt sich semantisch als „Korrespondenten von `self`, gefiltert auf T“ lesen. Ein Event auf einem `T`-Objekt x hätte dann als relevante Instanzen `corr(x) ∩ Kontexttyp` statt der ganzen Extension. Das ist **keine** Regel aus Paper 1, wo `allInstances` stets ein Class Event ist. Es ist eine eigene Idiom-Erkennung, die nur sound ist, wenn Korrespondenzen nicht ohne beobachtetes Event entstehen oder verschwinden (offen, Frage 2). Gerade für Tier B/T3 des Benchmarks wäre das der einzige Weg zu Stufe-3-Gewinnen.

**Fallback:** Immer Vollauswertung des Kontexttyps (Class Event). Er ist korrekt und erhält den Stufe-1/2-Gewinn.

## Frage 6: Wechselwirkungen

- **Lazy Evaluation [Code: `evaluator/lazy/package-info.java`]:** orthogonal. Die PSE-Analyse ist statisch und unabhängig von der Auswertungsstrategie. Eine Teilauswertung auf einer Instanz-Untermenge nutzt dieselben Lazy-Pfade. Ein Risiko besteht nur, falls `elementIterator()`-Konsumenten von der Reihenfolge der Extension abhängen (`first()` über `allInstances`). Dieser Fall ist durch „allInstances = Class Event“ bereits abgedeckt.
- **`allInstances()`-Cache [Code: `VsumWrapper.java:168-205`]:** Da nur die Analyse gecacht wird, ist heute **keine** Invalidierung nötig. Der Cache wird nur neu gebaut, wenn ein neuer Typ angefragt wird. Wird später ein *Ergebnis*-Cache ergänzt (für inkrementelle Läufe naheliegend), sind die Invalidierungstrigger: `CreateEObject`+Containment-Insert, `DeleteEObject`, Containment-Remove/Move und `Insert/RemoveRootEObject` für die betroffenen Typen samt Supertypen. Das sind genau die Events, die wir ohnehin klassifizieren.
- **Parallelisierung [Code: `ConstraintListEvaluator.java:55`]:** Arbeitspakete sind heute ganze Constraints. Für Stufe 3 braucht es (a) einen Evaluator-Einstieg „Kontext auf gegebener Instanzliste auswerten“. Heute ist `getAllInstances(eClass)` in `visitClassifierContextCS` hart verdrahtet (`EvaluationVisitor.java:314`); ein optionaler `instanceFilter`/`instanceOverride` ist dort der minimale Eingriff. Dazu kommt (b) Compile-einmal/Evaluate-oft, weil jede Task heute neu parst (`VitruvOCLCompiler.java:166`). Pakete (Constraint, Instanz-Chunk) sind möglich, aber bei kleinen Deltas wird der Overhead des Thread-Pools (pro Batch neu erzeugt, `:67`) dominieren. **[Annahme]** Für kleine Änderungen sequenziell oder mit einem persistenten Pool auswerten.
- **Thread-Sicherheit von `SymbolTable`/Visitor:** Pro Task wird ein eigener Compiler/Visitor erzeugt. Wiederverwendung eines gecachten ParseTrees über Threads hinweg ist zu prüfen (ANTLR-ParseTrees sind lesbar, `ParseTreeProperty` ist eine `IdentityHashMap`). **[Annahme]** Read-only ist ok, Symboltabellen-Zustand muss pro Lauf neu sein.

## Frage 7: Transaktionen und Reactions

**Befund:**
- `PropagatedChange` trennt Originaländerung und konsequenzielle Änderungen (Reactions) **[Code, Bytecode]**. Verkettete Reactions erscheinen als weitere `PropagatedChange`-Einträge bzw. als consequential changes.
- **Reihenfolge ist für unsere Prüfung weitgehend egal:** Wir werten den **Post-Zustand** aus und brauchen aus den Events nur die *Menge* betroffener (Typ, Feature, Objekt). Ausnahme ist `@pre`/`TransactionModel`, das chronologisch rückwärts rechnet (`TransactionModel.java:103-147`). Die Liste muss deshalb global chronologisch sein (original vor consequential, Propagationen in Reihenfolge). **[Annahme]** Ob `consequentialChanges` intern chronologisch geordnet sind, ist zu bestätigen.

**Net-Effect-Regeln (Paper 3, Abschn. 5.5) auf unsere Folgen:**

| Regel | Anwendbar? |
|---|---|
| Dedup gleicher (Objekt, Constraint) | **Ja**, trivial und sicher (= Paper 2, Prop. 4.2: Vereinigung) |
| Insert verdrängt Update (Update nach Create desselben Objekts) | **Ja**: Ein neues Objekt wird ohnehin voll geprüft, Update-Events darauf sind redundant. |
| Delete verwirft frühere Checks auf dem Objekt | **Ja**, und gelöschte Objekte zusätzlich aus dem Violation-Set entfernen. **Aber** Delete bleibt als Event für *andere* Constraints relevant (`allInstances`, Navigationen der Ex-Nachbarn). |
| Create+Delete desselben Objekts in einer Transaktion → beides weg | Ja, sofern das Objekt am Ende in keiner Extension auftaucht |
| Link+Unlink heben sich auf | **Nur eingeschränkt.** In geordneten, nicht-eindeutigen Listen (z. B. `Edge.nodes`, `unique="false"`) kann ein anderer Index übrig bleiben. Empfehlung: **nicht kürzen**, nur deduplizieren. Der Gewinn ist minimal, das Risiko real. |

**Overreach Detector:** Im Repo nicht gefunden (`grep -ri overreach` ohne Treffer). **Offen**, ob er woanders existiert. Falls er eine „berührte Elementmenge“ pro Transaktion berechnet, ist sie eine Obermenge unserer Objekt-Seeds und wiederverwendbar.

## Frage 8: Konsistente Ausgangsbasis

**Befund [Code]:** Der Evaluator ist **zustandslos**. Jeder Lauf erzeugt neue `ViolationRecord`s (`EvaluationVisitor.java:110`, `:432ff`), nichts wird zwischen Läufen gehalten. Bei inkonsistentem Vorzustand würde eine inkrementelle Prüfung **alte Verletzungen nicht mehr melden**. Das bricht mit der heutigen Ausgabe-Semantik („alle Verletzungen“).

**Optionen:**
1. **Violation-Set pflegen (empfohlen):** `Map<(ConstraintId, EObject), ViolationRecord>`. Beim Start wird es durch einen Full-Check befüllt. Pro Transaktion werden die Einträge für die neu ausgewerteten (Constraint, Instanz)-Paare ersetzt, gelöschte Objekte entfernt und Class-Event-Constraints komplett ersetzt. Damit fällt die Annahme „vorher konsistent“ weg. Das Korrektheitskriterium wird exakt prüfbar: **inkrementelles Violation-Set == Full-Check-Violation-Set**. Kosten: Speicher O(#Verletzungen).
2. **Nur Full-Check als Start und Abbruch bei Verletzung:** Das entspricht der Paper-Annahme. Es passt nicht zu unseren Reactions, wo inkonsistente Zwischenzustände normal sind.

Das Pruning (Frage 4) bleibt nur für Paare **außerhalb** des Violation-Sets erlaubt.

## Frage 9: Korrektheitssicherung (Differential-Tests)

**Strategie [Annahme]:**
1. **Orakel-Modus im Code:** Ein Flag `-Dvitruvocl.incremental.verify=true` (bzw. ein Test-Hook) führt nach jeder inkrementellen Auswertung einen Full-Check aus und vergleicht die Violation-Sets. Verglichen werden Constraint-Name, Instanz-Identität, Severity und Message; bei Abweichung wird geworfen.
2. **Bestehende Tests (2006 Annotationen):** Die meisten sind Single-State-Tests ohne Transaktion. Zwei Wege machen sie nutzbar:
   - **(a) Aufbau als Transaktion:** Leeres Modell plus Laden aller Instanzen als eine aufgezeichnete Transaktion (`ChangeRecorder`). Dann muss gelten: inkrementell == voll. Das prüft vor allem die Vollständigkeit der PSE-Abdeckung bei Inserts.
   - **(b) Mutationstests:** Pro Test-Modell N zufällige atomare Mutationen (Attribut setzen/unset, Referenz add/remove/move, Create/Delete, Containment-Move) mit `ChangeRecorder` aufzeichnen und nach jeder Mutation vergleichen. Das deckt Delete/Remove ab, was (a) nicht kann.
3. **Gezielte Randfälle:** polymorphe Feature-Namen in Subklassen; `OclAny`-Receiver; Containment-Move ohne Create/Delete; Create ohne Containment (verwaistes Objekt); Delete mit hängenden Cross-Refs; `first()/at()` nach Permutation; `not`/`select`/`reject`/`exists` (Gegen-Events); `implies` mit leerem Antezedens; `let`/`iterate`; `@pre`- und `post`-Constraints (müssen immer laufen); `~` mit `Type=`/`Tag=` bei Korrespondenzänderung ohne Modell-Event; Link+Unlink im selben Commit; mehrere `specificationCS`.
4. **Graph-gentool-Deltas:** Nach jedem Delta-Commit im Benchmark wird Orakel-Modus (1) ausgeführt. Das ist der realistischste Test mit Reactions.

## Frage 10: Benchmark-Design

**Befund [Code: `Measure-GraphGenerator/benchmark/harness/.../GraphMain.java`]:** Der Harness macht bereits *Basismodell-Replay → Delta-Replay* (`:112-121`) und misst danach eine **Vollauswertung** (`evaluateConstraint` pro Constraint und `evaluateConstraints(file)`, `:131-155`). Die Constraint-Suite liegt in `benchmark/constraints/graph-scaling-constraints.ocl` und ist in Tiers B, T1–T7 gegliedert.

**Erweiterung [Annahme]:**
1. Nach dem Basismodell: **Full-Check** (Violation-Set initialisieren, nicht im Messfenster).
2. Listener registrieren. Für **jeden** Delta-Commit (`DeltaSequenceReplayer`) die `PropagatedChange`s einsammeln und sofort `evaluateIncremental(delta)` ausführen. Gemessen wird pro Transaktion.
3. **Ablation** über ein Flag:
   - **Stufe 0:** Full-Check nach jedem Commit (Baseline, heutiges Verhalten)
   - **Stufe 1:** Feature-/Typ-Filter ohne Event-Art (Read-Set, Frage 3)
   - **Stufe 2:** PSE-Filter mit Polarität (Tab. 3.1–3.4)
   - **Stufe 3:** PSE + relevante Instanzen (Self/Containment/Index; sonst Class Event)
4. **Metriken pro Transaktion:**
   - Wall-Time Auswertung, getrennt Overhead (Event-Klassifikation, Index-Pflege) vs. Evaluation
   - #Constraints ausgewertet / übersprungen
   - #(Constraint, Instanz)-Paare ausgewertet vs. Stufe 0
   - #EChanges, #Class-Event-Fallbacks
   - einmalige PSE-Analysezeit; Heap-Overhead (Index, Violation-Set)
   - Korrektheit: Orakel-Abweichungen (muss 0 sein)
5. **Achsen:** Modellgröße (wie bisher), Deltagröße (1, 10, 100 Änderungen pro Commit), Threads (1 vs. N; bei kleinen Deltas wird 1 Thread vermutlich gewinnen).

**Erwartung [Annahme], ehrlich:** Für **T1/T5** (lokal) und **T2** gibt es große Gewinne ab Stufe 1, für **B/T3** (`allInstances().select(~)`) nur dann, wenn eine Transaktion die beteiligten Typen gar nicht berührt. Bei graph-gentool-Deltas, die ständig Nodes/Edges anlegen, ist das selten. Ohne die Idiom-Regel aus Frage 5 ist dort kaum Gewinn zu erwarten. Das sollte im Paper als Limitation stehen.

## Frage 11: Aufwand, Risiko, Plan

| Stufe | Inhalt | Schätzung [Annahme] | Risiko | Abbruchkriterium |
|---|---|---|---|---|
| A | Compile/Evaluate trennen, ParseTree+`nodeTypes` pro Constraint cachen | 0,5–1 Tag | Thread-Sicherheit von Symboltabelle/Visitor | Tests rot nach 1 Tag → nur PSE-Cache ohne Tree-Reuse |
| B | Listener + `TransactionDelta` (EChange → (Typ, Feature, Objekt, Art)) | 0,5 Tag | Listener-API auf `InternalVirtualModel`, Korrespondenz-Events | API nicht erreichbar → Harness liefert EChanges selbst (`ChangeRecorder`) |
| C | Stufe 1 Read-Set-Analyse + Violation-Set + Dedup | 1–1,5 Tage | Lücken in der Lesemenge (polymorphe Features, `~`) | Orakel findet Abweichungen, die nicht konservativ behebbar sind |
| D | Differential-Tests (Orakel, Mutationstests, Graph-Deltas) | 1 Tag (parallel zu C) | — | — |
| E | Benchmark-Erweiterung + Ablation 0/1 | 0,5–1 Tag | Laufzeit der Messreihen | — |
| F | Stufe 2 Polarität | 1–2 Tage | viel `±`, geringer Mehrwert | Stufe 1 nicht grün bis Tag 3 → streichen |
| G | Stufe 3 relevante Instanzen (Self + Containment), Instanz-Override im Evaluator | 2–3 Tage | Inverse-Pfade über `let`/Iteratoren, kein `eOpposite` | — |
| H | Reverse-Index / `~`-Idiom | 2–4 Tage | Soundness der `~`-Annahme | — |
| I | `and`-Pruning mit maskierter Auswertung | 1–2 Tage | Gewinn gering (eager `and`) | — |

**Realistisch bis Mitte Oktober (ca. 4–5 Tage):** A + B + C + D + E, also **Stufe 1 mit Violation-Set, Dedup, Differential-Tests und Ablation 0/1**. F nur bei Zeitüberschuss, G/H/I als Future Work. Der Vorschlag „PSE-Filter + Dedup zuerst“ deckt sich damit. Einschränkung: Statt der vollen Polaritäts-PSE (Stufe 2) zuerst den polaritätsfreien Read-Set-Filter bauen, weil er einfacher, sicher sound und schnell zu verifizieren ist.

**Formulierung für das Paper:** „Kein PSE aufgetreten“ gilt **für die beobachtete Transaktion**, also für die tatsächlich aufgezeichnete EChange-Folge. Es ist kein statischer Beweis, dass die Reactions den Constraint nie verletzen können. Die PSE-Menge selbst hängt nur von der Invariante ab und wird einmal berechnet.

**Abgrenzung zu ReOCL (Paper 4) [Paper 4, Abstract und Abschn. 4–5]:** ReOCL pflegt zustandsbehaftete Delta-Views pro Collection-Operator (reaktive Signale, konstante Kosten pro Element-Delta) in einem eingeschränkten OCL-Kern, mit Transaktionen samt Commit/Rollback für `@pre`. Wir halten **keinen** Zwischenzustand pro Teilausdruck, sondern filtern nur *welche* (Constraint, Instanz)-Paare neu ausgewertet werden, ohne Einschränkung der Sprache (voller OCL#-Umfang inkl. `iterate`, `sortedBy`, `~`). Einzige Gemeinsamkeit ist das Violation-Set als minimaler Zustand. Das gehört in Related Work. Die Abgrenzung trägt.

---

## Annahmen, die du als Entwickler bestätigen musst

1. Baseline-Branch für Vergleiche ist `main` (oder `vitruvocl`?), Optimierungs-Branch ist `vitruviusocl_parallel`.
2. Eine „Transaktion“ = ein `finishedChangePropagation`-Callback (= ein `commitChanges()`), nicht eine ganze Delta-Sequenz.
3. `InternalVirtualModel` exponiert `addChangePropagationListener` (laut Bytecode via `ChangeableModelRepository`).
4. `PropagatedChange.getConsequentialChanges()` ist chronologisch geordnet; original vor consequential ist die richtige globale Reihenfolge für `@pre`.
5. **Korrespondenzänderungen:** Sind sie im EChange-Strom sichtbar? Falls nicht: Werden Korrespondenzen nur zusammen mit beobachteten Create/Delete-Events der Partner angelegt bzw. entfernt? Davon hängt die Soundness jeder `~`-Optimierung ab.
6. Gelöschte Objekte mit hängenden Cross-Referenzen erzeugen beobachtbare `RemoveEReference`-Events (oder es gibt keine hängenden Referenzen).
7. Inkonsistente Zwischenzustände sind normal, deshalb wird das Violation-Set gepflegt statt „vorher konsistent“ anzunehmen.
8. `pre`/`post` und `@pre` werden immer voll ausgewertet (kein Filter).
9. Die Ausgabe-Semantik bleibt „alle aktuellen Verletzungen“ (Violation-Set) und nicht „nur neue Verletzungen“.
10. Die Mehrfach-`specificationCS`-Semantik (nur die letzte zählt, `EvaluationVisitor.java:606`) ist gewollt. Falls nicht, ist das ein separater Bug.
11. Ein Overreach Detector existiert außerhalb dieses Repos? Wenn ja, wo?
12. Die Testzahl „2400+“ bezieht sich auf ausgeführte Testfälle inkl. Parametrisierung; statisch sind es 2006 Annotationen.
