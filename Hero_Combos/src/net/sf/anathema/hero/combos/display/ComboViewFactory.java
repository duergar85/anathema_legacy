package net.sf.anathema.hero.combos.display;

import net.sf.anathema.character.main.type.ICharacterType;
import net.sf.anathema.character.main.framework.RegisteredCharacterView;
import net.sf.anathema.character.main.view.SubViewFactory;

@RegisteredCharacterView(ComboConfigurationView.class)
public class ComboViewFactory implements SubViewFactory {
  @Override
  public <T> T create(ICharacterType type) {
    return (T) new SwingComboConfigurationView();
  }
}