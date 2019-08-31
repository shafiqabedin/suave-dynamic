/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SagatQuestion.java
 *
 * Created on Jan 31, 2011, 1:45:56 AM
 */
package vbs2gui.sagat;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import vbs2gui.SimpleUAVSim.Main.ExperimentType;

/**
 *
 * @author nbb
 */
public class SagatQuestion extends javax.swing.JPanel {

  Logger logger = Logger.getLogger(SagatQuestion.class.getName());
  ActionListener answerActionListener;
  ButtonGroup[] items;
  Container[] questionContainers;
  String[] answers;
  private final LinkedHashMap<String, String> sagatQuestions = new LinkedHashMap<String, String>();

  {
    sagatQuestions.put("POWER1", "Does any of these UAVs have very low battery life?");//1
    sagatQuestions.put("TEMP1", "Does any of these UAVs have very low temperature?");//2
    sagatQuestions.put("ACTIVITY1", "Which area of the map has not been covered yet?");//3
    sagatQuestions.put("ACTIVITY2", "Are any of these targets moving towards the danger zone?");//4
    sagatQuestions.put("ACTIVITY3", " In what pattern the UAVs are moving?");//5
    sagatQuestions.put("ACTIVITY4", "What is the shape of the target?");//6
    sagatQuestions.put("ACTIVITY5", "How many targets have been identified?");//7
    sagatQuestions.put("ACTIVITY6", "Are any of these UAVs going to malfunction soon?");//8
    sagatQuestions.put("ACTIVITY7", "Are any of these UAVs flying over the danger zone?");//9
    sagatQuestions.put("GUGERTY1", "If you are standing in the parking lot lookign at the forest, housing is on your:");//10
    sagatQuestions.put("GUGERTY2", "If you are standing in the Forest lookign at the parking lot, housing is on your Left?");//11
    sagatQuestions.put("GUGERTY3", "If you are standing in the Air Strip lookign at the parking lot, forest is on your:");//12
    sagatQuestions.put("GUGERTY4", "If you are standing in the Forest lookign at the housing, the parking lot is on your right.");//13
    sagatQuestions.put("FUEL1", "Does any of the UAV have lowest fuel ?");//14
    sagatQuestions.put("FUEL2", "Are any of these UAVs close to full in fuel ?");//15
    sagatQuestions.put("TEMP2", "Does any of these UAVs have very low temperature?");//16
    sagatQuestions.put("POWER2", "Does any of these UAVs have very high battery life?");//17
    sagatQuestions.put("GUGERTY5", "If you are standing in the Housing lookign at the parking lot, forest is on your:");//18
  }
  private final LinkedHashMap<String, String[]> sagatChoices = new LinkedHashMap<String, String[]>();

  {
    sagatChoices.put("POWER1", new String[]{"Yes", "No"});//1
    sagatChoices.put("TEMP1", new String[]{"Yes", "No"});//2
    sagatChoices.put("ACTIVITY1", new String[]{"North", "South", "East", "West"});//3
    sagatChoices.put("ACTIVITY2", new String[]{"Yes", "No"});//4
    sagatChoices.put("ACTIVITY3", new String[]{"Circular", "Rectangular", "Spiral"});//5
    sagatChoices.put("ACTIVITY4", new String[]{"Circular", "Rectangular", "Triangular"});//6
    sagatChoices.put("ACTIVITY5", new String[]{"1-3", "4-7", "8-10", "More than 10"});//7
    sagatChoices.put("ACTIVITY6", new String[]{"Yes", "No"});//8
    sagatChoices.put("ACTIVITY7", new String[]{"Yes", "No"});//9
    sagatChoices.put("GUGERTY1", new String[]{"Left", "Right"});//10
    sagatChoices.put("GUGERTY2", new String[]{"True", "False"});//11
    sagatChoices.put("GUGERTY3", new String[]{"Left", "Right"});//12
    sagatChoices.put("GUGERTY4", new String[]{"True", "False"});//13
    sagatChoices.put("FUEL1", new String[]{"Yes", "No"});//14
    sagatChoices.put("FUEL2", new String[]{"Yes", "No"});//15
    sagatChoices.put("TEMP2", new String[]{"Yes", "No"});//16
    sagatChoices.put("POWER2", new String[]{"Yes", "No"});//17
    sagatChoices.put("GUGERTY5", new String[]{"Left", "Right"});//18
  }
  private final LinkedHashMap<String, String> sagatAnswers = new LinkedHashMap<String, String>();
  private String[] useQuestions;

