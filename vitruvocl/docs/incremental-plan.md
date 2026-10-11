# Implementierungsplan: Inkrementelle Constraint-Auswertung für VitruviusOCL

Stand: 2026-10-11 · Branch `vitruviusocl_parallel` (HEAD `39476b07`, Arbeitskopie mit fremden, nicht von mir stammenden Änderungen in `typechecker/*` und `vitruvocl-vscode-extension/*`, die ich nicht anfasse).
Grundlage: `docs/incremental-exploration.md`. Alle Code-Stellen unten habe ich vor dem Schreiben erneut geprüft.
Pfade relativ zu `language/src/main/java/tools/vitruv/dsls/vitruvocl/`, sofern nicht anders angegeben.

Legende: **[Code]** im Code verifiziert · **[Paper n]** · **[Annahme]** nicht verifiziert.

---

## 0. Re-Verifikation des Explorationsberichts (was hält, was ich korrigiere)

| Aussage im Explorationsbericht | Ergebnis der Nachprüfung |
|---|---|
| Kontext-Iteration holt immer die volle Extension (`EvaluationVisitor.java:314`) | bestätigt **[Code]** |
| Leeres Ergebnis wird weder als Verletzung noch als Erfolg verbucht (`:432-449`) | bestätigt **[Code]** |
| `evaluateSpecs` liefert nur das letzte Ergebnis (`:606-612`) | bestätigt **[Code]**. Ergänzung: `invCS` kann laut Grammatik zwei `specificationCS` haben (`inv name(spec): spec`), und jede `specificationCS` ist `expCS*` – `visitSpecificationCS` (`:4041`) liefert ebenfalls nur den letzten Ausdruck. Beides bleibt unverändert, Analyse nimmt die Vereinigung. |
| Jede Task parst neu (`VitruvOCLCompiler.java:166`) | bestätigt **[Code]** |
| `addChangePropagationListener` auf `InternalVirtualModel` erreichbar? | **bestätigt**: `VirtualModel extends ChangeableModelRepository` (javap, `tools.vitruv.framework.vsum-4.0.1-SNAPSHOT`) **[Code]** |
| Vergleiche auf leeren Operanden | **neu**: `<, <=, >, >=` liefern bei leerem Operanden `true` (vakuös), bei Nicht-Singleton `false` + Laufzeitfehler (`evaluateBinaryComparison`) **[Code]**. `and/or/xor` liefern bei leer/Nicht-Singleton `false` (`evaluateBinaryLogical`) **[Code]**. Konsequenz für Stufe 2, siehe Risiko R3. |
| Feature-Zugriff per Name auf der Laufzeitklasse (`navigateOne`, `:3393`) | bestätigt **[Code]** |
| `select(~)` bindet immer `self` des Kontexts (`:4963ff`, `resolveVariable("self")`) | bestätigt **[Code]** |
| Violation-Ausgabe | **neu**: `VitruvOCL.compileAndEvaluate` rendert pro Verletzung `describeInstance` (liest bis zu drei einwertige String/Integer/Boolean-Features der Instanz) und `getSourceFileForInstance` (Resource-URI) **zur Ausgabezeit** (`VitruvOCL.java:526-536`). `@message`-Templates mit `{self.attr}` lesen Attribute **zur Auswertungszeit** (`EvaluationVisitor.java:497`). Konsequenz siehe D. |
| Korrespondenzmodell im EChange-Strom? | Hinweis aus dem Bytecode: `DefaultChangeRecordingModelRepository` hält eine eigene `ResourceSet` für Modelle, der `ChangeRecorder` wird darauf angelegt; das Korrespondenzmodell wird über `CorrespondenceModelFactory.createPersistableCorrespondenceModel(URI)` separat erzeugt **[Code, javap]**. Ob dessen Änderungen dennoch aufgezeichnet werden, klärt das Experiment (Schritt 2). |
| Bestehende Tests nutzen eine **gemockte** `InternalVirtualModel` über echte EMF-Resources (`vsum/VsumConstraintTest.java`, `VsumTransactionConstraintTest.java`) | bestätigt **[Code]**. Eine echte VSUM mit `VirtualModelBuilder` + handgeschriebener `ChangePropagationSpecification` ist mit den vorhandenen Abhängigkeiten baubar (gleiches Muster wie `Measure-GraphGenerator/.../GraphMain.java:248`, nur gelesen). |

