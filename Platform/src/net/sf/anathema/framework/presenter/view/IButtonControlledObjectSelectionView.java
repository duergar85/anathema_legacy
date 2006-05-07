package net.sf.anathema.framework.presenter.view;

import net.sf.anathema.lib.control.objectvalue.IObjectValueChangedListener;
import net.sf.anathema.lib.gui.selection.IObjectSelectionView;

public interface IButtonControlledObjectSelectionView extends IObjectSelectionView {

  public void setButtonEnabled(boolean enabled);

  public void addButtonListener(IObjectValueChangedListener listener);
}