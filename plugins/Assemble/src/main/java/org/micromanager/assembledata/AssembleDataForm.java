///////////////////////////////////////////////////////////////////////////////
//FILE:          AssembleDataForm.java
//PROJECT:       Micro-Manager  
//SUBSYSTEM:     AssembleData plugin
//-----------------------------------------------------------------------------
//
// AUTHOR:       Nico Stuurman
//
// COPYRIGHT:    University of California, San Francisco 2019
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.

package org.micromanager.assembledata;

import com.google.common.eventbus.Subscribe;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.swing.DefaultComboBoxModel;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import net.miginfocom.swing.MigLayout;
import org.micromanager.Studio;
import org.micromanager.data.DataProvider;
import org.micromanager.display.DataViewer;
import org.micromanager.events.ShutdownCommencingEvent;
import org.micromanager.internal.utils.MMDialog;
import org.micromanager.propertymap.MutablePropertyMapView;

/**
 *
 * @author nico
 */
public class AssembleDataForm extends MMDialog {
   private final Studio studio_;
   private final MutablePropertyMapView profileSettings_;
   
   private boolean wasDisposed_ = false;
   private String statusMessage_;
   private final Font arialSmallFont_;
   private final Dimension buttonSize_;
   private final JLabel statusLabel_;
   
   private final JComboBox<String> dataSet1Box_;
   private final JComboBox<String> dataSet2Box_;
   
