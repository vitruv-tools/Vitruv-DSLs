package tools.vitruv.dsls.reactions.builder

import java.util.function.Consumer
import java.util.function.Function
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EClassifier
import org.eclipse.emf.ecore.EDataType
import org.eclipse.xtend.lib.annotations.Accessors
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.common.types.JvmOperation
import org.eclipse.xtext.xbase.XExpression
import org.eclipse.xtext.xbase.XbaseFactory
import tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsFactory
import tools.vitruv.dsls.reactions.language.RetrieveModelElement
import tools.vitruv.dsls.reactions.language.RetrieveOrRequireAbscenceOfModelElement
import tools.vitruv.dsls.reactions.language.toplevelelements.Routine
import tools.vitruv.dsls.reactions.language.toplevelelements.RoutineOverrideImportPath

import static com.google.common.base.Preconditions.*
import static tools.vitruv.dsls.reactions.codegen.ReactionsLanguageConstants.*
import tools.vitruv.dsls.reactions.language.LanguageFactory
import tools.vitruv.dsls.common.elements.ElementsFactory
import org.eclipse.xtext.xbase.XBlockExpression
import tools.vitruv.dsls.common.elements.NamedMetaclassReference
import org.eclipse.xtext.xbase.XFeatureCall
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine

class FluentRoutineBuilder extends FluentReactionsSegmentChildBuilder {

	@Accessors(PACKAGE_GETTER)
	protected var Routine routine
	@Accessors(PACKAGE_GETTER)
	protected var requireOldValue = false
	@Accessors(PACKAGE_GETTER)
	protected var requireNewValue = false
	@Accessors(PACKAGE_GETTER)
	protected boolean requireAffectedEObject = false
	@Accessors(PACKAGE_GETTER)
	protected var requireAffectedValue = false

	var EClassifier valueType
	var EClass affectedObjectType

	package new(String routineName, FluentBuilderContext context) {
		super(context)
		this.routine = TopLevelElementsFactory.eINSTANCE.createRoutine => [
			name = routineName
			input = TopLevelElementsFactory.eINSTANCE.createRoutineInput
		]
	}

	override protected attachmentPreparation() {
		super.attachmentPreparation()
		checkState((!requireOldValue && !requireNewValue && !requireAffectedValue) || valueType !== null,
			'''Although required, there was no value type set on the «this»''')
		checkState(!requireAffectedEObject || affectedObjectType !== null,
			'''Although required, there was no affected object type set on the «this»''')
		if (requireAffectedEObject) {
			addInputElementIfNotExists(affectedObjectType, CHANGE_AFFECTED_ELEMENT_ATTRIBUTE)
		}
		if (requireOldValue) {
			addInputElementIfNotExists(valueType, CHANGE_OLD_VALUE_ATTRIBUTE)
		}
		if (requireNewValue) {
			addInputElementIfNotExists(valueType, CHANGE_NEW_VALUE_ATTRIBUTE)
		}
	}

	def package start() {
		new RoutineStartBuilder(this)
	}

	def private addInputElementIfNotExists(EClassifier type, String parameterName) {
		if (routine.input.modelInputElements.findFirst[name == parameterName] !== null) return;
		addInputElement(type, parameterName)
	}

	def private dispatch void addInputElement(EClass type, String parameterName) {
		routine.input.modelInputElements += (ElementsFactory.eINSTANCE.createNamedMetaclassReference => [
			name = parameterName
		]).reference(type)
	}

	def private dispatch void addInputElement(EDataType type, String parameterName) {
		addInputElement(type.instanceClass, parameterName)
	}

	def private dispatch void addInputElement(Class<?> type, String parameterName) {
		routine.input.javaInputElements += (TopLevelElementsFactory.eINSTANCE.createNamedJavaElementReference => [
			name = parameterName
		]).reference(type)
	}

