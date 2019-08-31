package vbs2gui.sagat;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import javax.swing.border.Border;
import javax.swing.event.ChangeListener;

////////////////////////////////
////////////////////////////////
/**
 * @author Adrian BER (beradrian@yahoo.com)
 */
public class SagatPopupWindow extends JPanel {

    private JToggleButton invokePopupButton;
    private JFrame popupWindow;
    private Random randomGenerator = new Random();
    private String[][] answers = new String[3][2];
    private Container popupPane;
    ArrayList<Integer> nums = new ArrayList();
    Random random = new Random();

    public SagatPopupWindow() {
        init();
    }

    private void init() {

        invokePopupButton = new JToggleButton("Show popup");
        invokePopupButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                // set popup window visibility
                if (!popupWindow.isVisible()) {
                    // set location relative to button
                    Point location = invokePopupButton.getLocation();
                    SwingUtilities.convertPointToScreen(location, invokePopupButton.getParent());
                    location.translate(0, invokePopupButton.getHeight()
                            + (invokePopupButton.getBorder() == null ? 0
                            : invokePopupButton.getBorder().getBorderInsets(invokePopupButton).bottom));
                    popupWindow.setLocation(location);

                    // show the popup if not visible
                    invokePopupButton.setText("Hide popup");
                    popupWindow.setVisible(true);
                    popupWindow.requestFocus();
                } else {
                    // hide it otherwise
                    invokePopupButton.setText("Show popup");
                    popupWindow.setVisible(false);
                }
            }
        });

        // add components to main panel
        this.setLayout(new BorderLayout());
        this.add(invokePopupButton, BorderLayout.CENTER);

        // use frame
        popupWindow = new JFrame();
        //popupWindow.setUndecorated(true);
        popupWindow.setPreferredSize(new Dimension(600, 600));

        popupWindow.addWindowFocusListener(new WindowFocusListener() {

            public void windowGainedFocus(WindowEvent e) {
            }

            public void windowLostFocus(WindowEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        if (popupWindow.isVisible()) {
                            invokePopupButton.doClick();
                        }
                    }
                });
            }
        });

        // add some components to window
        popupPane = popupWindow.getContentPane();
        popupPane.setLayout(new BoxLayout(popupPane, BoxLayout.Y_AXIS));
        ;
        ((JComponent) popupPane).setBorder(BorderFactory.createEtchedBorder());
        JButton button = new JButton("Print Array");
        //Add action listener to button
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                //Execute when button is pressed
                for (int i = 0; i < answers.length; i++) {
                    for (int j = 0; j < answers[i].length; j++) {
                        System.out.print(answers[i][j]);
                    }
                    System.out.println();
                }
            }
        });
