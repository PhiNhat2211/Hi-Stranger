package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.PublicKey;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.synth.ColorType;
import javax.swing.text.DefaultCaret;

import org.apache.commons.lang3.StringUtils;

import model.Client;
import model.Message;
import model.Status;

import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JList;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.Toolkit;
import javax.swing.SwingConstants;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.Label;
import java.awt.event.WindowFocusListener;

public class ClientForm extends JFrame {

	public int i = 0;
	public JPanel mainPanel;
	public JPanel insideCenter;
	public String ipAddress;
	public int port;
	private JScrollPane scroll;
	private JButton btnSend;
	private JButton btnConnect;
	private JButton btnEnd;
	private JTextArea displayArea;
	private JTextField txtMessage;
	private JTextField txtName;
	private Client client;
	private volatile boolean running = true;
	Thread t;

	public void addPlaceHolderStyle(JTextField textField)  {
		textField.setForeground(Color.gray);
	}
	
	public void removePlaceHolderStyle(JTextField textField)  {
		textField.setForeground(Color.black);
	}
	
	public ClientForm() throws UnknownHostException, IOException {
		setForeground(Color.BLACK);
		setFont(new Font("Arial", Font.BOLD, 13));
		setIconImage(Toolkit.getDefaultToolkit().getImage("C:\\Users\\phinh\\eclipse-workspace\\Hi Stranger\\chat.png"));
		setBackground(Color.LIGHT_GRAY);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (client != null) {
					try {
						running = false;
						Message send = new Message(null, null, Status.EXIT);
						System.out.println(send);
						client.sendMessage(send);
						client.closeAll();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		
		setResizable(false);
		setSize(500, 500);

		setLocationRelativeTo(null);
		setTitle("Hi Stranger");
		mainPanel = new JPanel();
		mainPanel.setBackground(Color.WHITE);
		mainPanel.setLayout(new BorderLayout(0, 0));
		setContentPane(mainPanel);

		ipAddress = "localhost";
		port = 1234;

		insideCenter = new JPanel();
		insideCenter.setBorder(null);
		insideCenter.setPreferredSize(new Dimension(180, 650));
		insideCenter.setBackground(SystemColor.control);
		insideCenter.setLayout(null);
		mainPanel.add(insideCenter, BorderLayout.CENTER);

		displayArea = new JTextArea();
		displayArea.setForeground(Color.BLACK);
		displayArea.setBackground(Color.WHITE);
		displayArea.setFont(new Font("Arial", Font.PLAIN, 13));
		displayArea.setEditable(false);
		
		DefaultCaret caret = (DefaultCaret)displayArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.OUT_BOTTOM);

		scroll = new JScrollPane(displayArea);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setBounds(9, 50, 465, 326);
		
		insideCenter.add(scroll);

		txtMessage = new JTextField();
		txtMessage.setText("Enter message here...");
		txtMessage.addFocusListener(new FocusAdapter() {
			
			@Override
			public void focusGained(FocusEvent e) {
				if(txtMessage.getText().equals("Enter message here...")) {
					txtMessage.setText("");
					txtMessage.requestFocus();
					removePlaceHolderStyle(txtMessage);
				}
			}
			@Override
			public void focusLost(FocusEvent e) {
				if(txtMessage.getText().equals("")) {
					addPlaceHolderStyle(txtMessage);
					txtMessage.setText("Enter message here...");
				}
			}
		});
		txtMessage.setFont(new Font("Arial", Font.PLAIN, 13));
		addPlaceHolderStyle(txtMessage);
		txtMessage.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (txtMessage.getText().isEmpty()) {
					btnSend.setEnabled(false);
				}else {
					btnSend.setEnabled(true);
				}
			}
		});
		txtMessage.setBounds(9, 387, 370, 63);
		insideCenter.add(txtMessage);
		txtMessage.setColumns(10);

		btnSend = new JButton("SEND");
		btnSend.setForeground(new Color(0, 0, 0));
		btnSend.setFont(new Font("Arial", Font.BOLD, 10));
		btnSend.setBackground(new Color(154, 205, 50));
		btnSend.setEnabled(false);
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (client == null) {
						JOptionPane.showMessageDialog(null, "You have to make a connection first", "Alert",
								JOptionPane.ERROR_MESSAGE);
						txtMessage.setText("");
					} else {
						if (client.isMatched()) {
							if (!txtMessage.getText().isEmpty()) {
								Message send = new Message(client.getName(), txtMessage.getText(), Status.CHAT);
								client.sendMessage(send);
								displayArea.append(client.getName() + " : " + txtMessage.getText() + "\n");
								txtMessage.setText("");
								btnSend.setEnabled(false);
							}
						} else if(client.isMatched()==false) {
							JOptionPane.showMessageDialog(null, "Wait for someone to come", "Alert",
									JOptionPane.ERROR_MESSAGE);
							txtMessage.setText("");
						}
					}

				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnSend.setBounds(389, 387, 85, 63);
		insideCenter.add(btnSend);

		txtName = new JTextField();
		txtName.setText("Enter your name...");
		addPlaceHolderStyle(txtName);
		txtName.addFocusListener(new FocusAdapter() {
		
			@Override
			public void focusGained(FocusEvent e) {
				if(txtName.getText().equals("Enter your name...")) {
					txtName.setText("");
					txtName.requestFocus();
					removePlaceHolderStyle(txtName);
				}
			}
			@Override
			public void focusLost(FocusEvent e) {
				if(txtName.getText().equals("")) {
					addPlaceHolderStyle(txtName);
					txtName.setText("Enter your name...");
				}
			}
		});
		txtName.setForeground(Color.BLACK);
		txtName.setFont(new Font("Arial", Font.PLAIN, 13));
		txtName.setBounds(9, 11, 275, 28);
		insideCenter.add(txtName);
		txtName.setColumns(10);

		btnConnect = new JButton("CONNECT");
		btnConnect.setForeground(Color.BLACK);
		btnConnect.setFont(new Font("Arial", Font.BOLD, 10));
		btnConnect.setBackground(new Color(70, 130, 180));
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (txtName.getText().isEmpty()) {
					JOptionPane.showMessageDialog(null, "Give a name first", "Alert", JOptionPane.ERROR_MESSAGE);
				}
				else if(txtName.getText().equals("Enter your name...")) {
					JOptionPane.showMessageDialog(null, "Give a name first", "Alert", JOptionPane.ERROR_MESSAGE);
				}
				else {
					try {
						if (client == null) {
							client = new Client(new Socket(ipAddress, port), txtName.getText());
							Message welcome = new Message(txtName.getText(), null, Status.CONNECT);
							client.sendMessage(welcome);
						} else {
							Message welcome = new Message(txtName.getText(), null, Status.CONNECT);
							client.sendMessage(welcome);
							txtName.setEditable(false);
						}

					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					if (i == 0) {
						t = new Thread(new Runnable() {
							@Override
							public void run() {
								while (running) {
									// TODO Auto-generated method stub
									try {
										Message receivedMessage = client.receiveMessage();
										System.out.println(receivedMessage);
										switch (receivedMessage.getStatus()) {
										case MATCH:
											int action = JOptionPane.showConfirmDialog(null,
													"Do you want to chat with: " + receivedMessage.getName() + "?",
													"Notification", JOptionPane.YES_NO_OPTION);
											if (action == JOptionPane.OK_OPTION) {
												Message accept = new Message(client.getName(), null, Status.OK);
												client.sendMessage(accept);
												client.setMatched(true);
												btnEnd.setEnabled(true);
												displayArea.setText("");
											} else {
												Message refuse = new Message(client.getName(), null, Status.REFUSE);
												client.sendMessage(refuse);
												btnEnd.setEnabled(false);
											}
											break;
										case CHAT:
											displayArea.append(receivedMessage.getName() + " : " + receivedMessage.getMessage() + "\n");
											break;
										case EXIST:
											JOptionPane.showMessageDialog(null, "This name has been taken!", "Alert",
													JOptionPane.ERROR_MESSAGE);
											break;
										case UNMATCH:
											JOptionPane.showMessageDialog(null,
													receivedMessage.getName() + " has refused the connection!", "Alert",
													JOptionPane.ERROR_MESSAGE);
											client.setMatched(false);
											btnEnd.setEnabled(false);
											break;
										case EXIT:
											JOptionPane.showMessageDialog(null,
													receivedMessage.getName() + " has left the chat!", "Alert",
													JOptionPane.ERROR_MESSAGE);
											client.setMatched(false);
											btnEnd.setEnabled(false);
											displayArea.setText("");
											Message refuse = new Message(client.getName(), null, Status.MATCH);
											client.sendMessage(refuse);
											break;
										case CONNECTED:
											btnConnect.setEnabled(false);
											txtName.setEditable(false);
											break;
										default:

										}

									} catch (IOException | ClassNotFoundException e) {
										System.out.println();
									}
								}
							}
						});
						t.start();
						i++;
					}
				}
			}
		});

		btnConnect.setBounds(294, 10, 85, 30);
		insideCenter.add(btnConnect);

		btnEnd = new JButton("END");
		btnEnd.setForeground(Color.BLACK);
		btnEnd.setFont(new Font("Arial", Font.BOLD, 10));
		btnEnd.setBackground(new Color(255, 99, 71));
		btnEnd.setEnabled(false);
		btnEnd.setBounds(389, 10, 85, 30);
		btnEnd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
					try {
						Message end = new Message(null, null, Status.DISCONNECT);
						client.sendMessage(end);
						client.setMatched(false);
						btnEnd.setEnabled(false);
						displayArea.setText("");
						Message refuse = new Message(client.getName(), null, Status.MATCH);
						client.sendMessage(refuse);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
		});
		insideCenter.add(btnEnd);
	}
}