	def package setValueType(EClassifier type) {
		if (valueType === null) {
			valueType = type
		}
		if (!valueType.isAssignableFrom(type)) {
			throw new IllegalStateException('''The «this» already has the value type “«valueType.name»«
				»” set, which is not a super type of “«type.name»”. The value type can thus not be set to “«
				»«type.name»”!''')
		}
	}

	def package setAffectedObjectType(EClass type) {
		if (affectedObjectType === null) {
			affectedObjectType = type
		}
		if (!affectedObjectType.isAssignableFrom(type)) {
			throw new IllegalStateException('''The «this» already has the affected object type “«
				»«affectedObjectType.name»” set, which is not a super type of “«type.name»«
				»”. The affected element type can thus not be set to “«type.name»”!''')
		}
	}

	def static dispatch isAssignableFrom(EDataType a, EClass b) {
		false
	}

	def static dispatch isAssignableFrom(EClass a, EDataType b) {
		false
	}

	def static dispatch isAssignableFrom(EClass a, EClass b) {
		EcoreUtil2.isAssignableFrom(a, b)
	}

	def static dispatch isAssignableFrom(EDataType a, EDataType b) {
		a.instanceClass.isAssignableFrom(b.instanceClass)
	}

	static class CreatorOrUpdateBuilder extends UpdateBuilder {

		private new(FluentRoutineBuilder builder) {
			super(builder)
		}

		def create(Consumer<CreateStatementBuilder> creates) {
			routine.createBlock = TopLevelElementsFactory.eINSTANCE.createCreateBlock
			val statementBuilder = new CreateStatementBuilder(builder)
			creates.accept(statementBuilder)
			new UpdateBuilder(builder)
		}
	}
	
	static class MatchBlockOrCreatorOrUpdateBuilder extends CreatorOrUpdateBuilder {

		private new(FluentRoutineBuilder builder) {
			super(builder)
		}

		def match(Consumer<UndecidedMatchStatementBuilder> matches) {
			val matchBlock = TopLevelElementsFactory.eINSTANCE.createMatchBlock
			routine.matchBlock = matchBlock
			val statementsBuilder = new UndecidedMatchStatementBuilder(builder)
			matches.accept(statementsBuilder)
			if (routine.matchBlock.matchStatements.size == 0) {
				// remove matchBlock again:
				routine.matchBlock = null
			}
			new CreatorOrUpdateBuilder(builder)
		}
	}
	

	static class InputOrMatchBlockOrCreatorOrUpdateBuilder extends MatchBlockOrCreatorOrUpdateBuilder {

		private new(FluentRoutineBuilder builder) {
			super(builder)
		}

		def input(Consumer<InputBuilder> inputs) {
			inputs.accept(new InputBuilder(builder))
			new MatchBlockOrCreatorOrUpdateBuilder(builder)
		}

	}

	static class RoutineStartBuilder extends InputOrMatchBlockOrCreatorOrUpdateBuilder {

		private new(FluentRoutineBuilder builder) {
			super(builder)
		}

		def retrieveRoutineBuilder() {
			builder
		}

		def alwaysRequireAffectedEObject() {
			requireAffectedEObject = true
			this
		}

		def alwaysRequireNewValue() {
			requireNewValue = true
			this
		}

		def overrideAlongImportPath(FluentReactionsSegmentBuilder... importPathSegmentBuilders) {
			if (!importPathSegmentBuilders.nullOrEmpty) {
				var RoutineOverrideImportPath currentImportPath = null
				for (pathSegmentBuilder : importPathSegmentBuilders) {
					val nextPathSegment = TopLevelElementsFactory.eINSTANCE.createRoutineOverrideImportPath
					nextPathSegment.reactionsSegment = pathSegmentBuilder.segment
					nextPathSegment.parent = currentImportPath
					currentImportPath = nextPathSegment
				}
				routine.overrideImportPath = currentImportPath
			}
			new InputOrMatchBlockOrCreatorOrUpdateBuilder(builder)
		}
	}

