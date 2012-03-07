/*
 * Copyright 2011 Future Systems, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.krakenapps.confdb.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.krakenapps.confdb.CollectionEntry;
import org.krakenapps.confdb.CommitOp;
import org.krakenapps.confdb.ConfigChange;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigEntry;
import org.krakenapps.confdb.ConfigTransaction;
import org.krakenapps.confdb.Manifest;

public class FileConfigTransaction implements ConfigTransaction {
	private FileConfigDatabase db;

	private Manifest manifest;

	private List<ConfigChange> changeSet;

	private File changeLogFile;
	private File changeDatFile;
	private File manifestLogFile;
	private File manifestDatFile;

	private Map<File, RevLogWriter> writers;

	public FileConfigTransaction(FileConfigDatabase db) {
		this.db = db;
		File dbDir = db.getDbDirectory();

		// TODO: apply changeset rev
		manifest = db.getManifest(null);
		changeSet = new ArrayList<ConfigChange>();

		changeLogFile = new File(dbDir, "changeset.log");
		changeDatFile = new File(dbDir, "changeset.dat");
		manifestLogFile = new File(dbDir, "manifest.log");
		manifestDatFile = new File(dbDir, "manifest.dat");

		writers = new HashMap<File, RevLogWriter>();
	}

	public Map<File, RevLogWriter> getWriters() {
		return writers;
	}

	@Override
	public Manifest getManifest() {
		return manifest;
	}

	@Override
	public ConfigDatabase getDatabase() {
		return db;
	}

	@Override
	public void begin() {
		db.lock();
	}

	@Override
	public void begin(int timeout) {
		db.lock(timeout);
	}

	/**
	 * log all transaction
	 */
	@Override
	public void log(CommitOp op, String colName, int docId, long rev, int index) {
		CollectionEntry col = null;

		if (op == CommitOp.CreateCol) {
			col = new CollectionEntry(db.nextCollectionId(), colName);
		} else
			col = manifest.getCollectionEntry(colName);

		ConfigEntry entry = new ConfigEntry(col.getId(), docId, rev, index);

		if (op == CommitOp.CreateDoc || op == CommitOp.UpdateDoc)
			manifest.add(entry);
		else if (op == CommitOp.DeleteDoc)
			manifest.remove(entry);
		else if (op == CommitOp.CreateCol)
			manifest.add(col);
		else if (op == CommitOp.DropCol)
			manifest.remove(col);

		changeSet.add(new ConfigChange(op, col.getName(), col.getId(), docId));
	}

	@Override
	public void commit(String committer, String log) {
		try {
			Manifest manifest = writeManifestLog();
			ChangeSetWriter.log(changeLogFile, changeDatFile, changeSet, manifest.getId(), committer, log);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		} finally {
			closeWriters();
			db.unlock();
		}
	}

	@Override
	public void rollback() {
		closeWriters();
		db.unlock();
	}

	private void closeWriters() {
		for (RevLogWriter writer : writers.values()) {
			writer.close();
		}

		writers.clear();
	}

	private Manifest writeManifestLog() throws IOException {
		RevLog log = new RevLog();
		log.setRev(1);
		log.setOperation(CommitOp.CreateDoc);
		log.setDoc(manifest.serialize());

		RevLogWriter writer = null;
		try {
			writer = new RevLogWriter(manifestLogFile, manifestDatFile);
			manifest.setId(writer.write(log));
			return manifest;
		} finally {
			if (writer != null)
				writer.close();
		}
	}

}