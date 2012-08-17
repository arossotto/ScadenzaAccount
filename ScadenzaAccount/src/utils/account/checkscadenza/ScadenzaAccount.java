package utils.account.checkscadenza;

import java.awt.Color;
import java.awt.TextArea;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.swing.JFrame;

public class ScadenzaAccount {

	private static final String DATE_PATTERN = "MM/dd/yyyy h:mm a";
	private static final String SCADENZA_ACCOUNT = "Scadenza account";
	private static final String NOME_COMPLETO = "Nome completo";
	private static final String POPUP_TITLE = "Scadenze Account";

	public static void main(String[] args){
		new ScadenzaAccount().mostraScadenze();
	}
	
	private void mostraScadenze(){
		try {
			showPopup(getScadenze());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private List<Scadenza> getScadenze() throws FileNotFoundException, IOException, ParseException {
		List<Scadenza> scadenze = new ArrayList<Scadenza>();
		Properties prop = new Properties();
		prop.load(new FileInputStream(new File("utenze.properties")));
		for (String user : prop.stringPropertyNames()) {
			scadenze.add(parseOutput(user, new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("net user " + user + " /domain").getInputStream()))));
		}
		Collections.sort(scadenze, new Comparator<Scadenza>() {
			@Override
			public int compare(Scadenza o1, Scadenza o2) {
				return o1.getData().compareTo(o2.getData());
			}
		});
		return scadenze;
	}

	private Scadenza parseOutput(String user, BufferedReader br) throws IOException, ParseException {
		Scadenza scadenza = new Scadenza();
		for (String str; ((str = br.readLine()) != null);) {
			if (str.indexOf(SCADENZA_ACCOUNT) == 0) {
				scadenza.setUtenza(user);
				String data = str.split(SCADENZA_ACCOUNT)[1].trim();
				scadenza.setData(new SimpleDateFormat(DATE_PATTERN).parse("Mai".equals(data) ? "12/31/2099 0:00 AM" : data));
			} else if (str.indexOf(NOME_COMPLETO) == 0) {
				scadenza.setNome(str.split(NOME_COMPLETO)[1].trim());
			}
		}
		return scadenza;
	}

	private void showPopup(List<Scadenza> scadenze) {
		JFrame alert = new JFrame(POPUP_TITLE);
		alert.setAlwaysOnTop(true);
		alert.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		TextArea textArea = new TextArea();
		for (Scadenza scadenza : scadenze) {
			textArea.append(scadenza.toString() + "\n");
			if (scadenza.isTraMenoDiTotGiorni(15))
				textArea.setBackground(Color.RED);
		}
		alert.add(textArea);
		alert.setSize(520, 300);
		alert.setVisible(true);
	}

}

class Scadenza {
	private static final int GIORNO = 86400000;

	private String nome;
	private String utenza;
	private Date data;

	public String getNome() {
		return nome;
	}

	public boolean isTraMenoDiTotGiorni(int giorni) {
		return data!=null && data.getTime() - System.currentTimeMillis() < giorni * GIORNO;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getUtenza() {
		return utenza;
	}

	public void setUtenza(String utenza) {
		this.utenza = utenza;
	}

	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return nome + " (" + utenza + ") => " + data;
	}
}