**Baseline vor jeder Änderung [Test]:** `mvn -o -pl language -am test` auf dem aktuellen Arbeitsstand (inkl. der fremden, uncommitteten `typechecker/*`-Änderungen): `vitruvocl-language` **2559 Tests, 0 Failures, 0 Errors, 0 Skipped**; `vitruvocl-allinstances` 12 Tests, alle grün; BUILD SUCCESS. Diese Zahl ist die Referenz für „Stufe 0 unverändert“.

## 1. Architektur-Überblick

Neues Paket `tools.vitruv.dsls.vitruvocl.incremental` (alles neu, keine Änderung an bestehendem Verhalten bei Stufe 0):

```
VSUM commitChanges()
   └─ ChangePropagationListener.finishedChangePropagation(Iterable<PropagatedChange>)
        └─ TransactionCollector  (pro PropagatedChange: original, dann consequential → List<EChange<EObject>>)
             └─ IncrementalSession.onTransaction(changes)
                  ├─ EChangeClassifier  → TransactionDelta (ModelEvents, dedupliziert)
                  ├─ für jeden Constraint: ConstraintAnalysis (gecacht) × TransactionDelta
                  │     Stufe 1: ReadSet-Treffer?      → auswerten / überspringen
                  │     Stufe 2: PSE (Polarität)?      → auswerten / überspringen
                  │     Stufe 3: relevante Instanzen   → Teilmenge / Class-Event (voll)
                  ├─ Auswertung über CompiledConstraint (Compile einmal, Evaluate oft)
                  ├─ ViolationSet aktualisieren
                  ├─ IncrementalStats (nur Zähler, für Tests)
                  └─ optional Orakel: Full-Check, Vergleich, Exception mit Diff
```

## 2. Bausteine, Dateien, Reihenfolge

### Schritt 1 – Plan (dieses Dokument) → **Freigabe durch dich**

### Schritt 2 – Korrespondenz-Experiment (Entscheidung 3)
- **Test** `language/src/test/java/.../incremental/CorrespondenceVisibilityExperimentTest.java`:
  - Echte VSUM (`VirtualModelBuilder`, Temp-Ordner, `TestUserInteraction` falls verfügbar, sonst minimaler Interactor) mit zwei kleinen dynamischen Test-Metamodellen (neu als `.ecore` unter `src/test/resources/incremental/`) und einer **handgeschriebenen `ChangePropagationSpecification`**, die – wie generierter Reactions-Code – über die `EditableCorrespondenceModelView` Korrespondenzen anlegt und entfernt.
  - Fälle: (a) Create in A → Spec erzeugt Partner in B + Korrespondenz; (b) Delete in A → Spec löscht Partner + entfernt Korrespondenz; (c) Attributänderung in A → Spec **legt Korrespondenz zwischen zwei bestehenden Objekten an** bzw. entfernt sie, ohne Partnerobjekte zu ändern.
  - Gemessen (als Assertions/Protokoll): Tauchen Korrespondenzänderungen in `PropagatedChange.getOriginalChange()/getConsequentialChanges().getEChanges()` auf? Entstehen/verschwinden Korrespondenzen ohne beobachtetes Event der Partnerobjekte (Fall c)?
- **Konsequenz** (wird im Bericht festgehalten):
  - Erscheinen Korrespondenzänderungen **nicht** im Strom **und** ist Fall (c) möglich, dann ist schon die Default-Regel aus Entscheidung 3 („auswerten, wenn ein beteiligter Typ berührt ist“) nicht allgemein sound. Konservative Variante: **Constraints mit `~` werden bei jeder nicht-leeren Transaktion ausgewertet**; Idiom-Regel 3c bleibt implementiert, aber per Flag `vitruvocl.incremental.corrIdiom` standardmäßig **aus**. Optional prüfe ich, ob sich Korrespondenzänderungen direkt beobachten lassen (EMF-Adapter auf dem Korrespondenzmodell); nur wenn das ohne Reflection-Hacks geht, wird daraus ein eigenes Pseudo-Event `CorrChanged`.
  - Andernfalls gilt Entscheidung 3 wie vorgegeben.
