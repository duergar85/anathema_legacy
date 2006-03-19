package net.sf.anathema.character.generic.framework;

import java.util.ArrayList;
import java.util.List;

import net.sf.anathema.character.generic.framework.module.CharacterModuleContainer;
import net.sf.anathema.character.generic.framework.module.ICharacterGenericsModule;
import net.sf.anathema.lib.logging.Logger;
import net.sf.anathema.lib.resources.IResources;

public class CharacterModuleContainerInitializer {

  private Logger logger = Logger.getLogger(CharacterModuleContainerInitializer.class);

  public List<String> moduleNameList = new ArrayList<String>() {
    {
      add("net.sf.anathema.character.abyssal.AbyssalCharacterModule"); //$NON-NLS-1$
      add("net.sf.anathema.character.db.DbCharacterModule"); //$NON-NLS-1$
      add("net.sf.anathema.character.lunar.LunarCharacterModule"); //$NON-NLS-1$
      add("net.sf.anathema.character.solar.SolarCharacterModule"); //$NON-NLS-1$
      add("net.sf.anathema.character.sidereal.SiderealCharacterModule"); //$NON-NLS-1$
      add("net.sf.anathema.character.meritsflaws.MeritsFlawsModule"); //$NON-NLS-1$
    }
  };

  public CharacterModuleContainer initContainer(IResources resources) {
    CharacterModuleContainer container = new CharacterModuleContainer(resources);
    for (String moduleName : moduleNameList) {
      addModule(container, moduleName);
    }
    return container;
  }

  private void addModule(CharacterModuleContainer container, String moduleName) {
    try {
      ICharacterGenericsModule module = (ICharacterGenericsModule) Class.forName(moduleName).newInstance();
      container.addCharacterGenericsModule(module);
    }
    catch (ClassNotFoundException e) {
      logger.info(moduleName + " not installed."); //$NON-NLS-1$
    }
    catch (Exception e) {
      logger.error("Error initializing module " + moduleName, e); //$NON-NLS-1$
    }
  }
}