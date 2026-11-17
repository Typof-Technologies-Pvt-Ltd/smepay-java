import in.smepay.CreateParams;
import in.smepay.CreateResponse;
import in.smepay.CustomerDetails;
import in.smepay.Environment;
import in.smepay.GetStatusParams;
import in.smepay.GetStatusResponse;
import in.smepay.InitiateResponse;
import in.smepay.SMEPay;
import in.smepay.SMEPayException;
import java.time.Instant;
import java.util.TreeSet;

/**
 * Full flow: create → initiate → getStatus.
 *
 * <p>Credentials come from environment variables (keep them in a .env file, never commit it):
 *
 * <pre>
 * SMEPAY_CLIENT_ID=...
 * SMEPAY_CLIENT_SECRET=...
 * SMEPAY_ENVIRONMENT=test          # optional: "test" (default) or "live"
 * SMEPAY_CALLBACK_URL=https://...  # optional: where the result webhook is sent
 * </pre>
 *
 * <p>Run from the repo root (Java 11+):
 *
 * <pre>
 * mvn -q compile
 * set -a; source .env; set +a
 * java -cp target/classes examples/FullFlow.java
 * </pre>
 */
public class FullFlow {
  public static void main(String[] args) {
    String callbackUrl = System.getenv("SMEPAY_CALLBACK_URL");
    try {
      SMEPay client =
          SMEPay.builder()
              .clientId(System.getenv("SMEPAY_CLIENT_ID"))
              .clientSecret(System.getenv("SMEPAY_CLIENT_SECRET"))
              .environment(
                  "live".equals(System.getenv("SMEPAY_ENVIRONMENT"))
                      ? Environment.LIVE
                      : Environment.TEST)
              .build();
      System.out.println("Environment: " + client.getEnvironment());

      // 1. Create the order. orderId is your own reference and must be unique.
      CreateResponse order =
          client
              .payment()
              .create(
                  CreateParams.builder()
                      .amount("1.00")
                      .orderId("DEMO-" + System.currentTimeMillis())
                      .callbackUrl(
                          callbackUrl != null && !callbackUrl.isEmpty()
                              ? callbackUrl
                              : "https://example.com/smepay/callback")
                      .customerDetails(
                          CustomerDetails.builder()
                              .name("Demo Customer")
                              .mobile("9999999999")
                              .build())
                      .build());
      System.out.println("\n1. create");
      System.out.println("   order_id (SMEPay): " + order.getOrderId());
      System.out.println("   ref_id (yours):    " + order.getRefId());
      System.out.println("   order_slug:        " + order.getOrderSlug());
      System.out.println("   payment_status:    " + order.getPaymentStatus());
      System.out.println("   payment_url:       " + order.getPaymentUrl());

      // 2. Start the payment: show the QR code or UPI app links to the customer.
      InitiateResponse payment = client.payment().initiate(order.getOrderSlug());
      System.out.println("\n2. initiate");
      System.out.println("   payment_status:    " + payment.getPaymentStatus());
      System.out.println("   payment_link:      " + payment.getPaymentLink());
      System.out.println(
          "   qr_code:           base64, " + payment.getQrCode().length() + " chars");
      System.out.println(
          "   intents:           "
              + String.join(", ", new TreeSet<>(payment.getIntents().keySet())));
      System.out.println("   expires_at:        " + Instant.ofEpochSecond(payment.getExpiresAt()));

      // 3. Check the status by your reference (or by SMEPay's orderId).
      GetStatusResponse status =
          client
              .payment()
              .getStatus(GetStatusParams.builder().externalReferenceId(order.getRefId()).build());
      System.out.println("\n3. getStatus");
      System.out.println("   payment_status:    " + status.getPaymentStatus());
      System.out.println("   amount:            " + status.getAmount());
      Long processedAt = status.getProcessedAt();
      System.out.println(
          "   processed_at:      "
              + (processedAt != null ? Instant.ofEpochSecond(processedAt) : "(not yet)"));
      System.out.println(
          "   utr:               " + (status.getUtr() != null ? status.getUtr() : "(none yet)"));
    } catch (SMEPayException e) {
      System.out.println(
          "\nSMEPayException ["
              + e.getCode()
              + "] (HTTP "
              + (e.getStatusCode() != null ? e.getStatusCode() : "-")
              + "): "
              + e.getMessage());
      System.exit(1);
    }
  }
}
