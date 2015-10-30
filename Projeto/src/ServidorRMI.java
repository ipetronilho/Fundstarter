import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServidorRMI extends UnicastRemoteObject implements InterfaceRMI {
	
	
	private static final long serialVersionUID = 1L;
	AtomicInteger contadorIDProjeto = new AtomicInteger(0);
	AtomicInteger contadorIDRecompensa = new AtomicInteger(0);
	BaseDeDados bd = new BaseDeDados();
	
	protected ServidorRMI() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public static void main(String args[]) throws IOException, ClassNotFoundException {

		System.getProperties().put("java.security.policy", "policy.all");
        System.setSecurityManager(new RMISecurityManager());
        System.setProperty("java.rmi.server.hostname", "192.168.1.6");
		
		try {
			ServidorRMI servRMI= new ServidorRMI();
			// TODO
			
			java.rmi.registry.LocateRegistry.createRegistry(7000); 
			Naming.rebind("rmi://localhost:7000/benfica", servRMI);
			
			//Registry r = LocateRegistry.createRegistry(7000);
			//r.rebind("inte", servRMI);
			servRMI.iniciaDados(servRMI);
			//servRMI.carregaFicheiro();
			//servRMI.guardaFicheiro();
			
			System.out.println("Servidor RMI ready.");
			
			
			
		} catch (RemoteException re) {
			System.out.println("Exception: " + re);
		} catch (MalformedURLException e) {
			System.out.println("MalformedURLException: " + e);
		}

	}
	
	/* inicializa os dados iniciais */
	synchronized public void iniciaDados(ServidorRMI servRMI) throws IOException {
		Utilizador user1 = new Utilizador("Primeiro", "passe1", 100);
		//user1.id=bd.listaUtilizadores.size()-1;
		user1.id=bd.listaUtilizadores.size();
		String descricao="Corda que conta o nº de saltos por minuto";
		
		//Calendar dataInicial = new GregorianCalendar(2012, Calendar.MAY, 17);
		Calendar dataFinal = new GregorianCalendar(2014, Calendar.NOVEMBER, 18);
		int projID = contadorIDProjeto.getAndIncrement();
		Projeto proj1 = new Projeto(user1, "FitJump", 2, projID, dataFinal, descricao);
		
		Recompensa rec = new Recompensa(proj1, "O nome do apoiante aparece na lista", 5, contadorIDRecompensa.getAndIncrement(), 0);
		Recompensa rec2 = new Recompensa(proj1, "O apoiante recebe uma unidade de FitJumpingRope", 60, contadorIDRecompensa.getAndIncrement(), 0);
		Recompensa rec3 = new Recompensa(proj1, "O apoiante recebe duas unidades de FitJumpingRope", 100, contadorIDRecompensa.getAndIncrement(), 0);
		
		Voto v = new Voto("vermelho");
		Voto v2 = new Voto("azul");
		
		proj1.addRecompensa(proj1, rec);
		proj1.addRecompensa(proj1, rec2);
		proj1.addRecompensa(proj1, rec3);
		
		proj1.listaVotos.add(v);
		proj1.listaVotos.add(v2);
		
		user1.listaIDsProjeto.add(projID);
		
		
		bd.listaProjetos.add(proj1);
		bd.listaUtilizadores.add(user1);
		servRMI.guardaFicheiro();
	}

	/* CONSULTAR DADOS */
	public String listaProjetosActuais() {
		int i;
		String str="";
		for (i=0;i<bd.listaProjetos.size();i++) {
			if (bd.listaProjetos.get(i).verificaValidade()){
				str= str.concat(bd.listaProjetos.get(i).imprime());
				str=str.concat("RECOMPENSAS\n");
				str = str.concat(listaRecompensas(bd.listaProjetos.get(i).id));
			}
		}
		return str;
	}
	
	public String listaProjetosAntigos() {
		int i;
		String str="";
		for (i=0;i<bd.listaProjetos.size();i++) {
			if (!bd.listaProjetos.get(i).verificaValidade()){
				str= str.concat(bd.listaProjetos.get(i).imprime()); 
				str=str.concat("RECOMPENSAS\n");
				str = str.concat(listaRecompensas(bd.listaProjetos.get(i).id));
			}
		}
		return str;
	}
	
	/* LOGIN */
	/* adiciona um utilizador e devolve o seu ID */
	synchronized public int registaConta(String nomeUser, String password) {
		float saldoDefault=100;
		//int userID = bd.listaUtilizadores.size()-1;
		int userID = bd.listaUtilizadores.size();
		Utilizador user = new Utilizador(nomeUser, password, saldoDefault);
		user.setId(userID); // id do user é a sua posição na lista de users
		
		bd.listaUtilizadores.add(user);
		
		try {
			guardaFicheiro();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return userID;
	}
	
	/* verifica se o username e password são válidos */
	public int verificaLogin(String nomeUser, String password)  {
		int i;
		for (i=0;i<bd.listaUtilizadores.size();i++) {
			if ((bd.listaUtilizadores.get(i).nome.compareToIgnoreCase(nomeUser) == 0)&& (bd.listaUtilizadores.get(i).password.compareToIgnoreCase(password) == 0))
				return i;
		}
		return -1;
	}
	
	/* FICHEIROS */
	/* carrega dados de ficheiro*/
	synchronized public void carregaFicheiro() throws IOException, ClassNotFoundException {
		FileInputStream fin = new FileInputStream("ficheiros/utilizadores.txt");
		ObjectInputStream objin = new ObjectInputStream(fin);
		bd.listaUtilizadores = (ArrayList) objin.readObject();
		
        fin = new FileInputStream("ficheiros/projetos.txt");
        objin = new ObjectInputStream(fin);
        bd.listaProjetos = (ArrayList) objin.readObject();
        
        // vai buscar o último id
        contadorIDProjeto.set(bd.listaProjetos.get(bd.listaProjetos.size()-1).id+1);
        iniciaContadorRecompensas();
	}
	
	/* guarda dados em ficheiro */
	synchronized public void guardaFicheiro() throws IOException {
		
        FileOutputStream fout = new FileOutputStream("ficheiros/projetos.txt");
        ObjectOutputStream objout = new ObjectOutputStream(fout);
        objout.writeObject(bd.listaProjetos); // escrever arrayList
        objout.close();
        
        fout = new FileOutputStream("ficheiros/utilizadores.txt");
        objout = new ObjectOutputStream(fout);

        objout.writeObject(bd.listaUtilizadores);
        objout.close();
		
	}

	/* CONSULTA */
	/* retorna o saldo */
	public String consultarSaldo(int userID) {
		return "Saldo: "+bd.listaUtilizadores.get(userID).getSaldo()+"\n";
	}
	
	/* devolve a lista de Projetos em que o user é admin */
	public String consultarProjetos(int userID) {
		int i, j;
		String str="";
		
		Utilizador user = bd.listaUtilizadores.get(userID);
		for (i=0;i<user.listaIDsProjeto.size();i++) {
			for (j=0;j<bd.listaProjetos.size();j++) {
				if (bd.listaProjetos.get(j).id == user.listaIDsProjeto.get(i))
					str = str.concat(bd.listaProjetos.get(j).imprime());
			}
		}
		return str;
	}
	
	/* devolve informação sobre o Projeto especificado */
	public String imprimeDetalhesProjeto(int projID) {
		int i, j;
		String str="";
		for (j=0;j<bd.listaProjetos.size();j++) {
			if (bd.listaProjetos.get(j).id == projID)
				str = str.concat(bd.listaProjetos.get(j).imprimeDetalhes(bd.listaProjetos.get(j)));
		}
		
		return str;
	}
	
	
	public String imprimeDoacoesUser(int userID) {
		String str="";
		Utilizador user = bd.listaUtilizadores.get(userID); 
		int idProjeto;
		float doacao;
		Set<Entry<Integer, Float>> set = user.listaDoacoesUser.entrySet();
		Iterator<Entry<Integer, Float>> i= set.iterator();
		
		while (i.hasNext()) {
			Map.Entry <Integer, Float> mentry = (Entry<Integer, Float>) i.next();
			idProjeto = (int) mentry.getKey();
			doacao = (float) mentry.getValue();
			Projeto proj = procuraProjetoID(idProjeto);
			if(proj!=null)
			str = str.concat("Projeto: "+proj.nome+"\nDoacao: "+doacao+"\n");
			
		}
		return str;
	}
	
	/* devolve a lista de recompensas do user - check! */
	public String consultarRecompensas(int userID) {
		int i, j, m;
		Projeto proj;
		Recompensa rec;
		String str="";

		for (i=0;i<bd.listaProjetos.size();i++) {
			proj = bd.listaProjetos.get(i);
			str = str.concat(procuraRecompensa(proj, userID ));
			
		}
		
		return str;
	}
	
	/* pesquisa por nome e devolve o ID do Projeto (auxiliar) */
	public int procuraProjeto(String nome) {
		int i;
		//System.out.println("O tamanho da lista de Projetos e "+listaProjetos.size());
		for (i=0;i<bd.listaProjetos.size();i++) {
			if (bd.listaProjetos.get(i).nome.compareToIgnoreCase(nome)==0) {
				return bd.listaProjetos.get(i).id;
			}
		}
		return -1;
	}

	/* pesquisa por ID e devolve o Projeto (auxiliar) */
	public Projeto procuraProjetoID(int projID) {
		int i;
		//System.out.println("O tamanho da lista de Projetos e "+listaProjetos.size());
		for (i=0;i<bd.listaProjetos.size();i++) {
			if (bd.listaProjetos.get(i).id == projID) {
				return bd.listaProjetos.get(i);
			}
		}
		return null;
	}
	
	/* devolve uma string com todas as recompensas do user (auxiliar)*/
	public String procuraRecompensa(Projeto proj, int userID) {
		int i, j;
		String str="";
		
		Utilizador user = bd.listaUtilizadores.get(userID);
		// vai a cada projeto do user e compara todas as recompensas com as do user até chegar ao destino
		for (i=0;i<proj.listaRecompensas.size();i++) {
			for (j=0;j<user.listaIDsRecompensas.size();j++) {
				System.out.println("Vou comparar "+proj.listaRecompensas.get(i).id+" com "+user.listaIDsRecompensas.get(j));
				if (proj.listaRecompensas.get(i).id == user.listaIDsRecompensas.get(j)) {
					System.out.println("Entrei! Devia imprimir"+proj.listaRecompensas.get(i).nome);
					if (proj.listaRecompensas.get(i).valida==0)
						str=str.concat("(Por validar): \n");
					else if (proj.listaRecompensas.get(i).valida==1)
						str=str.concat("(Garantida): \n");
					str=str.concat(proj.listaRecompensas.get(i).imprimeRecompensa(i));
				}
			}
		}
		
		return str;
	}

	public String listaRecompensas(int projID) {
		int i=0;
		String str="";
		Projeto p = procuraProjetoID(projID);
		for (i=0;i<p.listaRecompensas.size();i++) {
			str = str.concat(p.listaRecompensas.get(i).imprimeRecompensa(i));
		}
		return str;
	}
	
	
	public String imprimeVotos(int projID) {
		Projeto proj = procuraProjetoID(projID);
		String str="";
		int i=0;
		for (i=0;i<proj.listaVotos.size();i++) {
			str=str.concat(proj.listaVotos.get(i).imprime(i));
		}
		return str;
	}
	
	synchronized public void escolheVoto(int userID, int projID, int index) {
		Projeto proj = procuraProjetoID(projID);
		proj.listaVotos.get(index).listaUsers.add(userID);
		proj.listaVotos.get(index).contador+=1;
	}
	
	/* OPERAÇÕES DE PROJETOS */
	
	/* criar um projeto - check! */
	synchronized public void criaProjeto(int userID, String nome, float valor_objetivo, Calendar dataFinal, String descricao) {
		int id = contadorIDProjeto.getAndIncrement();
		System.out.println("ID e "+id+" aqui ");
		bd.listaUtilizadores.get(userID).listaIDsProjeto.add(id);
		Projeto proj = new Projeto(bd.listaUtilizadores.get(userID), nome, valor_objetivo, id, dataFinal, descricao);
		bd.listaProjetos.add(proj);
		
		try {
			guardaFicheiro();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	synchronized public void eliminaProjeto(int userID, int projID) {
		
		//Utilizador user = bd.listaUtilizadores.get(userID);
		//System.out.println("O projeto primeiro chama-se "+bd.listaProjetos.get(0).nome);
		
		
		bd.listaUtilizadores.get(userID).listaIDsProjeto.remove(projID); // retira do admin
		Projeto proj = procuraProjetoID(projID);
		
		// devolve dinheiro aos users
		devolveDinheiro(proj);
		
		retiraRecompensa(userID, proj);
		
		retiraDoacao(userID, projID);
		
		bd.listaProjetos.remove(bd.listaProjetos.get(projID)); // retira da lista global de projetos
		
		try {
			guardaFicheiro();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// funciona
	public void devolveDinheiro(Projeto proj) {
		String str="";
		int userID;
		float doacao, saldo;
		Set<Entry<Integer, Float>> set = proj.listaDoacoes.entrySet();
		Iterator<Entry<Integer, Float>> i= set.iterator();
		
		while (i.hasNext()) { // para cada user que doou
			Map.Entry <Integer, Float> mentry = (Entry<Integer, Float>) i.next();
			userID = (int) mentry.getKey();
			doacao = (float) mentry.getValue();
			saldo = bd.listaUtilizadores.get(userID).getSaldo();
			bd.listaUtilizadores.get(userID).setSaldo(saldo+doacao);
			
			retiraRecompensa(userID, proj);
		}
		
		try {
			guardaFicheiro();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	synchronized public void retiraRecompensa(int userID, Projeto proj) {
		int i, j;
		// pesquisa a recompensa e remove
		for (i=0;i<proj.listaRecompensas.size();i++) {
			for (j=0;j<bd.listaUtilizadores.get(userID).listaIDsRecompensas.size();j++) {
				if (proj.listaRecompensas.get(i).id == bd.listaUtilizadores.get(userID).listaIDsRecompensas.get(j)) {
					bd.listaUtilizadores.get(userID).listaIDsRecompensas.remove(bd.listaUtilizadores.get(userID).listaIDsRecompensas.get(j));
				}
			}
		}
		
		try {
			guardaFicheiro();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	synchronized public void retiraDoacao(int userID, int projID) {
		
		String str="";
		Utilizador user = bd.listaUtilizadores.get(userID); 
		int idProjeto;
		float doacao;
		Set<Entry<Integer, Float>> set = user.listaDoacoesUser.entrySet();
		Iterator<Entry<Integer, Float>> i= set.iterator();
		
		while (i.hasNext()) {
			Map.Entry <Integer, Float> mentry = (Entry<Integer, Float>) i.next();
			idProjeto = (int) mentry.getKey();
			doacao = (float) mentry.getValue();
			if (idProjeto == projID) {
				user.listaDoacoesUser.remove(idProjeto, doacao);
			}
		}
		
		try {
			guardaFicheiro();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean validaDoacao(int userID, float dinheiro) {
		if (dinheiro > bd.listaUtilizadores.get(userID).getSaldo())
			return false;
		return true;
	}
	
	synchronized public void doarDinheiro(int userID, int projID, float dinheiro) {
		Utilizador user = bd.listaUtilizadores.get(userID);
	
		Projeto proj = procuraProjetoID(projID);
		
		float saldo = user.getSaldo();
		proj.listaDoacoes.put(userID, dinheiro);  		// acrescenta user ao Projeto
		System.out.println("Vai doar ao projeto "+proj.nome+" e procurar "+projID);
		
		if (user.contemDoacao(projID)) {			
			dinheiro += user.listaDoacoesUser.get(projID);
		}
		
		System.out.println("O saldo e "+user.getSaldo()+" menos "+dinheiro);
		
		user.listaDoacoesUser.put(proj.id, dinheiro);	// acrescenta projeto e doação ao user
		user.setSaldo(user.getSaldo()-dinheiro);					// retira dinheiro ao user
		
	}
	

	/* OPERAÇÕES DE RECOMPENSAS */
	synchronized public boolean escolherRecompensa(int userID, int projID, float dinheiro, int indexRecompensa) {
		int i=0;
		Projeto proj = procuraProjetoID(projID);
		//System.out.println("Projeto: "+proj.nome);
		Recompensa rec = proj.listaRecompensas.get(indexRecompensa);
		//System.out.println("Dinheiro: "+dinheiro+ " e valor da recomp: " + rec.valor);
		if (dinheiro >= rec.valor) {
			bd.listaUtilizadores.get(userID).listaIDsRecompensas.add(rec.id);
			
			try {
				guardaFicheiro();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return true;
		}
		return false;
	}
	
	
	synchronized public void addRecompensa(int userID, int projID, String nome, float valor) {
		int id = contadorIDRecompensa.getAndIncrement();
		Projeto proj = procuraProjetoID(projID); 
		Recompensa rec = new Recompensa(proj, nome, valor, id, 0);
		proj.listaRecompensas.add(rec);
		//listaUtilizadores.get(userID).listaIDsRecompensas.add(id);
		
		try {
			guardaFicheiro();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	
	synchronized public void removeRecompensa(int userID, int projID, String nome) {
		// e se os users já tiverem escolhido uma recompensa e ela for cancelada? dinheiro, votos?
		
		Projeto proj = procuraProjetoID(projID); 
		Recompensa rec = encontraRecompensa(projID, nome);
		proj.listaRecompensas.remove(rec);
		
		try {
			guardaFicheiro();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	/* OPERAÇÕES DE INBOX */
	synchronized public void adicionaMensagem(int userID, int projID, String mensagem) {
		Projeto proj = procuraProjetoID(projID);
		Mensagem mens = new Mensagem(userID, projID, mensagem);
		ArrayList <Mensagem> listaM = new ArrayList <Mensagem>();
		if (proj.inbox.get(userID)!=null) {
			listaM = proj.inbox.get(userID);
		}
		listaM.add(mens);
		proj.inbox.put(userID, listaM);
		
		try {
			guardaFicheiro();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	// TODO: hashmap no projeto faz corresponder user a arraylist de mensagens.
	// para adicionar uma mensagem basta ir à arraylist na hashmap e fazer lista.add(mensagem)
	// para responder a mesma coisa
	public String consultaMensagens(int projID) {
		Projeto proj = procuraProjetoID(projID);
		String str="";
		int userID;
		int j=0;
		ArrayList <Mensagem> listaMens;
		Set<Entry<Integer, ArrayList<Mensagem>>> set = proj.inbox.entrySet();
		Iterator<Entry<Integer, ArrayList<Mensagem>>> i= set.iterator();
		str=str.concat("Mensagens:\n");
		while (i.hasNext()) {
			Map.Entry <Integer, ArrayList<Mensagem>> mentry = (Entry<Integer, ArrayList<Mensagem>>) i.next();
			userID = (int) mentry.getKey();
			listaMens = (ArrayList<Mensagem>) mentry.getValue();
			
			str = str.concat("["+bd.listaUtilizadores.get(userID).nome+"]:(id="+bd.listaUtilizadores.get(userID).id+")\n");
			for (j=0;j<listaMens.size();j++) { // se foi o admin a escrever a resposta então marco
				System.out.println("Comparo"+listaMens.get(j).idUser+" com "+proj.admin.id);
				if (listaMens.get(j).idUser==proj.admin.id) {
					
					str=str.concat("A: ");
				}
				str = str.concat(listaMens.get(j).comentario+"\n");
			}
			
		}
		return str;
	}

	// admin responde a mensagens
	synchronized public void respondeMensagens(int adminID, int userID, int projID, String mensagem) {
		Projeto proj = procuraProjetoID(projID);
		Mensagem mens = new Mensagem(adminID, projID, mensagem);
		ArrayList <Mensagem> listaM = proj.inbox.get(userID);
		listaM.add(mens);
		proj.inbox.put(userID, listaM);
		
		try {
			guardaFicheiro();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/* AUXILIARES */
	public int verificaAdministrador(int userID, int projID) {
		int i=0;
		for (i=0;i<bd.listaUtilizadores.get(userID).listaIDsProjeto.size();i++) {
			if (projID == bd.listaUtilizadores.get(userID).listaIDsProjeto.get(i))
				return 1;
		}
		return 0;
	}
	
	public Recompensa encontraRecompensa(int projID, String nome) {
		int i=0;
		Projeto proj = procuraProjetoID(projID);
		for (i=0;i<proj.listaRecompensas.size();i++) {
			if (proj.listaRecompensas.get(i).nome.compareToIgnoreCase(nome)==0) {
				return proj.listaRecompensas.get(i);
			}
		}
		return null;
	}
	
	// fimProjeto
	public void updateValidadeProjetos() {
		// dinheiro é enviado para o admin e as recompensas dos users ficam válidas 
		for(int i=0; i<bd.listaProjetos.size();i++){ 
			if(bd.listaProjetos.get(i).idade){ // se for atual
				if(!bd.listaProjetos.get(i).verificaValidade()){  // se projeto tiver acabado data
					bd.listaProjetos.get(i).idade=false;  // projeto passa para antigo
					if (bd.listaProjetos.get(i).getValorRecolhido() >= bd.listaProjetos.get(i).valor_objetivo) { // conclui com sucesso
						bd.listaProjetos.get(i).admin.saldo+=bd.listaProjetos.get(i).getValorRecolhido();
						validaRecompensas(bd.listaProjetos.get(i));
					}
					else { // devolve dinheiro aos users
						devolveDinheiro(bd.listaProjetos.get(i));
					}
				}
			}
		}
		try {
			guardaFicheiro();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	public void validaRecompensas(Projeto proj) {
		int i;
		for (i=0;i<proj.listaRecompensas.size();i++) {
			proj.listaRecompensas.get(i).valida=1;
		}
	}
	
	/* função que diz onde começa o id de Recompensas */
	public void iniciaContadorRecompensas() {
		int i,j, max=0;
		for (i=0;i<bd.listaProjetos.size();i++) {
			for (j=0;j<bd.listaProjetos.get(i).listaRecompensas.size();j++) {
				if (bd.listaProjetos.get(i).listaRecompensas.get(j).id>max)
					max = bd.listaProjetos.get(i).listaRecompensas.get(j).id;
			}
		}
		contadorIDRecompensa.set(max+1);
	}
}
