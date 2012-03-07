package org.krakenapps.dom.api.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.api.PrimitiveParseCallback;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigIterator;
import org.krakenapps.confdb.ConfigParser;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.confdb.Predicate;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.dom.api.ConfigManager;
import org.krakenapps.dom.api.DOMException;
import org.krakenapps.dom.api.DefaultEntityEventProvider;
import org.krakenapps.dom.api.Transaction;

@Component(name = "dom-config-manager")
@Provides
public class ConfigManagerImpl implements ConfigManager {
	private static final String COMMITER = "kraken-dom";

	@Requires
	private ConfigService confsvc;

	private ConcurrentMap<Class<?>, ConfigParser> parsers = new ConcurrentHashMap<Class<?>, ConfigParser>();
	private ConcurrentMap<String, ParseCallback> callbacks = new ConcurrentHashMap<String, ParseCallback>();

	public void setConfigService(ConfigService conf) {
		this.confsvc = conf;
	}

	private PrimitiveParseCallback getCallback(String domain) {
		callbacks.putIfAbsent(domain, new ParseCallback(domain));
		return callbacks.get(domain);
	}

	@Override
	public void setParser(Class<?> cls, ConfigParser parser) {
		parsers.put(cls, parser);
	}

	@Override
	public ConfigDatabase findDatabase(String domain) {
		ConfigDatabase db = confsvc.getDatabase("kraken-dom-" + domain);
		return db;
	}

	@Override
	public ConfigDatabase getDatabase(String domain) {
		ConfigDatabase db = findDatabase(domain);
		if (db == null) {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("domain", domain);
			throw new DOMException("organization-not-found", m);
		}
		return db;
	}

	@Override
	public <T> Collection<T> all(String domain, Class<T> cls) {
		return all(domain, cls, null);
	}

	@Override
	public <T> Collection<T> all(String domain, Class<T> cls, Predicate pred) {
		ConfigIterator it = getDatabase(domain).ensureCollection(cls).find(pred);
		it.setParser(parsers.get(cls));
		return it.getDocuments(cls, getCallback(domain));
	}

	@Override
	public <T> T find(String domain, Class<T> cls, Predicate pred) {
		Config c = getDatabase(domain).ensureCollection(cls).findOne(pred);
		if (c == null)
			return null;
		return c.getDocument(cls, getCallback(domain));
	}

	@Override
	public <T> T get(String domain, Class<T> cls, Predicate pred, String notFoundMessage) {
		T t = find(domain, cls, pred);
		if (t == null)
			throw new DOMException(notFoundMessage);
		return t;
	}

	@Override
	public <T> void adds(String domain, Class<T> cls, List<Predicate> preds, List<T> docs, String alreadyExistMessage,
			DefaultEntityEventProvider<T> provider) {
		adds(domain, cls, preds, docs, alreadyExistMessage, provider, null);
	}

	@Override
	public <T> void adds(String domain, Class<T> cls, List<Predicate> preds, List<T> docs, String alreadyExistMessage,
			DefaultEntityEventProvider<T> provider, Object state) {
		ConfigDatabase db = getDatabase(domain);
		Transaction xact = new Transaction(domain, db);
		xact.addEventProvider(cls, provider);

		try {
			adds(xact, cls, preds, docs, alreadyExistMessage, state);
			xact.commit(COMMITER, "added " + docs.size() + " " + cls.getSimpleName() + "(s)");
		} catch (Throwable e) {
			if (e instanceof DOMException)
				throw (DOMException) e;
			xact.rollback();
		}
	}

	@Override
	public <T> void adds(Transaction xact, Class<T> cls, List<Predicate> preds, List<T> docs, String alreadyExistMessage) {
		adds(xact, cls, preds, docs, alreadyExistMessage, null);
	}

	@Override
	public <T> void adds(Transaction xact, Class<T> cls, List<Predicate> preds, List<T> docs,
			String alreadyExistMessage, Object state) {
		if (docs.isEmpty())
			return;

		if (preds.size() != docs.size())
			throw new IllegalArgumentException("preds and docs must has equal size");

		ConfigDatabase db = xact.getConfigDatabase();

		Predicate pred = Predicates.or(preds.toArray(new Predicate[0]));
		if (db.findOne(cls, pred) != null)
			throw new DOMException(alreadyExistMessage);

		Iterator<T> docIterator = docs.iterator();
		while (docIterator.hasNext())
			xact.add(docIterator.next(), state);
	}