//    popupWindow.getContentPane().add(button);
        this.add(button, BorderLayout.EAST);

        while (nums.size() < 3) {
            int next = random.nextInt(3);
            if (!nums.contains(next)) {
                nums.add(next);
            }
        }

        for (int idx = 0; idx <= 2; ++idx) {
            int randomint = nums.get(idx);
            System.out.println(randomint);
            switch (randomint) {
                case 0:
                    question_1(idx);
                    break;
                case 1:
                    question_2(idx);
                    break;
                case 2:
                    question_3(idx);
                    break;
                default:
                    break;
            }
        }
        popupWindow.pack();
    }

    public void question_1(final int idx) {
        String answerOptions[] = {"UAV # 1", "UAV # 2", "UAV # 3"};
        // Slice Parts
        answers[idx][0] = "Are any UAVs too low or too high ?";
        ActionListener answerActionListener = new ActionListener() {

            public void actionPerformed(ActionEvent actionEvent) {
                AbstractButton aButton = (AbstractButton) actionEvent.getSource();
                answers[idx][1] = aButton.getText();
                //System.out.println("Selected: " + aButton.getText());
            }
        };

        Container answerContainer = RadioButtonUtils.createRadioButtonGrouping(
                answerOptions, "Are any UAVs too low or too high ?", answerActionListener);

        popupPane.add(answerContainer);

    }

    public void question_2(final int idx) {
        String answerOptions[] = {"UAV # 1", "UAV # 2", "UAV # 3"};
        String str = "abc";

        // Slice Parts
        answers[idx][0] = "Enter the UAV experiencing malfunction (e.g. no fuel).";
        ActionListener answerActionListener = new ActionListener() {

            public void actionPerformed(ActionEvent actionEvent) {
                AbstractButton aButton = (AbstractButton) actionEvent.getSource();
                answers[idx][1] = aButton.getText();
                //System.out.println("Selected: " + aButton.getText());
            }
        };

        Container answerContainer = RadioButtonUtils.createRadioButtonGrouping(
                answerOptions, "Enter the UAV experiencing malfunction (no fuel).", answerActionListener);

        popupPane.add(answerContainer);

    }

    public void question_3(final int idx) {
        String answerOptions[] = {"Moving towards danger zone", "Walking", "Standing Still"};
        // Slice Parts
        answers[idx][0] = "Enter the Target’s activity in a sector.";
        ActionListener answerActionListener = new ActionListener() {

            public void actionPerformed(ActionEvent actionEvent) {
                AbstractButton aButton = (AbstractButton) actionEvent.getSource();
                answers[idx][1] = aButton.getText();
                //System.out.println("Selected: " + aButton.getText());
            }
        };

        Container answerContainer = RadioButtonUtils.createRadioButtonGrouping(
                answerOptions, "Enter the Target’s activity in a sector.", answerActionListener);

        popupPane.add(answerContainer);

    }

    public static void main(String[] args) {
        JFrame mainFrame = new JFrame("TestPopupWindow");
        mainFrame.getContentPane().add(new SagatPopupWindow());
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }
}

class RadioButtonUtils {

    public RadioButtonUtils() {
    }

    public static Enumeration getSelectedElements(Container container) {
        Vector selections = new Vector();
        Component components[] = container.getComponents();
        for (int i = 0, n = components.length; i < n; i++) {
            if (components[i] instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) components[i];
                if (button.isSelected()) {
                    selections.addElement(button.getText());
                }
            }
        }
        return selections.elements();
    }

    public static Container createRadioButtonGrouping(String elements[]) {
        return createRadioButtonGrouping(elements, null, null, null, null);
    }

    public static Container createRadioButtonGrouping(String elements[],
            String title) {
        return createRadioButtonGrouping(elements, title, null, null, null);
    }

    public static Container createRadioButtonGrouping(String elements[],
            String title, ItemListener itemListener) {
        return createRadioButtonGrouping(elements, title, null, itemListener,
                null);
    }

    public static Container createRadioButtonGrouping(String elements[],
            String title, ActionListener actionListener) {
        return createRadioButtonGrouping(elements, title, actionListener, null,
                null);
    }

    public static Container createRadioButtonGrouping(String elements[],
            String title, ActionListener actionListener,
            ItemListener itemListener) {
        return createRadioButtonGrouping(elements, title, actionListener,
                itemListener, null);
    }

    public static Container createRadioButtonGrouping(String elements[],
            String title, ActionListener actionListener,
            ItemListener itemListener, ChangeListener changeListener) {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        //   If title set, create titled border
        if (title != null) {
            Border border = BorderFactory.createTitledBorder(title);
            panel.setBorder(border);
        }
        //   Create group
        ButtonGroup group = new ButtonGroup();
        JRadioButton aRadioButton;
        //   For each String passed in:
        //   Create button, add to panel, and add to group
        for (int i = 0, n = elements.length; i < n; i++) {
            aRadioButton = new JRadioButton(elements[i]);
            panel.add(aRadioButton);
            group.add(aRadioButton);
            if (actionListener != null) {
                aRadioButton.addActionListener(actionListener);
            }
            if (itemListener != null) {
                aRadioButton.addItemListener(itemListener);
            }
            if (changeListener != null) {
                aRadioButton.addChangeListener(changeListener);
            }
        }
        return panel;
    }
}
