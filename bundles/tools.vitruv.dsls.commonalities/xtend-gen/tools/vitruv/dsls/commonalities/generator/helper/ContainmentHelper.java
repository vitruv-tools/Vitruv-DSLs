package tools.vitruv.dsls.commonalities.generator.helper;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.inject.Inject;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.commonalities.generator.GenerationContext;
import tools.vitruv.dsls.commonalities.generator.util.guice.GenerationScoped;
import tools.vitruv.dsls.commonalities.language.ParticipationAttribute;
import tools.vitruv.dsls.commonalities.participation.ReferenceContainment;
import tools.vitruv.dsls.commonalities.runtime.operators.participation.relation.ContainmentOperator;

@GenerationScoped
@SuppressWarnings("all")
public class ContainmentHelper {
  @Inject
  @Extension
  protected GenerationContext _generationContext;

  ContainmentHelper() {
  }

  private final Map<ReferenceContainment, EReference> containmentReferences = new HashMap<ReferenceContainment, EReference>();

  public EReference getEReference(final ReferenceContainment containment) {
    final Function<ReferenceContainment, EReference> _function = (ReferenceContainment it) -> {
      final EClass containerEClass = this._generationContext.getChangeClass(it.getContainer());
      final EClass containedEClass = this._generationContext.getChangeClass(it.getContained());
      ParticipationAttribute _reference = it.getReference();
      boolean _tripleEquals = (_reference == null);
      if (_tripleEquals) {
        return ContainmentHelper.getContainmentReference(containerEClass, containedEClass);
      } else {
        EStructuralFeature _correspondingEFeature = this._generationContext.getCorrespondingEFeature(it.getReference());
        return ((EReference) _correspondingEFeature);
      }
    };
    return this.containmentReferences.computeIfAbsent(containment, _function);
  }

  public static EReference getContainmentReference(final EClass container, final EClass contained) {
    return ContainmentOperator.getContainmentReference(container, container);
  }
}
