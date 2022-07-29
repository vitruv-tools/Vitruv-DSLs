package tools.vitruv.dsls.commonalities.generator.reactions.attribute

import com.google.inject.Inject
import edu.kit.ipd.sdq.activextendannotations.Lazy
import java.util.List
import java.util.Set
import tools.vitruv.dsls.commonalities.generator.reactions.ReactionsSubGenerator
import tools.vitruv.dsls.commonalities.generator.reactions.attribute.AttributeMappingOperatorHelper.AttributeMappingOperatorContext
import tools.vitruv.dsls.commonalities.generator.reactions.participation.ParticipationObjectsRetrievalHelper
import tools.vitruv.dsls.commonalities.generator.util.guice.InjectingFactoryBase
import tools.vitruv.dsls.commonalities.language.CommonalityAttribute
import tools.vitruv.dsls.commonalities.language.CommonalityAttributeMapping
import tools.vitruv.dsls.commonalities.language.Participation
import tools.vitruv.dsls.commonalities.language.ParticipationClass
import tools.vitruv.dsls.reactions.builder.FluentReactionsSegmentBuilder
import tools.vitruv.dsls.reactions.builder.FluentRoutineBuilder.RoutineStartBuilder
import tools.vitruv.dsls.reactions.builder.FluentRoutineBuilder.UndecidedMatchStatementBuilder

import static com.google.common.base.Preconditions.*

import static extension tools.vitruv.dsls.commonalities.language.extensions.CommonalitiesLanguageModelExtensions.*
import tools.vitruv.dsls.reactions.builder.FluentRoutineBuilder.UpdateStatementBuilder

class CommonalityAttributeChangeReactionsBuilder extends ReactionsSubGenerator {

	static class Factory extends InjectingFactoryBase {
		def createFor(CommonalityAttribute attribute, Participation targetParticipation) {
			return new CommonalityAttributeChangeReactionsBuilder(attribute, targetParticipation).injectMembers
		}
	}

	@Inject extension AttributeMappingHelper attributeMappingHelper
	@Inject extension AttributeChangeReactionsHelper attributeChangeReactionsHelper
	@Inject extension ParticipationObjectsRetrievalHelper participationObjectsRetrievalHelper

	val CommonalityAttribute attribute
	val Participation targetParticipation

	@Lazy val List<CommonalityAttributeMapping> relevantMappings = calculateRelevantMappings()
	@Lazy val Set<ParticipationClass> relevantParticipationClasses = calculateRelevantParticipationClasses()

	private new(CommonalityAttribute attribute, Participation targetParticipation) {
		checkNotNull(attribute, "attribute is null")
		checkNotNull(targetParticipation, "targetParticipation is null")
		this.attribute = attribute
		this.targetParticipation = targetParticipation
	}

	// Dummy constructor for Guice
	package new() {
		this.attribute = null
		this.targetParticipation = null
		throw new IllegalStateException("Use the Factory to create instances of this class!")
	}

	private def calculateRelevantMappings() {
		return attribute.getRelevantWriteMappings(targetParticipation).toList
	}

	private def calculateRelevantParticipationClasses() {
		return relevantMappings.flatMap[involvedParticipationClasses].toSet
	}

	def void generateReactions(FluentReactionsSegmentBuilder segment) {
		if (relevantMappings.size === 0) return;
		segment += reactionsForCommonalityAttributeChange
	}

	private def reactionsForCommonalityAttributeChange() {
		return attribute.getAttributeChangeReactions [ changeType , it |
			call[buildAttributeChangedRoutine]
		]
	}

	private def buildAttributeChangedRoutine(extension RoutineStartBuilder routineBuilder) {
		input [
			affectedEObject
		]
		.match [
			retrieveRelevantParticipationObjects()
		]
		.update [
			applyMappings()
		]
	}

	private def retrieveRelevantParticipationObjects(extension UndecidedMatchStatementBuilder matcherBuilder) {
		relevantParticipationClasses.forEach [ participationClass |
			// Note: If the intermediate has been moved during creation (eg. due to an attribute reference match), we
			// might receive attribute change events for the intermediate even though the creation of the target
			// participation is still pending. We ignore the attribute change event in this case (therefore unasserted
			// retrieval of participation objects). Instead, the attributes of the intermediate get applied to the
			// target participation during its later creation.
			matcherBuilder.retrieveUnassertedParticipationObject(participationClass) [
				affectedEObject // correspondence source
			]
		]
	}

	private def void applyMappings(extension UpdateStatementBuilder updateBuilder) {
		for (mapping : relevantMappings) {
			execute [ extension typeProvider |
				val participationClassToObject = typeProvider.participationClassToOptionalObject
				val operatorContext = new AttributeMappingOperatorContext(typeProvider, [affectedEObject],
					participationClassToObject)
				mapping.applyWriteMapping(operatorContext)
			]
		}
	}

}