	@Override
	public <T> void add(String domain, Class<T> cls, Predicate pred, T doc, String alreadyExistMessage,
			DefaultEntityEventProvider<T> provider) {
		add(domain, cls, pred, doc, alreadyExistMessage, provider, null);
	}

	@Override
	public <T> void add(String domain, Class<T> cls, Predicate pred, T doc, String alreadyExistMessage,
			DefaultEntityEventProvider<T> provider, Object state) {
		ConfigDatabase db = getDatabase(domain);
		Transaction xact = new Transaction(domain, db);
		xact.addEventProvider(cls, provider);

		try {
			add(xact, cls, pred, doc, alreadyExistMessage, state);
			xact.commit(COMMITER, "added 1 " + cls.getSimpleName());
		} catch (Throwable e) {
			xact.rollback();
			if (e instanceof DOMException)
				throw (DOMException) e;
			throw new RuntimeException(e);
		}
	}

	@Override
	public <T> void add(Transaction xact, Class<T> cls, Predicate pred, T doc, String alreadyExistMessage) {
		add(xact, cls, pred, doc, alreadyExistMessage, null);
	}

	@Override
	public <T> void add(Transaction xact, Class<T> cls, Predicate pred, T doc, String alreadyExistMessage, Object state) {
		ConfigDatabase db = xact.getConfigDatabase();

		if (db.findOne(cls, pred) != null)
			throw new DOMException(alreadyExistMessage);
		xact.add(doc, state);
	}
	
	@Override
	public <T> void updateForGuids(Transaction xact, Class<T> cls, List<String> guids, List<T> docs,
			String notFoundMessage) {
		updateForGuids(xact, cls, guids, docs, notFoundMessage, null);
	}
	
	@Override
	public <T> void updateForGuids(String domain, Class<T> cls, List<String> guids, List<T> docs,
			String notFoundMessage, DefaultEntityEventProvider<T> provider) {
		updateForGuids(domain, cls, guids, docs, notFoundMessage, provider, null);
	}

