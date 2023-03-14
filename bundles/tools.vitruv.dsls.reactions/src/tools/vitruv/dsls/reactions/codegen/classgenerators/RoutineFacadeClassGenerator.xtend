package tools.vitruv.dsls.reactions.codegen.classgenerators

import java.util.Map
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.common.types.JvmConstructor
import org.eclipse.xtext.common.types.JvmOperation
import org.eclipse.xtext.common.types.JvmVisibility
import tools.vitruv.dsls.reactions.language.toplevelelements.Routine
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacadesProvider
import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsSegment
import static extension tools.vitruv.dsls.reactions.codegen.helper.ClassNamesGenerators.*
import static extension tools.vitruv.dsls.reactions.codegen.helper.ReactionsImportsHelper.*
import tools.vitruv.dsls.reactions.codegen.typesbuilder.TypesBuilderExtensionProvider
import org.eclipse.xtext.common.types.JvmGenericType
import tools.vitruv.dsls.common.ClassNameGenerator
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving
import static extension tools.vitruv.dsls.reactions.codegen.helper.ReactionsElementsCompletionChecker.isReferenceable
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutinesFacade
import org.eclipse.xtext.common.types.JvmFormalParameter
import tools.vitruv.dsls.reactions.codegen.helper.AccessibleElement

class RoutineFacadeClassGenerator extends ClassGenerator {
	protected static val EXECUTION_STATE_ACCESS_CODE = "_getExecutionState()"
	
	val ReactionsSegment reactionsSegment
	val ClassNameGenerator routinesFacadeNameGenerator
	
	var Map<ReactionsSegment, ReactionsImportPath> includedRoutinesFacades
	var JvmGenericType generatedClass
	
	new(ReactionsSegment reactionsSegment, TypesBuilderExtensionProvider typesBuilderExtensionProvider) {
		super(typesBuilderExtensionProvider);
		if (!reactionsSegment.isReferenceable) {
			throw new IllegalArgumentException("incomplete");
		}
		this.reactionsSegment = reactionsSegment;
		this.routinesFacadeNameGenerator = reactionsSegment.routinesFacadeClassNameGenerator;
	}

	override generateEmptyClass() {
		generatedClass = reactionsSegment.toClass(routinesFacadeNameGenerator.qualifiedName) [
			visibility = JvmVisibility.PUBLIC;
		]
	}

	override generateBody() {
		this.includedRoutinesFacades = reactionsSegment.includedRoutinesFacades;
		generatedClass => [
			superTypes += typeRef(AbstractRoutinesFacade);
			members += generateConstructor();

			// fields for all routines facades of reactions segments imported with qualified names,
			// including transitively included routines facades for imports without qualified names:
			members += includedRoutinesFacades.entrySet.map [
				val includedReactionsSegment = it.key;
				val includedRoutinesFacadeFieldName = includedReactionsSegment.name;
				val includedRoutinesFacadeClassName = includedReactionsSegment.routinesFacadeClassNameGenerator.
					qualifiedName;
				reactionsSegment.toField(includedRoutinesFacadeFieldName, typeRef(includedRoutinesFacadeClassName)) [
					final = true;
					visibility = JvmVisibility.PUBLIC;
				]
			]

			// included original routines: own routines and routines imported without qualified names, including transitively included routines,
			// without any override routines
			members += reactionsSegment.getIncludedRoutines(true, false).entrySet.map [
				val routine = it.key;
				val relativeImportPath = it.value.relativeToRoot;
				if (relativeImportPath.isEmpty) {
					// regular local routine:
					generateCallMethod(routine);
				} else {
					// included external routine:
					generateIncludedRoutineCallMethod(routine, relativeImportPath);
				}
			];
		]
	}

	protected def JvmConstructor generateConstructor() {
		return reactionsSegment.toConstructor() [
			val routinesFacadesProviderParameter = generateParameter(new AccessibleElement("routinesFacadesProvider", RoutinesFacadesProvider));
			val reactionsImportPathParameter = generateParameter(new AccessibleElement("reactionsImportPath", ReactionsImportPath));
			parameters += routinesFacadesProviderParameter;
			parameters += reactionsImportPathParameter;
			body = '''
			super(«routinesFacadesProviderParameter.name», «reactionsImportPathParameter.name»);
			«this.getExtendedConstructorBody()»'''
		]
	}

	protected def StringConcatenationClient getExtendedConstructorBody() '''
		«FOR includedRoutinesFacadeEntry : includedRoutinesFacades.entrySet»
			«val includedReactionsSegment = includedRoutinesFacadeEntry.key»
			«val includedSegmentImportPath = includedRoutinesFacadeEntry.value»
			«val includedRoutinesFacadeFieldName = includedReactionsSegment.name»
			this.«includedRoutinesFacadeFieldName» = «includedSegmentImportPath.relativeToRoot.generateGetRoutinesFacadeCall»;
		«ENDFOR»
	'''

	protected def JvmOperation generateCallMethod(Routine routine) {
		val routineNameGenerator = routine.routineClassNameGenerator;
		routine.associatePrimary(routine.toMethod(routine.name, typeRef(Boolean.TYPE)) [
			visibility = JvmVisibility.PUBLIC;
			parameters += routine.inputElementsParameter
			body = '''
				«typeRef(routinesFacadeNameGenerator.qualifiedName)» _routinesFacade = «generateGetOwnRoutinesFacade()»;
				«ReactionExecutionState» _executionState = «EXECUTION_STATE_ACCESS_CODE»;
				«CallHierarchyHaving» _caller = this._getCurrentCaller();
				«typeRef(routineNameGenerator.qualifiedName)» routine = new «typeRef(routineNameGenerator.qualifiedName)»(_routinesFacade, _executionState, _caller«
					»«FOR parameter : parameters BEFORE ', ' SEPARATOR ', '»«parameter.name»«ENDFOR»);
				return routine.execute();
			'''
		])
	}

	// included routine (original, no override), together the relative import path to the routine's segment:
	private def JvmOperation generateIncludedRoutineCallMethod(Routine routine,
		ReactionsImportPath relativeImportPath) {
		val routinesFacadeNameGenerator = routine.reactionsSegment.routinesFacadeClassNameGenerator;
		routine.associatePrimary(routine.toMethod(routine.name, typeRef(Boolean.TYPE)) [
			visibility = JvmVisibility.PUBLIC;
			parameters += routine.inputElementsParameter
			body = '''
				«routinesFacadeNameGenerator.qualifiedName» _routinesFacade = «relativeImportPath.generateGetRoutinesFacadeCall»;
				return _routinesFacade.«routine.name»(«FOR parameter : parameters SEPARATOR ', '»«parameter.name»«ENDFOR»);
			'''
		])
	}
	
	private def Iterable<JvmFormalParameter> getInputElementsParameter(Routine routine) {
		routine.generateParameters(getInputElements(routine.input.modelInputElements, routine.input.javaInputElements, routine.input.enumInputElements))
	}

	protected def StringConcatenationClient generateGetOwnRoutinesFacade() '''
	this'''

	private def StringConcatenationClient generateGetRoutinesFacadeCall(ReactionsImportPath relativeImportPath) '''
	this._getRoutinesFacadesProvider().getRoutinesFacade(this._getReactionsImportPath().append(«ReactionsImportPath».fromPathString("«relativeImportPath.pathString»")))'''
}
