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

public class ServidorRMI extends UnicastRemoteObject implements InterfaceRMI {
	
	// TODO: elimina projeto
	
	AtomicInteger contadorIDProjeto = new AtomicInteger(0);
	AtomicInteger contadorIDRecompensa = new AtomicInteger(0);
	BaseDeDados bd = new BaseDeDados();
	
	protected ServidorRMI() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public static void main(String args[]) throws IOException, ClassNotFoundException {

		System.setProperty("java.security.policy","file:///urs/lib/jvm/java-7-openjdk-i386/jre/lib/security/java.policy");
        
		try {
			ServidorRMI servRMI= new ServidorRMI();
			// TODO
			java.rmi.registry.LocateRegistry.createRegistry(7000); 
			Naming.rebind("rmi://localhost:7000/benfica", servRMI);
			
			// TODO: arraylist IDs das recompensas etc?
			
			//servRMI.iniciaDados(servRMI);
			servRMI.carregaFicheiro();
			
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
		Utilizador user1 = new Utilizador("Primeiro", "passe1", 80);
		user1.id=bd.listaUtilizadores.size()-1;
		
		Calendar dataInicial = new GregorianCalendar(2012, Calendar.MAY, 17);
		Calendar dataFinal = new GregorianCalendar(2015, Calendar.NOVEMBER, 18);
		int idP = contadorIDProjeto.getAndIncrement();
		Projeto proj1 = new Projeto(user1, "proj", 30, idP, dataInicial, dataFinal);
		
		Recompensa rec = new Recompensa(proj1, "nomeRecompensa", 5, contadorIDRecompensa.getAndIncrement());
		
		proj1.addRecompensa(proj1, rec);
		user1.listaIDsProjeto.add(idP);
		
		bd.listaProjetos.add(proj1);
		bd.listaUtilizadores.add(user1);
		servRMI.guardaFicheiro();
	}
	
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

	/* CONSULTAR DADOS */
	public String listaProjetosActuais() {
		int i;
		String str="";
		for (i=0;i<bd.listaProjetos.size();i++) {
			if (bd.listaProjetos.get(i).verificaValidade())
				str.concat("PROJETO\n");
				str= str.concat(bd.listaProjetos.get(i).imprime());
				str.concat("--\n");
				str = str.concat(listaRecompensas(bd.listaProjetos.get(i).id));
		}
		return str;
	}
	
	public String listaProjetosAntigos() {
		int i;
		String str="";
		for (i=0;i<bd.listaProjetos.size();i++) {
			if (!bd.listaProjetos.get(i).verificaValidade())
				str= str.concat(bd.listaProjetos.get(i).imprime());
		}
		return str;
	}
	
	/* LOGIN */
	/* adiciona um utilizador e devolve o seu ID */
	synchronized public int registaConta(String nomeUser, String password) {
		float saldoDefault=100;
		int userID = bd.listaUtilizadores.size()-1;
		Utilizador user = new Utilizador(nomeUser, password, saldoDefault);
		user.setId(userID); // id do user é a sua posição na lista de users
		
		bd.listaUtilizadores.add(user);
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
	
	// TODO: nao resulta para 2 doações porquê? - resulta sim!
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
		
		// cada projeto tem obrigatoriamente de escolher uma recompensa
		for (i=0;i<bd.listaUtilizadores.get(userID).listaIDsProjeto.size();i++) {
			proj = procuraProjetoID(bd.listaUtilizadores.get(userID).listaIDsProjeto.get(i));
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
	
	/* OPERAÇÕES DE PROJETOS */
	
	/* criar um projeto - check! */
	synchronized public void criaProjeto(int userID, String nome, float valor_objetivo, Calendar dataInicial, Calendar dataFinal) {
		int id = contadorIDProjeto.getAndIncrement();
		System.out.println("ID e "+id+" aqui ");
		bd.listaUtilizadores.get(userID).listaIDsProjeto.add(id);
		Projeto proj = new Projeto(bd.listaUtilizadores.get(userID), nome, valor_objetivo, id, dataInicial, dataFinal);
		bd.listaProjetos.add(proj);
	}
	
	synchronized public void eliminaProjeto(int userID, int projID) {
		
		//Utilizador user = bd.listaUtilizadores.get(userID);
		//System.out.println("O projeto primeiro chama-se "+bd.listaProjetos.get(0).nome);
		
		bd.listaProjetos.remove(bd.listaProjetos.get(0));
		bd.listaUtilizadores.get(userID).listaIDsProjeto.remove(projID);
		Projeto proj = procuraProjetoID(projID);
		
		// devolve dinheiro aos users
		devolveDinheiro(proj);
		
		//TODO: retira as recompensas
		retiraRecompensa(userID, proj);
		
		
		// TODO: retira doacoes
		
		
		
	}
	
	synchronized public void devolveDinheiro(Projeto proj) {
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
		
	}
	
	synchronized public void retiraRecompensa(int userID, Projeto proj) {
		
		int i, j;
		
		Utilizador user = bd.listaUtilizadores.get(userID);
		for (i=0;i<proj.listaRecompensas.size();i++) {
			for (j=0;j<user.listaIDsRecompensas.size();j++) {
				if (proj.listaRecompensas.get(i).id == user.listaIDsRecompensas.get(j)) {
					user.listaIDsRecompensas.remove(user.listaIDsRecompensas.get(j));
				}
			}
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
		
		if (user.contemDoacao(projID)) {	// TODO: está mal porque acha que user ja doou ao projeto			
			dinheiro += user.listaDoacoesUser.get(projID);
		}
		
		System.out.println("O saldo e "+user.getSaldo()+" menos "+dinheiro);
		
		user.listaDoacoesUser.put(proj.id, dinheiro);	// acrescenta projeto e doação ao user
		user.setSaldo(user.getSaldo()-dinheiro);					// retira dinheiro ao user
		
	}
	

	/* OPERAÇÕES DE RECOMPENSAS */
	synchronized public void addRecompensa(int userID, int projID, String nome, float valor) {
		int id = contadorIDRecompensa.getAndIncrement();
		Projeto proj = procuraProjetoID(projID); 
		Recompensa rec = new Recompensa(proj, nome, valor, id);
		proj.listaRecompensas.add(rec);
		//listaUtilizadores.get(userID).listaIDsRecompensas.add(id);
	}
	
	synchronized public boolean escolherRecompensa(int userID, int projID, float dinheiro, int indexRecompensa) {
		int i=0;
		Projeto proj = procuraProjetoID(projID);
		//System.out.println("Projeto: "+proj.nome);
		Recompensa rec = proj.listaRecompensas.get(indexRecompensa);
		//System.out.println("Dinheiro: "+dinheiro+ " e valor da recomp: " + rec.valor);
		if (dinheiro >= rec.valor) {
			bd.listaUtilizadores.get(userID).listaIDsRecompensas.add(rec.id);
			return true;
		}
		return false;
	}
	
	synchronized public void removeRecompensa(int userID, int projID, String nome) {
		// e se os users já tiverem escolhido uma recompensa e ela for cancelada? dinheiro, votos?
		
		Projeto proj = procuraProjetoID(projID); 
		Recompensa rec = encontraRecompensa(projID, nome);
		proj.listaRecompensas.remove(rec);
		
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


	
}
