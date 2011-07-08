/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.logstorage.engine;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.krakenapps.logstorage.engine.v2.LogFileWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnlineWriter {
	private final Logger logger = LoggerFactory.getLogger(OnlineWriter.class.getName());

	private int tableId;
	private boolean closing;
	private Date day;
	private Date lastAccess = new Date();
	private AtomicInteger nextId;
	private LogFileWriter writer;

	public OnlineWriter(int tableId, Date day, int maxLogBuffering) throws IOException {
		this.tableId = tableId;
		this.day = day;
		File indexPath = DatapathUtil.getIndexFile(tableId, day);
		File dataPath = DatapathUtil.getDataFile(tableId, day);

		indexPath.getParentFile().mkdirs();
		dataPath.getParentFile().mkdirs();

		writer = new LogFileWriter(indexPath, dataPath, maxLogBuffering);
		nextId = new AtomicInteger(writer.getLastKey());
	}

	public boolean isOpen() {
		return writer != null && closing == false;
	}

	public boolean isClosed() {
		return closing == true && writer == null;
	}

	public int getTableId() {
		return tableId;
	}

	public Date getDay() {
		return day;
	}

	private int nextId() {
		// do NOT update last access here
		return nextId.incrementAndGet();
	}

	public Date getLastAccess() {
		return lastAccess;
	}

	public void write(LogRecord record) throws IOException {
		synchronized (this) {
			if (writer == null)
				throw new IOException("file closed");

			int nid = nextId();
			record.setId(nid);
			writer.write(record);
		}
	}

	public void write(Collection<LogRecord> logs) throws IOException {
		if (writer == null)
			throw new IllegalStateException("file closed");

		for (LogRecord record : logs) {
			record.setId(nextId());
		}

		synchronized (this) {
			writer.write(logs);
		}
	}

	public void flush() throws IOException {
		synchronized (this) {
			writer.flush();
			notifyAll();
		}
	}

	public void close() {
		if (closing)
			return;

		try {
			synchronized (this) {
				closing = true;
				flush();
				writer.close();
				notifyAll();
				writer = null;
			}

		} catch (IOException e) {
			logger.error("cannot close online log writer", e);
		}
	}
}