	static class InputBuilder {
		val extension FluentRoutineBuilder builder

		private new(FluentRoutineBuilder builder) {
			this.builder = builder
		}

		def void model(EClass eClass, String parameterName) {
			detectWellKnownType(eClass, parameterName)
			addInputElement(eClass, parameterName)
		}

		def void model(EClass eClass, WellKnownModelInput wellKnown) {
			wellKnown.apply(eClass)
		}

		def private detectWellKnownType(EClass eClass, String parameterName) {
			switch (parameterName) {
				case CHANGE_OLD_VALUE_ATTRIBUTE,
				case CHANGE_NEW_VALUE_ATTRIBUTE: valueType = eClass
				case CHANGE_AFFECTED_ELEMENT_ATTRIBUTE: affectedObjectType = eClass
			}
		}

		def WellKnownModelInput newValue() {
			requireNewValue = true
			return [valueType = it]
		}

		def WellKnownModelInput oldValue() {
			requireOldValue = true
			return [valueType = it]
		}

		def WellKnownModelInput affectedEObject() {
			requireAffectedEObject = true
			return [affectedObjectType = it]
		}

		def void plain(Class<?> javaClass, String parameterName) {
			addInputElement(javaClass, parameterName)
		}
	}

	interface WellKnownModelInput {
		def void apply(EClass type)
	}

	static class RetrieveModelElementMatchBlockStatementBuilder {
		val extension FluentRoutineBuilder builder
		val RetrieveModelElement statement

		private new(FluentRoutineBuilder builder, RetrieveModelElement statement) {
			this.builder = builder
			this.statement = statement
		}

		def retrieve(EClass modelElement) {
			internalRetrieveOne(modelElement)
			return new RetrieveModelElementMatchBlockStatementCorrespondenceBuilder(builder, statement)
		}

		def retrieveOptional(EClass modelElement) {
			val retrieveOneStatement = internalRetrieveOne(modelElement);
			retrieveOneStatement.optional = true
			return new RetrieveModelElementMatchBlockStatementCorrespondenceBuilder(builder, statement)
		}

		def retrieveAsserted(EClass modelElement) {
			val retrieveOneStatement = internalRetrieveOne(modelElement);
			retrieveOneStatement.asserted = true
			return new RetrieveModelElementMatchBlockStatementCorrespondenceBuilder(builder, statement)
		}

		def retrieveMany(EClass modelElement) {
			reference(modelElement)
			statement.retrievalType = LanguageFactory.eINSTANCE.createRetrieveManyModelElements();
			return new RetrieveModelElementMatchBlockStatementCorrespondenceBuilder(builder, statement)
		}

		private def internalRetrieveOne(EClass modelElement) {
			reference(modelElement)
			val retrieveOneElement = LanguageFactory.eINSTANCE.createRetrieveOneModelElement();
			statement.retrievalType = retrieveOneElement
			return retrieveOneElement
		}

		private def void reference(EClass modelElement) {
			statement.elementType = ElementsFactory.eINSTANCE.createMetaclassReference().reference(modelElement)
		}
	}

	static class RetrieveModelElementMatchBlockStatementCorrespondenceBuilder {
		val extension FluentRoutineBuilder builder
		val RetrieveOrRequireAbscenceOfModelElement statement

		private new(FluentRoutineBuilder builder, RetrieveOrRequireAbscenceOfModelElement statement) {
			this.builder = builder
			this.statement = statement
		}

		def correspondingTo() {
			new RetrieveModelElementMatchBlockStatementCorrespondenceElementBuilder(builder, statement)
		}

		def correspondingTo(String element) {
			statement.correspondenceSource = correspondingElement(element)
			new RetrieveModelElementMatchBlockStatementTagBuilder(builder, statement)
		}

		def correspondingTo(Function<TypeProvider, XExpression> expressionBuilder) {
			statement.correspondenceSource = correspondingElement(expressionBuilder)
			new RetrieveModelElementMatchBlockStatementTagBuilder(builder, statement)
		}
	}

