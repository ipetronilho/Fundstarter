import java.io.Serializable;
import java.sql.Date;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Projeto implements Serializable {
	
	// todo: valor_objetivo2 em que se passar tem de ter uma extra recompensa
	ArrayList <Mensagem> inboxProjeto = new ArrayList <Mensagem>();

	ArrayList <Recompensa> listaRecompensas = new ArrayList <Recompensa>();
	ArrayList <Voto> listaVotos = new ArrayList <Voto>();
	// implementar hashmap
	AtomicInteger contadorIDRecompensa = new AtomicInteger(0);
	
	Utilizador admin;
	float valor_objetivo;
	String descricao;
	String nome;
	float percentagem;
	boolean idade; // 0 é aantigo, 1 é novo
	
	//Calendar dataInicial = new GregorianCalendar();
	Calendar dataFinal = new GregorianCalendar();
	// Calendar user = new GregorianCalendar(2012, Calendar.MAY, 17);
	int id;
	float valor_final; // valor final que conseguiu atingir
	
	// hashmap que liga IDs dos utilizadores backers às doações
	HashMap <Integer, Float> listaDoacoes = new HashMap<Integer, Float>();
	
	//inbox que liga os IDs dos users às mensagens que eles escrevem
	HashMap <Integer, ArrayList<Mensagem>> inbox = new HashMap <Integer, ArrayList<Mensagem>>();
	 
	
	// TODO: adicionei agora uma descrição e tenho de pedi-la
	public Projeto(Utilizador admin, String nome, float valor_objetivo, int id, Calendar dataFinal, String descricao) {
		this.admin=admin;
		this.nome=nome;
		this.valor_objetivo=valor_objetivo;
		this.id=id;
		this.descricao=descricao;
		this.dataFinal=dataFinal;
		this.idade=true;
	}
	
	
	public boolean verificaValidade() {
		Calendar diadeHoje= new GregorianCalendar();
		return (dataFinal.compareTo(diadeHoje)>=0);	// true: actual. false: antigo
	}

	
	public String imprime() {
		DecimalFormat df = new DecimalFormat("#.##");
		float valor_recolhido = getValorRecolhido();
		percentagem = valor_recolhido/valor_objetivo*100;
		return "\nProjeto: "+this.nome+"\nDescricao:"+this.descricao+"\nValor Recolhido:"+valor_recolhido+"("+df.format(percentagem)+"%)"+"\nValor objetivo: "+valor_objetivo+"\nData limite:"+imprimeData(this)+"\nId: "+id+"\n\n";
	}
	
	public float getValorRecolhido() {
		Set set = listaDoacoes.entrySet();
		Iterator i= set.iterator();
		float totalRecolhido=0;
		while (i.hasNext()) {
			Map.Entry mentry = (Map.Entry)i.next();
			System.out.print("key is: "+ mentry.getKey() + " & Value is: ");
	        System.out.println(mentry.getValue());
	        totalRecolhido += (float) mentry.getValue();
		}
		return totalRecolhido;
	}

	// recompensas, data limite, descrição
	public String imprimeDetalhes(Projeto proj) {
		String str="";
		str=str.concat("Id:"+this.id+"Projeto: "+this.nome+"\nDescricao:"+this.descricao+"\nData Final:"+imprimeData(proj)+"\nValor objetivo: "+valor_objetivo+"\n");

		str = str.concat(imprimeRecompensas(proj));
		return str;
	}
	
	public String imprimeRecompensas(Projeto proj) {
		int i;
		String str="";
		for (i=0;i<proj.listaRecompensas.size();i++) {
			str=str.concat(proj.listaRecompensas.get(i).imprimeRecompensa(i));
		}
		return str;
	}
	
	public String imprimeData(Projeto proj) {
		String str="";
		str=str.concat(proj.dataFinal.get(Calendar.DAY_OF_MONTH)+"/"+proj.dataFinal.get(Calendar.MONTH)+"/"+proj.dataFinal.get(Calendar.YEAR));
		return str;
	}
	
	public void addDescricao(Projeto proj, String s) {
		proj.descricao=s;
	}
	
	public void addRecompensa(Projeto proj, Recompensa rec) {
		proj.listaRecompensas.add(rec);
	}
	
	public void addVoto(Projeto proj, Voto v) {
		proj.listaVotos.add(v);
	}

}

