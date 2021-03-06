package tools.vitruv.dsls.reactions.runtime

import org.eclipse.emf.ecore.EObject

interface ReactionElementsHandler {
	def void addCorrespondenceBetween(EObject firstElement, EObject secondElement, String tag);
	def void removeCorrespondenceBetween(EObject firstElement, EObject secondElement, String tag);
	def void deleteObject(EObject element);
}