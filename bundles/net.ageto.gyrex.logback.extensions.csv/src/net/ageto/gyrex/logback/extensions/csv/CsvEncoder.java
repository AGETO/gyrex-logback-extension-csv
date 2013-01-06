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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVFormat.CSVFormatBuilder;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.Quote;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.EncoderBase;

/**
 * An encoder that outputs log events as CSV.
 */
public class CsvEncoder extends EncoderBase<ILoggingEvent> {

	/**
	 * The charset to use when converting a String into bytes.
	 * <p/>
	 * By default this property has the value
	 * <code>null</null> which corresponds to
	 * the UTF-8 charset.
	 */
	private Charset charset;

	/**
	 * If true, flushes after every event.
	 */
	private boolean immediateFlush = true;

	private String separator;
	private String encapsulator;
	private String newline;
	private String escape;

	private final List<String> fieldPatterns = new ArrayList<String>();

	private List<PatternLayout> fields;
	private CSVFormat csvFormat;
	private CSVPrinter csvPrinter;

	/**
	 * Adds a field.
	 * 
	 * @param fieldPattern
	 *            the {@link PatternLayout pattern} for generating the field
	 */
	public void addField(final String fieldPattern) {
		fieldPatterns.add(fieldPattern);
	}

	@Override
	public void close() throws IOException {
		if (csvPrinter != null) {
			csvPrinter.flush();
			csvPrinter.close();
			csvPrinter = null;
		}
	}

	@Override
	public void doEncode(final ILoggingEvent event) throws IOException {
		if ((csvPrinter == null) || (fields == null) || fields.isEmpty()) {
			return;
		}

		final List<String> values = new ArrayList<String>(fields.size());
		for (final PatternLayout field : fields) {
			values.add(field.doLayout(event));
		}

		csvPrinter.printRecord(values);
		if (isImmediateFlush()) {
			csvPrinter.flush();
		}
	}

	public String getEncapsulator() {
		return encapsulator;
	}

	public String getEscape() {
		return escape;
	}

	public String getNewline() {
		return newline;
	}

	public String getSeparator() {
		return separator;
	}

	@Override
	public void init(final OutputStream os) throws IOException {
		super.init(os);

		if ((outputStream != null) && (csvFormat != null)) {
			final PrintStream printStream = new PrintStream(outputStream, false, charset != null ? charset.name() : "UTF-8");
			csvPrinter = new CSVPrinter(printStream, csvFormat);
		}
	}

	public boolean isImmediateFlush() {
		return immediateFlush;
	}

	public void setCharset(final Charset charset) {
		this.charset = charset;
	}

	public void setEncapsulator(final String encapsulator) {
		this.encapsulator = encapsulator;
	}

	public void setEscape(final String escape) {
		this.escape = escape;
	}

	public void setImmediateFlush(final boolean immediateFlush) {
		this.immediateFlush = immediateFlush;
	}

	public void setNewline(final String newline) {
		this.newline = newline;
	}

	public void setSeparator(final String separator) {
		this.separator = separator;
	}

	@Override
	public void start() {
		if (fieldPatterns.isEmpty()) {
			addError("No fields defined!");
			return;
		}

		fields = new ArrayList<PatternLayout>(fieldPatterns.size());
		for (final String pattern : fieldPatterns) {
			final PatternLayout layout = new PatternLayout();
			layout.setContext(getContext());
			layout.setPattern(pattern);
			layout.start();
			fields.add(layout);
		}

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

		csvFormat = formatBuilder.build();

		super.start();
	}

	@Override
	public void stop() {
		super.stop();

		if (fields != null) {
			for (final PatternLayout field : fields) {
				field.stop();
			}
			fields = null;
		}
	}

}