- **Abbruchkriterium:** Lässt sich die echte VSUM im Test nicht innerhalb eines halben Tages aufbauen, dokumentiere ich das Ergebnis der Bytecode-Analyse als **[Code]** (nicht **[Test]**) und nehme die konservative Variante.

### Schritt 3 – Bausteine A, B, D, F + Stufe 1 + Orakel-Tests → **Pause, Commit-Vorschlag**

| Baustein | Neue / geänderte Dateien | Inhalt |
|---|---|---|
| Konfiguration | `incremental/IncrementalLevel.java` (enum 0–3), `incremental/IncrementalConfig.java` (record: level, verify, corrIdiom, threads) | System-Properties `vitruvocl.incremental.level`, `vitruvocl.incremental.verify`, `vitruvocl.incremental.corrIdiom`; Default Stufe 0. |
| **A** Compile/Evaluate | `incremental/CompiledConstraint.java`, `incremental/ConstraintCompiler.java`; **minimal** in `pipeline/VitruvOCLCompiler.java` (neue Methode, bestehende unverändert) | ParseTree + Tokens + `nodeTypes` + Compile-Fehler pro Constraint-Text einmal je Session. Pro Auswertung **frischer** `SymbolTableImpl` (Pass 1 erneut auf dem gecachten Baum), weil der Evaluator Symbole/Scopes mutiert (`EvaluationVisitor.java:321-358`, `VariableSymbol.setValue`). Ob Pass 2 erneut laufen muss, entscheidet ein Äquivalenztest (gecachter Pfad vs. `compileAndEvaluate` über den Testkorpus). Thread-Sicherheit: ein Constraint wird pro Transaktion nur von genau einer Task ausgewertet, der Baum wird nicht parallel benutzt; `nodeTypes` wird im Evaluator nur gelesen (kein `nodeTypes.put` in `evaluator/`) **[Code]**. |
| **B** Transaktions-Hook | `incremental/TransactionCollector.java` (implements `ChangePropagationListener`), `incremental/EChangeClassifier.java`, `incremental/ModelEvent.java`, `incremental/TransactionDelta.java`; in `pipeline/VsumWrapper.java` zwei kleine Methoden `addTransactionListener/removeTransactionListener` | Mapping nach Frage 2 des Explorationsberichts; Containment-Move konservativ als Insert/DeleteET aller (transitiv) enthaltenen Typen. Fallback-API ohne Listener: `IncrementalSession.onTransaction(List<EChange<EObject>>)` (in Tests per `ChangeRecorder`). **Abweichung von der Vorgabe:** Der Listener wird **nicht** im `VsumWrapper`-Konstruktor registriert, sondern erst beim Öffnen einer inkrementellen Session (und beim Schließen entfernt). Begründung: „Stufe 0 darf sich in keiner Weise ändern“; ein immer registrierter Listener wäre eine (wenn auch kleine) Verhaltensänderung und müsste EChanges puffern, auch wenn niemand sie abholt. |
| Session / API | `incremental/IncrementalSession.java`, `incremental/IncrementalResult.java`; in `pipeline/VitruvOCL.java` neue statische Methoden `openIncrementalSession(Path/List<String>, IncrementalConfig)` und `evaluateConstraintsIncremental(...)` (bestehende Methoden unverändert) | Session hält Wrapper, Constraint-Liste, Analysen, Violation-Set, Stats. `initialize()` = Full-Check (befüllt Violation-Set). Pro Transaktion Ergebnis als `BatchValidationResult` mit derselben Ausgabe-Semantik „alle aktuellen Verletzungen“. |
| **D** Violation-Set | `incremental/ViolationSet.java`, `incremental/ViolationKey.java` | `Map<(ConstraintText, EObject-Identität), List<ViolationRecord>>` + pro Constraint die übrigen Ergebnisteile (Compile-/Laufzeitfehler, Skip-Hinweise). `describeInstance`/Quelldatei werden zur **Ausgabezeit** gerendert (wie heute); `{self.attr}` aus `@message` kommt ins Read-Set (siehe unten). Gelöschte Objekte (nicht mehr unter einer Wurzel) werden entfernt. |
| **F** Test-Zähler | `incremental/IncrementalStats.java` | #Constraints ausgewertet/übersprungen, #(Constraint, Instanz)-Paare ausgewertet, #Class-Event-Fallbacks, #Voll-Auswertungen wegen Flags. Keine Zeiten. |
| **Stufe 1** Read-Set | `incremental/analysis/ReadSetAnalyzer.java` (Visitor über den typgeprüften Baum), `incremental/analysis/ConstraintAnalysis.java`, `incremental/analysis/ReadSet.java` | Gelesene (EClass-Menge, Feature-Name)-Paare (statischer Empfängertyp aus `nodeTypes`, Abgleich zur Laufzeit per `isSuperTypeOf` ⇒ Subklassen automatisch); OclAny/unbekannt ⇒ Feature-Name in allen Klassen. `allInstances`-Typen, Kontexttypen, Flags `usesCorrespondence`, `usesPre`, `isPrePost` (inkl. `OCLisNew/Modified/Deleted`), `hasErrorNodes`, `@message`-Attribute. Skip-Regel exakt wie vorgegeben; Strukturevents (Create/Delete/Containment/Root) treffen einen Constraint, wenn das betroffene Objekt (oder ein laut Metamodell transitiv enthaltener Typ) Instanz eines Extensions-Typs (Kontext oder `allInstances`) sein kann. |
| Orakel | `incremental/OracleVerifier.java`, `incremental/IncrementalMismatchException.java` | `-Dvitruvocl.incremental.verify=true` bzw. `IncrementalConfig.verify`: nach jeder inkrementellen Auswertung Full-Check auf demselben Zustand, Vergleich (Constraint-Name, Instanz-Identität, Severity, Message; zusätzlich Erfolg/Fehlerliste pro Constraint), Exception mit Diff. |
| CLI | `cli/VitruvOclCli.java`: Option `--incremental-level <0-3>` (setzt die Konfiguration) | Die CLI hat keine Transaktion (Explorationsbericht, Abweichung 4); dort wirkt die Option nur als Konfiguration, die Auswertung bleibt der initiale Full-Check. Im Bericht so dokumentiert. |

