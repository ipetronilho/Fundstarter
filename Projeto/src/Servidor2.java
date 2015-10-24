// TCPServer2.java: Multithreaded server
import java.net.*;
import java.util.ArrayList;
import java.io.*;

public class Servidor2{
   static int serv_id = 2;
   static boolean DEBUG = true, Primario;

   
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
        
     // se o servidor 1 está vivo, este não é primário
        if (serv_id == 2 && cudp.estaVivo() == true) { 
            Primario = false;
        } else { 
            Primario = true;
        }
        
        // parte TCP
        try{
        	listenSocket = new ServerSocket(8000); // serverSockets[serv_id - 1]
            System.out.println("Ligo-me ao " + 8000);
            
            while(true) {
                Socket clientSocket = listenSocket.accept(); // BLOQUEANTE
                System.out.println("CLIENT_SOCKET (created at accept())="+clientSocket);
                numero ++;
                tentaLigacao(cudp);
                new ConexaoTCP(clientSocket, numero, tentaLigacao(cudp), lista);
            }
        }catch(IOException e)
        {System.out.println("Listen:" + e.getMessage());}
    }
	
    private static String tentaLigacao(ConexaoUDP c_udp) {
        String st;
        if (Primario == true) { 
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
