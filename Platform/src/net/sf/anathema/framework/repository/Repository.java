package net.sf.anathema.framework.repository;

import com.google.common.base.Preconditions;
import net.sf.anathema.framework.item.IItemType;
import net.sf.anathema.framework.item.IRepositoryConfiguration;
import net.sf.anathema.framework.presenter.IItemManagementModel;
import net.sf.anathema.framework.presenter.action.IFileProvider;
import net.sf.anathema.framework.repository.access.IRepositoryReadAccess;
import net.sf.anathema.framework.repository.access.IRepositoryWriteAccess;
import net.sf.anathema.framework.repository.access.MultiFileReadAccess;
import net.sf.anathema.framework.repository.access.MultiFileWriteAccess;
import net.sf.anathema.framework.repository.access.SingleFileReadAccess;
import net.sf.anathema.framework.repository.access.SingleFileWriteAccess;
import net.sf.anathema.framework.repository.access.printname.IPrintNameFileAccess;
import net.sf.anathema.framework.repository.access.printname.PrintNameFileAccess;
import net.sf.anathema.framework.view.PrintNameFile;
import net.sf.anathema.lib.control.IChangeListener;
import org.apache.commons.io.FileUtils;
import org.jmock.example.announcer.Announcer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;

public class Repository implements IRepository {

  private final PrintNameFileAccess printNameFileAccess;
  private final File repositoryFolder;
  private final RepositoryFileResolver resolver;
  private final Announcer<IChangeListener> control = Announcer.to(IChangeListener.class);

  public Repository(File repositoryFolder, IItemManagementModel itemManagement) {
    Preconditions.checkArgument(repositoryFolder.exists());
    this.resolver = new RepositoryFileResolver(repositoryFolder);
    this.repositoryFolder = repositoryFolder;
    this.printNameFileAccess = new PrintNameFileAccess(resolver, itemManagement);
  }

  @Override
  public String getRepositoryPath() {
    return repositoryFolder.toString();
  }

  @Override
  public IPrintNameFileAccess getPrintNameFileAccess() {
    return printNameFileAccess;
  }

  @Override
  public IRepositoryFileResolver getRepositoryFileResolver() {
    return resolver;
  }

  @Override
  public synchronized IRepositoryWriteAccess createWriteAccess(IItem item) throws RepositoryException {
    try {
      if (item.getId() == null) {
        item.getRepositoryLocation().setId(createUniqueRepositoryId(item.getRepositoryLocation()));
      }
      if (item.getItemType().getRepositoryConfiguration().isItemSavedToSingleFile()) {
        return createSingleFileWriteAccess(item);
      }
      return createMultiFileWriteAccess(item);
    } catch (RepositoryException e) {
      String pattern = "Could not create RepositoryItem for {0}, {1}."; //$NON-NLS-1$
      throw new RepositoryException(MessageFormat.format(pattern, item.getItemType(), item.getId()), e);
    }
  }

  @Override
  public synchronized IRepositoryWriteAccess createWriteAccess(IItemType type, String id) throws RepositoryException {
    try {
      // TODO: Handle non-unique ID
      // if (item.getId() == null) {
      // item.getRepositoryLocation().setId(createUniqueRepositoryId(item.getRepositoryLocation()));
      // }
      if (type.getRepositoryConfiguration().isItemSavedToSingleFile()) {
        return createSingleFileWriteAccess(type, id);
      }
      return createMultiFileWriteAccess(type, id);
    } catch (RepositoryException e) {
      String pattern = "Could not create RepositoryItem for {0}, {1}."; //$NON-NLS-1$
      throw new RepositoryException(MessageFormat.format(pattern, type, id), e);
    }
  }

  private IRepositoryWriteAccess createMultiFileWriteAccess(IItemType type, String id) {
    File itemFolder = resolver.getExistingItemFolder(type, id);
    return createMultiFileWriteAccess(type, itemFolder);
  }

  private IRepositoryWriteAccess createMultiFileWriteAccess(IItem item) {
    File itemFolder = resolver.getExistingItemFolder(item);
    return createMultiFileWriteAccess(item.getItemType(), itemFolder);
  }