	static class RetrieveModelElementMatchBlockStatementCorrespondenceElementBuilder {
		val extension FluentRoutineBuilder builder
		val RetrieveOrRequireAbscenceOfModelElement statement

		private new(FluentRoutineBuilder builder, RetrieveOrRequireAbscenceOfModelElement statement) {
			this.builder = builder
			this.statement = statement
		}

		def affectedEObject() {
			requireAffectedEObject = true
			statement.correspondenceSource = correspondingElement(CHANGE_AFFECTED_ELEMENT_ATTRIBUTE)
			new RetrieveModelElementMatchBlockStatementTagBuilder(builder, statement)
		}

		def newValue() {
			requireNewValue = true
			statement.correspondenceSource = correspondingElement(CHANGE_NEW_VALUE_ATTRIBUTE)
			new RetrieveModelElementMatchBlockStatementTagBuilder(builder, statement)
		}

		def oldValue() {
			requireOldValue = true
			statement.correspondenceSource = correspondingElement(CHANGE_OLD_VALUE_ATTRIBUTE)
			new RetrieveModelElementMatchBlockStatementTagBuilder(builder, statement)
		}
	}

	static class UndecidedMatchStatementBuilder {
		val extension FluentRoutineBuilder builder

		private new(FluentRoutineBuilder builder) {
			this.builder = builder
		}

		def vall(String valName) {
			val statement = LanguageFactory.eINSTANCE.createRetrieveModelElement => [
				name = valName
			]
			routine.matchBlock.matchStatements += statement
			new RetrieveModelElementMatchBlockStatementBuilder(builder, statement)
		}

		def requireAbsenceOf(EClass absentMetaclass) {
			val statement = LanguageFactory.eINSTANCE.createRequireAbscenceOfModelElement() => [
				elementType = ElementsFactory.eINSTANCE.createMetaclassReference().reference(absentMetaclass)
			]
			routine.matchBlock.matchStatements += statement
			return new RetrieveModelElementMatchBlockStatementCorrespondenceBuilder(builder, statement)
		}

		def check(Function<TypeProvider, XExpression> expressionBuilder) {
			val statement = LanguageFactory.eINSTANCE.createMatchCheckStatement => [
				condition = XbaseFactory.eINSTANCE.createXBlockExpression.whenJvmTypes [
					expressions += extractExpressions(expressionBuilder.apply(typeProvider))
				]
			]
			routine.matchBlock.matchStatements += statement
			return statement
		}

		def checkAsserted(Function<TypeProvider, XExpression> expressionBuilder) {
			val statement = check(expressionBuilder);
			statement.asserted = true;
		}
	}

	static class RetrieveModelElementMatchBlockStatementTagBuilder {
		val extension FluentRoutineBuilder builder
		val RetrieveOrRequireAbscenceOfModelElement statement

		private new(FluentRoutineBuilder builder, RetrieveOrRequireAbscenceOfModelElement statement) {
			this.builder = builder
			this.statement = statement
		}
		
		def void taggedWithAnything() {
			statement.tag = XbaseFactory.eINSTANCE.createXNullLiteral
		}

		def void taggedWith(String tag) {
			statement.tag = XbaseFactory.eINSTANCE.createXStringLiteral => [
				value = tag
			]
		}

		def void taggedWith(Function<TypeProvider, XExpression> tagExpressionBuilder) {
			statement.tag = XbaseFactory.eINSTANCE.createXBlockExpression.whenJvmTypes [
				expressions += extractExpressions(tagExpressionBuilder.apply(typeProvider))
			]
		}
	}
	
	static class CreateStatementBuilder {
		val extension FluentRoutineBuilder builder
		
		private new(FluentRoutineBuilder builder) {
			this.builder = builder
		}
		
