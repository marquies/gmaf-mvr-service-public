package de.swa.gmaf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;

import de.swa.vo.RecordVO;

public class CaptureReceiver extends Thread {
	public CaptureReceiver() {
		start();
	}

	public void run() {
		try {
			ServerSocket srv = new ServerSocket(8133);
			while (true) {
				try {
					Socket s = srv.accept();
					InputStream sin = s.getInputStream();
					ObjectInputStream oin = new ObjectInputStream(sin);

					SecretKey key = (SecretKey) readFromFile("secretkey.dat");
					SealedObject sealedObject = (SealedObject) oin.readObject();
					String algorithmName = sealedObject.getAlgorithm();
					Cipher cipher = Cipher.getInstance(algorithmName);
					cipher.init(Cipher.DECRYPT_MODE, key);
					RecordVO vo = (RecordVO) sealedObject.getObject(cipher);

//					RecordVO vo = (RecordVO) oin.readObject();
					String user = vo.getUser();
					String device = vo.getDevice();
					long timestamp = vo.getTimestamp();

					File f = new File("temp/" + user + "_" + device + "_" + timestamp + ".png");
					FileOutputStream fout = new FileOutputStream(f);
					fout.write(vo.getImageData());
					fout.close();
					System.out.println("image written...");
					s.close();

					GMAF gmaf = new GMAF();
					gmaf.processAsset(f);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static Object readFromFile(String filename) throws Exception {
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(filename)));
		Object object = ois.readObject();
		ois.close();
		return object;
	}

	public static void main(String[] args) {
		new CaptureReceiver();
	}
}
