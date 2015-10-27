import java.io.DataInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
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
     * ficheiro
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
    
    public void apagaConteudoFicheiro(String st, int tipo) {
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
    

	public boolean ficheiroVazio(String st){
		BufferedReader br; 
		try {
			br= new BufferedReader(new FileReader("ficheiros/"+st+"_operacao.txt"));  
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
    // TODO: ficheiro com login vazio nao estava a funcionar, tive de aldrabar e
	//meter "primeiro e passe1
	public boolean ficheiroLoginVazio(String st){
		BufferedReader br; 
		try {
			br= new BufferedReader(new FileReader("ficheiros/"+st+"_infologin.txt"));  
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
    
	public void guardaFicheiroLogin(String st, String username) {
    	String filepath;
    	
    	filepath="ficheiros/"+username+"_infologin.txt";
    	
		try {
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(filepath, true)));
		    writer.println(st);
		    writer.close();
			
		} catch (FileNotFoundException e) {
			System.out.println("Ficheiro nao encontrado");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }

    public void guardaFicheiro(String st, String username) {
    	String filepath;
		filepath="ficheiros/"+username+"_operacao.txt";
    	
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
    
    
    //=============================
    public void run() {
        String resposta;
     // Cliente RMI que se liga ao Servidor RMI
        InterfaceRMI intRMI;
        int checkLogin=0;
        String nomeUser="";
        int guardaDados=0;
        int guardaDadosLogin=0;
        
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
                out.writeUTF(confirma); //SIM ou NAO, caso aceite ou nao pedidos
                

		        //Verifica o login e efectua o registo.
		        if (confirma.compareToIgnoreCase("SIM") == 0) {
		            verif = 0;
		            // thread acorda de hora a hora e Verifica a Validade dos projetos
		            
		            while(verif==0){
		            	
		            	// TODO: mudar Naming...
						intRMI = (InterfaceRMI) Naming.lookup("rmi://localhost:7000/benfica");
						int userID=-1; 				// o ID do user é a posição na arrayList 
						
				            while(true){
				                //an echo server
					            if (checkLogin == 0) {
					            	/* ---- MENU ---- */
					            	// TODO: sair a 0 que mete todos os ficheiros em branco
					                out.writeUTF("Bem vindo! Seleccione uma opcao! 1-Login; 2-Registar; 3-Consultar dados");
					                
					                String data = in.readUTF();
					                int opcao=Integer.parseInt(data);
					                //System.out.println("Recebi "+data);
					                
				                	
					                
					                
					                /* LOGIN */
					                if (opcao==1) {
					                	out.writeUTF("--LOGIN--");
					                	out.writeUTF("Insira nome de Utilizador");
					                	nomeUser = in.readUTF();
					                	//--
					                	out.writeUTF("USER");
					                	out.writeUTF(nomeUser);
					                	//--
					                	// só guarda se for a 1ª vez que está a fazer login
					                	if (ficheiroLoginVazio(nomeUser)) {
					                		System.out.println("Vou ter de guardar os dados");
					                		guardaDados=1;
					                	}
					                	if(guardaDados==1)
					                		guardaFicheiroLogin(nomeUser, nomeUser);
					                	
					                	out.writeUTF("Insira password:");
					                	String password = in.readUTF();
					                	if(guardaDados==1)
					                		guardaFicheiroLogin(password,nomeUser);
					                	
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
					                	nomeUser = in.readUTF();
					                	guardaFicheiroLogin(nomeUser,nomeUser);
					                	out.writeUTF("Insira password:");
					                	String password = in.readUTF();
					                	guardaFicheiroLogin(password,nomeUser);
					                	
					                	userID = intRMI.registaConta(nomeUser, password);
					                	checkLogin=1;
					                }
					                
					                /* CONSULTAR DADOS */
					                else if (opcao==3) {
					                	guardaFicheiro(data,nomeUser);
					                	out.writeUTF("Consultar dados...\n1-Listar Projetos Actuais\n2-Listar Projetos Antigos\n3-Consultar Detalhes de um projeto\n0-Sair");
					                	data = in.readUTF();
					                	opcao=Integer.parseInt(data);
					                	guardaFicheiro(data,nomeUser);
					                	
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
					                		guardaFicheiro(nomeProjeto,nomeUser);
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
					            	out.writeUTF("A opcao que escolhi e "+opcao);
					            	//if (ficheiroVazio(nomeUser))
					            		guardaFicheiro(data,nomeUser);
					            	
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
					                	guardaFicheiro(data,nomeUser);
					                	
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
					                		guardaFicheiro(nomeProjeto,nomeUser);
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
					            		// escrevo num ficheiro
					            		int id=-1;
					            		String nome;
					            		do {
					            			out.writeUTF("Nome do projeto:");
					            			nome = in.readUTF();
					            			guardaFicheiro(nome,nomeUser);
						            		id = intRMI.procuraProjeto(nome);
						            		//out.writeUTF("O meu id e "+id);
						            		if (id!=-1)
						            			out.writeUTF("Ja existe um projeto com esse nome!");
					            		}while(id!=-1);
					            		
					            		try {
						            		// TODO: proteção - strings
						            		out.writeUTF("Valor objetivo:");
						            		String valor=in.readUTF();
						            		guardaFicheiro(valor,nomeUser);
						            		float valor_objetivo = Float.parseFloat(valor);
						            		/*
						            		 * out.writeUTF("Data inicial do projeto:");
						            		 * Calendar user = new GregorianCalendar(2012, Calendar.MAY, 17);
						            		 */
						            		Calendar dataInicial = new GregorianCalendar();
						            		
						            		out.writeUTF("Data final do projeto:");
						            		out.writeUTF("Dia: ");
						            		String diaLido=in.readUTF();
						            		guardaFicheiro(diaLido,nomeUser);
						            		int dia = Integer.parseInt(diaLido);
						            		
						            		out.writeUTF("Mês: ");
						            		String mesLido=in.readUTF();
						            		int mes = Integer.parseInt(mesLido);
						            		guardaFicheiro(mesLido,nomeUser);
						            		
						            		out.writeUTF("Ano: ");
						            		String anoLido=in.readUTF();
						            		int ano = Integer.parseInt(anoLido);
						            		guardaFicheiro(anoLido,nomeUser);
						            		
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
					             		// escrevo num ficheiro
					            		int projID=-1;
					            		String nome;
					            		do {
					            			out.writeUTF("Nome do projeto:");
					            			nome = in.readUTF();
					            			guardaFicheiro(nome,nomeUser);
						            		projID = intRMI.procuraProjeto(nome);
						            		
						            		if (projID==-1)
						            			out.writeUTF("Nao existe um projeto com esse nome!");
					            		}while(projID==-1);
					            		
					            		intRMI.eliminaProjeto(userID, projID);
					            		
					            		
					            	}
					            	
					            	else if (opcao == 8) {
					            		// escrevo num ficheiro
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
						            		guardaFicheiro(nome,nomeUser);
						            		projID = intRMI.procuraProjeto(nome);
						            		System.out.println("Recebi o id "+projID);
						            		if (projID== -1)
						            			out.writeUTF("Nao existe um projeto com esse nome.");
					            		}while(projID==-1);
						            	
					            		// pede a quantia
					            		do {
					            			out.writeUTF("Quantia a doar?\n");
					            			String quantia=in.readUTF();
					            			guardaFicheiro(quantia,nomeUser);
					            			dinheiro = Float.parseFloat(quantia);
					            		}while(dinheiro<=0);
					            		
					            		if (intRMI.validaDoacao(userID, dinheiro)) {
					            		
						            		intRMI.doarDinheiro(userID, projID, dinheiro);
						            		
						            		// pede a recompensa
						            		String listaRecompensas=""; 
						            		listaRecompensas = intRMI.listaRecompensas(projID); 
						            		
						            		if (listaRecompensas.compareToIgnoreCase("")==0)
						            			out.writeUTF("Projeto nao tem recompensas.");
						            		else {
						            			out.writeUTF("Escolher uma recompensa.\n");
						            			out.writeUTF(listaRecompensas);
						            			resposta=in.readUTF();
						            			guardaFicheiro(resposta,nomeUser);
						            			int indexRecompensa = Integer.parseInt(resposta);
						            			
						            		
				
							            		boolean sucesso = intRMI.escolherRecompensa(userID, projID, dinheiro, indexRecompensa);
							            		if (sucesso) {
							            			out.writeUTF("Recompensa adicionada.");
							            			out.writeUTF("Votar?");
							            			
							            		}
							            		else
							            			out.writeUTF("Recompensa falhou.\n");
							            		// TODO: acrescentar aqui o caso de ele querer 2+ recompensas 
						            		}
					            		}
					            		else
					            			out.writeUTF("Saldo insuficiente.");
					            	}
					            	
					            	else if (opcao == 9) {
					            		// escrevo num ficheiro
					            		int checkAdmin=0;
					            		int aux=0;
					            		int projID;
					            		// procura o projeto
					            		do {
						            		out.writeUTF("Insira o nome do Projeto:");
						            		String nomeProjeto = in.readUTF();
						            		guardaFicheiro(nomeProjeto,nomeUser);
						            		projID = intRMI.procuraProjeto(nomeProjeto);
						            		if (projID == -1)
						            			out.writeUTF("Nao existe um projeto com esse nome.");
						            		checkAdmin = intRMI.verificaAdministrador(userID, projID);
						            		if (checkAdmin == 0) {
						            			out.writeUTF("Nao é administrador desse projeto!");
						            			out.writeUTF("0-Sair\n1-Tentar de novo");
						            			resposta=in.readUTF();
						            			aux=Integer.parseInt(resposta);
						            			guardaFicheiro(resposta,nomeUser);
						            		}
						            		else if (checkAdmin==1) {
							            		out.writeUTF("Nome da recompensa:");
							            		String nome=in.readUTF();
							            		guardaFicheiro(nome,nomeUser);
							            		out.writeUTF("Valor da recompensa:");
							            		resposta=in.readUTF();
							            		guardaFicheiro(resposta,nomeUser);
							            		float valor = Float.parseFloat(resposta);
							            		intRMI.addRecompensa(userID, projID, nome, valor);
						            		}
					            		}while(projID==-1 || checkAdmin==0 || aux==1);
					            		
					            		
					            	}
					            	
					            	else if (opcao == 10) {
					            		// escrevo num ficheiro
					            		int checkAdmin=0;
					            		int aux=0;
					            		int projID;
					            		// procura o projeto
					            		do {
						            		out.writeUTF("Insira o nome do Projeto:");
						            		String nomeProjeto = in.readUTF();
						            		guardaFicheiro(nomeProjeto,nomeUser);
						            		projID = intRMI.procuraProjeto(nomeProjeto);
						            		if (projID == -1)
						            			out.writeUTF("Nao existe um projeto com esse nome.");
						            		checkAdmin = intRMI.verificaAdministrador(userID, projID);
						            		if (checkAdmin == 0) {
						            			out.writeUTF("Nao é administrador desse projeto!");
						            			out.writeUTF("0-Sair\n1-Tentar de novo");
						            			resposta=in.readUTF();
						            			aux=Integer.parseInt(resposta);
						            			guardaFicheiro(resposta,nomeUser);
						            		}
						            		else if (checkAdmin==1) {
							            		out.writeUTF("Nome da recompensa:");
							            		String nome=in.readUTF();
							            		guardaFicheiro(nome,nomeUser);
							            		intRMI.removeRecompensa(userID, projID, nome);
						            		}
					            		}while(projID==-1 || checkAdmin==0 || aux==1);
					            		
					            		
					            	}
					            	// TODO: não perder mensagens
					            	else if (opcao==11) {
					            		out.writeUTF("Nome do projeto:");
					            		String projNome = in.readUTF();
					            		int projID = intRMI.procuraProjeto(projNome);
					            		int checkAdmin = intRMI.verificaAdministrador(userID, projID);
					            		do {
						            		out.writeUTF("1-Deixar mensagem");
						            		out.writeUTF("2-Consultar mensagens");
						            		out.writeUTF("3-Responder a mensagem [Apenas admins]");
						            		out.writeUTF("0-Sair");
						            		data=in.readUTF();
						            		opcao=Integer.parseInt(data);
						            		
						            		if (opcao==1) {
						            			out.writeUTF("Mensagem:");
						            			String mensagem = in.readUTF();
						            			guardaFicheiro(mensagem,nomeUser);
						            			
						            			intRMI.adicionaMensagem(userID, projID, mensagem);
						            		}
						            		// TODO: mostrar apenas aquelas que são mandadas pelo user?
						            		else if (opcao==2) {
					            				resposta = intRMI.consultaMensagens(projID);
					            				guardaFicheiro(resposta,nomeUser);
					            				out.writeUTF(resposta);
						            		}
						            		else if (opcao==3) {
						            			if (checkAdmin==1) {
						            				out.writeUTF("Responder a que utilizador? (inserir ID)");
						            				resposta=in.readUTF();
						            				guardaFicheiro(resposta,nomeUser);
						            				int id=Integer.parseInt(resposta);
						            				out.writeUTF("Resposta: ");
						            				String mensagem=in.readUTF();
						            				
						            				intRMI.respondeMensagens(userID, id, projID, mensagem);
						            				
						            			}
						            		}
					            		}while(opcao!=0);
					            	}
					            	apagaConteudoFicheiro(nomeUser, 0);
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
        finally {
        	
        }
    }
}
