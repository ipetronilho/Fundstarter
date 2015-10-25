/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.net.*;
import java.io.*;
import java.util.ArrayDeque;
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
                    System.out.println("Vou ligar-me ao servidor: " + (i + 1));
                }
                
                msg = conexaoServidor(serverAddress, serversockets[i]);

                if (msg.compareToIgnoreCase("TROCA") != 0 && msg.length() > 0) {
                    for (j = 0; j < tentativas; j++) { // tenta 3 vezes
                        if (msg.compareToIgnoreCase("TROCA") != 0 && msg.length() > 0) {
                            Thread.sleep(1000); // espera 1 segundo e volta a tentar
                            msg = conexaoServidor(serverAddress, serversockets[i]);
                        } 
                        else {
                            j = tentativas;
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
        // regista as operações

        try {
            System.out.println("\nHost é '" + serverAddress + "' e socket" + serversocket);
            try {
                s = new Socket(serverAddress, serversocket);	// conectou-se ao servidor
            } catch (SocketException e) { // os dois servidores estão offline
                return "TROCA";
            }
            DataInputStream in = new DataInputStream(s.getInputStream());
            DataOutputStream out = new DataOutputStream(s.getOutputStream());

            out.writeUTF("PEDIDO");
            s.setSoTimeout(TIMEOUT);	// tempo limitado
            data = in.readUTF();

            if (data.compareToIgnoreCase("SIM") == 0) { //pedido aceite
                str = "";

                Receiver MyThread = new Receiver(in);
                MyThread.start();

                String texto = "";
                InputStreamReader input = new InputStreamReader(System.in);
                BufferedReader reader = new BufferedReader(input);
                

                while (true) {
                    try {
                        texto = reader.readLine();
                    } catch (Exception e) {
                    }
                    try {
                        out.writeUTF(texto);
                    } catch (SocketException e) {
                        System.out.println("O Socket Servidor fechou" + e.getMessage());
                        return "TROCA";
                    }
                }
            } 
            else if (data.compareToIgnoreCase("NAO") == 0) { // não consegue ligar-se ao servidor
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
	Stack <Integer> stack = new Stack <Integer>();
    DataInputStream in;
    
    // dados gravados no cliente em caso de falha de rede
    int userID;

    public Receiver(DataInputStream ain) {
        this.in = ain;
    }

    //=============================
    public void run() {
        while (true) {
            // READ FROM SOCKET
            try {
                String data = in.readUTF();
                // DISPLAY WHAT WAS READ
                // acabou de fazer uma operação logo tenho de a gravar
                /*if (data.compareToIgnoreCase("OPERACAO")==0) {
                	System.out.println("A gravar a operacao");
                	int opcao = Integer.parseInt(in.readUTF());
                	stack.push(opcao);  acrescenta login à lista de operações
                	if (opcao==1 || opcao==2) {  servidor tem de enviar o userID
                		userID=Integer.parseInt(in.readUTF());  ver como
                	}
                	
                	
                }*/
                	System.out.println("> " + data);
            } catch (SocketException e) {
                System.out.print("O Socket Servidor fechou"); //Caso o socket de conecção ao cliente se fechar este imprime o erro
                break;
            } catch (Exception e) {
                System.out.println("Sock:" + e.getMessage());
                break;	
            }
        }
        System.out.println("Fim.");

    }
}
