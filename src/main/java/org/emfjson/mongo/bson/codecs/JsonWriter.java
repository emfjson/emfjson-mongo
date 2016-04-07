package org.emfjson.mongo.bson.codecs;

import static javax.xml.bind.DatatypeConverter.printBase64Binary;

import java.io.IOException;
import java.io.Writer;

import org.bson.BSONException;
import org.bson.BsonBinary;
import org.bson.BsonContextType;
import org.bson.BsonDbPointer;
import org.bson.BsonRegularExpression;
import org.bson.BsonTimestamp;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import org.bson.types.ObjectId;

public class JsonWriter extends org.bson.json.JsonWriter {

	private final JsonWriterSettings settings;

	public JsonWriter(Writer writer) {
		this(writer, new JsonWriterSettings());
	}

	public JsonWriter(final Writer writer, final JsonWriterSettings settings) {
		super(writer, settings);
		this.settings = settings;
		if(settings.getOutputMode().equals(JsonMode.SHELL))
			throw new IllegalArgumentException("JsonMode must not be SHELL");
		setContext(new Context(null, BsonContextType.TOP_LEVEL, ""));
	}

	@Override
	protected void doWriteStartDocument() {
		try {
			if (getState() == State.VALUE || getState() == State.SCOPE_DOCUMENT) {
				writeNameHelper(getName());
			}
			getWriter().write("{");

			BsonContextType contextType = (getState() == State.SCOPE_DOCUMENT) ? BsonContextType.SCOPE_DOCUMENT
					: BsonContextType.DOCUMENT;
			setContext(new Context(getContext(), contextType, settings.getIndentCharacters()));
		} catch (IOException e) {
			throwBSONException(e);
		}
	}

	@Override
	protected void doWriteStartArray() {
		try {
			writeNameHelper(getName());
			getWriter().write("[");
			setContext(new Context(getContext(), BsonContextType.ARRAY, settings.getIndentCharacters()));
		} catch (IOException e) {
			throwBSONException(e);
		}
	}

	@Override
	protected void doWriteBinaryData(final BsonBinary binary) {
		writeStartDocument();
		writeString("$binary", printBase64Binary(binary.getData()));
		writeString("$type", Integer.toHexString(binary.getType() & 0xFF));
		writeEndDocument();
	}

	@Override
	public void doWriteBoolean(final boolean value) {
		try {
			writeNameHelper(getName());
			getWriter().write(value ? "true" : "false");
		} catch (IOException e) {
			throwBSONException(e);
		}
	}

	@Override
	protected void doWriteDateTime(final long value) {
		try {
			writeNameHelper(getName());
			getWriter().write(Long.toString(value));
		} catch (IOException e) {
			throwBSONException(e);
		}
	}

	@Override
	protected void doWriteDBPointer(final BsonDbPointer value) {
		writeStartDocument();
		writeString("$ref", value.getNamespace());
		writeObjectId("$id", value.getId());
		writeEndDocument();
	}

	@Override
	protected void doWriteDouble(final double value) {
		try {
			writeNameHelper(getName());
			getWriter().write(Double.toString(value));
			setState(getNextState());
		} catch (IOException e) {
			throwBSONException(e);
		}
	}

	@Override
	protected void doWriteInt32(final int value) {
		try {
			writeNameHelper(getName());
			getWriter().write(Integer.toString(value));
		} catch (IOException e) {
			throwBSONException(e);
		}
	}

	@Override
	protected void doWriteInt64(final long value) {
		try {
			writeNameHelper(getName());
			getWriter().write(Long.toString(value));
		} catch (IOException e) {
			throwBSONException(e);
		}
	}

	@Override
	protected void doWriteJavaScript(final String code) {
		writeStartDocument();
		writeString("$code", code);
		writeEndDocument();
	}

	@Override
	protected void doWriteJavaScriptWithScope(final String code) {
		writeStartDocument();
		writeString("$code", code);
		writeName("$scope");
	}

	@Override
	protected void doWriteMaxKey() {
		writeStartDocument();
		writeInt32("$maxKey", 1);
		writeEndDocument();
	}

	@Override
	protected void doWriteMinKey() {
		writeStartDocument();
		writeInt32("$minKey", 1);
		writeEndDocument();
	}

	@Override
	public void doWriteNull() {
		try {
			writeNameHelper(getName());
			getWriter().write("null");
		} catch (IOException e) {
			throwBSONException(e);
		}
	}

	@Override
	public void doWriteObjectId(final ObjectId objectId) {
		writeStartDocument();
		writeString("$oid", objectId.toString());
		writeEndDocument();
	}

	@Override
	public void doWriteRegularExpression(final BsonRegularExpression regularExpression) {
		try {
			writeNameHelper(getName());
			getWriter().write("/");
			String escaped = (regularExpression.getPattern().equals("")) ? "(?:)"
					: regularExpression.getPattern().replace("/", "\\/");
			getWriter().write(escaped);
			getWriter().write("/");
			getWriter().write(regularExpression.getOptions());
		} catch (IOException e) {
			throwBSONException(e);
		}
	}