**Tests Schritt 3** (alle unter `language/src/test/java/tools/vitruv/dsls/vitruvocl/incremental/`):
1. `IncrementalTestSupport` – Hilfen: Laden von Test-Metamodellen/-Modellen, gemockte VSUM wie in den bestehenden VSUM-Tests, `ChangeRecorder`-Transaktionen, Mutationsgenerator mit festem Seed.
2. `BuildAsTransactionOracleTest` (Orakel 2a): leeres Modell, alle Instanzen eines bestehenden Testmodells per `ChangeRecorder` als eine Transaktion einfügen, dann inkrementell == voll. Korpus: die `.ocl`-Dateien unter `src/test/resources` plus die modellbasierten Constraint-Strings aus den bestehenden Metamodell-/Korrespondenz-/VSUM-Tests, die ich als Korpus-Datei `src/test/resources/incremental/corpus/*.ocl` übernehme (Kopie, bestehende Tests bleiben unverändert).
3. `MutationOracleTest` (Orakel 2b): pro Testmodell N zufällige atomare Mutationen (Attribut set/unset, Referenz add/remove/move, Create/Delete, Containment-Move), nach jeder Mutation Orakelvergleich; Seeds fest, N per Property einstellbar.
4. `IncrementalEdgeCaseTest` (Randfälle aus der Vorgabe, Punkt 3), jeweils als Orakeltest.
5. `ReadSetSkipTest` (Skip-Tests mit Zählern, Punkt 4).
6. `CompiledConstraintEquivalenceTest` (Baustein A: gecachter Pfad == bisheriger Pfad).
7. **Alle bestehenden Tests** mit Stufe 0 laufen lassen; tatsächliche Zahl aus dem Surefire-Report berichten.

