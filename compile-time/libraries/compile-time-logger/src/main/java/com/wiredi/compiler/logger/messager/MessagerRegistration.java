package com.wiredi.compiler.logger.messager;

import com.wiredi.lang.DataAccess;

import javax.annotation.processing.Messager;
import java.util.ArrayList;
import java.util.List;

public class MessagerRegistration {

	private static final List<MessagerAware> listeners = new ArrayList<>();
	private static final DataAccess dataAccess = new DataAccess();

	public static boolean register(MessagerAware messagerAware) {
		return dataAccess.writeValue(() -> listeners.add(messagerAware));
	}

	public static boolean unregister(MessagerAware messagerAware) {
		return dataAccess.writeValue(() -> listeners.remove(messagerAware));
	}

	public static void announce(Messager messager) {
		dataAccess.readValue(() -> new ArrayList<>(listeners)).forEach(it -> it.setMessager(messager));
	}
}
