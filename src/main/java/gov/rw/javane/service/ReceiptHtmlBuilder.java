package gov.rw.javane.service;

import gov.rw.javane.domain.entity.Bill;
import gov.rw.javane.domain.enums.TariffType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@Component
public class ReceiptHtmlBuilder {

    private static final DecimalFormat AMOUNT_FORMAT = new DecimalFormat("#,##0.##",
            DecimalFormatSymbols.getInstance(Locale.US));

    public String buildPaymentReceipt(Bill bill, BigDecimal amountPaid, BigDecimal outstandingBalance) {
        String period = formatBillingPeriod(bill);
        String customerName = escapeHtml(bill.getCustomer().getFullName());
        String meterNumber = bill.getMeter() != null ? escapeHtml(bill.getMeter().getMeterNumber()) : "—";
        BigDecimal consumption = bill.getConsumption();
        BigDecimal consumptionAmount = bill.getConsumptionAmount();
        BigDecimal fixedCharge = bill.getFixedCharge();
        BigDecimal taxAmount = bill.getTaxAmount();
        BigDecimal totalAmount = bill.getTotalAmount();
        BigDecimal vatRate = bill.getTariffVersion() != null
                ? bill.getTariffVersion().getVatRate()
                : BigDecimal.ZERO;
        boolean fullyPaid = outstandingBalance.compareTo(BigDecimal.ZERO) == 0;
        String balanceDisplay = fullyPaid ? "0 FRW" : formatAmount(outstandingBalance) + " FRW";
        String balanceColor = fullyPaid ? "#16a34a" : "#dc2626";
        String footerMessage = fullyPaid
                ? "Thank you for your payment. This receipt confirms your bill has been fully settled."
                : "Thank you for your payment. Please settle the remaining balance before the due date.";

        String step1Detail = buildStep1Detail(bill, consumption, consumptionAmount);

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8"/>
                  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
                  <title>Payment Receipt</title>
                </head>
                <body style="margin:0;padding:0;background:#f4f6f8;font-family:Segoe UI,Arial,sans-serif;color:#1a1a2e;">
                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background:#f4f6f8;padding:24px 12px;">
                    <tr>
                      <td align="center">
                        <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="max-width:560px;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);">
                          <tr>
                            <td style="background:linear-gradient(135deg,#0d47a1,#1565c0);padding:28px 32px;text-align:center;">
                              <div style="font-size:22px;font-weight:700;color:#ffffff;letter-spacing:0.5px;">WASAC / REG</div>
                              <div style="font-size:13px;color:#bbdefb;margin-top:4px;">Utility Billing — Payment Receipt</div>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:28px 32px;">
                              <p style="margin:0 0 16px;font-size:15px;color:#475569;">Dear <strong>%s</strong>,</p>
                              <p style="margin:0 0 4px;font-size:13px;color:#64748b;text-transform:uppercase;letter-spacing:1px;">Customer</p>
                              <p style="margin:0 0 16px;font-size:18px;font-weight:600;">%s</p>
                              <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="margin-bottom:24px;">
                                <tr>
                                  <td style="padding:8px 0;border-bottom:1px solid #e2e8f0;font-size:14px;color:#64748b;">Billing period</td>
                                  <td style="padding:8px 0;border-bottom:1px solid #e2e8f0;font-size:14px;text-align:right;font-weight:600;">%s</td>
                                </tr>
                                <tr>
                                  <td style="padding:8px 0;border-bottom:1px solid #e2e8f0;font-size:14px;color:#64748b;">Meter</td>
                                  <td style="padding:8px 0;border-bottom:1px solid #e2e8f0;font-size:14px;text-align:right;font-weight:600;">%s</td>
                                </tr>
                                <tr>
                                  <td style="padding:8px 0;font-size:14px;color:#64748b;">Units consumed</td>
                                  <td style="padding:8px 0;font-size:14px;text-align:right;font-weight:600;">%s</td>
                                </tr>
                              </table>
                              <div style="background:#f8fafc;border-radius:8px;padding:20px;margin-bottom:24px;">
                                <p style="margin:0 0 12px;font-size:13px;font-weight:700;color:#0d47a1;text-transform:uppercase;letter-spacing:0.5px;">Bill calculation</p>
                                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0">
                                  <tr>
                                    <td style="padding:6px 0;font-size:14px;color:#475569;">Step 1 — Subtotal</td>
                                    <td style="padding:6px 0;font-size:14px;text-align:right;color:#475569;">%s</td>
                                  </tr>
                                  <tr>
                                    <td style="padding:6px 0;font-size:14px;color:#475569;">Step 2 — VAT (%s%%)</td>
                                    <td style="padding:6px 0;font-size:14px;text-align:right;color:#475569;">%s FRW</td>
                                  </tr>
                                  <tr>
                                    <td style="padding:6px 0;font-size:14px;color:#475569;">Step 3 — Service charge</td>
                                    <td style="padding:6px 0;font-size:14px;text-align:right;color:#475569;">%s FRW</td>
                                  </tr>
                                </table>
                              </div>
                              <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="margin-bottom:24px;">
                                <tr>
                                  <td style="padding:12px 0;border-top:2px solid #0d47a1;border-bottom:2px solid #0d47a1;font-size:16px;font-weight:700;">Total bill</td>
                                  <td style="padding:12px 0;border-top:2px solid #0d47a1;border-bottom:2px solid #0d47a1;font-size:16px;font-weight:700;text-align:right;">%s FRW</td>
                                </tr>
                                <tr>
                                  <td style="padding:10px 0;font-size:14px;color:#16a34a;font-weight:600;">Amount paid</td>
                                  <td style="padding:10px 0;font-size:14px;color:#16a34a;font-weight:600;text-align:right;">%s FRW</td>
                                </tr>
                                <tr>
                                  <td style="padding:10px 0;font-size:14px;color:#64748b;">Outstanding balance</td>
                                  <td style="padding:10px 0;font-size:14px;font-weight:600;text-align:right;color:%s;">%s</td>
                                </tr>
                              </table>
                              <p style="margin:0;font-size:13px;color:#64748b;text-align:center;">%s</p>
                            </td>
                          </tr>
                          <tr>
                            <td style="background:#f1f5f9;padding:16px 32px;text-align:center;font-size:12px;color:#94a3b8;">
                              WASAC / REG Utility Billing System
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(
                customerName,
                customerName,
                period,
                meterNumber,
                formatAmount(consumption),
                step1Detail,
                formatAmount(vatRate),
                formatAmount(taxAmount),
                formatAmount(fixedCharge),
                formatAmount(totalAmount),
                formatAmount(amountPaid),
                balanceColor,
                balanceDisplay,
                footerMessage
        );
    }

    private String buildStep1Detail(Bill bill, BigDecimal consumption, BigDecimal consumptionAmount) {
        if (bill.getTariffVersion() != null
                && bill.getTariffVersion().getTariffType() == TariffType.FLAT
                && bill.getTariffVersion().getFlatRate() != null) {
            return "%s units × %s FRW = %s FRW".formatted(
                    formatAmount(consumption),
                    formatAmount(bill.getTariffVersion().getFlatRate()),
                    formatAmount(consumptionAmount));
        }
        return "%s units — consumption charge: %s FRW".formatted(
                formatAmount(consumption),
                formatAmount(consumptionAmount));
    }

    private String formatBillingPeriod(Bill bill) {
        return String.format("%02d/%d", bill.getBillingMonth(), bill.getBillingYear());
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return "0";
        }
        return AMOUNT_FORMAT.format(amount.stripTrailingZeros());
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
