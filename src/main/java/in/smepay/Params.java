package in.smepay;

/** Shared checks for request builders. */
final class Params {
  private Params() {}

  static boolean blank(String value) {
    return value == null || value.isBlank();
  }

  static void require(String value, String name) {
    if (blank(value)) {
      throw new SMEPayException("invalid_request", name + " is required");
    }
  }
}
