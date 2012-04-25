package org.krakenapps.confdb.file;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.json.JSONException;
import org.json.JSONWriter;
import org.krakenapps.codec.Base64;
import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Exporter {
	private final Logger logger = LoggerFactory.getLogger(Exporter.class.getName());
	private FileConfigDatabase db;

	public Exporter(FileConfigDatabase db) {
		this.db = db;
	}

	public void exportData(OutputStream os) {
		logger.debug("kraken confdb: start export data");
		Comparator<String> reverseOrder = new Comparator<String>() {
			@Override
			public int compare(String s1, String s2) {

				return s2.compareTo(s1);
			}
		};

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		StringWriter writer = new StringWriter(10240);
		JSONWriter jsonWriter = new JSONWriter(writer);
		Map<String, Object> m = new TreeMap<String, Object>(reverseOrder);
		Map<String, Object> metadata = new TreeMap<String, Object>();
		Map<String, Object> cols = new TreeMap<String, Object>();

		metadata.put("version", 1);
		metadata.put("date", sdf.format(new Date()));

		m.put("metadata", metadata);

		for (String name : db.getCollectionNames()) {
			List<Object> docs = new ArrayList<Object>();
			ConfigCollection col = db.getCollection(name);

			ConfigIterator it = col.findAll();
			try {
				while (it.hasNext()) {
					Object doc = it.next().getDocument();
					docs.add(doc);
				}
			} finally {
				it.close();
			}
			cols.put(name, insertType(docs));
		}

		m.put("collections", cols);

		try {
			jsonWriter.object();

			for (String key : m.keySet())
				jsonWriter.key(key).value(m.get(key));

			jsonWriter.endObject();

		} catch (JSONException e) {
			logger.error("kraken confdb: cannot serialize json", e);
		}

		logger.debug("kraken confdb: export complete");
		writeJsonString(writer.toString(), os);
	}

	private void writeJsonString(String json, OutputStream os) {
		OutputStreamWriter writer = null;
		try {
			writer = new OutputStreamWriter(os, "utf-8");
			writer.write(json);
		} catch (IOException e) {
			logger.error("frodo xtmconf: cannot write json string", e);
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
			}
		}
	}

	private Object insertType(Object doc) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		if (doc instanceof String) {
			return createList("string", doc);
		} else if (doc instanceof Integer) {
			return createList("int", doc);
		} else if (doc instanceof Boolean) {
			return createList("bool", doc);
		} else if (doc instanceof Long) {
			return createList("long", doc);
		} else if (doc instanceof Inet4Address) {
			return createList("ip4", ((Inet4Address) doc).getHostAddress());
		} else if (doc instanceof Inet6Address) {
			return createList("ip6", ((Inet6Address) doc).getHostAddress());
		} else if (doc instanceof Double) {
			return createList("double", doc);
		} else if (doc instanceof Float) {
			return createList("float", doc);
		} else if (doc instanceof Date) {
			Date docDate = (Date) doc;
			return createList("date", sdf.format(docDate));
		} else if (doc instanceof Short) {
			return createList("short", doc);
		} else if (doc == null) {
			return createList("null", doc);
		} else if (doc instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> m = (Map<String, Object>) doc;

			for (String name : m.keySet()) {
				m.put(name, insertType(m.get(name)));
			}

			return createList("map", m);
		} else if (doc instanceof List) {
			@SuppressWarnings("unchecked")
			List<Object> l = (List<Object>) doc;
			List<Object> newList = new ArrayList<Object>();

			for (Object o : l) {
				newList.add(insertType(o));
			}

			return createList("list", newList);
		} else if (doc.getClass().isArray()) {
			if (doc.getClass().getName().equals("[B")) {
				byte[] bs = (byte[]) doc;
				return createList("blob", Base64.encodeString(new String(bs)));

			} else {
				Object[] os = (Object[]) doc;
				Object[] newOs = new Object[os.length];
				for (int index = 0; index < os.length; index++) {
					newOs[index] = insertType(os[index]);
				}

				return createList("list", newOs);
			}
		} else {
			throw new IllegalArgumentException("unsupported type [" + doc.getClass().getName() + "]");
		}
	}

	private List<Object> createList(String type, Object doc) {
		List<Object> l = new ArrayList<Object>();
		l.add(type);
		l.add(doc);

		return l;
	}
}
