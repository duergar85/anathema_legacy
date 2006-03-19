package net.sf.anathema.character.generic.framework.xml.trait;

import net.sf.anathema.character.generic.framework.xml.core.AbstractXmlTemplateParser;
import net.sf.anathema.character.generic.framework.xml.registry.IXmlTemplateRegistry;
import net.sf.anathema.character.generic.framework.xml.trait.pool.GenericTraitTemplatePool;
import net.sf.anathema.character.generic.framework.xml.trait.pool.GenericTraitTemplatePoolParser;
import net.sf.anathema.character.generic.framework.xml.trait.types.AbilityPoolParser;
import net.sf.anathema.character.generic.framework.xml.trait.types.AttributePoolParser;
import net.sf.anathema.character.generic.framework.xml.trait.types.VirtuePoolParser;
import net.sf.anathema.lib.exception.PersistenceException;

import org.dom4j.Element;

public class GenericTraitTemplateFactoryParser extends AbstractXmlTemplateParser<GenericTraitTemplateFactory> {

  private static final String TAG_BACKGROUNDS = "backgrounds"; //$NON-NLS-1$
  private static final String TAG_ESSENCE = "essence"; //$NON-NLS-1$
  private static final String TAG_WILLPOWER = "willpower"; //$NON-NLS-1$
  private final IXmlTemplateRegistry<GenericTraitTemplatePool> poolTemplateRegistry;

  public GenericTraitTemplateFactoryParser(
      IXmlTemplateRegistry<GenericTraitTemplateFactory> templateRegistry,
      IXmlTemplateRegistry<GenericTraitTemplatePool> poolTemplateRegistry) {
    super(templateRegistry);
    this.poolTemplateRegistry = poolTemplateRegistry;
  }

  @Override
  protected GenericTraitTemplateFactory createNewBasicTemplate() {
    return new GenericTraitTemplateFactory();
  }

  public GenericTraitTemplateFactory parseTemplate(Element element) throws PersistenceException {
    GenericTraitTemplateFactory templateFactory = getBasicTemplate(element);
    parseBackgrounds(element, templateFactory);
    new AbilityPoolParser(poolTemplateRegistry, templateFactory).parseTraits(element);
    new AttributePoolParser(poolTemplateRegistry, templateFactory).parseTraits(element);
    new VirtuePoolParser(poolTemplateRegistry, templateFactory).parseTraits(element);
    parseEssence(element, templateFactory);
    parseWillpower(element, templateFactory);
    return templateFactory;
  }

  private void parseBackgrounds(Element element, GenericTraitTemplateFactory templateFactory)
      throws PersistenceException {
    Element backgroundsElement = element.element(TAG_BACKGROUNDS);
    if (backgroundsElement == null) {
      return;
    }
    GenericTraitTemplatePoolParser traitTemplatePoolParser = new GenericTraitTemplatePoolParser(
        poolTemplateRegistry,
        null);
    GenericTraitTemplatePool backgroundPool = traitTemplatePoolParser.parseTemplate(backgroundsElement);
    templateFactory.setBackgroundPool(backgroundPool);
  }

  private void parseWillpower(Element element, GenericTraitTemplateFactory templateFactory) throws PersistenceException {
    Element willpowerElement = element.element(TAG_WILLPOWER);
    if (willpowerElement == null) {
      return;
    }
    GenericTraitTemplate willpowerTemplate = GenericTraitTemplateParser.parseTraitTemplate(willpowerElement);
    templateFactory.setWillpowerTemplate(willpowerTemplate);
  }

  private void parseEssence(Element element, GenericTraitTemplateFactory templateFactory) throws PersistenceException {
    Element essenceElement = element.element(TAG_ESSENCE);
    if (essenceElement == null) {
      return;
    }
    GenericTraitTemplate essenceTemplate = GenericTraitTemplateParser.parseTraitTemplate(essenceElement);
    templateFactory.setEssenceTemplate(essenceTemplate);
  }
}