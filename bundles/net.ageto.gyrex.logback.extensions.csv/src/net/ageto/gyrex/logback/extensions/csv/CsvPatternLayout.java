/**
 * Copyright (c) 2012 Gunnar Wagenknecht and others.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 */
package net.ageto.gyrex.logback.extensions.csv;

import java.lang.reflect.Field;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVFormat.CSVFormatBuilder;
import org.apache.commons.csv.Quote;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.Converter;

/**
 * Specialization of {@link PatternLayout} what ensures values are properly
 * escaped for CSV output.
 * <p>
 * The pattern is split into fields using {@link #getSeparator() a separator}.
 * Each field value
 * </p>
 */
public class CsvPatternLayout extends PatternLayout {

	String separator;
	String encapsulator;
	String newline;
	String escape;

	Converter<ILoggingEvent> head;

	public String getEncapsulator() {
		return encapsulator;
	}

	public String getSeparator() {
		return separator;
	}

	public void setEncapsulator(final String encapsulator) {
		this.encapsulator = encapsulator;
	}

	public void setSeparator(final String separator) {
		this.separator = separator;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void start() {
		final CSVFormatBuilder formatBuilder = CSVFormat.newBuilder();

		if (separator != null) {
			if (separator.length() != 1) {
				addError("Invalid separator:'" + separator + "'");
				return;
			}
			formatBuilder.withDelimiter(separator.charAt(0));
		}

		if (newline != null) {
			if (newline.length() == 0) {
				addError("Invalid newline:'" + newline + "'");
				return;
			}
			formatBuilder.withRecordSeparator(newline);
		}

		if (encapsulator != null) {
			if (encapsulator.length() != 1) {
				addError("Invalid encapsulator:'" + encapsulator + "'");
				return;
			}
			formatBuilder.withQuoteChar(encapsulator.charAt(0));
			formatBuilder.withQuotePolicy(Quote.MINIMAL);
		}

		if (escape != null) {
			if (escape.length() != 1) {
				addError("Invalid escape:'" + escape + "'");
				return;
			}
			formatBuilder.withEscape(escape.charAt(0));
			if (encapsulator == null) {
				formatBuilder.withQuotePolicy(Quote.NONE);
			}
		}

		super.start();

		try {
			final Field field = getClass().getDeclaredField("head");
			if (!field.isAccessible()) {
				field.setAccessible(true);
			}
			head = (Converter<ILoggingEvent>) field.get(this);
		} catch (final Exception e) {
			addError("Unable to initialize internal variable", e);
			stop();
			return;
		}

	}

	@Override
	protected String writeLoopOnConverters(final ILoggingEvent event) {
		final StringBuilder buf = new StringBuilder(128);
		Converter<ILoggingEvent> c = head;
		while (c != null) {
			c.write(buf, event);
			c = c.getNext();
		}
		return buf.toString();
	}
}
