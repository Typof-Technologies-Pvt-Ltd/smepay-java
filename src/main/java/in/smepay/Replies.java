package in.smepay;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Reads typed fields from a decoded reply; missing or wrong-typed fields give null. */
final class Replies {
  private Replies() {}

  static String string(Map<String, Object> reply, String key) {
    Object value = reply.get(key);
    return value instanceof String ? (String) value : null;
  }

  static boolean bool(Map<String, Object> reply, String key) {
    return Boolean.TRUE.equals(reply.get(key));
  }

  static BigDecimal decimal(Map<String, Object> reply, String key) {
    Object value = reply.get(key);
    if (value instanceof BigDecimal) {
      return (BigDecimal) value;
    }
    if (value instanceof String) {
      try {
        return new BigDecimal((String) value);
      } catch (NumberFormatException e) {
        return null;
      }
    }
    return null;
  }

  static Long epochSeconds(Map<String, Object> reply, String key) {
    Object value = reply.get(key);
    return value instanceof BigDecimal ? ((BigDecimal) value).longValue() : null;
  }

  static Map<String, String> stringMap(Map<String, Object> reply, String key) {
    Object value = reply.get(key);
    Map<String, String> out = new LinkedHashMap<>();
    if (value instanceof Map) {
      for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
        if (entry.getValue() instanceof String) {
          out.put(String.valueOf(entry.getKey()), (String) entry.getValue());
        }
      }
    }
    return Collections.unmodifiableMap(out);
  }
}
