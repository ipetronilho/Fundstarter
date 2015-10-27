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
                
                if (nomeUser.compareToIgnoreCase("")!=0) {
                	leFicheiroLogin(out,nomeUser);
                }
                leFicheiroBackup(out, nomeUser);
                while (true) {
                    try {
                        texto = reader.readLine(); // lê de teclado
                    } catch (Exception e) {
                    }
                    try {
                        out.writeUTF(texto); // escreve
                    } catch (SocketException e) {
                    
                    	
                        System.out.println("A conectar-se a outro servidor...");
                        setNomeUser(MyThread.getNomeUser());// fica com o username
                        
                        salvaFicheiroBackup(nomeUser);
                        guardaFicheiro(texto, nomeUser);
                        
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
    
public static void guardaFicheiro(String st, String username) {
    	String filepath;
		filepath="ficheiros/"+username+"_backup.txt";
    	System.out.println("Acrescento "+st);
		try {
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(filepath, true)));
		    writer.println(st);
		    writer.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    
    
    public static boolean ficheiroVazio(String username){
		BufferedReader br; 
		try {
			br= new BufferedReader(new FileReader("ficheiros/"+username+"_infologin.txt"));  
			if (br.readLine() == null) {
				br.close();
			    return true;
			}
			br.close();
			
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
    }
    
    /* lê login */
    public static void leFicheiroLogin(DataOutputStream out, String nomeUser) {
        String fileName = "ficheiros/"+nomeUser+"_infologin.txt";
        String line = null;
        System.out.println("--LE FICHEIRO LOGIN --");
        System.out.println("User e "+nomeUser);
        // VER: quando ele chega aqui, já não há nada no ficheiro

        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            out.writeUTF("1"); // login - só aqui entra se nomeUser!="" logo já fez login
            while((line = bufferedReader.readLine()) != null) { // lê user e passw
                out.writeUTF(line);
                System.out.println("Vou enviar "+line);
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
    
    public static void leFicheiroBackup(DataOutputStream out, String nomeUser) {
    	System.out.println("--LE FICHEIRO BACKUP --");
    	if(nomeUser.compareToIgnoreCase("")!=0) {
	        if (!ficheiroVazio(nomeUser)) {
		    	String fileName = "ficheiros/"+nomeUser+"_backup.txt";
		        String line = null;
		
		        try {
		        	/* lê operações */
		            FileReader fileReader = new FileReader(fileName);
		            BufferedReader bufferedReader = new BufferedReader(fileReader);
		            while((line = bufferedReader.readLine()) != null) {
		            	System.out.println("Vou enviar "+line);
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
    }
    public static void salvaFicheiroBackup(String st) {
    	String filePathOperacao="ficheiros/"+st+"_operacao.txt";
    	String filePathBackup="ficheiros/"+st+"_backup.txt";
    	//apaga backup
    	
    	apagaConteudoFicheiro(st,2); // apaga o conteudo do backup, se ja existir
    	
    	BufferedReader br;
		try {
			
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(filePathBackup, true)));
		    
			br = new BufferedReader(new FileReader(filePathOperacao));
			String linha=br.readLine();
	    	while(linha!=null) { // escreve no ficheiro de Backup o que lê de Operação
	    		System.out.println("Na _operacao estava: "+linha);
	    		writer.println(linha);
	    		linha=br.readLine();
	    	}
	    	br.close();
	    	writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}  
    	
		apagaConteudoFicheiro(st,0); // apaga as Operações
    }
    
    public static void apagaConteudoFicheiro(String st, int tipo) {
    	String filepath="";
    	if (tipo==0)
    		filepath="ficheiros/"+st+"_operacao.txt";
		else if (tipo==1)
			filepath="ficheiros/"+st+"_infologin.txt";
		else if (tipo==2)
			filepath="ficheiros/"+st+"_backup.txt";
    		
		try {
			PrintWriter writer = new PrintWriter(filepath, "UTF-8");
			
		    writer.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
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