### Schritt 4 – Stufe 2 (Polarität) → **Pause, Commit-Vorschlag**
- `incremental/analysis/PolarityAnalyzer.java`, `incremental/analysis/PseSet.java`, Erweiterung von `ConstraintAnalysis`.
- Annotation `+`/`−`/`und` top-down nach Paper 1 Tab. 3.1–3.3, PSE-Ableitung nach Tab. 3.4; Sonderregeln für `not` (Gegen-Events) und `select` (Gegen-Events der Bedingung). `exists ≡ not forAll(not …)`, `reject ≡ select(not …)`. Alle Konstrukte ohne Tabellenzeile: `±` an alle Kinder (Tabelle Frage 3 des Explorationsberichts).
- **Eigene Anpassungen an unsere Semantik (siehe R3):** (i) Vergleichsoperanden und Operanden von `and/or/xor/implies/not` erhalten `±`, weil leere oder nicht-singleton Werte das Ergebnis nicht-monoton ändern; (ii) Polaritäts-Skip nur, wenn der Constraint aktuell **keine** Einträge im Violation-Set und keine Laufzeitfehler hat (Polarität schließt nur „erfüllt → verletzt“ aus, nicht „verletzt → erfüllt“; sonst bliebe eine veraltete Verletzung stehen).
- Tests: Orakel wie Schritt 3 mit Stufe 2, `PolaritySkipTest` (z. B. `self.items->size() <= 3`: Remove wird übersprungen, Insert ausgewertet), Randfälle `not/select/reject/exists`.

### Schritt 5 – Stufe 3 (relevante Instanzen) → **Pause, Commit-Vorschlag**
- `incremental/analysis/PathAnalyzer.java` (PathVar/Inverse nach Paper 1 Abschn. 3.2), `incremental/RelevantInstanceResolver.java`, `incremental/ReverseIndex.java`.
- **Minimaler Eingriff im Evaluator:** optionaler Instanz-Provider in `visitClassifierContextCS` (`EvaluationVisitor.java:314`): Feld `Function<ClassifierContextCSContext, List<EObject>> contextInstanceOverride`, Default `null` ⇒ heutiges Verhalten bitgleich.
- (a) Self-Pfade (leere Inverse) und Containment-Pfade über `eContainer()`/`eContainmentFeature()`.
- (b) Reverse-Index für Referenzen ohne `eOpposite`: Entscheidung mit Begründung im Bericht. Meine Tendenz: eigener Index **nur** für Features, die auf Inverse-Pfaden vorkommen, lazy beim ersten Bedarf per Scan aufgebaut und aus den EChanges der Transaktion gepflegt; `ECrossReferenceAdapter` verworfen, falls er die Ressourcen der VSUM von außen nicht sauber erreichen kann oder alle Referenzen indiziert. Zustandsfreie Alternative zum Vergleich: Inverse per Scan der Quellklasse.
- (c) Idiom-Regel `Y.allInstances()->select(~ …)`: nur aktiv, wenn das Experiment es erlaubt, sonst Flag `corrIdiom` standardmäßig aus.
- Alles, was sich nicht eingrenzen lässt (`allInstances`, `iterate`, Collection-Mengenoperationen mit mehreren Ursprüngen, unbekannte Typen, `let` mit komplexem Init, Laufzeitfehler im Vorzustand): Class-Event ⇒ volle Extension des Kontexttyps.
- Instanzen im Violation-Set werden bei jedem Read-Set-Treffer auf ihrem Pfad neu ausgewertet; Polaritäts-Skip nur für Paare außerhalb des Violation-Sets.
- Tests: Orakel mit Stufe 3, `RelevantInstanceSkipTest` (Zähler zeigen Teilmenge), Randfälle Delete mit hängenden Cross-Refs, `first()/at()` nach Permutation, Containment-Move.

### Schritt 6 – Bericht finalisieren, „Paper-ready summary“

`and`-Pruning (Paper 2, Def. 3.3.1) ist **niedrig priorisiert** und nur vorgesehen, wenn nach Stufe 3 Zeit bleibt; sonst als nicht umgesetzt dokumentiert (Begründung: `and` wird eager ausgewertet, `EvaluationVisitor.java:2963ff`, Gewinn erst mit maskierter Auswertung).

## 3. Parallel geführter Bericht

