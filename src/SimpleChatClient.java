import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;



public class SimpleChatClient {
	

	JFrame frame;
	JPanel mainPanel;
	JTextArea incoming;
	JTextField outgoing;
	JButton sendButton;
	JScrollPane qScroller;
	BufferedReader reader;
	PrintWriter writer;
	Socket sock;
	String nameClient;
	
	JFrame frameLogin;
	JPanel mainPanelLogin;
	JTextField outgoingLogin;
	JButton sendButtonLogin;
	JLabel loginLabel;
	JLabel NameLabel;
	
	
	AudioInputStream audioInputStream = null;
	Clip clip = null;

	
	public SimpleChatClient() {
		
		
		//фрейм для запроса ника
		frameLogin = new JFrame("Дипломный проект(клиент)");
		mainPanelLogin = new JPanel();
		outgoingLogin = new JTextField(20);
		sendButtonLogin = new JButton("Отправить");
		loginLabel = new JLabel("введите свой никнейм");
		mainPanelLogin.add(loginLabel);
		mainPanelLogin.add(outgoingLogin);
		mainPanelLogin.add(sendButtonLogin);
		sendButtonLogin.addActionListener(new sendButtonListenerLogin());
		frameLogin.getContentPane().add(BorderLayout.CENTER,  mainPanelLogin);
		frameLogin.setSize(250, 120);
		frameLogin.setLocationRelativeTo(null);
		frameLogin.setVisible(true);
		
	}
	
	public static void main(String[] args) {
		new SimpleChatClient();
	}
	
//	подключаемся к серверу и отправляем ник
	private void setUpNetworking() {
		
		try {
			sock = new Socket ("localhost", 5000);
			InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
			reader = new BufferedReader(streamReader);
			writer = new PrintWriter(sock.getOutputStream());
			writer.println(nameClient);
			writer.println("#STOP#");
			writer.flush();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
//	каждый раз при нажатии кнопки отправить отправляем сообщение из текстового поля ввода
	public class sendButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			try {
				
				writer.println(nameClient+" : "+outgoing.getText());
				writer.flush();
				
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			outgoing.setText("");
			outgoing.requestFocus();
		}
	}
	
	//обработчик кнопки, считываем ник, и если он не null, создаем новый фрейм и запускаем поток для чтения данных с сервера
	public class sendButtonListenerLogin implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			
			nameClient = outgoingLogin.getText();
			
			
			
			if(!nameClient.isEmpty()) {
				
				frameLogin.dispose();
				
				frame = new JFrame("Дипломный проект");
				mainPanel = new JPanel();
				mainPanel.setLayout(null);
				incoming = new JTextArea(20, 30);
				incoming.setLineWrap(true);
				incoming.setWrapStyleWord(true);
				incoming.setEditable(false);
				qScroller = new JScrollPane(incoming);
				outgoing = new JTextField(20);
				qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
				qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
				NameLabel = new JLabel();
				NameLabel.setText("<html>"+"<h1>Online</h1>"+"</html>");
				NameLabel.setVerticalAlignment(JLabel.TOP);
				NameLabel.setHorizontalAlignment(JLabel.LEFT);
				sendButton = new JButton("Отправить");
				sendButton.addActionListener(new sendButtonListener());
				
				try {
					audioInputStream = AudioSystem.getAudioInputStream(new File("Starry.mid"));
					clip = AudioSystem.getClip();
					clip.open(audioInputStream);
					FloatControl vc = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
					vc.setValue(-15);
				} catch(Exception ex) {
					ex.printStackTrace();
				}
				
				qScroller.setLocation(0,0);
				qScroller.setSize(450, 500);
				outgoing.setLocation(0,505);
				outgoing.setSize(340, 25);
				sendButton.setLocation(340,505);
				sendButton.setSize(100, 25);
				NameLabel.setLocation(450,0);
				NameLabel.setSize(200, 530);
				
				mainPanel.add(qScroller);
				mainPanel.add(NameLabel);
				mainPanel.add(outgoing);
				mainPanel.add(sendButton);
				
				
				setUpNetworking();
				
				Thread readerThread = new Thread(new IncomingReader());
				readerThread.start();
					
				
				frame.getContentPane().add(BorderLayout.CENTER,  mainPanel);
				frame.setSize(650, 580);
				frame.setLocationRelativeTo(null);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setVisible(true);
			}
		}
	}
	
	public class IncomingReader implements Runnable {
		
		public void run() {
			String message;
			
			try {
			    
				while ((message = reader.readLine()) != null) {

					if(!message.isEmpty())	{
						String []InputString = message.split("#STOP#");
						
							for(int n = 0; n < 2; n++) {
								if(n==0) {

									NameLabel.setText("<html>"+"<h1>Online</h1>"+InputString[n]+"</html>");
								} 
								if(n==1 && (n == InputString.length-1)) {
									incoming.append(InputString[n] + "\n");
								}
							}
							
						clip.setFramePosition(0);
						clip.start();															
					}	
				}	
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
}
