package in.smepay.internal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal JSON (RFC 8259) writer and reader, so the SDK needs no dependencies.
 *
 * <p>Reading gives {@code Map<String, Object>} (key order kept), {@code List<Object>}, {@code
 * String}, {@code BigDecimal}, {@code Boolean} or {@code null}.
 *
 * <p>Internal: not part of the public API; may change without notice.
 */
public final class Json {
  private static final int MAX_DEPTH = 256;

  private Json() {}

  /** Thrown when text is not valid JSON or a value cannot be written. */
  public static final class JsonException extends Exception {
    private static final long serialVersionUID = 1L;

    JsonException(String message) {
      super(message);
    }
  }

  // ---- writing ----

  /**
   * Encode a value: {@code Map} with string keys, {@code List}, {@code String}, {@code Number},
   * {@code Boolean} or {@code null}.
   */
  public static String write(Object value) throws JsonException {
    StringBuilder out = new StringBuilder();
    write(value, out, 0);
    return out.toString();
  }

  private static void write(Object value, StringBuilder out, int depth) throws JsonException {
    if (depth > MAX_DEPTH) {
      throw new JsonException("value is nested too deeply");
    }
    if (value == null) {
      out.append("null");
    } else if (value instanceof String) {
      writeString((String) value, out);
    } else if (value instanceof Boolean) {
      out.append(value);
    } else if (value instanceof BigDecimal) {
      out.append(((BigDecimal) value).toString());
    } else if (value instanceof Integer || value instanceof Long) {
      out.append(value);
    } else if (value instanceof Double || value instanceof Float) {
      double d = ((Number) value).doubleValue();
      if (Double.isNaN(d) || Double.isInfinite(d)) {
        throw new JsonException("NaN and Infinity are not valid JSON numbers");
      }
      out.append(BigDecimal.valueOf(d).toString());
    } else if (value instanceof Map) {
      out.append('{');
      boolean first = true;
      for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
        if (!(entry.getKey() instanceof String)) {
          throw new JsonException("object keys must be strings");
        }
        if (!first) {
          out.append(',');
        }
        first = false;
        writeString((String) entry.getKey(), out);
        out.append(':');
        write(entry.getValue(), out, depth + 1);
      }
      out.append('}');
    } else if (value instanceof List) {
      out.append('[');
      boolean first = true;
      for (Object item : (List<?>) value) {
        if (!first) {
          out.append(',');
        }
        first = false;
        write(item, out, depth + 1);
      }
      out.append(']');
    } else {
      throw new JsonException("cannot encode " + value.getClass().getName() + " as JSON");
    }
  }

  private static void writeString(String s, StringBuilder out) {
    out.append('"');
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '"':
          out.append("\\\"");
          break;
        case '\\':
          out.append("\\\\");
          break;
        case '\n':
          out.append("\\n");
          break;
        case '\r':
          out.append("\\r");
          break;
        case '\t':
          out.append("\\t");
          break;
        case '\b':
          out.append("\\b");
          break;
        case '\f':
          out.append("\\f");
          break;
        default:
          if (c < 0x20) {
            out.append(String.format("\\u%04x", (int) c));
          } else {
            out.append(c);
          }
      }
    }
    out.append('"');
  }

  // ---- reading ----

  /** Decode one JSON value; the whole text must be that value (whitespace allowed around it). */
  public static Object parse(String text) throws JsonException {
    Reader reader = new Reader(text);
    reader.skipWhitespace();
    Object value = reader.readValue(0);
    reader.skipWhitespace();
    if (reader.pos != text.length()) {
      throw reader.error("unexpected text after JSON value");
    }
    return value;
  }

  private static final class Reader {
    private final String s;
    private int pos;

    Reader(String s) {
      this.s = s;
    }

    JsonException error(String message) {
      return new JsonException(message + " at position " + pos);
    }

    void skipWhitespace() {
      while (pos < s.length()) {
        char c = s.charAt(pos);
        if (c != ' ' && c != '\t' && c != '\n' && c != '\r') {
          return;
        }
        pos++;
      }
    }

    Object readValue(int depth) throws JsonException {
      if (depth > MAX_DEPTH) {
        throw error("JSON is nested too deeply");
      }
      if (pos >= s.length()) {
        throw error("unexpected end of JSON");
      }
      char c = s.charAt(pos);
      switch (c) {
        case '{':
          return readObject(depth);
        case '[':
          return readArray(depth);
        case '"':
          return readString();
        case 't':
          expectWord("true");
          return true;
        case 'f':
          expectWord("false");
          return false;
        case 'n':
          expectWord("null");
          return null;
        default:
          if (c == '-' || (c >= '0' && c <= '9')) {
            return readNumber();
          }
          throw error("unexpected character '" + c + "'");
      }
    }

    private void expectWord(String word) throws JsonException {
      if (!s.startsWith(word, pos)) {
        throw error("invalid literal");
      }
      pos += word.length();
    }

    private Map<String, Object> readObject(int depth) throws JsonException {
      pos++; // {
      Map<String, Object> map = new LinkedHashMap<>();
      skipWhitespace();
      if (pos < s.length() && s.charAt(pos) == '}') {
        pos++;
        return Collections.unmodifiableMap(map);
      }
      while (true) {
        skipWhitespace();
        if (pos >= s.length() || s.charAt(pos) != '"') {
          throw error("expected object key");
        }
        String key = readString();
        skipWhitespace();
        if (pos >= s.length() || s.charAt(pos) != ':') {
          throw error("expected ':'");
        }
        pos++;
        skipWhitespace();
        map.put(key, readValue(depth + 1));
        skipWhitespace();
        if (pos >= s.length()) {
          throw error("unexpected end of JSON");
        }
        char c = s.charAt(pos++);
        if (c == '}') {
          return Collections.unmodifiableMap(map);
        }
        if (c != ',') {
          throw error("expected ',' or '}'");
        }
      }
    }

    private List<Object> readArray(int depth) throws JsonException {
      pos++; // [
      List<Object> list = new ArrayList<>();
      skipWhitespace();
      if (pos < s.length() && s.charAt(pos) == ']') {
        pos++;
        return Collections.unmodifiableList(list);
      }
      while (true) {
        skipWhitespace();
        list.add(readValue(depth + 1));
        skipWhitespace();
        if (pos >= s.length()) {
          throw error("unexpected end of JSON");
        }
        char c = s.charAt(pos++);
        if (c == ']') {
          return Collections.unmodifiableList(list);
        }
        if (c != ',') {
          throw error("expected ',' or ']'");
        }
      }
    }

    private String readString() throws JsonException {
      pos++; // opening quote
      StringBuilder out = new StringBuilder();
      while (true) {
        if (pos >= s.length()) {
          throw error("unterminated string");
        }
        char c = s.charAt(pos++);
        if (c == '"') {
          return out.toString();
        }
        if (c < 0x20) {
          throw error("control character in string");
        }
        if (c != '\\') {
          out.append(c);
          continue;
        }
        if (pos >= s.length()) {
          throw error("unterminated escape");
        }
        char e = s.charAt(pos++);
        switch (e) {
          case '"':
          case '\\':
          case '/':
            out.append(e);
            break;
          case 'b':
            out.append('\b');
            break;
          case 'f':
            out.append('\f');
            break;
          case 'n':
            out.append('\n');
            break;
          case 'r':
            out.append('\r');
            break;
          case 't':
            out.append('\t');
            break;
          case 'u':
            if (pos + 4 > s.length()) {
              throw error("bad \\u escape");
            }
            int code = 0;
            for (int i = 0; i < 4; i++) {
              int digit = Character.digit(s.charAt(pos++), 16);
              if (digit < 0) {
                throw error("bad \\u escape");
              }
              code = code * 16 + digit;
            }
            out.append((char) code);
            break;
          default:
            throw error("bad escape '\\" + e + "'");
        }
      }
    }

    private BigDecimal readNumber() throws JsonException {
      int start = pos;
      if (s.charAt(pos) == '-') {
        pos++;
      }
      if (pos >= s.length()) {
        throw error("bad number");
      }
      if (s.charAt(pos) == '0') {
        pos++;
      } else if (isDigit()) {
        while (isDigit()) {
          pos++;
        }
      } else {
        throw error("bad number");
      }
      if (pos < s.length() && s.charAt(pos) == '.') {
        pos++;
        if (!isDigit()) {
          throw error("bad number");
        }
        while (isDigit()) {
          pos++;
        }
      }
      if (pos < s.length() && (s.charAt(pos) == 'e' || s.charAt(pos) == 'E')) {
        pos++;
        if (pos < s.length() && (s.charAt(pos) == '+' || s.charAt(pos) == '-')) {
          pos++;
        }
        if (!isDigit()) {
          throw error("bad number");
        }
        while (isDigit()) {
          pos++;
        }
      }
      try {
        return new BigDecimal(s.substring(start, pos));
      } catch (NumberFormatException | ArithmeticException ex) {
        throw error("bad number");
      }
    }

    private boolean isDigit() {
      return pos < s.length() && s.charAt(pos) >= '0' && s.charAt(pos) <= '9';
    }
  }
}