		def vall(String vallName) {
			val statement = ElementsFactory.eINSTANCE.createNamedMetaclassReference => [
				name = vallName
			]
			routine.createBlock.createStatements += statement
			new CreateStatementTypeBuilder(builder, statement)
		}
	}
	
	static class CreateStatementTypeBuilder {
		val extension FluentRoutineBuilder builder
		val NamedMetaclassReference statement

		private new(FluentRoutineBuilder builder, NamedMetaclassReference statement) {
			this.builder = builder
			this.statement = statement
		}

		def create(EClass element) {
			statement.reference(element)
		}
	}

	static class UpdateBuilder {
		protected val extension FluentRoutineBuilder builder

		private new(FluentRoutineBuilder builder) {
			this.builder = builder
		}
		
		def withoutUpdate() {
			readyToBeAttached = true
			return builder
		}

		def update(Consumer<UpdateStatementBuilder> updates) {
			routine.updateBlock = TopLevelElementsFactory.eINSTANCE.createUpdateBlock
			val statementBuilder = new UpdateStatementBuilder(builder)
			updates.accept(statementBuilder)
			readyToBeAttached = true
			return builder
		}
	}

	static class UpdateStatementBuilder {
		val extension FluentRoutineBuilder builder
		val XBlockExpression expressionBlock
		
		private new(FluentRoutineBuilder builder) {
			this.builder = builder
			this.expressionBlock = XbaseFactory.eINSTANCE.createXBlockExpression
			routine.updateBlock.code = expressionBlock
		}

		def delete(String existingElement) {
			val statement = (XbaseFactory.eINSTANCE.createXFeatureCall => [
				explicitOperationCall = true
			]).whenJvmTypes [
				feature = typeProvider.findMethod(AbstractRoutine.Update, 'removeObject')
				featureCallArguments += existingElement(existingElement)
			]
			expressionBlock.expressions += statement
		}

		def addCorrespondenceBetween() {
			val statement = createCorrespondenceMethodCall()
			expressionBlock.expressions += statement
			new CorrespondenceElementBuilder(builder, new CorrespondenceTargetBuilder(builder, statement), [
				statement.featureCallArguments.add(0, it)
			])
		}

		def addCorrespondenceBetween(String existingElement) {
			val statement = createCorrespondenceMethodCall() => [
				featureCallArguments += existingElement(existingElement)
			]
			expressionBlock.expressions += statement
			new CorrespondenceTargetBuilder(builder, statement)
		}

		def addCorrespondenceBetween(Function<TypeProvider, XExpression> expressionBuilder) {
			val statement = createCorrespondenceMethodCall() => [
				featureCallArguments += existingElement(expressionBuilder)
			]
			expressionBlock.expressions += statement
			new CorrespondenceTargetBuilder(builder, statement)
		}

		private def createCorrespondenceMethodCall() {
			(XbaseFactory.eINSTANCE.createXFeatureCall => [
				explicitOperationCall = true
			]).whenJvmTypes [
				feature = typeProvider.findMethod(AbstractRoutine.Update, 'addCorrespondenceBetween') 
			]
		}
		
		def removeCorrespondenceBetween() {
			val statement = deleteCorrespondenceMethodCall()
			expressionBlock.expressions += statement
			new CorrespondenceElementBuilder(builder, new CorrespondenceTargetBuilder(builder, statement), [
				statement.featureCallArguments += it
			])
		}

		def removeCorrespondenceBetween(String existingElement) {
			val statement = deleteCorrespondenceMethodCall() => [
				featureCallArguments += existingElement(existingElement)
			]
			expressionBlock.expressions += statement
			new CorrespondenceTargetBuilder(builder, statement)
		}

		def removeCorrespondenceBetween(Function<TypeProvider, XExpression> expressionBuilder) {
			val statement = deleteCorrespondenceMethodCall() => [
				featureCallArguments +=  existingElement(expressionBuilder)
			]
			expressionBlock.expressions += statement
			new CorrespondenceTargetBuilder(builder, statement)
		}
		
