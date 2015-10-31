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
    //static String serverAddress = "169.254.36.100";
    // tem de ter 2 ips e dois portos distintos
    static String serverAddress = "localhost";
    static String nomeUser="";
    //static int[] availablesockets = {6000, 8000};
    static int[] serversockets = {6000, 8000};
    //static String serverAddress = "169.254.143.125";
    
    static String filename_login="";
    static String filename_backup="";
    static String filename_operacao="";
    
    public static void main(String args[]) throws InterruptedException {
        // args[0] <- hostname of destination
        if (args.length == 0) {
	         System.out.println("java TCPClient hostname");
	         System.exit(0);
         }
        
        carregaPortosFicheiro(); // carrega os portos que correspondem a cada ficheiro
        int i, j, fail_counter = 0;
        String msg = "";
        
        while (true) { // alterna constantemente entre servidores para se ligar
        	
            for (i = 0; i < 2; i++) {

                if (DEBUG == true) {
                    System.out.println("Nova ligacao ao servidor: " + (i + 1));
                }
                
                
                msg = conexaoServidor(serverAddress, serversockets[i], i);
             
                
                if (msg.compareToIgnoreCase("TROCA") != 0 && msg.length() > 0) {
                    for (j = 0; j < tentativas; j++) { // tenta 3 vezes
                    	
                        if (msg.compareToIgnoreCase("TROCA") != 0 && msg.length() > 0) {
                            Thread.sleep(1000); // espera 1 segundo e volta a tentar
                            msg = conexaoServidor(serverAddress, serversockets[i], i);
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
    public static String conexaoServidor(String serverAddress, int serversocket, int i) { 
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
            
            // ligou-se com sucesso
            guardaPortosFicheiro(i);
            DataInputStream in = new DataInputStream(s.getInputStream());
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            System.out.println("Cheguei-c");
            out.writeUTF("PEDIDO");
            System.out.println("Cheguei-c2");
            s.setSoTimeout(TIMEOUT);	// tempo limitado
            data = in.readUTF();

            if (data.compareToIgnoreCase("SIM") == 0) { //pedido aceite
            	
                str = "";
                
                // lê o id da sessão
                System.out.println("Aqui...");
                String id_sessao = in.readUTF();
                //int id_sessao=Integer.parseInt(data);
                System.out.println("Ca estou! o meu id e "+id_sessao);
                
                filename_login="ficheiros/"+id_sessao+"_infologin.txt";
	            filename_backup="ficheiros/"+id_sessao+"_backup.txt";
	            filename_operacao="ficheiros/"+id_sessao+"_operacao.txt";
                
                
                Receiver MyThread = new Receiver(in); // lê de teclado
                MyThread.start();
                
                String texto = "";
                InputStreamReader input = new InputStreamReader(System.in);
                BufferedReader reader = new BufferedReader(input);
                
                
                File f = new File(filename_login);
        		if(f.exists() && !f.isDirectory()) {
	                if (!ficheiroVazio(filename_login)) {
	                	MyThread.setImprime(0);
	                	leFicheiroLogin(out);
	                }
        		}
                
        		File f2 = new File(filename_backup);
        		if(f2.exists() && !f2.isDirectory()) {
        	
	                //leFicheiroBackup(out, nomeUser, MyThread);
	                if (!ficheiroVazio(filename_backup)) {
	                	MyThread.setImprime(0);
	                	leFicheiroBackup(out, MyThread);
	                }
        		}
                
                System.out.println("Cheguei aquiiiii");
                
                while (true) {
                    try {
                        texto = reader.readLine(); // lê de teclado
                    } catch (Exception e) {
                    }
                    try {
                        out.writeUTF(texto); // escreve
                    } catch (SocketException e) {
                    
                    	
                        System.out.println("A conectar-se a outro servidor...");
                        
                        salvaFicheiroBackup();
                        guardaFicheiro(texto);
                        
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
    
    // guarda os portos. recebe o index do porto primário
    public static void guardaPortosFicheiro(int i) {
    	PrintWriter writer;
		try {
			writer = new PrintWriter("ficheiros/portos.txt", "UTF-8");
			
			if (i==1) {
				System.out.println("Guardo "+serversockets[i]+" como primário");
				writer.println(serversockets[i]);	//8000
				writer.println(serversockets[i-1]);	//6000
			}
			else {
				System.out.println("Guardo "+serversockets[i]+" como primário");
				writer.println(serversockets[i]);	//6000
				writer.println(serversockets[i+1]);	//8000
			}
		    writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
    }
    
    // carrega os portos
    public static void carregaPortosFicheiro() {
    	String fileName = "ficheiros/portos.txt";

        try {
        	/* lê operações */
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            
            String line = bufferedReader.readLine();
            serversockets[0]= Integer.parseInt(line);
            System.out.println("Li "+serversockets[0]);
            
            line = bufferedReader.readLine();
            serversockets[1]= Integer.parseInt(line);
            System.out.println("Li "+serversockets[1]);
            
            bufferedReader.close();
                
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + fileName + "'");                
        }
        catch(IOException ex) {
            System.out.println("Error reading file '" + fileName + "'");                  

        }
    }
    
    public static void cleanupFicheiros(String st) {
    	apagaConteudoFicheiro(filename_operacao);
    	apagaConteudoFicheiro(filename_login);
    	apagaConteudoFicheiro(filename_backup);
    }



    
    
    public static void guardaFicheiro(String st) {
    	System.out.println("Acrescento "+st);
		try {
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(filename_backup, true)));
		    writer.println(st);
		    writer.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    }
    
    
	public static boolean ficheiroVazio(String filename){
	
		BufferedReader br; 
		try {
			br= new BufferedReader(new FileReader(filename));  
			if (br.readLine() == null) {
				br.close();
			    return true;
			}
			br.close();
			
		} catch (FileNotFoundException e1) {
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
    }
    
	
    /* lê login */
    public static void leFicheiroLogin(DataOutputStream out) {
    	
        String line = null;
        System.out.println("--LE FICHEIRO LOGIN --");
        System.out.println("User e "+nomeUser);

        try {
            FileReader fileReader = new FileReader(filename_login);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            out.writeUTF("1"); // "1": opção de login no menu do servidor
            while((line = bufferedReader.readLine()) != null) { // lê user e passw
                out.writeUTF(line);
                System.out.println("Vou enviar "+line);
            }
            bufferedReader.close();
                
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + filename_login + "'");                
        }
        catch(IOException ex) {
            System.out.println("Error reading file '" + filename_login + "'");                  

        }
    	
    }
    
    public static void leFicheiroBackup(DataOutputStream out, Receiver MyThread) {
    	System.out.println("--LE FICHEIRO BACKUP --");
    	
		        String line = null;
		        
		        try {
		        	/* lê operações */
		            FileReader fileReader = new FileReader(filename_backup);
		            BufferedReader bufferedReader = new BufferedReader(fileReader);
		            while((line = bufferedReader.readLine()) != null) {
		            	//System.out.println("Vou enviar "+line);
		            	
		                out.writeUTF(line);
		            }
		            
		            bufferedReader.close();
		                
		        }
		        catch(FileNotFoundException ex) {
		            System.out.println("Unable to open file '" + filename_backup + "'");                
		        }
		        catch(IOException ex) {
		            System.out.println("Error reading file '" + filename_backup + "'");                  
		
		        }
    }
    
    public static int countLines(String filename) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(filename));
        try {
            byte[] c = new byte[1024];
            int count = 0;
            int readChars = 0;
            boolean empty = true;
            while ((readChars = is.read(c)) != -1) {
                empty = false;
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
            }
            return (count == 0 && !empty) ? 1 : count;
        } finally {
            is.close();
        }
    }
    
    public static void salvaFicheiroBackup() {

    	//apaga backup
    	
    	File f = new File(filename_login);
		if(!f.exists() && !f.isDirectory()) {
			criaFicheiros(filename_backup);
		}
    	
    	
    	apagaConteudoFicheiro(filename_backup); // apaga o conteudo do backup, se ja existir
    	
    	BufferedReader br;
		try {
			
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(filename_backup, true)));
		    
			br = new BufferedReader(new FileReader(filename_operacao));
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
    	
		apagaConteudoFicheiro(filename_operacao); // apaga as Operações
    }
    
    public static void criaFicheiros(String filepath) {
    	File file = new File(filepath);
	      
	      try {
			if (file.createNewFile()){
			    System.out.println("File is created!");
			  }else{
			    System.out.println("File already exists.");
			  }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static void apagaConteudoFicheiro(String filepath) {

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
    int imprime=1;

    public Receiver(DataInputStream ain) {
        this.in = ain;
    }
    
    
    public void setImprime(int imprime) {
    	this.imprime=imprime;
    }

    //=============================
    public void run() {
        while (true) {
            // READ FROM SOCKET
            try {
                String data = in.readUTF(); // lê o que foi escrito

                if (data.compareToIgnoreCase("IMPRIME")==0) {
                	setImprime(1);
                }
                
                else if (imprime==1)
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
