package tools.vitruv.dsls.reactions.builder;

import allElementTypes.AllElementTypesPackage;
import allElementTypes2.AllElementTypes2Package;
import javax.inject.Inject;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.extension.ExtendWith;
import tools.vitruv.dsls.reactions.tests.ReactionsLanguageInjectorProvider;

@ExtendWith(InjectionExtension.class)
@InjectWith(ReactionsLanguageInjectorProvider.class)
@SuppressWarnings("all")
public abstract class FluentReactionsBuilderTest {
  protected static final EClass Root = AllElementTypesPackage.eINSTANCE.getRoot();

  protected static final EClass NonRoot = AllElementTypesPackage.eINSTANCE.getNonRoot();

  protected static final EClass Root2 = AllElementTypes2Package.eINSTANCE.getRoot2();

  protected static final EClass EObject = EcorePackage.eINSTANCE.getEObject();

  @Inject
  protected GeneratedReactionsMatcherBuilder matcher;

  @Inject
  protected FluentReactionsLanguageBuilder create;

  protected Matcher<? super FluentReactionsFileBuilder> builds(final String reactionsTest) {
    return this.matcher.builds(reactionsTest);
  }
}
