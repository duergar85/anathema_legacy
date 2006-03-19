package net.sf.anathema.character.generic.framework.module;

import net.sf.anathema.character.generic.framework.ICharacterGenerics;
import net.sf.anathema.lib.resources.IResources;

public interface ICharacterGenericsModule {

  public void addCharacterTemplates(ICharacterGenerics characterGenerics);

  public void addBackgroundTemplates(ICharacterGenerics generics);

  public void addAdditionalTemplateData(ICharacterGenerics characterGenerics);

  public void addReportTemplates(ICharacterGenerics generics, IResources resources);

  public void registerCommonData(ICharacterGenerics characterGenerics);
}