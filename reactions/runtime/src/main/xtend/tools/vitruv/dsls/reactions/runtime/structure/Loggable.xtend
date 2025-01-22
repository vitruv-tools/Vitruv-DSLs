package tools.vitruv.dsls.reactions.runtime.structure

import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.LogManager

class Loggable {
	val Logger LOGGER;
	
	new() {
		LOGGER = LogManager.getLogger(this.class);
	}
	
	protected def Logger getLogger() {
		return LOGGER;
	}
}