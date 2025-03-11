package tools.vitruv.dsls.commonalities.generator;

import com.google.inject.Singleton;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtext.xbase.lib.Pure;

@Singleton
@Accessors
@SuppressWarnings("all")
public class CommonalitiesGenerationSettings {
  public static final boolean CREATE_REACTIONS_FILES_DEFAULT = false;

  public static final boolean CREATE_ECORE_FILES_DEFAULT = false;

  CommonalitiesGenerationSettings() {
  }

  private boolean createReactionFiles = CommonalitiesGenerationSettings.CREATE_REACTIONS_FILES_DEFAULT;

  private boolean createEcoreFiles = CommonalitiesGenerationSettings.CREATE_ECORE_FILES_DEFAULT;

  @Pure
  public boolean isCreateReactionFiles() {
    return this.createReactionFiles;
  }

  public void setCreateReactionFiles(final boolean createReactionFiles) {
    this.createReactionFiles = createReactionFiles;
  }

  @Pure
  public boolean isCreateEcoreFiles() {
    return this.createEcoreFiles;
  }

  public void setCreateEcoreFiles(final boolean createEcoreFiles) {
    this.createEcoreFiles = createEcoreFiles;
  }
}
