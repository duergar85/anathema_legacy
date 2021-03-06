package net.sf.anathema.framework.preferences.perspective;

import net.sf.anathema.framework.preferences.elements.PreferenceModel;
import net.sf.anathema.framework.preferences.elements.RegisteredPreferenceModel;
import net.sf.anathema.framework.preferences.persistence.PreferencePto;
import net.sf.anathema.initialization.InitializationException;
import net.sf.anathema.framework.environment.ObjectFactory;

import java.util.ArrayList;
import java.util.Collection;

public class CollectingPreferencesModel implements PreferencesModel {

  private final Collection<PreferenceModel> models;

  public CollectingPreferencesModel(ObjectFactory factory) {
    Collection<PreferenceModel> objects = factory.instantiateOrdered(RegisteredPreferenceModel.class);
    this.models = new ArrayList<>(objects);
  }

  @Override
  public PreferencePto serialize() {
    PreferencePto pto = new PreferencePto();
    for (PreferenceModel model : models) {
      model.serializeTo(pto);
    }
    return pto;
  }

  @Override
  public void initializeFrom(PreferencePto pto) {
    for (PreferenceModel model : models) {
      model.initializeFrom(pto);
    }
  }

  @Override
  public PreferenceModel find(Class modelClass) {
    for (PreferenceModel model : models) {
      if (modelClass.isInstance(model)) {
        return model;
      }
    }
    throw new InitializationException("Unknown preference requested: " + modelClass);
  }
}