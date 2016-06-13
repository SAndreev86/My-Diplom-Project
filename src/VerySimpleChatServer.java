import java.io.*; 
import java.net.*; 
import java.util.*;
import javax.swing.JFrame;


public class VerySimpleChatServer { 
	
	ArrayList<PrintWriter> clientOutputStreams; 
	ArrayList<String> NickName = new ArrayList<String>(); 
	
	boolean IfNewUser = false;
	
	public class ClientHandler implements Runnable { 
		
		BufferedReader reader; 
		Socket sock; 
		int FinalIndex = 0;
		
//		символьный поток для приема строк от клиента
		public ClientHandler(Socket clientSocket) { 
			try { 
				sock = clientSocket; 
				InputStreamReader isReader = new InputStreamReader(sock.getInputStream()); 
				reader = new BufferedReader(isReader); 
				
			} catch(Exception ex) {
					ex.printStackTrace();
			} 
		} 
		
		//принимаем строки от каждого клиента(если только подключился к серву, заносим ник в дин.массив)
		//и рассылаем всем клиентам
		public void run() { 
			String message; 
			try { 
				while ((message = reader.readLine()) != null) {
					
					System.out.println("Получил");
					if(!message.isEmpty())	{
	
						if(IfNewUser && !message.contains("#STOP#")) {
							NickName.add(message);
							FinalIndex = NickName.indexOf(message);
						}	
						if(message.contains("#STOP#")) {
							IfNewUser = false;
							tellEveryone (message);
						}	
						if(!IfNewUser && !message.contains("#STOP#")) {
							tellEveryone (message);
						}												
					}
				}
			}	catch(Exception ex) {
				NickName.remove(FinalIndex);
			} 
		}
	}
	
	public static void main (String[] args) {
		
		JFrame frameLogin = new JFrame("Дипломный проект(сервер)");
		frameLogin.setSize(250, 120);
		frameLogin.setLocationRelativeTo(null);
		frameLogin.setVisible(true);
		frameLogin.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		new VerySimpleChatServer().go(); 
	}
	
	
	public void go() { 
		clientOutputStreams = new ArrayList<PrintWriter>(); 
		try { 
			@SuppressWarnings("resource")
			ServerSocket serverSock = new ServerSocket(5000);
			
			//при подключении нового клиента, в отдельном потоке читаем данные от него и рассылаем всем остальным
			while(true) { 
				
				Socket clientSocket = serverSock.accept(); 
				IfNewUser = true;
				PrintWriter writer = new PrintWriter(clientSocket.getOutputStream()); 
				clientOutputStreams.add(writer); 
				Thread t = new Thread(new ClientHandler(clientSocket));
				

				t.start(); 
	
			}
		} catch(Exception ex) {
			ex.printStackTrace(); 
		}
	} 
	
//отправляем всем пользователям сообщение, в начале которого записываем все ники для отображения их в онлайне
	public void tellEveryone(String message) { 
		
		Iterator<PrintWriter> itText = clientOutputStreams.iterator();
		message= (String.join("<br>", NickName))+ "#STOP#" + message;
		
		while(itText.hasNext()) { 
			
			try { 
				
				PrintWriter writer = itText.next(); 

					writer.println(message); 
					writer.flush(); 
					
			} catch(Exception ex) { 
					ex.printStackTrace(); 
			} 
		}		
	}
}