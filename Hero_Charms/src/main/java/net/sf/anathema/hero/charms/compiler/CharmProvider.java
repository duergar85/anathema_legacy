package net.sf.anathema.hero.charms.compiler;

import net.sf.anathema.character.magic.charm.Charm;
import net.sf.anathema.character.magic.charm.ICharmLearnableArbitrator;
import net.sf.anathema.hero.charms.model.CharmIdMap;
import net.sf.anathema.hero.charms.model.special.ISpecialCharm;
import net.sf.anathema.character.framework.type.CharacterType;
import net.sf.anathema.lib.util.Identifier;

public interface CharmProvider {

  Charm[] getMartialArtsCharms();

  Charm[] getCharms(CharacterType characterType);

  ISpecialCharm[] getSpecialCharms(Identifier type);

  ISpecialCharm[] getSpecialCharms(ICharmLearnableArbitrator arbitrator, CharmIdMap map, Identifier preferredType);
}