  private IRepositoryWriteAccess createMultiFileWriteAccess(IItemType type, File itemFolder) {
    IRepositoryConfiguration configuration = type.getRepositoryConfiguration();
    return new MultiFileWriteAccess(itemFolder, configuration.getMainFileName(), configuration.getFileExtension());
  }

  private IRepositoryWriteAccess createSingleFileWriteAccess(IItemType type, String id) throws RepositoryException {
    File file = resolver.getMainFile(type, id);
    return createSingleFileWriteAccess(file);
  }

  private IRepositoryWriteAccess createSingleFileWriteAccess(IItem item) throws RepositoryException {
    File file = resolver.getItemFile(item);
    return createSingleFileWriteAccess(file);
  }

  private IRepositoryWriteAccess createSingleFileWriteAccess(File file) throws RepositoryException {
    if (!file.exists()) {
      try {
        file.createNewFile();
      } catch (IOException e) {
        throw new RepositoryException("Error creating file: " + file, e); //$NON-NLS-1$
      }
    }
    return new SingleFileWriteAccess(file);
  }

  @Override
  public String createUniqueRepositoryId(IBasicRepositoryIdData repositoryLocation) {
    int count = 0;
    String repositoryId = repositoryLocation.getIdProposal();
    IItemType itemType = repositoryLocation.getItemType();
    while (idExists(itemType, repositoryId)) {
      count++;
      repositoryId = repositoryLocation.getIdProposal() + count;
    }
    return repositoryId;
  }

  private boolean idExists(IItemType type, String id) {
    return resolver.getMainFile(type, id).exists();
  }

  @Override
  public IRepositoryReadAccess openReadAccess(IItemType type, IFileProvider provider) {
    if (provider.getFile() == null) {
      return null;
    }
    if (type.getRepositoryConfiguration().isItemSavedToSingleFile()) {
      return new SingleFileReadAccess(provider.getFile());
    }
    IRepositoryConfiguration repositoryConfiguration = type.getRepositoryConfiguration();
    return new MultiFileReadAccess(provider.getFile(), repositoryConfiguration.getMainFileName(),
            repositoryConfiguration.getFileExtension());
  }

  @Override
  public IRepositoryReadAccess openReadAccess(IItemType type, String id) {
    if (type.getRepositoryConfiguration().isItemSavedToSingleFile()) {
      return new SingleFileReadAccess(getRepositoryFileResolver().getMainFile(type, id));
    }
    IRepositoryConfiguration repositoryConfiguration = type.getRepositoryConfiguration();
    return new MultiFileReadAccess(getRepositoryFileResolver().getItemTypeFolder(type),
            repositoryConfiguration.getMainFileName(), repositoryConfiguration.getFileExtension());
  }

  @Override
  public boolean knowsItem(IItemType type, String id) {
    if (type.getRepositoryConfiguration().isItemSavedToSingleFile()) {
      return getRepositoryFileResolver().getMainFile(type, id).exists();
    }
    return getRepositoryFileResolver().getItemTypeFolder(type).exists();
  }

  @Override
  public boolean containsClosed(IItemType... types) {
    boolean hasClosed = false;
    for (IItemType type : types) {
      hasClosed |= printNameFileAccess.collectClosedPrintNameFiles(type).length > 0;
    }
    return hasClosed;
  }

  @Override
  public Path getDataBaseDirectory(String subfolder) {
    return resolver.getExistingDataFolder(subfolder).toPath();
  }

  @Override
  public void deleteAssociatedItem(PrintNameFile file) throws RepositoryException {
    try {
      if (file.getFile().exists()) {
        FileUtils.forceDelete(file.getFile());
      }
      refresh();
    } catch (IOException e) {
      throw new RepositoryException("Deletion failed.", e); //$NON-NLS-1$
    }
  }

  @Override
  public void addRefreshListener(IChangeListener listener) {
    control.addListener(listener);
  }

  @Override
  public void refresh() {
    control.announce().changeOccurred();
  }
}