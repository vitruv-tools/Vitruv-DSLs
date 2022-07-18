package tools.vitruv.dsls.reactions.runtime;

import tools.vitruv.dsls.reactions.runtime.correspondence.ReactionsCorrespondence;
import tools.vitruv.change.correspondence.CorrespondenceModelView;
import tools.vitruv.change.correspondence.CorrespondenceModelViewFactory;
import tools.vitruv.change.correspondence.InternalCorrespondenceModel;
import tools.vitruv.change.correspondence.impl.CorrespondenceModelViewImpl;
import tools.vitruv.dsls.reactions.runtime.correspondence.CorrespondenceFactory;

public class ReactionsCorrespondenceModelViewFactory implements
		CorrespondenceModelViewFactory<CorrespondenceModelView<ReactionsCorrespondence>> {
	private static ReactionsCorrespondenceModelViewFactory instance;
	
	private ReactionsCorrespondenceModelViewFactory() {}
	
	public static synchronized ReactionsCorrespondenceModelViewFactory getInstance() {
		if (instance == null) {
			instance = new ReactionsCorrespondenceModelViewFactory();
		}
		return instance;
	}
	
	@Override
	public CorrespondenceModelView<ReactionsCorrespondence> createCorrespondenceModelView(
			InternalCorrespondenceModel correspondenceModel) {
		return new CorrespondenceModelViewImpl<ReactionsCorrespondence>(ReactionsCorrespondence.class,
				correspondenceModel, null);
	}

	@Override
	public CorrespondenceModelView<ReactionsCorrespondence> createEditableCorrespondenceModelView(
			InternalCorrespondenceModel correspondenceModel) {
		return new CorrespondenceModelViewImpl<ReactionsCorrespondence>(ReactionsCorrespondence.class,
				correspondenceModel, () -> CorrespondenceFactory.eINSTANCE.createReactionsCorrespondence());
	}

}
