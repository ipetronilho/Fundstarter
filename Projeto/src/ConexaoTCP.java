import java.io.DataInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.rmi.ConnectException;
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
    int counter;
    int number_lines=-1;
    int id_sessao;
    
    String filename_login="";
    String filename_operacao="";
    String filename_backup="";
    
    /* 
     * TODO:
     * 
     * ficheiro
     * 
     * hashmaps: em vez de ==, posso simplesmente procurar por chave e ele devolve o valor
     * 
     * experimentar com 2 PCs e desligar e ver que excepção dá e tratá-la
     * 
     * ordem total
     * 
     * */
    
    public ConexaoTCP (Socket aClientSocket, int numero, String ack, ArrayList <DataOutputStream> lista) {
  
        try{
        	this.lista = lista;
            clientSocket = aClientSocket;
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
            this.id_sessao=numero;
            confirma = ack;
            lista.add(out);
            this.start();
        }catch(IOException e){System.out.println("Connection:" + e.getMessage());}
    }
    
    
    //=============================
    public void run() {
        String resposta;
        InterfaceRMI intRMI;
        int checkLogin=0;
        String nomeUser="";
        int guardaDados=0;
        int tentativas=0, total_tentativas=100;
        
        String dados="";
        String s_id_sessao="";
       

	        	
	        		
			try {
				dados=in.readUTF();
			} catch (IOException e3) {
				// TODO Auto-generated catch block
				e3.printStackTrace();
			}
					
	        	
	        	System.out.println("antes do Pedido");
	            if (dados.compareToIgnoreCase("PEDIDO") == 0) {
	                try {
						out.writeUTF(confirma);
					} catch (IOException e3) {
						// TODO Auto-generated catch block
						e3.printStackTrace();
					} //SIM ou NAO, caso aceite ou nao pedidos
	                
	                
			        //Verifica o login e efectua o registo.
			        if (confirma.compareToIgnoreCase("SIM") == 0) {
			        	System.out.println("Cheguei");
			        	// envia o id da sessão ao cliente
			        	//String s_id_sessao=Integer.toString(this.id_sessao);
			            //out.writeUTF(s_id_sessao);
			        	
			        	try {
							out.writeUTF("SESSAO");
							s_id_sessao = in.readUTF();
						} catch (IOException e3) {
							// TODO Auto-generated catch block
							e3.printStackTrace();
						}
			        	
					
						id_sessao=Integer.parseInt(s_id_sessao);
						System.out.println("A minha sessao e "+s_id_sessao);
			            
			            filename_login = "ficheiros/"+id_sessao+"_infologin.txt";
			            filename_operacao = "ficheiros/"+id_sessao+"_operacao.txt";
			            filename_backup = "ficheiros/"+id_sessao+"_backup.txt";
			            
			            // cria os ficheiros
			            
			            
			            criaFicheiros(filename_operacao);
			            criaFicheiros(filename_backup);
			            try {
							number_lines = countLines(filename_backup);
						} catch (IOException e3) {
							// TODO Auto-generated catch block
							e3.printStackTrace();
						}
			            
			        
			            
			            
	            while(tentativas<total_tentativas){
	            	
	    	        try{
	    	        	System.out.println("Tentativa n"+tentativas);
	    	        	
			            
							intRMI = (InterfaceRMI) Naming.lookup("rmi://localhost:7000/benfica");
							//intRMI = (InterfaceRMI) LocateRegistry.getRegistry(7000).lookup("inte");
							
							if (tentativas>0) {
								apagaFicheiros(filename_login);
			            		apagaFicheiros(filename_operacao);
			            		apagaFicheiros(filename_backup);
			            		checkLogin=0;
							}

							// thread acorda de hora a hora e verifica a validade
							ThreadValidade MyThread = new ThreadValidade(intRMI); 
							
							
							MyThread.start();
							int userID=-1;
							System.out.println("Cheguei antes do check login");
					            while(true){
					                //an echo server
						            if (checkLogin == 0) {
						            	
						            	/* ---- MENU ---- */
						                out.writeUTF("Bem vindo! Seleccione uma opcao! 1-Login; 2-Registar; 3-Consultar dados");
						                
						                String data = in.readUTF();
						                // so se o ficheiro de login nao existir
						                File f = new File(filename_login);
						        		if(!f.exists() && !f.isDirectory()) {
						        			incrementCounter(out);
						        		}
						                int opcao=Integer.parseInt(data);
						                //System.out.println("Recebi "+data);
						                
					                	
						                
						                
						                /* LOGIN */
						                if (opcao==1) {
						                	out.writeUTF("--LOGIN--");
						                	out.writeUTF("Insira nome de Utilizador");
						                	nomeUser = in.readUTF();
						                	//--
						                	/*out.writeUTF("ID_SESSAO");
						                	out.writeUTF(s_id_sessao);*/
						                	//--
						                	// só guarda se for a 1ª vez que está a fazer login
						                	File f2 = new File(filename_login);
							        		if(!f2.exists() && !f2.isDirectory()) {
							        			criaFicheiros(filename_login);
							        			guardaDados=1;
							        		}
						                	if(guardaDados==1) {
						                		guardaFicheiroLogin(data);
						                		guardaFicheiroLogin(nomeUser);
						                	}
						                	
						                	out.writeUTF("Insira password:");
						                	String password = in.readUTF();
						                	if(guardaDados==1)
						                		guardaFicheiroLogin(password);
						                	
						                	// alterar
						                	try {
						                		userID = intRMI.verificaLogin(nomeUser, password);
						                	}catch (ConnectException e) {
						                		tentativas++;
						                		System.out.println("Nao ha rmi");
						                		
					                			//intRMI = (InterfaceRMI) Naming.lookup("rmi://localhost:7000/benfica");
						                		
						                		
						                	}
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
						                	guardaFicheiroLogin(nomeUser);
						                	out.writeUTF("Insira password:");
						                	String password = in.readUTF();
						                	guardaFicheiroLogin(password);
						                	
						                	userID = intRMI.registaConta(nomeUser, password);
						                	checkLogin=1;
						                }
						                
						                /* CONSULTAR DADOS */
						                else if (opcao==3) {
						                	guardaFicheiro(data,filename_operacao);
						                	out.writeUTF("Consultar dados...\n1-Listar Projetos Actuais\n2-Listar Projetos Antigos\n3-Consultar Detalhes de um projeto\n0-Sair");
						                	data = in.readUTF();
						                	incrementCounter(out);
						                	opcao=Integer.parseInt(data);
						                	guardaFicheiro(data,filename_operacao);
						                	
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
						                	
						                	else if (opcao==3) {
						                		out.writeUTF("Nome do projeto:");
						                		String nomeProjeto = in.readUTF();
						                		incrementCounter(out);
						                		guardaFicheiro(nomeProjeto,filename_operacao);
						                		int projID = intRMI.procuraProjeto(nomeProjeto);
						                		resposta = intRMI.imprimeDetalhesProjeto(projID);
						                		out.writeUTF(resposta);
						                	}
						                }
						                apagaConteudoFicheiro(filename_operacao);
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
						            	out.writeUTF("11-Consultar inbox de um projeto");
						            	out.writeUTF("0-logout");
						            	
						            	
						            
						            	String data = in.readUTF();
						            	incrementCounter(out);
						            	
						            	int opcao=Integer.parseInt(data);
						            	out.writeUTF("A opcao que escolhi e "+opcao);
						            	//if (ficheiroVazio(nomeUser))
						            		guardaFicheiro(data,filename_operacao);
						            	
						            	if (opcao == 0) {
						            		out.writeUTF("A sair...");
						            		apagaFicheiros(filename_login);
						            		apagaFicheiros(filename_operacao);
						            		apagaFicheiros(filename_backup);
						            		terminaThread();
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
						                	incrementCounter(out);
						                	opcao=Integer.parseInt(data);
						                	guardaFicheiro(data,filename_operacao);
						                	
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
						                	
						                	else if (opcao==3) {
						                		out.writeUTF("Nome do projeto:");
						                		String nomeProjeto = in.readUTF();
						                		incrementCounter(out);
						                		guardaFicheiro(nomeProjeto,filename_operacao);
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
						            			incrementCounter(out);
						            			guardaFicheiro(nome,filename_operacao);
							            		id = intRMI.procuraProjeto(nome);
							            		//out.writeUTF("O meu id e "+id);
							            		if (id!=-1)
							            			out.writeUTF("Ja existe um projeto com esse nome!");
						            		}while(id!=-1);
						            		
						            		try {
						            			out.writeUTF("Descricao:");
						            			String descricao = in.readUTF();
						            			incrementCounter(out);
							            		// TODO: proteção - strings
						            			guardaFicheiro(descricao,filename_operacao);
							            		out.writeUTF("Valor objetivo:");
							            		String valor=in.readUTF();
							            		incrementCounter(out);
							            		guardaFicheiro(valor,filename_operacao);
							            		float valor_objetivo = Float.parseFloat(valor);
							            		/*
							            		 * out.writeUTF("Data inicial do projeto:");
							            		 * Calendar user = new GregorianCalendar(2012, Calendar.MAY, 17);
							            		 */
							            		Calendar dataInicial = new GregorianCalendar();
							            		
							            		out.writeUTF("Data final do projeto:");
							            		out.writeUTF("Dia: ");
							            		String diaLido=in.readUTF();
							            		incrementCounter(out);
							            		guardaFicheiro(diaLido,filename_operacao);
							            		int dia = Integer.parseInt(diaLido);
							            		
							            		out.writeUTF("Mês: ");
							            		String mesLido=in.readUTF();
							            		incrementCounter(out);
							            		int mes = Integer.parseInt(mesLido);
							            		guardaFicheiro(mesLido,filename_operacao);
							            		
							            		out.writeUTF("Ano: ");
							            		String anoLido=in.readUTF();
							            		incrementCounter(out);
							            		int ano = Integer.parseInt(anoLido);
							            		guardaFicheiro(anoLido,filename_operacao);
							            		
							            		Calendar dataFinal = new GregorianCalendar();
							            		dataFinal.set(Calendar.YEAR, ano);
							            		dataFinal.set(Calendar.MONTH, mes);
							            		dataFinal.set(Calendar.DAY_OF_MONTH, dia);
							            		// Calendar user = new GregorianCalendar(2012, Calendar.MAY, 17);
							            		
							            		intRMI.criaProjeto(userID, nome, valor_objetivo, dataFinal, descricao);
							            		
							            		
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
						            			incrementCounter(out);
						            			guardaFicheiro(nome,filename_operacao);
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
						            		
						            		out.writeUTF("1-Listar os projetos existentes;0-Não listar");
					            			data = in.readUTF();
					            			incrementCounter(out);
					            			opcao=Integer.parseInt(data);
					            			if (opcao==1) {
					            				resposta = intRMI.listaProjetosActuais();
						                		out.writeUTF(resposta);
					            			}
						            		
						            		// procura o projeto
						            		do {
						            			
						            			
							            		out.writeUTF("Insira o nome do Projeto a doar:");
							            		String nome = in.readUTF();
							            		incrementCounter(out);
							            		guardaFicheiro(nome,filename_operacao);
							            		projID = intRMI.procuraProjeto(nome);
							            		System.out.println("Recebi o id "+projID);
							            		if (projID== -1)
							            			out.writeUTF("Nao existe um projeto com esse nome.");
						            		}while(projID==-1);
							            	
						            		// pede a quantia
						            		do {
						            			out.writeUTF("Quantia a doar?\n");
						            			String quantia=in.readUTF();
						            			incrementCounter(out);
						            			guardaFicheiro(quantia,filename_operacao);
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
							            			incrementCounter(out);
							            			guardaFicheiro(resposta,filename_operacao);
							            			int indexRecompensa = Integer.parseInt(resposta);
							            			
							            		
					
								            		boolean sucesso = intRMI.escolherRecompensa(userID, projID, dinheiro, indexRecompensa);
								            		if (sucesso) {
								            			out.writeUTF("Recompensa adicionada.");
								            		}
								            		else
								            			out.writeUTF("Recompensa falhou.\n");
							            		}
							            		/*
							            		out.writeUTF("Votar?");
							            		intRMI.imprimeVotos(projID);
							            		
							            		data = in.readUTF();
							            		incrementCounter(out);
								                opcao=Integer.parseInt(data);
							            		
							            		intRMI.escolheVoto(userID, projID, opcao;*/
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
							            		incrementCounter(out);
							            		guardaFicheiro(nomeProjeto,filename_operacao);
							            		projID = intRMI.procuraProjeto(nomeProjeto);
							            		if (projID == -1)
							            			out.writeUTF("Nao existe um projeto com esse nome.");
							            		checkAdmin = intRMI.verificaAdministrador(userID, projID);
							            		if (checkAdmin == 0) {
							            			out.writeUTF("Nao é administrador desse projeto!");
							            			out.writeUTF("0-Sair\n1-Tentar de novo");
							            			resposta=in.readUTF();
							            			incrementCounter(out);
							            			aux=Integer.parseInt(resposta);
							            			guardaFicheiro(resposta,filename_operacao);
							            		}
							            		else if (checkAdmin==1) {
								            		out.writeUTF("Nome da recompensa:");
								            		String nome=in.readUTF();
								            		incrementCounter(out);
								            		guardaFicheiro(nome,filename_operacao);
								            		out.writeUTF("Valor da recompensa:");
								            		resposta=in.readUTF();
								            		incrementCounter(out);
								            		guardaFicheiro(resposta,filename_operacao);
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
							            		incrementCounter(out);
							            		guardaFicheiro(nomeProjeto,filename_operacao);
							            		projID = intRMI.procuraProjeto(nomeProjeto);
							            		if (projID == -1)
							            			out.writeUTF("Nao existe um projeto com esse nome.");
							            		checkAdmin = intRMI.verificaAdministrador(userID, projID);
							            		if (checkAdmin == 0) {
							            			out.writeUTF("Nao é administrador desse projeto!");
							            			out.writeUTF("0-Sair\n1-Tentar de novo");
							            			resposta=in.readUTF();
							            			incrementCounter(out);
							            			aux=Integer.parseInt(resposta);
							            			guardaFicheiro(resposta,filename_operacao);
							            		}
							            		else if (checkAdmin==1) {
								            		out.writeUTF("Nome da recompensa:");
								            		String nome=in.readUTF();
								            		incrementCounter(out);
								            		guardaFicheiro(nome,filename_operacao);
								            		intRMI.removeRecompensa(userID, projID, nome);
							            		}
						            		}while(projID==-1 || checkAdmin==0 || aux==1);
						            		
						            		
						            	}
						            	// TODO: não perder mensagens
						            	else if (opcao==11) {
						            		out.writeUTF("Nome do projeto:");
						            		String projNome = in.readUTF();
						            		incrementCounter(out);
						            		int projID = intRMI.procuraProjeto(projNome);
						            		int checkAdmin = intRMI.verificaAdministrador(userID, projID);
						            		do {
							            		out.writeUTF("1-Deixar mensagem");
							            		out.writeUTF("2-Consultar mensagens");
							            		out.writeUTF("3-Responder a mensagem [Apenas admins]");
							            		out.writeUTF("0-Sair");
							            		data=in.readUTF();
							            		incrementCounter(out);
							            		opcao=Integer.parseInt(data);
							            		
							            		if (opcao==1) {
							            			out.writeUTF("Mensagem:");
							            			String mensagem = in.readUTF();
							            			incrementCounter(out);
							            			//guardaFicheiro(mensagem,id_sessao);
							            			
							            			intRMI.adicionaMensagem(userID, projID, mensagem);
							            		}
							            		// TODO: mostrar apenas aquelas que são mandadas pelo user?
							            		else if (opcao==2) {
						            				resposta = intRMI.consultaMensagens(projID);
						            				//guardaFicheiro(resposta,id_sessao);
						            				out.writeUTF(resposta);
							            		}
							            		else if (opcao==3) {
							            			if (checkAdmin==1) {
							            				out.writeUTF("Responder a que utilizador? (inserir ID)");
							            				resposta=in.readUTF();
							            				incrementCounter(out);
							            				guardaFicheiro(resposta,filename_operacao);
							            				int id=Integer.parseInt(resposta);
							            				out.writeUTF("Resposta: ");
							            				String mensagem=in.readUTF();
							            				incrementCounter(out);
							            				
							            				intRMI.respondeMensagens(userID, id, projID, mensagem);
							            				
							            			}
							            		}
						            		}while(opcao!=0);
						            	}
						            	apagaConteudoFicheiro(filename_operacao);
						            }
					                
					            }
			            
			        
		        }
	        catch(ConnectException e) {
		        System.out.println("A tentar outra vez...");
	        	tentativas++;
		        	
	        }
	        catch(EOFException e){
	        	System.out.println("EOF:" + e);
		    }
	        catch(IOException e){
		    	System.out.println("IO:" + e);
		    	apagaFicheiros(filename_login);
        		apagaFicheiros(filename_operacao);
        		apagaFicheiros(filename_backup);
        		terminaThread();
			} 
	        catch (NotBoundException e1) {
				System.out.println("Not bound...");
			} 
	        catch (NumberFormatException e2) {
				System.out.println("Formato invalido! " + e2);
			} 
        }
    	
    }
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
    
    public static void apagaFicheiros(String filepath) {
		
    	try{
    		File file = new File(filepath);
        	
    		if(file.delete()){
    			System.out.println(file.getName() + " is deleted!");
    		}else{
    			System.out.println("Delete operation is failed.");
    		}
    	   
    	}catch(Exception e){
    		
    		e.printStackTrace();
    		
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
    
    public void criaFicheiros(String filepath) {
    	File file = new File(filepath);
	      
	      try {
			if (file.createNewFile()){
			    System.out.println("File is created!");
			  }else{
			    System.out.println("File already exists.");
			  }
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public void incrementCounter(DataOutputStream out) {
    	counter++;
    	//System.out.println("Comparo "+counter+" com "+number_lines);
    	if (counter==number_lines) {
    		try {
    			// TODO: tirar sleep e ver se resulta
				out.writeUTF("IMPRIME");
				System.out.println("Mandei msg para ele imprimir");
				sleep(1000);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
    }
    	
	public void terminaThread() {
		try {
			this.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
    
    

	public boolean ficheiroLoginVazio(){
		BufferedReader br; 
		try {
			br= new BufferedReader(new FileReader(filename_login));  
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
    
	public void guardaFicheiroLogin(String st) {
		System.out.println("Vou guardar...");
		try {
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(filename_login, true)));
		    writer.println(st);
		    writer.close();
			
		} catch (FileNotFoundException e) {
			System.out.println("Ficheiro nao encontrado");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    }

    public void guardaFicheiro(String st, String filename) {
    	
		try {
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)));
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
    
}

class ThreadValidade extends Thread{
	InterfaceRMI intRMI;
	
	public ThreadValidade(InterfaceRMI intRMI) {
		this.intRMI=intRMI;
	}
	
	public void run() {
		while(true) {
			try {
				// TODO: dorme 1 dia
				sleep(60000);
				System.out.println("Ola da thread!");
				intRMI.updateValidadeProjetos();
				Thread.sleep(10000);
			} catch (RemoteException e1) {
				try {
					intRMI = (InterfaceRMI) Naming.lookup("rmi://localhost:7000/benfica");
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (NotBoundException e) {
					e.printStackTrace();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} // dorme 1 minuto
			//dorme 1 hora
		}
	}
}

