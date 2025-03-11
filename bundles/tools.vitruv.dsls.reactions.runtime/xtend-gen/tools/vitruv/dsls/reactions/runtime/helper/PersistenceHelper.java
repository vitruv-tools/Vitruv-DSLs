package tools.vitruv.dsls.reactions.runtime.helper;

import edu.kit.ipd.sdq.activextendannotations.Utility;
import edu.kit.ipd.sdq.commons.util.org.eclipse.emf.common.util.URIUtil;
import java.nio.file.Path;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import tools.vitruv.change.propagation.ProjectMarker;

@Utility
@SuppressWarnings("all")
public final class PersistenceHelper {
  private static URI getURIOfElementProject(final EObject element) {
    final URI elementUri = element.eResource().getURI();
    boolean _isPlatform = elementUri.isPlatform();
    if (_isPlatform) {
      final IFile sourceModelFile = URIUtil.getIFileForEMFUri(elementUri);
      final IProject projectSourceModel = sourceModelFile.getProject();
      String srcFolderPath = projectSourceModel.getFullPath().toString();
      return URI.createPlatformResourceURI(srcFolderPath, true);
    } else {
      boolean _isFile = elementUri.isFile();
      if (_isFile) {
        final Path elementPath = Path.of(elementUri.toFileString());
        return URIUtil.createFileURI(ProjectMarker.getProjectRootFolder(elementPath).toFile());
      } else {
        throw new UnsupportedOperationException(
          "Other URI types than file and platform are currently not supported");
      }
    }
  }

  private static URI appendPathToURI(final URI baseURI, final String relativePath) {
    final String[] newModelFileSegments = relativePath.split("/");
    boolean _contains = IterableExtensions.<String>last(((Iterable<String>)Conversions.doWrapArray(newModelFileSegments))).contains(".");
    boolean _not = (!_contains);
    if (_not) {
      throw new IllegalArgumentException("File extension must be specified");
    }
    return baseURI.appendSegments(newModelFileSegments);
  }

  /**
   * Returns the URI of the project folder, relative as specified in <code>relativePath</code>
   * to the project root, determined from the element <code>source</code>.
   * 
   * @param source -
   * 		An {@link EObject} that is persisted within a resource of the project
   * @param relativePath -
   * 		The relative path within the project to get the {@link URI} for, using "/" as separator char
   * 
   * @returns the {@link URI} of the folder within the project of the given element
   */
  public static URI getURIFromSourceProjectFolder(final EObject source, final String relativePath) {
    final URI baseURI = PersistenceHelper.getURIOfElementProject(source);
    return PersistenceHelper.appendPathToURI(baseURI, relativePath);
  }

  private PersistenceHelper() {
    
  }
}