   private final String DATAVIEWER1 = "DataViewer1";
   private final String DATAVIEWER2 = "DataViewer2";
   private final String XOFFSET = "XOffset";
   private final String YOFFSET = "YOffset";
   
     
    /**
     * Creates new form MultiChannelShadingForm
     * @param studio
     */
   @SuppressWarnings("LeakingThisInConstructor")
   public AssembleDataForm(Studio studio) {
      studio_ = studio;
      profileSettings_ = 
              studio_.profile().getSettings(AssembleDataForm.class);
      super.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      super.addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent arg0) {
            dispose();
         }
      }
      );
      arialSmallFont_ = new Font("Arial", Font.PLAIN, 12);
      buttonSize_ = new Dimension(70, 21);

      
      super.setLayout(new MigLayout("flowx, fill, insets 8"));
      super.setTitle(AssembleData.MENUNAME);

      super.loadAndRestorePosition(100, 100, 375, 275);

      //populate DataViewer ComboBoxes

      
      dataSet1Box_ = new JComboBox();
      setupDataViewerBox(dataSet1Box_, DATAVIEWER1);
      dataSet1Box_.addActionListener((java.awt.event.ActionEvent evt) -> {
         profileSettings_.putString(DATAVIEWER1, (String) 
                 dataSet1Box_.getSelectedItem());
      });
      super.add(new JLabel("First Data Set:"));
      super.add(dataSet1Box_, "wrap");
      
      
      dataSet2Box_ = new JComboBox();
      setupDataViewerBox(dataSet2Box_, DATAVIEWER2);
      dataSet2Box_.addActionListener((java.awt.event.ActionEvent evt) -> {
         profileSettings_.putString(DATAVIEWER2, (String) 
                 dataSet2Box_.getSelectedItem());
      });
      super.add(new JLabel("Second Data Set:"));
      super.add(dataSet2Box_, "wrap");
    
      JLabel xOffsetLabel = new JLabel("X Offset (pixels)");
      super.add(xOffsetLabel);
      int xOffset = profileSettings_.getInteger(XOFFSET, -350);
      final SpinnerNumberModel xModel = new SpinnerNumberModel(xOffset, -1000, 1000, 1);
      final JSpinner xSpinner = new JSpinner (xModel);
      xSpinner.addChangeListener((ChangeEvent e) -> {
         profileSettings_.putInteger(XOFFSET, (Integer) xSpinner.getValue());
      });
      super.add(xSpinner, "wrap");
            
      JLabel yOffsetLabel = new JLabel("YOffset (pixels)");
      super.add(yOffsetLabel);
      int yOffset = profileSettings_.getInteger(YOFFSET, 200);
      final SpinnerNumberModel yModel = new SpinnerNumberModel(yOffset, -1000, 1000, 1);
      final JSpinner ySpinner = new JSpinner (yModel);
      ySpinner.addChangeListener((ChangeEvent e) -> {
         profileSettings_.putInteger(YOFFSET, (Integer) ySpinner.getValue());
      });
      super.add(ySpinner, "wrap");
                  
      JButton helpButton = new JButton("Help");
      helpButton.addActionListener((ActionEvent e) -> {
         new Thread(org.micromanager.internal.utils.GUIUtils.makeURLRunnable(
                 "https://micro-manager.org/wiki/AssembleData")).start();
      });
      super.add (helpButton, "span 2, split 4");
                 
      final JButton testButton =  new JButton("Test");
      testButton.addActionListener((ActionEvent e) -> {
         runTest();
      });
      super.add(testButton);

      
      statusLabel_ = new JLabel(" ");
      super.add(statusLabel_, "span 3, wrap");
      super.pack();
      
      super.setVisible(true);
      super.toFront();
      
      studio_.events().registerForEvents(this);
   }
   
   private void runTest() {
      String dataViewerName1 = profileSettings_.getString(DATAVIEWER1,"");
      String dataViewerName2 = profileSettings_.getString(DATAVIEWER2,"");
      DataProvider dp1 = null;
      DataProvider dp2 = null;
      for (DataViewer dv : studio_.displays().getAllDataViewers()) {
         if (dv.getName().equals(dataViewerName1)) {
            dp1 = dv.getDataProvider();
         }
         if (dv.getName().equals(dataViewerName2)) {
            dp2 = dv.getDataProvider();
         }
      }
      int xOffset = profileSettings_.getInteger(XOFFSET, 0);
      int yOffset = profileSettings_.getInteger(YOFFSET, 0);
      AssembleDataTest.test(studio_, dp1, dp2, xOffset, yOffset);
      
      
   }
  
   public void showGUI() {
      setVisible(true);
   }
   
   @Override
   public void dispose() {
      wasDisposed_ = true;
      super.dispose();
   }
   
   public boolean wasDisposed() { 
      return wasDisposed_; 
   }
 
   
   public final Font getButtonFont() {
      return arialSmallFont_;
   }
   
   public final Dimension getButtonDimension() {
      return buttonSize_;
   }
   
   
   public final JButton mcsButton(Dimension buttonSize, Font font) {
      JButton button = new JButton();
      button.setPreferredSize(buttonSize);
      button.setMinimumSize(buttonSize);
      button.setFont(font);
      button.setMargin(new Insets(0, 0, 0, 0));
      
      return button;
   }
   
   private void setupDataViewerBox(final JComboBox<String> box, final String key) {
      List<DataViewer> allDataViewers = studio_.displays().getAllDataViewers();
      String[] dataViewers = new String[allDataViewers.size()];
      for (int i = 0; i < allDataViewers.size(); i++) {
         dataViewers[i] = allDataViewers.get(i).getName();
      }
      box.setModel(new DefaultComboBoxModel<>(
              dataViewers));
      String dataViewer = profileSettings_.getString(key, "");
      box.setSelectedItem(dataViewer);
      profileSettings_.putString(key, (String)box.getSelectedItem());
   }
   
   public synchronized void setStatus(final String status) {
      statusMessage_ = status;
      SwingUtilities.invokeLater(() -> {
         // update the statusLabel from this thread
         if (status != null) {
            statusLabel_.setText(status);
         }
      });
   }

   public synchronized String getStatus() {
      String status = statusMessage_;
      statusMessage_ = null;
      return status;
   }
   

   @Subscribe
   public void closeRequested( ShutdownCommencingEvent sce){
      this.dispose();
   }
    
}
