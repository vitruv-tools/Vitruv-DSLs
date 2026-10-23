package tools.vitruv.dsls.reactions.codegen.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder;
import static edu.kit.ipd.sdq.commons.util.java.lang.IterableUtil.*;
import org.eclipse.xtext.common.types.JvmUnknownTypeReference;
import java.util.Optional;

public class AccessibleElement {
	private static final String UNKNOWN_TYPEREF_NAME = "unknown";
	private final String name;
	private final String fullyQualifiedTypeName;
	private final List<String> typeParameters;
	
	public AccessibleElement(String name, String fullyQualifiedTypeName) {
		this.name = name;
		this.fullyQualifiedTypeName = fullyQualifiedTypeName;
		this.typeParameters = new ArrayList<>();
	}

	public AccessibleElement(String name, String fullyQualifiedTypeName, String... typeParameters) {
		this(name, fullyQualifiedTypeName);
		this.typeParameters.addAll(List.of(typeParameters));
	}

	public AccessibleElement(String name, Class<?> type) {
		this(name, type.getName());
	}

	public String getName() {
		return name;
	}

	public boolean isOptional() {
		return Optional.class.getName().equals(fullyQualifiedTypeName);
	}
	
	public JvmTypeReference generateTypeRef(JvmTypeReferenceBuilder typeReferenceBuilder) {
		if (fullyQualifiedTypeName == null) {
			return typeReferenceBuilder.typeRef(UNKNOWN_TYPEREF_NAME);
		}
		List<JvmTypeReference> typeParameterReferences = typeParameters.stream()
				.map(parameter ->
          Objects.requireNonNullElse(parameter, UNKNOWN_TYPEREF_NAME)
				)
				.map(typeReferenceBuilder::typeRef)
				.toList();

		// This is a hack to make Xtext print parameterized types even if one of
		// the parameter classes are not currently on the classpath. While Xtext prints
		// a direct type reference to some unknown class normally, it does not print type
		// references with unknown type parameters. For example, ‘Known<Unknown>’ becomes 
		// ‘/* Known<Unknown> */ Object’ in the code.
		// This hack is bad because a) it misuses the API of `typeRef` and b) it 
		// leads to a lookup of the java code (e.g. Known<Unknown>), which must fail.
		// I (Joshua Gleitze) found no other way, but a better way would be highly 
		// appreciated!
		if (typeParameterReferences
				.stream()
				.anyMatch(jvmTypeReference -> jvmTypeReference instanceof JvmUnknownTypeReference)) {
			return typeReferenceBuilder.typeRef(toJavaCode());
		} else {
			return typeReferenceBuilder.typeRef(fullyQualifiedTypeName,
					typeParameterReferences.toArray(new JvmTypeReference[]{}));
		}
	}
	
	private String toJavaCode() {
		if (typeParameters.isEmpty()) {
			return fullyQualifiedTypeName;
		} else {
			return fullyQualifiedTypeName +
					"<" + String.join(", ", typeParameters) + ">";
		}
	}
	
	@Override
	public String toString() {
		return toJavaCode();
	}
}