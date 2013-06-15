package net.sf.anathema.character.main.description.model;

import net.sf.anathema.character.model.CharacterModelAutoCollector;
import net.sf.anathema.character.model.CharacterModelFactory;
import net.sf.anathema.character.model.Hero;
import net.sf.anathema.character.model.ModelCreationContext;

@CharacterModelAutoCollector
public class CharacterDescriptionFactory implements CharacterModelFactory {

  @Override
  public TextualCharacterDescription create(ModelCreationContext context, Hero hero) {
    return new TextualCharacterDescription();
  }
}