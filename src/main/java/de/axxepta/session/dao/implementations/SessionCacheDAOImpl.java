package de.axxepta.session.dao.implementations;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.annotation.PreDestroy;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.CachingSessionDAO;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.axxepta.tools.SerializeObjectUtil;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.bindings.CompressedUnsignedLongArrayByteIterable;
import jetbrains.exodus.bindings.StringBinding;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Environments;
import jetbrains.exodus.env.Store;
import jetbrains.exodus.env.Transaction;
import jetbrains.exodus.env.TransactionalExecutable;

import static jetbrains.exodus.env.StoreConfig.WITHOUT_DUPLICATES;

public class SessionCacheDAOImpl extends CachingSessionDAO {

	public static final String STORE_SESSIONS_PATH_DB = System.getProperty("user.dir") + File.separator + "shiro-res"
			+ File.separator + ".store-sessions";

	private final Environment env;

	private final Store storeSession;

	private static final Logger LOG = LoggerFactory.getLogger(SessionCacheDAOImpl.class);

	private static final String KEY_ENCRYPT = "Argon Server KEY";

	private Session session;

	public SessionCacheDAOImpl() {
		env = Environments.newInstance(STORE_SESSIONS_PATH_DB);
		storeSession = env.computeInTransaction(txn -> env.openStore("Sessions", WITHOUT_DUPLICATES, txn));
		LOG.info("Create session DAO object");
	}

	@Override
	protected void doUpdate(Session session) {
		Serializable sessionId = generateSessionId(session);
		assignSessionId(session, sessionId);
		@NotNull
		final ByteIterable key = StringBinding.stringToEntry(sessionId.toString());
		try {
			@NotNull
			final ByteIterable value = StringBinding
					.stringToEntry(new String(SerializeObjectUtil.serialize(session, KEY_ENCRYPT)));
			env.executeInTransaction((txn) -> {
				if (storeSession.get(txn, key) == null)
					return;
				storeSession.put(txn, key, value);
			});
			LOG.info("Update session with id " + session.getId());
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException | IOException e) {
			LOG.error(e.getMessage());
		}
	}

	@Override
	
	protected void doDelete(Session session) {
		Serializable sessionId = generateSessionId(session);
		@NotNull
		final ByteIterable key = StringBinding.stringToEntry(sessionId.toString());

		env.executeInTransaction((txn) -> {
			boolean isDel = storeSession.delete(txn, key);
			if (!isDel) {
				LOG.error("Session with id " + sessionId + " cannot be deleted");
			}
		});
		LOG.info("Delete session with id " + session.getId());
	}

	@Override
	protected Serializable doCreate(Session session) {
		Serializable sessionId = generateSessionId(session);
		assignSessionId(session, sessionId);
		@NotNull
		final ByteIterable key = StringBinding.stringToEntry(sessionId.toString());
		try {
			@NotNull
			final ByteIterable value = StringBinding
					.stringToEntry(new String(SerializeObjectUtil.serialize(session, KEY_ENCRYPT)));
			env.executeInTransaction((txn) -> {
				storeSession.put(txn, key, value);
			});
			LOG.info("Create session with id " + session.getId());
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException | IOException e) {
			LOG.error(e.getMessage());
			return null;
		}
		return session.getId();
	}

	@Override
	protected Session doReadSession(Serializable sessionId) {
		@NotNull
		final ByteIterable key = StringBinding.stringToEntry(sessionId.toString());
		env.executeInTransaction(new TransactionalExecutable() {
			@Override
			public void execute(@NotNull final Transaction txn) {
				final ByteIterable entry = storeSession.get(txn, key);
				
				if(entry == null)
					LOG.error("Entry is null");
				
				byte[] array = CompressedUnsignedLongArrayByteIterable.readIterator(entry.iterator(),
						entry.getLength());

				try {
					session = (Session) SerializeObjectUtil.deserialize(array, KEY_ENCRYPT);
					LOG.info("Session with id " + sessionId + " was read");
				} catch (InvalidKeyException | ClassNotFoundException | NoSuchAlgorithmException
						| NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | IOException e) {
					LOG.error(e.getMessage());
				}
			}
		});

		return session;
	}

	@PreDestroy
	private void close() {
		env.close();
	}
}