	@Override
	public void doWriteString(final String value) {
		try {
			writeNameHelper(getName());
			writeStringHelper(value);
		} catch (IOException e) {
			throwBSONException(e);
		}
	}

	@Override
	public void doWriteSymbol(final String value) {
		writeStartDocument();
		writeString("$symbol", value);
		writeEndDocument();
	}

	@Override
	public void doWriteTimestamp(final BsonTimestamp value) {
			writeStartDocument();
			writeStartDocument("$timestamp");
			writeInt32("t", value.getTime());
			writeInt32("i", value.getInc());
			writeEndDocument();
			writeEndDocument();
	}

	@Override
	public void doWriteUndefined() {
		try {
			writeNameHelper(getName());
			getWriter().write("undefined");
		} catch (IOException e) {
			throwBSONException(e);
		}
	}

	private void throwBSONException(final IOException e) {
		throw new BSONException("Wrapping IOException", e);
	}

	private void writeNameHelper(final String name) throws IOException {
		switch (getContext().getContextType()) {
		case ARRAY:
			// don't write Array element names in JSON
			if (getContext().hasElements()) {
				getWriter().write(", ");
			}
			break;
		case DOCUMENT:
		case SCOPE_DOCUMENT:
			if (getContext().hasElements()) {
				getWriter().write(",");
			}
			if (settings.isIndent()) {
				getWriter().write(settings.getNewLineCharacters());
				getWriter().write(getContext().getIndentation());
			} else {
				getWriter().write(" ");
			}
			writeStringHelper(name);
			getWriter().write(" : ");
			break;
		case TOP_LEVEL:
			break;
		default:
			throw new BSONException("Invalid contextType.");
		}

		getContext().hasElements(true);
	}

	private void writeStringHelper(final String str) throws IOException {
		getWriter().write('"');
		for (final char c : str.toCharArray()) {
			switch (c) {
			case '"':
				getWriter().write("\\\"");
				break;
			case '\\':
				getWriter().write("\\\\");
				break;
			case '\b':
				getWriter().write("\\b");
				break;
			case '\f':
				getWriter().write("\\f");
				break;
			case '\n':
				getWriter().write("\\n");
				break;
			case '\r':
				getWriter().write("\\r");
				break;
			case '\t':
				getWriter().write("\\t");
				break;
			default:
				switch (Character.getType(c)) {
				case Character.UPPERCASE_LETTER:
				case Character.LOWERCASE_LETTER:
				case Character.TITLECASE_LETTER:
				case Character.OTHER_LETTER:
				case Character.DECIMAL_DIGIT_NUMBER:
				case Character.LETTER_NUMBER:
				case Character.OTHER_NUMBER:
				case Character.SPACE_SEPARATOR:
				case Character.CONNECTOR_PUNCTUATION:
				case Character.DASH_PUNCTUATION:
				case Character.START_PUNCTUATION:
				case Character.END_PUNCTUATION:
				case Character.INITIAL_QUOTE_PUNCTUATION:
				case Character.FINAL_QUOTE_PUNCTUATION:
				case Character.OTHER_PUNCTUATION:
				case Character.MATH_SYMBOL:
				case Character.CURRENCY_SYMBOL:
				case Character.MODIFIER_SYMBOL:
				case Character.OTHER_SYMBOL:
					getWriter().write(c);
					break;
				default:
					getWriter().write("\\u");
					getWriter().write(Integer.toHexString((c & 0xf000) >> 12));
					getWriter().write(Integer.toHexString((c & 0x0f00) >> 8));
					getWriter().write(Integer.toHexString((c & 0x00f0) >> 4));
					getWriter().write(Integer.toHexString(c & 0x000f));
					break;
				}
				break;
			}
		}
		getWriter().write('"');
	}

	@Override
	protected Context getContext() {
		return (Context) super.getContext();
	}

	/**
	 * The context for the writer, inheriting all the values from
	 * {@link org.bson.AbstractBsonWriter.Context}, and additionally providing
	 * settings for the indentation level and whether there are any child
	 * elements at this level.
	 */
	public class Context extends org.bson.json.JsonWriter.Context {
		private final String indentation;

		public String getIndentation() {
			return indentation;
		}

		private boolean hasElements;

		public boolean hasElements() {
			return hasElements;
		}

		public void hasElements(boolean hasElements) {
			this.hasElements = hasElements;
		}

		/**
		 * Creates a new context.
		 *
		 * @param parentContext
		 *            the parent context that can be used for going back up to
		 *            the parent level
		 * @param contextType
		 *            the type of this context
		 * @param indentChars
		 *            the String to use for indentation at this level.
		 */
		public Context(final Context parentContext, final BsonContextType contextType, final String indentChars) {
			super(parentContext, contextType, indentChars);
			this.indentation = (parentContext == null) ? indentChars : parentContext.indentation + indentChars;
		}

	}
}
