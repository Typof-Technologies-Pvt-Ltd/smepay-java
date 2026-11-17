# smepay-java

Official Java SDK for the SMEPay External API. Accept UPI payments in three calls; login, tokens and errors are handled for you.

Java 11+ · zero dependencies · thread-safe.

```xml
<dependency>
  <groupId>in.smepay</groupId>
  <artifactId>smepay-java</artifactId>
  <version>0.1.0</version>
</dependency>
```

Gradle: `implementation("in.smepay:smepay-java:0.1.0")`

## Usage

```java
import in.smepay.*;

SMEPay client = SMEPay.builder()
    .clientId(System.getenv("SMEPAY_CLIENT_ID")) // from the SMEPay dashboard
    .clientSecret(System.getenv("SMEPAY_CLIENT_SECRET"))
    .environment(Environment.TEST) // TEST (default) or LIVE
    .build();

// 1. Create an order
CreateResponse order = client.payment().create(CreateParams.builder()
    .amount("100.50") // string, in rupees
    .orderId("ORD-1001") // your reference, unique per order
    .callbackUrl("https://yoursite.com/smepay/callback")
    .customerDetails(CustomerDetails.builder().name("Asha").mobile("9876543210").build()) // email or mobile required
    .build());

// 2. Start the payment: show payment.getQrCode() (base64 PNG) or payment.getIntents() (UPI app links)
InitiateResponse payment = client.payment().initiate(order.getOrderSlug());

// 3. Check the status (or use .orderId(order.getOrderId()))
GetStatusResponse status = client.payment().getStatus(
    GetStatusParams.builder().externalReferenceId("ORD-1001").build());
if (PaymentStatus.SUCCESS.equals(status.getPaymentStatus())) {
    System.out.println(status.getUtr()); // "123456789012"
}
```

Create one client and reuse it (it's thread-safe). Getters map to the API's fields; `getRaw()` gives the full reply. Each request waits at most 60 seconds.

| Method | Returns |
|---|---|
| `payment().create` | `getOrderSlug()`, `getOrderId()` (SMEPay's), `getRefId()` (yours), `getPaymentUrl()`, `getPaymentStatus()` |
| `payment().initiate` | `getPaymentLink()`, `getQrCode()`, `getIntents()`, `getExpiresAt()`, `getPaymentStatus()` |
| `payment().getStatus` | `getPaymentStatus()`, `getAmount()`, `getCreatedAt()`, `getProcessedAt()` (`null` until processed), `getUtr()` (`null` until available) |

`initiate` can be called again if a payment failed or expired. Alternatively, skip `initiate` and redirect the customer to `getPaymentUrl()` (the hosted page); use one or the other per order.

**Statuses** (`PaymentStatus` constants): `CREATED` → `INITIATED` / `PENDING` → `SUCCESS` or `FAILED` / `EXPIRED`; also `SETTLED`, `REFUNDED`, `REFUND_PENDING`. Test-mode businesses get `TEST_SUCCESS`, `TEST_FAILED`, `TEST_PENDING`.

## Errors

Every failure throws the unchecked `in.smepay.SMEPayException` with `getCode()`, `getMessage()`, `getStatusCode()` (`null` if no reply) and `getRaw()` (API reply).

```java
try {
    client.payment().create(params);
} catch (SMEPayException e) {
    if ("duplicate_order_id".equals(e.getCode())) {
        // ...
    }
}
```

| Code | Meaning |
|---|---|
| `invalid_config` | Missing `clientId` / `clientSecret` / `environment` |
| `invalid_client_id`, `invalid_credentials` | Wrong credentials |
| `environment_mismatch` | `environment` doesn't match your business's mode |
| `ip_not_whitelisted` | Live: your server IP isn't whitelisted |
| `insufficient_wallet_balance`, `wallet_validation_failed` | Wallet balance too low |
| `invalid_request`, `validation_error` | Missing or invalid field |
| `amount_below_minimum`, `amount_exceeds_maximum` | Amount outside your allowed range |
| `duplicate_order_id` | `orderId` already used |
| `transaction_not_found` | No such order for your business |
| `payment_already_processed` | `initiate` on an order already paid / in progress |
| `network_error`, `timeout`, `invalid_response` | Couldn't reach SMEPay / no reply in 60s / unreadable reply |

Calls are never retried automatically, so a failed `create` is never sent twice.

## Test vs live

| | `TEST` | `LIVE` |
|---|---|---|
| Server | `staging.smepay.in` | `extranet.smepay.in` |
| Money / fees | None | Real; fees from your SMEPay wallet |
| Requirements | — | Server IP whitelisted, wallet ≥ ₹10 |

## Webhooks

When a payment finishes, SMEPay POSTs `ref_id`, `transaction_id`, `status`, `amount`, `created_at`, `processed_at` and `utr` to your `callbackUrl`. Confirm with `payment().getStatus` before marking an order paid.

## Security

Use the SDK only on your server. Keep `clientSecret` in an environment variable, never in code. The SDK never logs your secret or token, and `toString()` doesn't show them.

## Kotlin

`in` is a Kotlin keyword, so escape it in imports: ``import `in`.smepay.SMEPay``.

## Example

[`examples/FullFlow.java`](https://github.com/mahi-qwe/smepay-java/blob/main/examples/FullFlow.java): put `SMEPAY_CLIENT_ID` / `SMEPAY_CLIENT_SECRET` in `.env`, then `mvn -q compile; set -a; source .env; set +a; java -cp target/classes examples/FullFlow.java`.

## License

MIT
