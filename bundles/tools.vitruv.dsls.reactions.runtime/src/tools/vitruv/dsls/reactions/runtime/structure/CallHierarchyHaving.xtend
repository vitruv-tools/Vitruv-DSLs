package tools.vitruv.dsls.reactions.runtime.structure

class CallHierarchyHaving extends Loggable {
	val CallHierarchyHaving calledBy;
	
	new() {
		calledBy = null;
	}
	
	new (CallHierarchyHaving calledBy) {
		this.calledBy = calledBy;
	}
	
	def CallHierarchyHaving getCalledBy() {
		return calledBy;
	}
	
	def String getCalledByString() {
		return '''(«this.class.simpleName»)«IF calledBy !== null» called by «calledBy.calledByString»«ENDIF»''';
	}
}