		private def deleteCorrespondenceMethodCall() {
			(XbaseFactory.eINSTANCE.createXFeatureCall => [
				explicitOperationCall = true
			]).whenJvmTypes [
				feature = typeProvider.findMethod(AbstractRoutine.Update, 'removeCorrespondenceBetween')
			]
		}

		def XExpression execute(Function<TypeProvider, XExpression> expressionBuilder) {
			val placeholderExpression = XbaseFactory.eINSTANCE.createXBlockExpression
			expressionBlock.expressions += placeholderExpression
			expressionBlock.whenJvmTypes [
				expressions.addAll(expressions.indexOf(placeholderExpression), extractExpressions(expressionBuilder.apply(typeProvider)))
				expressions -= placeholderExpression
			]
			return expressionBlock
		}

		def XExpression call(Function<TypeProvider, XExpression> expressionBuilder) {
			execute(expressionBuilder)
			return expressionBlock
		}

		def void call(FluentRoutineBuilder routineBuilder, RoutineCallParameter... parameters) {
			checkNotNull(routineBuilder)
			checkState(routineBuilder.readyToBeAttached,
				'''The «routineBuilder» is not sufficiently initialised to be set on the «builder»''')
			checkState(!routineBuilder.requireNewValue,
				'''The «routineBuilder» requires a new value, and can thus only be called from reactions, not routines!''')
			checkState(!routineBuilder.requireOldValue || valueType !== null,
				'''The «routineBuilder» requires an old value, and can thus only be called from reactions, not routines!''')
			var hasFittingAffectedEObjectParameter = false
			if (parameters.size > 0) {
				val param = parameters.get(0)
				if (param.parameterArgumentType) {
					// TODO: check if matching type
					hasFittingAffectedEObjectParameter = true
				}
			}
			checkState(!routineBuilder.requireAffectedEObject
				|| (routineBuilder.requireAffectedEObject && hasFittingAffectedEObjectParameter),
				'''The «routineBuilder» requires an affectedEObject, and can thus only be called from reactions, not«
					» routines!''')

			builder.transferReactionsSegmentTo(routineBuilder)
			addRoutineCall(routineBuilder, parameters)
		}

		def private addRoutineCall(FluentRoutineBuilder routineBuilder, RoutineCallParameter... parameters) {
			expressionBlock.expressions += routineBuilder.routineCall(parameters)
		}

		def private routineCall(FluentRoutineBuilder routineBuilder, RoutineCallParameter... parameters) {
			(XbaseFactory.eINSTANCE.createXFeatureCall => [
				explicitOperationCall = true
			]).whenJvmTypes [
				feature = routineBuilder.jvmOperation
				implicitReceiver = jvmOperationRoutineFacade
				val typeProvider = typeProvider
				featureCallArguments += parameters.map[getExpression(typeProvider)]
			]
		}
	}

	static class RoutineCallParameter {
		Object argument;

		new(String parameter) {
			this.argument = parameter
		}

		new(XExpression expression) {
			this.argument = expression
		}

		new(Function<TypeProvider, XExpression> expressionBuilder) {
			this.argument = expressionBuilder
		}

		def isParameterArgumentType() {
			argument instanceof String
		}

		def XExpression getExpression(TypeProvider typeProvider) {
			if (parameterArgumentType) {
				return typeProvider.variable(argument as String)
			} else if (argument instanceof XExpression) {
				return argument
			} else {
				val expressionBuilder = argument as Function<TypeProvider, XExpression>
				return expressionBuilder.apply(typeProvider)
			}
		}
	}

	static class CorrespondenceElementBuilder<NextType> {
		val extension FluentRoutineBuilder builder
		val Consumer<XExpression> elementConsumer
		val NextType next

		private new(FluentRoutineBuilder builder, NextType next, Consumer<XExpression> elementConsumer) {
			this.builder = builder
			this.elementConsumer = elementConsumer
			this.next = next
		}

