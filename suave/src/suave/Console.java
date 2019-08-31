package suave;

import java.awt.*;
import javax.swing.*;

public class Console extends JFrame {

    JTextArea textArea = new JTextArea();
    private int idealSize;
    private int maxExcess;

    public Console(int rows, int columns, int idealSize, int maxExcess, int fontSize) {
        this.idealSize = idealSize;
        this.maxExcess = maxExcess;

        textArea.setEditable(false);
        textArea.setRows(rows);
        textArea.setColumns(columns);
        textArea.setFont(new Font("Sans", Font.BOLD, fontSize));


        getContentPane().add(new JScrollPane(textArea), BorderLayout.CENTER);
        pack();
        setVisible(true);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

// 	addWindowListener(new java.awt.event.WindowAdapter() {
// 		public void windowClosing(WindowEvent winEvt) {
// 		    // Perhaps ask user if they want to save any unsaved files first.
// 		    System.exit(0); 
// 		}
// 	    });
    }

    public Console() {
        this(20, 50, 1000, 500, 36);
    }

    public void addText(String line) {
        final String fline = line;
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                textArea.append(fline);

                // Make sure the last line is always visible
                textArea.setCaretPosition(textArea.getDocument().getLength());

                // Keep the text area down to a certain character size
                int excess = textArea.getDocument().getLength() - idealSize;
                if (excess >= maxExcess) {
                    textArea.replaceRange("", 0, excess);
                }
            }
        });
    }
}

