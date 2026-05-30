package com.group_finity.mascot.imagesetchooser;

import javax.swing.*;
import java.awt.*;

/**
 * A JList that can be populated with ImageSetChooserPanel objects
 */

public class ShimejiList extends JList<ImageSetChooserPanel> {

  public ShimejiList() {
    setCellRenderer(new CustomCellRenderer());
  }

  static class CustomCellRenderer implements ListCellRenderer<ImageSetChooserPanel> {
    public Component getListCellRendererComponent (JList<? extends ImageSetChooserPanel> list, ImageSetChooserPanel value,
			int index,boolean isSelected,boolean cellHasFocus) {
      if (value instanceof ImageSetChooserPanel)
      {
          ((ImageSetChooserPanel) value).setForeground (Color.white);
        ((ImageSetChooserPanel) value).setBackground (isSelected ? SystemColor.controlHighlight : Color.white);
        ((ImageSetChooserPanel) value).setCheckbox( isSelected );
        return (ImageSetChooserPanel) value;
      }
      else
      {
        return new JLabel("???");
      }
    }
  }
}