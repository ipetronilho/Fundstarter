/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Inês
 */
public class ConexaoUDP extends Thread {

    static String Host;
    static String Status = "baixo";
    static int id;
    static int serverPort = 6789;
    static long LastTimestamp;
    static boolean DEBUG = true;

    public ConexaoUDP(String host, int id) {
        this.Host = host;
        this.id = id;
        System.out.println("O meu host e " + host + " e o meu id e " + Integer.toString(id));
        this.start();
    }

    public void run() {

        DatagramSocket aSocket = null;
        String texto;
        long timestamp;

        try {
        	// O servidor primário falhou mesmo ou é só uma falha transitória de rede? 
            if (id == 2) {
                aSocket = new DatagramSocket();
                while (true) {

                    texto = "" + System.currentTimeMillis();
                    byte[] m = texto.getBytes();
                    InetAddress aHost = InetAddress.getByName(Host);
                    DatagramPacket request = new DatagramPacket(m, m.length, aHost, serverPort);
                    aSocket.send(request);

                    byte[] buffer = new byte[1000];
                    DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

                    aSocket.receive(reply);
                    texto = new String(reply.getData(), 0, reply.getLength());
                    if (texto.length() > 0 && texto != null) {
                        timestamp = Long.parseLong(texto); // tempo recebido
                        updateEstado(timestamp);
                    } else {
                        setEstado("baixo");
                    }
                    
                }
            } 
            
            // Servidor 1
            else if (id == 1) {

                aSocket = new DatagramSocket(serverPort);

                while (true) {

                    byte[] buffer = new byte[1000];
                    DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

                    // recebe a resposta
                    aSocket.receive(reply);
                    texto = new String(reply.getData(), 0, reply.getLength());
                    
                    // envia a data do sistema
                    if (texto.length() > 0 && texto != null) {
                        timestamp = Long.parseLong(texto);
                        updateEstado(timestamp);
                    } 
                    
                    //não recebe
                    else { 
                        setEstado("baixo");
                    }

                    
                    texto = "" + System.currentTimeMillis();
                    buffer = texto.getBytes();
                    DatagramPacket request = new DatagramPacket(buffer, buffer.length, reply.getAddress(), reply.getPort());
                    aSocket.send(request);

                }
            }

        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (aSocket != null) {
                aSocket.close();
            }
        }

    }

    public boolean estaVivo() {
        if (getEstado().compareToIgnoreCase("vivo") == 0) {
            return true;
        } else {
            return false;
        }
    }

    private void updateEstado(long timestamp) {
        setLastTimestamp(timestamp);
        // está vivo se os pedidos demoram menos de 5s a chegar
        if (System.currentTimeMillis() - timestamp < 5000) { 
            setEstado("vivo"); 
        } else {
        	//demora mais de 5s a chegar ou não chega
            setEstado("baixo"); 
        }
        return;
    }

    public long getLastTimestamp() {
        return LastTimestamp;
    }

    public static void setLastTimestamp(long lastTimestamp) {
        LastTimestamp = lastTimestamp;
    }

    public static int getPort() {
        return serverPort;
    }

    public static void setPort(int UDP_PORT) {
        serverPort = UDP_PORT;
    }

    public static String getHost() {
        return Host;
    }

    public static void setHost(String host) {
        Host = host;
    }

    public String getEstado() {
        return Status;
    }

    public static void setEstado(String status) {
        Status = status;
    }

}
