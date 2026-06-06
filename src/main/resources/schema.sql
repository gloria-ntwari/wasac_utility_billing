-- Database routines: payment balance/status updates (notifications handled in Java + email).
-- Statements end with ;; because spring.sql.init.separator=;;

-- Hibernate ddl-auto creates bills_status_check without new enum values; refresh it on startup.
ALTER TABLE bills DROP CONSTRAINT IF EXISTS bills_status_check;;

ALTER TABLE bills ADD CONSTRAINT bills_status_check
  CHECK (status IN ('PENDING_APPROVAL', 'APPROVED', 'PARTIALLY_PAID', 'PAID', 'REJECTED'));;

CREATE OR REPLACE FUNCTION fn_after_payment_update_bill()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
  paid_sum numeric;
  total numeric;
  balance numeric;
BEGIN
  SELECT coalesce(sum(p.amount_paid), 0) INTO paid_sum
  FROM payments p
  WHERE p.bill_id = NEW.bill_id;

  SELECT b.total_amount INTO total
  FROM bills b
  WHERE b.id = NEW.bill_id;

  balance := greatest(total - paid_sum, 0);

  UPDATE bills
  SET outstanding_balance = balance,
      status = CASE
        WHEN balance = 0 THEN 'PAID'
        WHEN balance > 0 THEN 'PARTIALLY_PAID'
        ELSE status
      END,
      updated_at = now()
  WHERE id = NEW.bill_id;

  RETURN NEW;
END;
$$;;

DROP TRIGGER IF EXISTS trg_bill_insert_notification ON bills;;

DROP FUNCTION IF EXISTS fn_insert_bill_notification();;

DROP TRIGGER IF EXISTS trg_payment_after_insert_update_bill ON payments;;

CREATE TRIGGER trg_payment_after_insert_update_bill
AFTER INSERT ON payments
FOR EACH ROW
EXECUTE FUNCTION fn_after_payment_update_bill();;