  /** Creates new form SagatQuestion */
  public SagatQuestion(ExperimentType experimentType, int roundNum) {
    if (experimentType == ExperimentType.FEED_11_1 || experimentType == ExperimentType.SUAVE_11_1 || experimentType == ExperimentType.FEED_22_1 || experimentType == ExperimentType.SUAVE_22_1) {
      switch (roundNum) {
        case (1):
          useQuestions = new String[]{"POWER1", "TEMP1", "GUGERTY2"};
          break;
        case (2):
          useQuestions = new String[]{"GUGERTY1", "ACTIVITY1", "ACTIVITY2"};
          break;
        case (3):
          useQuestions = new String[]{"FUEL1", "ACTIVITY3", "ACTIVITY4"};
          break;
        default:
          logger.warning("Could not find the SAGAT questiosn you requested: ExperimentType: " + experimentType + " Round: " + roundNum);
          break;
      }
    } else if (experimentType == ExperimentType.FEED_11_2 || experimentType == ExperimentType.SUAVE_11_2 || experimentType == ExperimentType.FEED_22_2 || experimentType == ExperimentType.SUAVE_22_2) {
      switch (roundNum) {
        case (1):
          useQuestions = new String[]{"ACTIVITY7", "GUGERTY3", "FUEL2"};
          break;
        case (2):
          useQuestions = new String[]{"GUGERTY5", "ACTIVITY5", "ACTIVITY6"};
          break;
        case (3):
          useQuestions = new String[]{"GUGERTY4", "TEMP2", "POWER2"};
          break;
        default:
          logger.warning("Could not find the SAGAT questiosn you requested: ExperimentType: " + experimentType + " Round: " + roundNum);
          break;
      }
    } else {
      logger.warning("Could not find the SAGAT questiosn you requested: ExperimentType: " + experimentType + " Round: " + roundNum);
    }
    initComponents();
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">
  private void initComponents() {
    if (useQuestions != null) {
      answers = new String[useQuestions.length];
      JLabel label = new JLabel("Time left:");
      JPanel buttons = new JPanel();

      javax.swing.BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
      this.setLayout(layout);
      this.setPreferredSize(new Dimension(600, 600));
      for (int i = 0; i < useQuestions.length; i++) {
        if (sagatQuestions.containsKey(useQuestions[i])) {
          final String thisQuestion = useQuestions[i];
          Container questionContainer = RadioButtonUtils.createRadioButtonGrouping(
                  sagatChoices.get(useQuestions[i]),
                  sagatQuestions.get(useQuestions[i]),
                  new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                      sagatAnswers.put(thisQuestion, ((AbstractButton) e.getSource()).getText());
                    }
                  });
          this.add(questionContainer);
        } else {
          System.out.println("Failed to find question '" + useQuestions[i] + "'");
        }
      }
      buttons = new JPanel();
      buttons.setMaximumSize(new Dimension(800, 150));
      buttons.setLayout(new BorderLayout());
//            buttons.add(label, BorderLayout.WEST);
      this.add(buttons);
    }
  }

  public void saveForm() {
    for (int i = 0; i < useQuestions.length; i++) {
      if (sagatQuestions.containsKey(useQuestions[i])) {
        logger.info(sagatQuestions.get(useQuestions[i]) + "|" + sagatAnswers.get(useQuestions[i]));
      }
    }
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String args[]) {
    JFrame mainFrame = new JFrame("Test");
    mainFrame.getContentPane().add(new SagatQuestion(null, 1));
    mainFrame.pack();
    mainFrame.setLocationRelativeTo(null);
    mainFrame.setVisible(true);
  }
}
