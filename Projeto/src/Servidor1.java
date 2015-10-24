// TCPServer2.java: Multithreaded server
import java.net.*;
import java.util.ArrayList;
import java.io.*;

public class Servidor1{
   static int serv_id = 1;
   static boolean DEBUG = true, Primario;
   static int[] serverSockets = {6000, 7000};

   
	public static void main(String args[]){
        int numero=0;
        ArrayList <DataOutputStream> lista = new ArrayList <DataOutputStream>();
        
        String host = "localhost"; // host
        // PARTE UDP
        ConexaoUDP cudp = new ConexaoUDP(host, serv_id);
        ServerSocket listenSocket=null;
        try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
     
        if (serv_id == 2 && cudp.estaVivo() == true) { 
            Primario = false;
        } else { 
            Primario = true;
        }
        
        // parte TCP
        try{
        	listenSocket = new ServerSocket(6000); // mudar
            System.out.println("Ligo-me ao " + serverSockets[serv_id - 1]);
            
            while(true) {
                Socket clientSocket = listenSocket.accept(); // BLOQUEANTE
                System.out.println("CLIENT_SOCKET (created at accept())="+clientSocket);
                numero ++;
                new ConexaoTCP(clientSocket, numero, tentaLigacao(cudp), lista);
            }
        }  catch(IOException e){
        	System.out.println("Listen:" + e.getMessage());
		}
}
		
    
    private static String tentaLigacao(ConexaoUDP c_udp) { //retorna SIM ou NAO caso seja possível ligar-se ao servidor
        String st;
        if (Primario == true) { //liga-se sempre ao servidor primário
            st = "SIM";
        } 
        else { 
            if (serv_id == 2) {
                Primario= true;
            }
            else if (serv_id == 1) {
                Primario = false;
            }
            st = "NAO";
        }
        return st;
    }
}
