/*******************************************************************************
 * Copyright (c) 2004-2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package j2se.typestate.frame;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.TextArea;

/**
 * Should produce 1 true warning with any TypeState engine.
 * Example from:
 * http://javaalmanac.com/egs/java.awt/frame_CreateFrame.html?l=rel
 * @author Eran Yahav 
 */
public final class FrameExample1 {

  public static void main(String[] args) {
    // Create frame
    String title = "Frame Title";
    Frame frame = new Frame(title);

    // Create a component to add to the frame
    Component comp = new TextArea();

    // Add the component to the frame; by default, the frame has a border
    // layout
    frame.add(comp, BorderLayout.CENTER);

    // Show the frame
    int width = 300;
    int height = 300;
    frame.setSize(width, height);
    // frame.setVisible(true);
    // EY: modified this to directly call show()
    frame.show();
    frame.setUndecorated(true);
  }
}