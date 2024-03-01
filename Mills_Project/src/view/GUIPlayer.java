package view;

import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Font;
import java.awt.Label;
import java.awt.Panel;
import java.awt.SystemColor;
import java.awt.TextArea;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JFrame;

public class GUIPlayer extends Panel {
    public static final int HUMAN = 0;
    public static final int CPU = 1;
    
    public static final int Easy = 0,
    						Medium = 1,
    						Hard = 2;

    private static final Font CHOICE_FONT = new Font("SansSerif", Font.BOLD, 16);
    private static final Color ACTIVE_INFOAREA_COLOR = new Color(192,255,192);
    private static final Color PASSIVE_INFOAREA_COLOR = SystemColor.control;

    private Label header;
    private TextArea infoArea;
    private Choice playerChoice;
    private Choice levelChoice;
    private Panel topRow;

    private String hiddenInfoText = "";

    public GUIPlayer(String name,
                     Color nameColor,
                     Color background,
                     int defaultPlayer) throws IllegalArgumentException {
        if (name == null || nameColor == null || background == null ||
            defaultPlayer < HUMAN || defaultPlayer > CPU) {
            throw new IllegalArgumentException(
                "No argument for GUIPlayer can be null."+
                "Defaultplayer:"+defaultPlayer+" must be 0 or 1.");
        }

        // header
        this.header = new Label(name, Label.LEFT);
        this.header.setFont(CHOICE_FONT);
        this.header.setForeground(nameColor);
        this.header.setBackground(background);

        // player choices
        this.playerChoice = new Choice();
        this.playerChoice.setFont(CHOICE_FONT);
        this.playerChoice.add("Human");
        this.playerChoice.add("CPU");
        this.playerChoice.select(defaultPlayer); 

        // level choices
        this.levelChoice = new Choice();
        this.levelChoice.setFont(CHOICE_FONT);
	
        this.levelChoice.add("Easy");
        this.levelChoice.add("Medium");
        this.levelChoice.add("Hard");
        this.levelChoice.select("Easy");

        // ylin rivi: nimi ja valinnat
        this.topRow = new Panel(new BorderLayout());
        this.topRow.setBackground(background);
        this.topRow.add(this.header, BorderLayout.WEST);
        this.topRow.add(this.playerChoice, BorderLayout.CENTER);
        this.topRow.add(this.levelChoice, BorderLayout.EAST);

        // info area
        this.infoArea = new TextArea("",6,40, TextArea.SCROLLBARS_NONE);
        this.infoArea.setEditable(false);
        this.infoArea.setForeground(Color.BLUE);

        // kaikki kiinni Paneliin
        super.setLayout(new BorderLayout()); //hgap, vgap
        super.add(this.topRow, BorderLayout.NORTH);
        super.add(this.infoArea, BorderLayout.CENTER);

        // level-valinta ei n�y ihmispelaajalla
        if (defaultPlayer == CPU) {
            this.levelChoice.setVisible(true);
        }
        else {
            this.levelChoice.setVisible(false);
        }

        // aluksi vuoro ei ole kell��n
        this.setActive(false);

        // tapahtumakuuntelija player-valintaan.
        this.playerChoice.addItemListener( new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if (playerChoice.getSelectedIndex() == CPU) {
                        levelChoice.setVisible(true);
                    }
                    else {
                        levelChoice.setVisible(false);
                    }
                    topRow.validate();
                }
            }
        );

        super.validate();
    }

    public void setInfoText(String s) {
        this.infoArea.setText(s);
        this.hiddenInfoText = s;
        this.infoArea.validate();                 
    }

    public void showInfoText(boolean value) {
        if (value) {
            this.infoArea.setText(this.hiddenInfoText);
        }
        else {
            this.infoArea.setText("");
        }
        this.infoArea.validate();
    }

    public void setActive(boolean active) {
        if (active) {
            this.infoArea.setBackground(ACTIVE_INFOAREA_COLOR);
        }
        else {
            this.infoArea.setBackground(PASSIVE_INFOAREA_COLOR);
            this.infoArea.setBackground(GUI.BACKGROUND_COLOR);
        }
        this.infoArea.invalidate();
    }

    public void setChoicesActive(boolean active) {
        this.playerChoice.setEnabled(active);
        this.levelChoice.setEnabled(active);
        this.playerChoice.validate();
        this.levelChoice.validate();
    }

    public int getPlayerChoice() {
        return this.playerChoice.getSelectedIndex();
    }

    public int getCPULevelChoice() throws IllegalStateException {
        if (this.getPlayerChoice() == HUMAN) {
            throw new IllegalStateException("Level choices are available only for CPU players.");
        }
        return this.levelChoice.getSelectedIndex();
    }
    
    public static void main(String[] args) {
    	JFrame f = new JFrame();
    	GUIPlayer panel = new GUIPlayer("game", ACTIVE_INFOAREA_COLOR, ACTIVE_INFOAREA_COLOR, CPU);
    	
    	f.add(panel);
    	f.setVisible(true);
    	f.setBounds(100, 100, 600, 900);
		
	}
}
