package com.shelfx;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class EchoThread extends Thread {
	protected Socket socket;
	protected EchoThread respont_to_thread=null;
	public String key="N/A";
	private boolean autoclose=true;
	public PrintWriter out=null;
	private ArrayList<EchoThread> lists;
	private String app="Generic";
	private String pass="";
	private boolean responded;
	private boolean monitor;
	
	public EchoThread(Socket clientSocket, ArrayList<EchoThread> lists) {
		this.socket = clientSocket;
		this.lists = lists;
		System.out.println("Thread created "+socket.getInetAddress().toString());
	}

	public void run() {
		InputStream inp = null;
		BufferedReader brinp = null;
		try {
			inp = socket.getInputStream();
			out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
			brinp =new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
			out.write("SocketsMe Server Version: 2.01\r\n");
			out.flush();
			key=socket.getInetAddress().toString();
		} catch (IOException e) {
			System.out.println("Removing publish socket 1 "+key+" "+app);
			
			try{
				socket.close();

			} catch (Exception e2) {
			}		
			synchronized (lists) {
			lists.remove(this);
			}
			return;
		}
		String line;
		EchoThread echo=null;
		while (true) {
			try {
				


				line = brinp.readLine();
				//ping socket every 10 minutes


				if (line!=null)
				{
					responded=true;
					System.out.println("Got from: "+key+" "+app+" : " + line);
					if (line.length()>0)
						if ((int)line.charAt(0)==4)
						{
							System.out.println("Removing publish socket 1 "+key+" "+app);
							
							try{
								socket.close();

							} catch (Exception e2) {
							}		
							synchronized (lists) {
							lists.remove(this);
							}
							break;

						} 
	
					if (line.equals("list"))
					{
						synchronized (lists) {
							for (int i = 0; i < lists.size(); i++) {
								try
								{
									echo = (EchoThread)lists.get(i);
									if (echo!=null)
										if (echo!=this )
										{
											out.write(echo.key+"\r\n");  						    

										}


								} catch (Exception e) {
									//e.printStackTrace();


								}

							}
							out.flush();
						}
					} else
						if (line.equals("OK")) {
							//got OK
							if (respont_to_thread!=null)
							{
								

								//try{
								respont_to_thread.out.write("1\r\n");
								respont_to_thread.out.flush();
								

							}
						} else
							if (line.equals("ec")) {
								//got echo / got it
								out.write("gi\r\n");
								out.flush(); 
							} else
							if (line.equals("p")) {
								//got ping
								out.write("p\r\n");
								out.flush(); 
							} else
								if (line.equals("info")) {
									synchronized (lists) {
										out.write("lists count:"+lists.size()+"\r\n");
										out.flush(); 
									}
								} else
									if (line.startsWith("publish ")) {
										//find all sockets with same key and publish the message
										String mkey=line.split(" ")[1];
										String message = line.substring(9+mkey.length());
										
										SimpleDateFormat dateformatMMDDYYYY = new SimpleDateFormat(
												"yyyy-MM-dd HH:mm:ss");
										
										Date session_date = new Date();
										StringBuilder nowMMDDYYYY = new StringBuilder(
												dateformatMMDDYYYY.format(session_date));

								

										
										int count=0;
										synchronized (lists) {
											for (int i = 0; i < lists.size(); i++) {
												try
												{
													echo = (EchoThread)lists.get(i);
													
													if (echo!=null)
													{
														if (echo.monitor)
														{
															echo.out.write( nowMMDDYYYY+": "+mkey+" "+message+"\r\n");
															echo.out.flush();
													
														}
														if (echo.key!=null)
														{
															String keys[]=echo.key.split(",");
															for (int n = 0; n < keys.length; n++) {
															if (echo!=this && (keys[n]+echo.pass).equals(mkey))
															{
																echo.respont_to_thread=this;
																echo.responded=false;
																echo.out.write(message+"\r\n");
																echo.out.flush();
																count++;
																System.out.println("Sent to "+i+" "+n+" "+mkey+" "+echo.key+" "+echo.app+": "+message);    						    
															}
															}
														}
													}

												} catch (Exception e) {
													e.printStackTrace();
											
													try{
														System.out.println("Removing echo socket "+echo.key);
														lists.remove(echo);

													} catch (Exception e2) {
													}
													
													try{
														echo.socket.close();

													} catch (Exception e2) {
													}	

												}

											}
											if (count==0)
											{
												System.err.println("No match "+key+" "+app);
												out.write("0\r\n");
												out.flush();
												
												
												//break;
											}
										}



									}
									
									
									
									else
										if (line.startsWith("pass ")) {
											pass=line.substring(5);
										} else
										if (line.startsWith("app ")) {
											app=line.substring(4);
											autoclose=false;
											//out.writeBytes(key+"\r\n");
											//out.writeBytes("OK\r\n");
											//out.flush();

										} else
											if (line.equals("monitor")) {
												monitor=true;
												autoclose=false;
												//out.writeBytes(key+"\r\n");
												//out.writeBytes("OK\r\n");
												//out.flush();

											} else
										if (line.startsWith("key ")) {
											key=line.substring(4);
											autoclose=false;
											//out.writeBytes(key+"\r\n");
											//out.writeBytes("OK\r\n");
											//out.flush();

										} else
											if ((line == null) || line.equalsIgnoreCase("QUIT")) {
												synchronized (lists) {
													System.out.println("Removing socket "+key+" "+app);

													lists.remove(this);
												}
												try {
													socket.close();
												} catch (IOException e1) {
													// TODO Auto-generated catch block
													e1.printStackTrace();
												}

												break;
											} else {
												//echoing back unknown command
												out.write(line + "\r\n");
												out.flush();

												sleep(250);
												System.out.println("Removing publish socket 4 "+key+" "+app);
												synchronized (lists) {
													lists.remove(this);	
												}
												try {
													socket.close();
												} catch (IOException e1) {
													// TODO Auto-generated catch block
													e1.printStackTrace();
												}
												break;


											}
				} else {
					//we did not get anything. Lets check if out socket is OK
					
						System.err.println("Timeout 3, closing socket "+key+" "+app+" "+responded);

						try {
							out.write("0\r\n");
							out.flush();
							sleep(250);
						} catch (Exception e2) {
							// TODO Auto-generated catch block
							e2.printStackTrace();
						}

						synchronized (lists) {
							System.out.println("Removing socket "+key+" "+app);

							lists.remove(this);
						}
						try {
							socket.close();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						break;
					}
					responded=false;

				
				sleep(10);

			} catch (Exception e) {
				//e.printStackTrace();
				if ( e.getMessage().equals("Read timed out"))
				{
					System.err.println("Read timeout "+key+" "+app+" "+responded+" "+respont_to_thread);
					if (autoclose)
					{
						System.err.println("Timeout 1, closing socket "+key+" "+app+" "+responded);

						try {
							out.write("0\r\n");
							out.flush();
							sleep(250);
						} catch (Exception e2) {
							// TODO Auto-generated catch block
							e2.printStackTrace();
						}

						synchronized (lists) {
							System.out.println("Removing socket "+key+" "+app);

							lists.remove(this);
						}
						try {
							socket.close();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						break;
					}
				} else
					try{
						synchronized (lists) {
							if (e.getMessage()==null)
							{
								System.err.println("Error:" + e.getMessage());
								System.err.println("\n2");
								System.err.println(e.getMessage());
								System.err.println("\n3");
								System.err.println(e.getLocalizedMessage());
								System.err.println("\n4");
								System.err.println(e.getCause());
								System.err.println("\n5");
								System.err.println(Arrays.toString(e.getStackTrace()));
								System.err.println("\n6");
							}
							System.out.println("Removing publish socket 3 "+e.getMessage());
							try {
								socket.close();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							lists.remove(this);


							if (echo!=null)
							{
								System.out.println("Removing echo socket 3 "+echo.key);
								try {
									echo.socket.close();
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								lists.remove(echo);

							}


						}
						System.out.println("Thread ended" );
						if (e.getMessage()==null)
						{
							//
						}
						else
							if (e.getMessage().equals("Socket closed") || e.getMessage().equals("Connection reset") 
									|| e.getMessage().equals("Read timed out"))
							{
								//do nothing
							} else {
								System.err.println("Error:" + e.getMessage());
								System.err.println("\n2");
								System.err.println(e.getMessage());
								System.err.println("\n3");
								System.err.println(e.getLocalizedMessage());
								System.err.println("\n4");
								System.err.println(e.getCause());
								System.err.println("\n5");
								System.err.println(Arrays.toString(e.getStackTrace()));
								System.err.println("\n6");
							}
						break;

					} catch (Exception e2) {
						System.err.println("Error:" + e2.getMessage());
						System.err.println("\n2");
						System.err.println(e2.getMessage());
						System.err.println("\n3");
						System.err.println(e2.getLocalizedMessage());
						System.err.println("\n4");
						System.err.println(e2.getCause());
						System.err.println("\n5");
						System.err.println(Arrays.toString(e2.getStackTrace()));
						System.err.println("\n6");

						try {
							socket.close();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						synchronized (lists) {
							System.out.println("Removing socket 4"+key+" "+app);

							lists.remove(this);
						}
						break;
					}
			}
		}
		try {
			socket.close();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
		synchronized (lists) {
		lists.remove(this);
		}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}