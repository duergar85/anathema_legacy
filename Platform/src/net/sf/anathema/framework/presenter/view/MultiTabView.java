package net.sf.anathema.framework.presenter.view;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import net.sf.anathema.framework.view.util.TabDirection;
import net.sf.anathema.framework.view.util.TabbedView;

public class MultiTabView extends AbstractTabView<Object> implements IMultiTabView {

  public MultiTabView(String header) {
    super(header);
  }

  private TabbedView tabbedView = new TabbedView(TabDirection.Up);

  @Override
  protected void createContent(JPanel panel, Object properties) {
    panel.setLayout(new BorderLayout());
    panel.add(tabbedView.getComponent(), BorderLayout.CENTER);
  }

  public void addTabView(ISimpleTabView view, String name) {
    tabbedView.addTab(view, name);
    tabbedView.getComponent().revalidate();
  }
}