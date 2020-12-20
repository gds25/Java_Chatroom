package server;

import javax.swing.JEditorPane;
import javax.swing.JPanel;

public class Client extends JPanel{
	
	private static final long serialVersionUID = 1L;
	private String name;
    private JEditorPane nameField;

	// same functionality as User class in client package, used for serverUI
    public Client(String name) {
    	this.name = name;
    	nameField = new JEditorPane();
     	nameField.setContentType("text/html");
     	nameField.setText(name);
     	//nameField.setText(String.format(wrapper, name));
     	nameField.setEditable(false);
     	this.add(nameField);
    }

    public String getName() {
    	return name;
    }

	public void setName(String name) {
		// TODO Auto-generated method stub
		nameField.setText(name);
	}
    
}