		def oldValue() {
			requireOldValue = true
			elementConsumer.accept(existingElement(CHANGE_OLD_VALUE_ATTRIBUTE))
			next
		}

		def newValue() {
			requireNewValue = true
			elementConsumer.accept(existingElement(CHANGE_NEW_VALUE_ATTRIBUTE))
			next
		}

		def affectedEObject() {
			requireAffectedEObject = true
			elementConsumer.accept(existingElement(CHANGE_AFFECTED_ELEMENT_ATTRIBUTE))
			next
		}
	}

	static class CorrespondenceTargetBuilder {
		val extension FluentRoutineBuilder builder
		val XFeatureCall statement

		private new(FluentRoutineBuilder builder, XFeatureCall statement) {
			this.builder = builder
			this.statement = statement
		}

		def and() {
			val tagBuilder = new TagWithBuilder(builder, statement)
			new CorrespondenceElementBuilder(builder, tagBuilder, [statement.secondElement = it])
		}

		def and(String existingElement) {
			statement.secondElement = existingElement(existingElement)
			new TagWithBuilder(builder, statement)
		}

		def and(Function<TypeProvider, XExpression> expressionBuilder) {
			statement.secondElement = existingElement(expressionBuilder)
			new TagWithBuilder(builder, statement)
		}

		def private setSecondElement(XFeatureCall correspondenceStatement,
			XExpression existingElement) {
			correspondenceStatement => [
				featureCallArguments += existingElement
			]
		}
	}
	
	static class TagWithBuilder {
		val extension FluentRoutineBuilder builder
		val XFeatureCall correspondenceCreation

		private new(FluentRoutineBuilder builder, XFeatureCall correspondenceCreation) {
			this.builder = builder
			this.correspondenceCreation = correspondenceCreation
		}
		
		def void taggedWithAnything() {
			correspondenceCreation.featureCallArguments += XbaseFactory.eINSTANCE.createXNullLiteral
		}

		def void taggedWith(String tag) {
			correspondenceCreation.featureCallArguments += XbaseFactory.eINSTANCE.createXStringLiteral => [
				value = tag
			]
		}

		def void taggedWith(Function<TypeProvider, XExpression> tagExpressionBuilder) {
			correspondenceCreation.featureCallArguments += XbaseFactory.eINSTANCE.createXBlockExpression.whenJvmTypes [
				expressions += extractExpressions(tagExpressionBuilder.apply(typeProvider))
			]
		}
	}

	def private existingElement(String name) {
		XbaseFactory.eINSTANCE.createXFeatureCall.whenJvmTypes [
			feature = correspondingMethodParameter(name)
		]
	}

	def private existingElement(Function<TypeProvider, XExpression> expressionBuilder) {
		XbaseFactory.eINSTANCE.createXBlockExpression.whenJvmTypes [
			expressions += extractExpressions(expressionBuilder.apply(typeProvider))
		]
	}

	def private correspondingElement(String name) {
		XbaseFactory.eINSTANCE.createXFeatureCall.whenJvmTypes [
			feature = correspondingMethodParameter(name)
		]
	}

	def private correspondingElement(Function<TypeProvider, XExpression> expressionBuilder) {
		XbaseFactory.eINSTANCE.createXBlockExpression.whenJvmTypes [
			expressions += extractExpressions(expressionBuilder.apply(typeProvider))
		]
	}

	override toString() {
		'''routine builder for «routine.name»'''
	}

	def getJvmOperation() {
		val jvmMethod = context.jvmModelAssociator.getPrimaryJvmElement(routine)
		if (jvmMethod instanceof JvmOperation) {
			return jvmMethod
		}
		throw new IllegalStateException('''Could not find the routine facade method corresponding to the routine “«
			»«routine.name»”''')
	}

	override protected getCreatedElementName() {
		routine.name
	}

	override protected getCreatedElementType() {
		"routine"
	}
	
}