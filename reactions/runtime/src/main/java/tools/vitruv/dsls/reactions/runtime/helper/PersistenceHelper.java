package tools.vitruv.dsls.reactions.runtime.helper;

import static edu.kit.ipd.sdq.commons.util.org.eclipse.emf.common.util.URIUtil.createFileURI;
import static edu.kit.ipd.sdq.commons.util.org.eclipse.emf.common.util.URIUtil.getIFileForEMFUri;
import static tools.vitruv.change.utils.ProjectMarker.getProjectRootFolder;

import edu.kit.ipd.sdq.activextendannotations.Utility;
import java.nio.file.Path;
import lombok.val;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;


/**
 * The PersistenceHelper utility class allows to get the URI of an {@link EObject} 
 * from a Vitruvius project. It can be called within routines.
 */
@Utility
public class PersistenceHelper {
  private PersistenceHelper() {
    // Empty constructor for utility class
  }

  private static URI getURIOfElementProject(EObject element) {
    val elementUri = element.eResource().getURI();
    if (elementUri.isPlatform()) {
      final IFile sourceModelFile = getIFileForEMFUri(elementUri);
      final IProject projectSourceModel = sourceModelFile.getProject();
      final String srcFolderPath = projectSourceModel.getFullPath().toString();
      return URI.createPlatformResourceURI(srcFolderPath, true);
    } else if (elementUri.isFile()) {
      val elementPath = Path.of(elementUri.toFileString());
      return createFileURI(getProjectRootFolder(elementPath).toFile());
    } else {
      throw new UnsupportedOperationException(
          "Other URI types than file and platform are currently not supported");
    }
  }

  private static URI appendPathToURI(URI baseURI, String relativePath) {
    final String[] newModelFileSegments = relativePath.split("/");
    final int last = newModelFileSegments.length - 1;
    if (!newModelFileSegments[last].contains(".")) {
      throw new IllegalArgumentException("File extension must be specified");
    }
    return baseURI.appendSegments(newModelFileSegments);
  }

  /**
   * Returns the URI of the project folder, relative as specified in <code>relativePath</code>
   * to the project root, determined from the element <code>source</code>.
   *
   * @param source - An {@link EObject} that is persisted within a resource of the project.
   *     The URI under which source is persisted must be either <code>file</code>, 
   *     or <code>platform</code>.
   * @param relativePath - The relative path within the project to get the {@link URI} for, 
   *     using "/" as separator char.
   * 
   * @return the {@link URI} of the folder within the project of the given element.
   */
  public static URI getURIFromSourceProjectFolder(EObject source, String relativePath) {
    val baseURI = getURIOfElementProject(source);
    return appendPathToURI(baseURI, relativePath);
  }

}
