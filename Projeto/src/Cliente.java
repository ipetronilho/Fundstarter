/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.net.*;
import java.io.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Properties;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Cliente {
	
	static int TIMEOUT = 600000;
    static boolean DEBUG = true;
    static int tentativas = 3, falhou = 10;
   // static String serverAddress = "169.254.36.100";
    // tem de ter 2 ips e dois portos distintos
    static String serverAddress = "localhost";
    
    public static void main(String args[]) throws InterruptedException {

        // args[0] <- hostname of destination
        if (args.length == 0) {
         System.out.println("java TCPClient hostname");
         System.exit(0);
         }
         
        
/*
        InputStream inconfig = null;
        try {
            Properties properties = new Properties();
            inconfig = new FileInputStream("app.properties");
            properties.load(inconfig);
            serverAddress = properties.getProperty("server.address");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inconfig != null) {
                try {
                    inconfig.close();
                } catch (IOException e) {
                }
            }
        }
        
        */
        int[] serversockets = {6000, 8000};

        int i, j, fail_counter = 0;
        String msg = "";
        

        while (true) { // alterna constantemente entre servidores para se ligar
        	
            for (i = 0; i < 2; i++) {

                if (DEBUG == true) {
                    System.out.println("Nova ligacao ao servidor: " + (i + 1));
                }
                
                msg = conexaoServidor(serverAddress, serversockets[i]);
             
                if (msg.compareToIgnoreCase("TROCA") != 0 && msg.length() > 0) {
                    for (j = 0; j < tentativas; j++) { // tenta 3 vezes
                        if (msg.compareToIgnoreCase("TROCA") != 0 && msg.length() > 0) {
                            Thread.sleep(1000); // espera 1 segundo e volta a tentar
                            msg = conexaoServidor(serverAddress, serversockets[i]);
                            System.out.println("Recebi "+msg);
                        } 
                        else {
                            j = tentativas;
                            System.out.println("Vou trocar!");
                        }
                    }
                }

            }

            if (fail_counter == falhou) { // se tentou ligar-se 10 vezes a cada servidor e mesmo assim falhou as 10, desiste
                System.out.println("Servidores em baixo. Tente mais tarde.");
                System.exit(-1); // erro
            }
            fail_counter++;
        }//while

    }

    // retorna "" se conseguir ligar-se e "TROCA" se for preciso tentar outra vez
    public static String conexaoServidor(String serverAddress, int serversocket) { 
        String str = "";
        Socket s = null;
        String data;
        // regista as opera��es

        try {
            System.out.println("\nHost � '" + serverAddress + "' e socket" + serversocket);
            try {
                s = new Socket(serverAddress, serversocket);	// conectou-se ao servidor
            } catch (SocketException e) { // os dois servidores est�o offline
                return "TROCA";
            }
            DataInputStream in = new DataInputStream(s.getInputStream());
            DataOutputStream out = new DataOutputStream(s.getOutputStream());

            out.writeUTF("PEDIDO");
            s.setSoTimeout(TIMEOUT);	// tempo limitado
            data = in.readUTF();

            if (data.compareToIgnoreCase("SIM") == 0) { //pedido aceite
                str = "";

                Receiver MyThread = new Receiver(in, out);
                MyThread.start();

                String texto = "";
                InputStreamReader input = new InputStreamReader(System.in);
                BufferedReader reader = new BufferedReader(input);
                

                while (true) {
                    try {
                        texto = reader.readLine(); // l� de teclado
                    } catch (Exception e) {
                    }
                    try {
                        out.writeUTF(texto); // escreve
                    } catch (SocketException e) {
                        System.out.println("A conectar-se a outro servidor...");
                        return "TROCA";
                    }
                }
            }
            else if (data.compareToIgnoreCase("NAO") == 0) { // n�o consegue ligar-se ao servidor
                str = "TROCA";
            }
        } catch (IOException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (IOException e) {
                    System.out.println("close:" + e.getMessage());
                }
            }
        }

        return str;

    }
    
    
}

class Receiver extends Thread {
	ArrayList <Integer> listaOperacoes = new ArrayList <Integer>();
    DataInputStream in;
    DataOutputStream out;
    // dados gravados no cliente em caso de falha de rede
    int userID=-1;

    public Receiver(DataInputStream ain, DataOutputStream out) {
        this.in = ain;
        this.out = out;
    }
    
    

    //=============================
    public void run() {
        while (true) {
            // READ FROM SOCKET
            try {
                String data = in.readUTF(); // l� o que foi escrito
                // TODO: est� a dar um problema qualquer de strings e inteiros, ver isto!
                // acabou de fazer uma opera��o logo tenho de a gravar
             /*
                if (data.compareToIgnoreCase("OPERACAO")==0) {
                	//System.out.println("\nEntrei");
                	String opcao=in.readUTF();
                	System.out.println("()"+opcao);
                	int operacao=Integer.parseInt(opcao);
                	
                	if (listaOperacoes.isEmpty()) {
                		listaOperacoes.add(operacao);
                	}
                	else {
	                	// quando faz login/regista, tenho o id do user
	                	if ((listaOperacoes.get(0) == 1 || listaOperacoes.get(0) == 2) && userID==-1) {
                			// obtenho o userID
                    		userID=operacao;
                    	}
	                	//j� fez login
	                	else if (userID!=-1) {
	                		listaOperacoes.add(operacao);
	                		
	                	}
                	}
                }
                //remove a opera��o
                else if (data.compareToIgnoreCase("DESFAZ")==0) {
                	
                }
                
                else if (data.compareToIgnoreCase("NOVASESSAO")==0) {
                	int i;
                	System.out.println("Alerta nova sessao!");
                	if (!listaOperacoes.isEmpty()) {
                		
	                	for (i=0;i<listaOperacoes.size();i++) {
	                		//System.out.println("Recebo"+in.readUTF());
	                		String operacao = Integer.toString(listaOperacoes.get(i));
	                		out.writeUTF(operacao);
	                	}
                	}
                }
                
                else*/
                	System.out.println("> " + data); // print o que o serv. escreveu
            } catch (SocketException e) {
                //System.out.print("O Socket Servidor fechou"); //Caso o socket de conec��o ao cliente se fechar este imprime o erro
                break;
            } catch (Exception e) {
                System.out.println("Sock:" + e.getMessage());
                break;	
            }
        }
        //System.out.println("Fim.");

    }
}