`docs/incremental-implementation-report.md` (Englisch, Struktur 1–10 + „Paper-ready summary“ wie vorgegeben, jede Aussage mit Etikett **[Code]/[Test]/[Paper n]/[Annahme]**). Aktualisiert nach jedem Baustein. Keine Aussagen zu Geschwindigkeit. Mögliche Bugs (z. B. Mehrfach-`specificationCS`) werden dort gemeldet, nicht behoben.

## 4. Risiken

| # | Risiko | Gegenmaßnahme |
|---|---|---|
| R1 | Read-Set unvollständig (implizite Modellzugriffe: Extension-Reihenfolge, `@message`-Attribute, Resource-Zugehörigkeit, polymorphe Features) | Strukturevents konservativ; Abgleich per Feature-**Name** + `isSuperTypeOf`; Orakel + Mutationstests |
| R2 | Korrespondenzen ändern sich ohne Modell-Event | Experiment; konservativ „`~` ⇒ bei jeder nicht-leeren Transaktion auswerten“ |
| R3 | Polaritätsregeln setzen zweiwertige, monotone Semantik voraus; bei uns: leere Operanden ⇒ Vergleich `true`, Nicht-Singleton ⇒ `false` + Fehler, `not` auf leer ⇒ leer | `±` an Vergleichs-/Logikoperanden; Polaritäts-Skip nur ohne aktuelle Verletzungen/Fehler; als Anpassung im Bericht |
| R4 | Wiederverwendung des ParseTrees mit frischer Symboltabelle verhält sich anders als Neukompilieren | Äquivalenztest; Abbruchkriterium unten |
| R5 | Echte VSUM mit dynamischen Metamodellen im Test nicht lauffähig | Fallback: Bytecode-Analyse als **[Code]**, konservative Regel |
| R6 | Reihenfolge der Verletzungsmeldungen bei Stufe 3 weicht von der Vollauswertung ab | Orakel vergleicht Mengen (Multiset); im Bericht als Limitation |
| R7 | Laufzeitfehler (z. B. Nicht-Singleton-Vergleich) hängen am Zustand, sind aber nicht instanzbezogen gespeichert | Constraint mit Fehlern im Vorzustand ⇒ bei jedem Read-Set-Treffer volle Auswertung (kein Polaritäts-/Instanz-Skip) |
| R8 | Fremde, uncommittete Änderungen in `typechecker/*` könnten Tests beeinflussen | Baseline-Testlauf vor meinen Änderungen; ich fasse diese Dateien nicht an und nenne sie in keinem Commit-Vorschlag |

## 5. Abbruchkriterien

- **Baustein A:** Äquivalenztest nicht grün nach vertretbarem Aufwand ⇒ nur Analyse-Cache; Auswertung weiterhin über `VitruvOCLCompiler` (neu kompilieren pro Auswertung). Korrektheit unberührt.
- **Stufe 1:** Orakel findet Abweichungen, die sich nicht konservativ beheben lassen ⇒ Stufe 1 bleibt aus (Default 0), Befund im Bericht; Stufe 2/3 werden nicht begonnen.
- **Stufe 2:** Polaritätsregeln führen trotz Anpassungen R3 zu Orakel-Abweichungen ⇒ betroffene Konstrukte auf `±`; wenn danach kein Konstrukt mehr echten Polaritätsgewinn hat, Stufe 2 als „äquivalent zu Stufe 1“ dokumentieren.
- **Stufe 3:** Inverse-Pfade für ein Konstrukt nicht sicher bestimmbar ⇒ Class-Event-Fallback für dieses Konstrukt. Reverse-Index nicht konsistent pflegbar ⇒ zustandsfreier Scan der Quellklasse.
- Allgemein: Ich gehe über diese Kriterien nicht hinaus und schreibe den Abbruch mit Begründung in den Bericht.

## 6. Was ich ausdrücklich nicht tue

Kein Branchwechsel, keine Git-Schreibbefehle (Commits machst du), keine Änderungen an Benchmark-Code/-Harness/-Suiten/Messskripten (auch nicht im Repo `Measure-GraphGenerator`, das ich nur gelesen habe), keine Zeitmessungen, keine Implementierung von Paper 1 Abschn. 3.3, AspectJ-Mechanik, „in-the-graph“-Heuristik oder ReOCL-Delta-Pflege.
