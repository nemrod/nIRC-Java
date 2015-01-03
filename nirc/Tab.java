package nirc;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.DefaultCaret;

public class Tab extends JPanel {
    private static final long serialVersionUID = 5655982895261650359L;

    private Buffer buffer;
	
	private JLabel topicLabel;
	
	private JPanel mainPanel;
	private JTextArea messageTextArea;
	private JList<String> nickList;
	
	private JPanel inputPanel;
	private JTextField inputTextField;
	private JButton inputButton;
	private ButtonListener buttonListener;

	public Tab(Buffer buffer) {
		this.buffer = buffer;

		this.initialize();
		this.createGUI();
	}
	
	private void initialize() {
		this.topicLabel = new JLabel();

		this.mainPanel = new JPanel();
		this.messageTextArea = new JTextArea();
		this.nickList = new JList<String>();
		
		this.inputPanel = new JPanel();
		this.inputTextField = new JTextField();
		this.inputButton = new JButton();
		this.buttonListener = new ButtonListener();
	}
	
	private void createGUI() {
		this.topicLabel.setText(this.buffer.getName());
		if(this.buffer instanceof ChannelBuffer) {
			this.topicLabel.setText(((ChannelBuffer)this.buffer).getTopic());
		}
		this.topicLabel.setAlignmentY(LEFT_ALIGNMENT);

		this.messageTextArea.setText(this.buffer.getMessagesAsText());
		this.messageTextArea.setEditable(false);
		this.messageTextArea.setLineWrap(true);
		this.messageTextArea.setWrapStyleWord(true);
		DefaultCaret caret = (DefaultCaret)this.messageTextArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		if(this.buffer instanceof ChannelBuffer) {
			this.nickList.setListData(((ChannelBuffer)this.buffer).getNicksAsArray());
		}
		
		this.mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1;
		constraints.weighty = 1;
		constraints.fill = GridBagConstraints.BOTH;
		JScrollPane messageScrollPane = new JScrollPane(this.messageTextArea);
		messageScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		messageScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		this.mainPanel.add(messageScrollPane, constraints);
		if(this.buffer instanceof ChannelBuffer) {
			constraints.gridx = 1;
			constraints.weightx = 0.5;
			JScrollPane nicksScrollPane = new JScrollPane(this.nickList);
			nicksScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			nicksScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			constraints.fill = GridBagConstraints.BOTH;
			this.mainPanel.add(nicksScrollPane, constraints);
		}
		
		this.inputButton.setText("Submit");
		this.inputButton.addActionListener(this.buttonListener);
		this.inputPanel.setLayout(new GridBagLayout());
		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1;
		constraints.weighty = 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		this.inputPanel.add(this.inputTextField, constraints);
		constraints.gridx = 1;
		constraints.weightx = 0;
		constraints.fill = GridBagConstraints.NONE;
		this.inputPanel.add(this.inputButton, constraints);
		
		this.setLayout(new GridBagLayout());
		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1;
		constraints.weighty = 0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		this.add(topicLabel, constraints);
		
		constraints.gridy = 1;
		constraints.weighty = 1;
		constraints.fill = GridBagConstraints.BOTH;
		this.add(mainPanel, constraints);
		
		constraints.gridy = 2;
		constraints.weighty = 0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		this.add(inputPanel, constraints);
	}
	
	public Buffer getBuffer() {
		return this.buffer;
	}
	
	public void addMessage(String message) {
		this.messageTextArea.append(message + "\n");
	}
	
	public void updateNickList() {
		if(this.buffer instanceof ChannelBuffer) {
			this.nickList.setListData(((ChannelBuffer)this.buffer).getNicksAsArray());
		}	
	}
	
	public void updateTopicLabel() {
		this.topicLabel.setText(this.buffer.getName());
		if(this.buffer instanceof ChannelBuffer) {
			this.topicLabel.setText(((ChannelBuffer) this.buffer).getTopic());
		}
	}
	
	private class ButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			switch(e.getActionCommand()) {
				case "Submit":
					submitInput();
			}
		}
	}
	
	private void submitInput() {
		this.buffer.parseInput(this.inputTextField.getText());
		this.inputTextField.setText("");
	}
}