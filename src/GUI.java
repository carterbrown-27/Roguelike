import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextArea;
import javax.swing.JLabel;


public class GUI extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public JPanel contentPane;
	
	public JTextArea textArea;
	public JPanel panel;
	private JLabel lblNewLabel;

	public void run() {
		try {
			GUI frame = new GUI();
			frame.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * Create the frame.
	 */
	public GUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 520);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		panel = new JPanel();
		panel.setBounds(12, 0, 476, 476);
		contentPane.add(panel);
		
		lblNewLabel = new JLabel("New label");
		panel.add(lblNewLabel);
		
		textArea = new JTextArea();
		textArea.setBounds(505, 234, 277, 219);
		contentPane.add(textArea);
		textArea.setEditable(false);
		textArea.setRows(12);
	}
}