	@Override
	public <T> void updateForGuids(String domain, Class<T> cls, List<String> guids, List<T> docs,
			String notFoundMessage, DefaultEntityEventProvider<T> provider, Object state) {
		ConfigDatabase db = getDatabase(domain);
		Transaction xact = new Transaction(domain, db);
		xact.addEventProvider(cls, provider);

		try {
			updateForGuids(xact, cls, guids, docs, notFoundMessage, state);
			xact.commit(COMMITER, "updated " + docs.size() + " " + cls.getSimpleName() + "(s)");
		} catch (Throwable e) {
			xact.rollback();
			if (e instanceof DOMException)
				throw (DOMException) e;
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public <T> void updateForGuids(Transaction xact, Class<T> cls, List<String> guids, List<T> docs,
			String notFoundMessage, Object state) {
		if (docs.isEmpty())
			return;

		if (guids.size() != docs.size())
			throw new IllegalArgumentException("preds and docs must has equal size");

		ConfigDatabase db = xact.getConfigDatabase();

		ConfigIterator confIt = null;
		try {
			confIt = db.find(cls, in(guids));

			Iterator<T> docIterator = docs.iterator();
			Map<String, T> docMap = new HashMap<String, T>();
			for (String guid : guids) {
				T t = docIterator.next();
				docMap.put(guid, t);
			}

			while (confIt.hasNext()) {
				Config c = confIt.next();

				@SuppressWarnings("unchecked")
				Map<String, Object> config = (Map<String, Object>) c.getDocument();
				String configGuid = (String) config.get("guid");
				if (configGuid == null)
					throw new DOMException(notFoundMessage);

				T doc = docMap.get(configGuid);
				if (doc == null)
					throw new DOMException(notFoundMessage);

				xact.update(c, doc, state);
			}
		} finally {
			if (confIt != null) {
				confIt.close();
			}
		}
	}

	private Predicate in(List<String> guids) {
		Predicate[] preds = new Predicate[guids.size()];
		int i = 0;
		for (String guid : guids)
			preds[i++] = Predicates.field("guid", guid);
		return Predicates.or(preds);
	}

	@Override
	public <T> void updates(Transaction xact, Class<T> cls, List<Predicate> preds, List<T> docs, String notFoundMessage) {
		updates(xact, cls, preds, docs, notFoundMessage, null);
	}

	@Override
	public <T> void updates(String domain, Class<T> cls, List<Predicate> preds, List<T> docs, String notFoundMessage,
			DefaultEntityEventProvider<T> provider) {
		updates(domain, cls, preds, docs, notFoundMessage, provider, null);
	}

	@Override
	public <T> void updates(String domain, Class<T> cls, List<Predicate> preds, List<T> docs, String notFoundMessage,
			DefaultEntityEventProvider<T> provider, Object state) {
		ConfigDatabase db = getDatabase(domain);
		Transaction xact = new Transaction(domain, db);
		xact.addEventProvider(cls, provider);

		try {
			updates(xact, cls, preds, docs, notFoundMessage, state);
			xact.commit(COMMITER, "updated " + docs.size() + " " + cls.getSimpleName() + "(s)");
		} catch (Throwable e) {
			xact.rollback();
			if (e instanceof DOMException)
				throw (DOMException) e;
			throw new RuntimeException(e);
		}
	}

	@Deprecated
	@Override
	public <T> void updates(Transaction xact, Class<T> cls, List<Predicate> preds, List<T> docs,
			String notFoundMessage, Object state) {
		if (docs.isEmpty())
			return;

		if (preds.size() != docs.size())
			throw new IllegalArgumentException("preds and docs must has equal size");

		ConfigDatabase db = xact.getConfigDatabase();

		Predicate pred = Predicates.or(preds.toArray(new Predicate[0]));
		ConfigIterator confIt = null;
		try {
			confIt = db.find(cls, pred);

			Iterator<T> docIterator = docs.iterator();
			while (docIterator.hasNext()) {
				if (!confIt.hasNext())
					throw new DOMException(notFoundMessage);
				Config c = confIt.next();
				xact.update(c, docIterator.next(), state);
			}
		} finally {
			if (confIt != null) {
				confIt.close();
			}
		}
	}

	@Override
	public <T> void update(String domain, Class<T> cls, Predicate pred, T doc, String notFoundMessage,
			DefaultEntityEventProvider<T> provider) {
		update(domain, cls, pred, doc, notFoundMessage, provider, null);
	}

	@Override
	public <T> void update(String domain, Class<T> cls, Predicate pred, T doc, String notFoundMessage,
			DefaultEntityEventProvider<T> provider, Object state) {
		ConfigDatabase db = getDatabase(domain);
		Transaction xact = new Transaction(domain, db);
		xact.addEventProvider(cls, provider);

		try {
			update(xact, cls, pred, doc, notFoundMessage, state);
			xact.commit(COMMITER, "updated 1 " + cls.getSimpleName());
		} catch (Throwable e) {
			xact.rollback();
			if (e instanceof DOMException)
				throw (DOMException) e;
			throw new RuntimeException(e);
		}
	}

	@Override
	public <T> void update(Transaction xact, Class<T> cls, Predicate pred, T doc, String notFoundMessage) {
		update(xact, cls, pred, doc, notFoundMessage, null);
	}

	@Override
	public <T> void update(Transaction xact, Class<T> cls, Predicate pred, T doc, String notFoundMessage, Object state) {
		ConfigDatabase db = xact.getConfigDatabase();

		Config c = get(db, cls, pred, notFoundMessage);
		xact.update(c, doc, state);
	}

	@Override
	public <T> Collection<T> removes(String domain, Class<T> cls, List<Predicate> preds, String notFoundMessage,
			DefaultEntityEventProvider<T> provider) {
		return removes(domain, cls, preds, notFoundMessage, provider, null, null);
	}

	@Override
	public <T> Collection<T> removes(String domain, Class<T> cls, List<Predicate> preds, String notFoundMessage,
			DefaultEntityEventProvider<T> provider, Object removingState, Object removedState) {
		ConfigDatabase db = getDatabase(domain);
		Transaction xact = new Transaction(domain, db);

		Collection<T> docs = null;
		try {
			docs = removes(xact, domain, cls, preds, notFoundMessage, provider, removingState, removedState);
			xact.commit(COMMITER, "removed " + preds.size() + " " + cls.getSimpleName() + "(s)");
		} catch (Throwable e) {
			xact.rollback();
			if (e instanceof DOMException)
				throw (DOMException) e;
			throw new RuntimeException(e);
		}

		return docs;
	}

	@Override
	public <T> Collection<T> removes(Transaction xact, String domain, Class<T> cls, List<Predicate> preds,
			String notFoundMessage, DefaultEntityEventProvider<T> provider) {
		return removes(xact, domain, cls, preds, notFoundMessage, provider, null, null);
	}

	@Override
	public <T> Collection<T> removes(Transaction xact, String domain, Class<T> cls, List<Predicate> preds,
			String notFoundMessage, DefaultEntityEventProvider<T> provider, Object removingState, Object removedState) {
		if (preds.isEmpty())
			return new ArrayList<T>();

		ConfigDatabase db = xact.getConfigDatabase();
		xact.addEventProvider(cls, provider);

		Predicate pred = Predicates.or(preds.toArray(new Predicate[0]));
		if (db.findOne(cls, pred) == null)
			throw new DOMException(notFoundMessage);

		Collection<T> docs = new ArrayList<T>();
		ConfigIterator it = db.find(cls, pred);
		try {
			it.setParser(parsers.get(cls));
			while (it.hasNext()) {
				Config c = it.next();
				T doc = c.getDocument(cls, getCallback(domain));
				docs.add(doc);
				xact.remove(c, doc, removingState, removedState);
			}
		} finally {
			it.close();
		}

		return docs;
	}

	@Override
	public <T> T remove(String domain, Class<T> cls, Predicate pred, String notFoundMessage,
			DefaultEntityEventProvider<T> provider) {
		return remove(domain, cls, pred, notFoundMessage, provider, null, null);
	}

	@Override
	public <T> T remove(String domain, Class<T> cls, Predicate pred, String notFoundMessage,
			DefaultEntityEventProvider<T> provider, Object removingState, Object removedState) {
		ConfigDatabase db = getDatabase(domain);
		Transaction xact = new Transaction(domain, db);

		T doc = null;
		try {
			doc = remove(xact, domain, cls, pred, notFoundMessage, provider, removingState, removedState);
			xact.commit(COMMITER, "removed 1 " + cls.getSimpleName());
		} catch (Throwable e) {
			xact.rollback();
			if (e instanceof DOMException)
				throw (DOMException) e;
			throw new RuntimeException(e);
		}

		return doc;
	}

	@Override
	public <T> T remove(Transaction xact, String domain, Class<T> cls, Predicate pred, String notFoundMessage,
			DefaultEntityEventProvider<T> provider) {
		return remove(xact, domain, cls, pred, notFoundMessage, provider, null, null);
	}

	@Override
	public <T> T remove(Transaction xact, String domain, Class<T> cls, Predicate pred, String notFoundMessage,
			DefaultEntityEventProvider<T> provider, Object removingState, Object removedState) {
		ConfigDatabase db = xact.getConfigDatabase();
		xact.addEventProvider(cls, provider);

		Config c = get(db, cls, pred, notFoundMessage);
		T doc = c.getDocument(cls, getCallback(domain));
		xact.remove(c, doc, removingState, removedState);

		return doc;
	}

	private Config get(ConfigDatabase db, Class<?> cls, Predicate pred, String notFoundMessage) {
		Config c = db.findOne(cls, pred);
		if (c == null)
			throw new DOMException(notFoundMessage);
		return c;
	}

	@Override
	public PrimitiveParseCallback getParseCallback(String domain) {
		return getCallback(domain);
	}

	private class ParseCallback implements PrimitiveParseCallback {
		private String domain;

		public ParseCallback(String domain) {
			this.domain = domain;
		}

		@Override
		public <T> T onParse(Class<T> cls, Map<String, Object> referenceKey) {
			return find(domain, cls, Predicates.field(referenceKey));
		}
	}
}