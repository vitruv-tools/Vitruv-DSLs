package tools.vitruv.dsls.commonalities.tests.util;

import com.google.common.base.Objects;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import tools.vitruv.dsls.commonalities.language.Commonality;
import tools.vitruv.dsls.commonalities.language.LanguagePackage;
import tools.vitruv.dsls.commonalities.language.elements.EClassAdapter;
import tools.vitruv.dsls.commonalities.language.elements.EFeatureAdapter;
import tools.vitruv.testutils.printing.ModelPrinter;
import tools.vitruv.testutils.printing.PrintIdProvider;
import tools.vitruv.testutils.printing.PrintResult;
import tools.vitruv.testutils.printing.PrintResultExtension;
import tools.vitruv.testutils.printing.PrintTarget;

@SuppressWarnings("all")
public class CommonalitiesLanguageElementsPrinter implements ModelPrinter {
  @Override
  public PrintResult printObject(final PrintTarget target, final PrintIdProvider idProvider, final Object object) {
    PrintResult _switchResult = null;
    boolean _matched = false;
    if (object instanceof EClassAdapter) {
      _matched=true;
    }
    if (!_matched) {
      if (object instanceof EFeatureAdapter) {
        _matched=true;
      }
    }
    if (_matched) {
      final Function1<PrintTarget, PrintResult> _function = (PrintTarget it) -> {
        return it.print(((MinimalEObjectImpl.Container)object).toString());
      };
      _switchResult = this.printReference(target, _function);
    }
    if (!_matched) {
      _switchResult = PrintResult.NOT_RESPONSIBLE;
    }
    return _switchResult;
  }

  @Override
  public PrintResult printObjectShortened(final PrintTarget target, final PrintIdProvider idProvider, final Object object) {
    PrintResult _switchResult = null;
    boolean _matched = false;
    if (object instanceof Commonality) {
      _matched=true;
      final Function1<PrintTarget, PrintResult> _function = (PrintTarget it) -> {
        return it.print(((Commonality)object).toString());
      };
      _switchResult = this.printReference(target, _function);
    }
    if (!_matched) {
      _switchResult = this.printObject(target, idProvider, object);
    }
    return _switchResult;
  }

  @Override
  public PrintResult printFeatureValue(final PrintTarget target, final PrintIdProvider idProvider, final EObject object, final EStructuralFeature feature, final Object value) {
    PrintResult _switchResult = null;
    boolean _matched = false;
    if (Objects.equal(feature, LanguagePackage.Literals.COMMONALITY_REFERENCE__REFERENCE_TYPE)) {
      _matched=true;
      final Function1<PrintTarget, PrintResult> _function = (PrintTarget it) -> {
        return this.printObjectShortened(it, idProvider, value);
      };
      _switchResult = this.printReference(target, _function);
    }
    if (!_matched) {
      _switchResult = PrintResult.NOT_RESPONSIBLE;
    }
    return _switchResult;
  }

  public PrintResult printReference(@Extension final PrintTarget target, final Function1<? super PrintTarget, ? extends PrintResult> valuePrinter) {
    PrintResult _print = target.print("↪");
    PrintResult _apply = valuePrinter.apply(target);
    return PrintResultExtension.operator_plus(_print, _apply);
  }

  @Override
  public ModelPrinter withSubPrinter(final ModelPrinter subPrinter) {
    return this;
  }
}
