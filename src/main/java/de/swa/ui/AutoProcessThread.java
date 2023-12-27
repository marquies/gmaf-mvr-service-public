package de.swa.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;

import de.swa.gc.GraphCode;
import de.swa.gc.GraphCodeGenerator;
import de.swa.gc.GraphCodeIO;
import de.swa.gmaf.GMAF;
import de.swa.mmfg.MMFG;
import de.swa.mmfg.builder.FeatureVectorBuilder;
import de.swa.mmfg.builder.XMLEncodeDecode;

/** this thread is used as background processing of GMAF assets **/
public class AutoProcessThread extends Thread {
	public void run() {
		while (true) {
			try {
				System.out.println("auto processing started");
				
				for (MMFG m : MMFGCollectionFactory.createOrGetCollection().getCollection()) {
					String fileName = m.getGeneralMetadata().getFileName();
					File existingMMFG = new File(Configuration.getInstance().getMMFGRepo() + File.separatorChar + fileName + ".mmfg");
					if (!existingMMFG.exists()) {
						GMAF gmaf = new GMAF();
						try {
							File f = m.getGeneralMetadata().getFileReference();
							FileInputStream fs = new FileInputStream(f);
							byte[] bytes = fs.readAllBytes();
							MMFG fv = gmaf.processAsset(bytes, f.getName(), "system", Configuration.getInstance().getMaxRecursions(),	Configuration.getInstance().getMaxNodes(), f.getName(), f);
							System.out.println("MMFG created");
							
							String xml = FeatureVectorBuilder.flatten(fv, new XMLEncodeDecode());
							RandomAccessFile rf = new RandomAccessFile(Configuration.getInstance().getMMFGRepo() + File.separatorChar + f.getName() + ".mmfg", "rw");
							rf.setLength(0);
							rf.writeBytes(xml);
							rf.close();
							
							System.out.println("MMFG exported to " + Configuration.getInstance().getMMFGRepo());
							
							GraphCode gc = GraphCodeGenerator.generate(fv);
							GraphCodeIO.write(gc, new File(Configuration.getInstance().getGraphCodeRepository() + File.separatorChar + f.getName() + ".gc"));
							
							System.out.println("GraphCode exported to " + Configuration.getInstance().getGraphCodeRepository());
							MMFGCollectionFactory.createOrGetCollection().addToCollection(fv);
							MMFGCollectionFactory.createOrGetCollection().refresh();
						}
						catch (Exception x) {
							x.printStackTrace();
							System.out.println("error " + x.getMessage());
						}
					}
				}
				
				System.out.println("auto processing finished");
				System.out.println("next run in 5 minutes");
				Thread.sleep(1000 * 60 * 2);
			}
			catch (Exception x) {}
		}
	}
}
