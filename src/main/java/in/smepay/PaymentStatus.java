package in.smepay;

/**
 * Values of {@code payment_status}. Replies keep it a {@code String}, so a new status from the API
 * never breaks your code; compare with these constants.
 */
public final class PaymentStatus {
  /** Order created; payment not started. */
  public static final String CREATED = "CREATED";

  /** Payment started (QR / UPI link issued). */
  public static final String INITIATED = "INITIATED";

  /** Payment in progress. */
  public static final String PENDING = "PENDING";

  /** Paid. */
  public static final String SUCCESS = "SUCCESS";

  /** Payment failed. */
  public static final String FAILED = "FAILED";

  /** Payment window expired. */
  public static final String EXPIRED = "EXPIRED";

  /** Paid and settled. */
  public static final String SETTLED = "SETTLED";

  /** Refunded. */
  public static final String REFUNDED = "REFUNDED";

  /** Refund in progress. */
  public static final String REFUND_PENDING = "REFUND_PENDING";

  /** Test-mode business: paid. */
  public static final String TEST_SUCCESS = "TEST_SUCCESS";

  /** Test-mode business: failed. */
  public static final String TEST_FAILED = "TEST_FAILED";

  /** Test-mode business: in progress. */
  public static final String TEST_PENDING = "TEST_PENDING";

  private PaymentStatus() {}
}
