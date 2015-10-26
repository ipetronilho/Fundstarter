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
    static String nomeUser="";
    
    public static void main(String args[]) throws InterruptedException {
        // args[0] <- hostname of destination
        if (args.length == 0) {
         System.out.println("java TCPClient hostname");
         System.exit(0);
         }
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

    public static void setNomeUser(String username) {
    	nomeUser=username;
    	System.out.println("WHOOHOO - O meu nome e "+nomeUser);
    }
    public static String getNomeUser() {
    	return nomeUser;
    }
    
    // retorna "" se conseguir ligar-se e "TROCA" se for preciso tentar outra vez
    public static String conexaoServidor(String serverAddress, int serversocket) { 
        String str = "";
        Socket s = null;
        String data;
        //String nomeUser="";
        // regista as operações
        
        try {
            System.out.println("\nHost é '" + serverAddress + "' e socket" + serversocket);
            try {
                s = new Socket(serverAddress, serversocket);	// conectou-se ao servidor
            } catch (SocketException e) { 						// não consegue ligar-se ao servidor
                return "TROCA";
            }
            DataInputStream in = new DataInputStream(s.getInputStream());
            DataOutputStream out = new DataOutputStream(s.getOutputStream());

            out.writeUTF("PEDIDO");
            s.setSoTimeout(TIMEOUT);	// tempo limitado
            data = in.readUTF();

            if (data.compareToIgnoreCase("SIM") == 0) { //pedido aceite
            	
                str = "";
                
                Receiver MyThread = new Receiver(in); // lê de teclado
                MyThread.start();
                
                String texto = "";
                InputStreamReader input = new InputStreamReader(System.in);
                BufferedReader reader = new BufferedReader(input);
                
                
                while (true) {
                    try {
                        texto = reader.readLine(); // lê de teclado
                    } catch (Exception e) {
                    }
                    try {
                        out.writeUTF(texto); // escreve
                    } catch (SocketException e) {
                        System.out.println("A conectar-se a outro servidor...");
                        setNomeUser(MyThread.getNomeUser());
                        return "TROCA";
                    }
                }
            }
            else if (data.compareToIgnoreCase("NAO") == 0) { // não consegue ligar-se ao servidor
                str = "TROCA";
            }
        } catch (IOException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        } finally {	// tenho de fechar o socket
            if (s != null) {
                try {
                    s.close();
                } catch (IOException e) {
                    System.out.println("close:" + e.getMessage());
                }
            }
        }
        //MyThread.
        //setNomeUser(MyThread.);
        return str;

    }
    
    public static void leFicheiro(DataOutputStream out, String nomeUser) {
    	// The name of the file to open.
        String fileName = "ficheiros/"+nomeUser+"_infologin.txt";

        // This will reference one line at a time
        String line = null;

        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            out.writeUTF("1"); // login
            while((line = bufferedReader.readLine()) != null) { // escreve os comandos que estavam perdidos
                out.writeUTF(line);
            }   

            bufferedReader.close();         
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + fileName + "'");                
        }
        catch(IOException ex) {
            System.out.println("Error reading file '" + fileName + "'");                  

        }
    	
    }
    
}

class Receiver extends Thread {
	ArrayList <Integer> listaOperacoes = new ArrayList <Integer>();
    DataInputStream in;
    DataOutputStream out;
    // dados gravados no cliente em caso de falha de rede
    int userID=-1;
    String nomeUser;

    public Receiver(DataInputStream ain) {
        this.in = ain;
    }
    
    public String getNomeUser() {
    	return nomeUser;
    }

    //=============================
    public void run() {
        while (true) {
            // READ FROM SOCKET
            try {
                String data = in.readUTF(); // lê o que foi escrito
                
                if (data.compareToIgnoreCase("USER")==0) { // restabelece ligação perdida
	                nomeUser=in.readUTF(); // capta o username
	                //setNomeUser(nomeUser);
	                System.out.println("Captei o username!! é "+getNomeUser());
	               /* if(nomeUser.compareToIgnoreCase("")!=0)
	                	leFicheiro(out, nomeUser);*/
	               
            	}
                else
                	System.out.println("> " + data); // print o que o serv. escreveu
            } catch (SocketException e) {
            	
                System.out.print("O Socket Servidor fechou"); //Caso o socket de conecção ao cliente se fechar este imprime o erro
                break;
            } catch (Exception e) {
                System.out.println("Sock:" + e.getMessage());
                break;	
            }
        }
        //System.out.println("Fim.");

    }

}
