import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;


//= Thread para tratar de cada canal de comunicação com um cliente
class ConexaoTCP extends Thread {
    DataInputStream in;
    DataOutputStream out;
    Socket clientSocket;
    int thread_number;
    ArrayList <DataOutputStream> lista = new ArrayList <DataOutputStream>();
    String confirma;
    
    /* 
     * TODO:
     * 
     * hashmaps: em vez de ==, posso simplesmente procurar por chave e ele devolve o valor
     * 
     * transaccional: as 2 operações (doar dinheiro e receber recompensa) têm de devolver true.
     * No fim de chamar as duas funções, se recebi false numa delas tenho de desfazer a outra
     * e tentar de novo
     * 
     * experimentar com 2 PCs e desligar e ver que excepção dá e tratá-la
     * 
     * ordem total
     * 
     * */
    
    public ConexaoTCP (Socket aClientSocket, int numero, String ack, ArrayList <DataOutputStream> lista) {
        thread_number = numero;
        try{
        	this.lista = lista;
            clientSocket = aClientSocket;
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
            confirma = ack;
            lista.add(out);
            this.start();
        }catch(IOException e){System.out.println("Connection:" + e.getMessage());}
    }
    
    //=============================
    public void run() {
        String resposta;
     // Cliente RMI que se liga ao Servidor RMI
        InterfaceRMI intRMI;
        int checkLogin=0;
        
        int verif;
        String dados="";
		try {
			dados = in.readUTF();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
        
        try{
            if (dados.compareToIgnoreCase("PEDIDO") == 0) {
                out.writeUTF(confirma); //confirmação conforme o estado do outro servidor
                

		        //Verifica o login e efectua o registo.
		        if (confirma.compareToIgnoreCase("SIM") == 0) {
		            verif = 0;
		            // thread acorda de hora a hora e Verifica a Validade dos projetos
		            
		            while(verif==0){
		            	
		            	// TODO: mudar Naming...
						intRMI = (InterfaceRMI) Naming.lookup("rmi://localhost:7000/benfica");
						int userID=-1; // o ID do user é a posição na arrayList 
						Utilizador user = new Utilizador(); // TODO: apagar
			
				        	
				            while(true){
				                //an echo server
				            	
					            if (checkLogin == 0) {
					            	/* ---- MENU ---- */
					                out.writeUTF("Bem vindo! Seleccione uma opcao! 1-Login; 2-Registar; 3-Consultar dados");
					                
					                
					                int i=0;
					                String data = in.readUTF();
					                int opcao=Integer.parseInt(data);
					                
					                
					                /* LOGIN */
					                if (opcao==1) {
					                	out.writeUTF("--LOGIN--");
					                	out.writeUTF("Insira nome de Utilizador");
					                	String nomeUser = in.readUTF();
					                	out.writeUTF("Insira password:");
					                	String password = in.readUTF();
					                	
					                	// alterar
					                	userID = intRMI.verificaLogin(nomeUser, password);
					                	//out.writeUTF("O meu id e "+userID);
					                	if (userID != -1) {
					                		checkLogin=1;
					                		out.writeUTF("Login efectuado com sucesso\n");
					                	}
					                	else
					                		out.writeUTF("Login invalido.");
					                	
					                }
					                
					                /* REGISTAR */
					                else if (opcao==2) {
					                	out.writeUTF("--REGISTO--");
					                	
					                	out.writeUTF("Insira nome de Utilizador");
					                	String nomeUser = in.readUTF();
					                	out.writeUTF("Insira password:");
					                	String password = in.readUTF();
					                	
					                	userID = intRMI.registaConta(nomeUser, password);
					                	checkLogin=1;
					                }
					                
					                /* CONSULTAR DADOS */
					                else if (opcao==3) {
					                	out.writeUTF("Consultar dados...\n1-Listar Projetos Actuais\n2-Listar Projetos Antigos\n3-Consultar Detalhes de um projeto\n0-Sair");
					                	data = in.readUTF();
					                	opcao=Integer.parseInt(data);
					                	if (opcao==1) {
					                		out.writeUTF("--PROJETOS ACTUAIS--");
					                		resposta = intRMI.listaProjetosActuais();
					                		out.writeUTF(resposta);
					                	}
					                	else if (opcao==2) {
					                		out.writeUTF("--PROJETOS ANTIGOS--");
					                		resposta =intRMI.listaProjetosAntigos();
					                		out.writeUTF(resposta);
					                	}
					                	// TODO: opcao=3
					                	else if (opcao==3) {
					                		out.writeUTF("Nome do projeto:");
					                		String nomeProjeto = in.readUTF();
					                		int projID = intRMI.procuraProjeto(nomeProjeto);
					                		resposta = intRMI.imprimeDetalhesProjeto(projID);
					                		out.writeUTF(resposta);
					                	}
					                }
					            }
					                
					            else if (checkLogin==1) {
					            	out.writeUTF("O que deseja fazer?");
					            	out.writeUTF("---CONSULTA---");
					            	out.writeUTF("1-Consultar saldo");
					            	out.writeUTF("2-Consultar recompensas");
					            	out.writeUTF("3-Consultar os seus Projetos"); // admin
					            	out.writeUTF("4-Consultar todos os projetos do sistema");
					            	out.writeUTF("5-Consultar doacoes de Projetos");
					            	
					            	out.writeUTF("---PROJETOS---");
					            	out.writeUTF("6-Criar um projeto");
					            	out.writeUTF("7-Cancelar projeto");
					            	out.writeUTF("8-Doar dinheiro a um projeto");
					            	out.writeUTF("9-Adicionar recompensas a um projeto");
					            	out.writeUTF("10-Remover recompensas a um projeto");
					            	out.writeUTF("11-Consultar inbox de um projeto"); // TODO
					            	out.writeUTF("0-logout");
					            	
					            	String data = in.readUTF();
					            	int opcao=Integer.parseInt(data);
					            	
					            	if (opcao == 0) {
					            		out.writeUTF("A sair...");
					            		checkLogin=0;
					            	}
					            	
					            	else if (opcao == 1) {
					            		resposta = intRMI.consultarSaldo(userID);
					            		out.writeUTF(resposta);
					            	}
					            	
					            	else if (opcao == 2) {
					            		resposta = intRMI.consultarRecompensas(userID);
					            		out.writeUTF(resposta);
					            	}
					            	
					            	else if (opcao == 3) {
					            		resposta = intRMI.consultarProjetos(userID); // imprime
					            		out.writeUTF(resposta);
					            	}
					            	
					            	else if (opcao==4) {
					                	out.writeUTF("Consultar dados...\n1-Listar Projetos Actuais\n2-Listar Projetos Antigos\n3-Consultar Detalhes de um projeto\n0-Sair");
					                	data = in.readUTF();
					                	opcao=Integer.parseInt(data);
					                	if (opcao==1) {
					                		out.writeUTF("--PROJETOS ACTUAIS--");
					                		resposta = intRMI.listaProjetosActuais();
					                		out.writeUTF(resposta);
					                	}
					                	else if (opcao==2) {
					                		out.writeUTF("--PROJETOS ANTIGOS--");
					                		resposta =intRMI.listaProjetosAntigos();
					                		out.writeUTF(resposta);
					                	}
					                	// TODO
					                	else if (opcao==3) {
					                		out.writeUTF("Nome do projeto:");
					                		String nomeProjeto = in.readUTF();
					                		int projID = intRMI.procuraProjeto(nomeProjeto);
					                		resposta = intRMI.imprimeDetalhesProjeto(projID);
					                		out.writeUTF(resposta);
					                	}
					                }
					            	
					            		
					            	else if (opcao == 5) {
					            		resposta = intRMI.imprimeDoacoesUser(userID);
					            		out.writeUTF(resposta);
					            	}
					            	
					            	
					            	else if (opcao == 6) { // criar projeto - falta descricao
					            		
					            		int id=-1;
					            		String nome;
					            		do {
					            			out.writeUTF("Nome do projeto:");
					            			nome = in.readUTF();
						            		id = intRMI.procuraProjeto(nome);
						            		//out.writeUTF("O meu id e "+id);
						            		if (id!=-1)
						            			out.writeUTF("Ja existe um projeto com esse nome!");
					            		}while(id!=-1);
					            		
					            		try {
						            		// TODO: proteção - strings
						            		out.writeUTF("Valor objetivo:");
						            		float valor_objetivo = Float.parseFloat(in.readUTF());
						            		/*
						            		 * out.writeUTF("Data inicial do projeto:");
						            		 * Calendar user = new GregorianCalendar(2012, Calendar.MAY, 17);
						            		 */
						            		Calendar dataInicial = new GregorianCalendar();
						            		
						            		out.writeUTF("Data final do projeto:");
						            		out.writeUTF("Dia: ");
						            		int dia = Integer.parseInt(in.readUTF());
						            		out.writeUTF("Mês: ");
						            		int mes = Integer.parseInt(in.readUTF());
						            		out.writeUTF("Ano: ");
						            		int ano = Integer.parseInt(in.readUTF());
						            		Calendar dataFinal = new GregorianCalendar();
						            		dataFinal.set(Calendar.YEAR, ano);
						            		dataFinal.set(Calendar.MONTH, mes);
						            		dataFinal.set(Calendar.DAY_OF_MONTH, dia);
						            		// Calendar user = new GregorianCalendar(2012, Calendar.MAY, 17);
						            		
						            		intRMI.criaProjeto(userID, nome, valor_objetivo, dataInicial, dataFinal);
						            		
						            		
					            		}catch(NumberFormatException e) {
					            			out.writeUTF("Numero invalido.");
					            		}
					            		
					            		
					            	}
					            	
					            	else if (opcao == 7) { /* eliminar projeto */
					             		
					            		int projID=-1;
					            		String nome;
					            		do {
					            			out.writeUTF("Nome do projeto:");
					            			nome = in.readUTF();
						            		projID = intRMI.procuraProjeto(nome);
						            		
						            		if (projID==-1)
						            			out.writeUTF("Nao existe um projeto com esse nome!");
					            		}while(projID==-1);
					            		
					            		intRMI.eliminaProjeto(userID, projID);
					            		
					            		
					            	}
					            	
					            	else if (opcao == 8) {
					            		Projeto proj;
					            		float dinheiro;
					            		int projID=-1;
					            		
					            		// procura o projeto
					            		do {
					            			out.writeUTF("1-Listar os projetos existentes;0-Não listar");
					            			data = in.readUTF();
					            			opcao=Integer.parseInt(data);
					            			if (opcao==1) {
					            				resposta = intRMI.listaProjetosActuais();
						                		out.writeUTF(resposta);
					            			}
						            		out.writeUTF("Insira o nome do Projeto a doar:");
						            		String nome = in.readUTF();
						            		projID = intRMI.procuraProjeto(nome);
						            		if (projID== -1)
						            			out.writeUTF("Nao existe um projeto com esse nome.");
					            		}while(projID==-1);
						            	
					            		// pede a quantia
					            		do {
					            			out.writeUTF("Quantia a doar?\n");
					            			dinheiro = Float.parseFloat(in.readUTF());
					            		}while(dinheiro<=0);
					            		
					            		
					            		intRMI.doarDinheiro(userID, projID, dinheiro);
					            		
					            		// pede a recompensa
					            		String listaRecompensas=""; 
					            		listaRecompensas = intRMI.listaRecompensas(projID); 
					            		
					            		if (listaRecompensas.compareToIgnoreCase("")==0)
					            			out.writeUTF("Projeto nao tem recompensas.");
					            		else {
					            			out.writeUTF("Escolher uma recompensa.\n");
					            			out.writeUTF(listaRecompensas);
					            			int indexRecompensa = Integer.parseInt(in.readUTF());
					            		
			
						            		boolean sucesso = intRMI.escolherRecompensa(userID, projID, dinheiro, indexRecompensa);
						            		if (sucesso) {
						            			out.writeUTF("Recompensa adicionada.\n");
						            			out.writeUTF("Votar?");
						            			
						            		}
						            		else
						            			out.writeUTF("Recompensa falhou.\n");
						            		// TODO: acrescentar aqui o caso de ele querer 2+ recompensas 
					            		}
					            	}
					            	
					            	else if (opcao == 9) {
					            		int checkAdmin=0;
					            		int aux=0;
					            		int projID;
					            		// procura o projeto
					            		do {
						            		out.writeUTF("Insira o nome do Projeto:");
						            		String nomeProjeto = in.readUTF();
						            		projID = intRMI.procuraProjeto(nomeProjeto);
						            		if (projID == -1)
						            			out.writeUTF("Nao existe um projeto com esse nome.");
						            		checkAdmin = intRMI.verificaAdministrador(userID, projID);
						            		if (checkAdmin == 0) {
						            			out.writeUTF("Nao é administrador desse projeto!");
						            			out.writeUTF("0-Sair\n1-Tentar de novo");
						            			aux=Integer.parseInt(in.readUTF());
						            		}
						            		else if (checkAdmin==1) {
							            		out.writeUTF("Nome da recompensa:");
							            		String nome=in.readUTF();
							            		out.writeUTF("Valor da recompensa:");
							            		float valor = Float.parseFloat(in.readUTF());
							            		intRMI.addRecompensa(userID, projID, nome, valor);
						            		}
					            		}while(projID==-1 || checkAdmin==0 || aux==1);
					            		
					            		
					            	}
					            	
					            	else if (opcao == 10) {
					            		int checkAdmin=0;
					            		int aux=0;
					            		int projID;
					            		// procura o projeto
					            		do {
						            		out.writeUTF("Insira o nome do Projeto:");
						            		String nomeProjeto = in.readUTF();
						            		projID = intRMI.procuraProjeto(nomeProjeto);
						            		if (projID == -1)
						            			out.writeUTF("Nao existe um projeto com esse nome.");
						            		checkAdmin = intRMI.verificaAdministrador(userID, projID);
						            		if (checkAdmin == 0) {
						            			out.writeUTF("Nao é administrador desse projeto!");
						            			out.writeUTF("0-Sair\n1-Tentar de novo");
						            			aux=Integer.parseInt(in.readUTF());
						            		}
						            		else if (checkAdmin==1) {
							            		out.writeUTF("Nome da recompensa:");
							            		String nome=in.readUTF();
							            		intRMI.removeRecompensa(userID, projID, nome);
						            		}
					            		}while(projID==-1 || checkAdmin==0 || aux==1);
					            		
					            		
					            	}
					            	
					            	
					            }
				                
				            }
				        }
		        }
            }
        }catch(EOFException e){
        	System.out.println("EOF:" + e);
	    }catch(IOException e){
	    	System.out.println("IO:" + e);
		} catch (NotBoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NumberFormatException e2) {
			System.out.println("Formato invalido! " + e2);
		}
    }
}
