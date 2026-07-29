
# Mission

## Problem

A burger restaurant sells products that are assembled from shared raw ingredients. Every order silently
consumes stock across several products at once — a beef burger and a chicken burger both draw down the
same cheese and onion. Without automatic tracking, the merchant only discovers an ingredient has run out
when an order cannot be fulfilled, and the customer only finds out after the sale has been taken.

The delivered service solves that for one anonymous restaurant. Three gaps remain in the way of running it
for real: anyone can call it, so orders cannot be attributed and one restaurant's stock is not walled off
from another's; the low-stock alert goes to a single hard-coded address rather than to the supplier who
would actually restock the ingredient; and a customer has no way to read a menu before ordering from it, so
they can only discover an unavailable product by being rejected.

## Mission

Provide an order-taking service that atomically decrements ingredient stock as orders are placed, rejects
any order that would exceed available stock, and alerts the merchant by email the first time an ingredient
falls below half of its original amount — early enough to restock before it runs out.

Extend it into a service several restaurants can share: every caller authenticated and acting only within
their own role and their own data, low-stock alerts routed to the supplier responsible for each ingredient,
and a menu customers can read — with availability derived from live stock — before they commit to an order.

## Stakeholders

| Stakeholder | What they need | How success is measured |
| --- | --- | --- |
| Merchant / restaurant owner | To know an ingredient is running low while there is still time to reorder, and to never sell what they cannot make | Exactly one alert email per ingredient per depletion cycle, sent the moment stock crosses below 50%; stock recorded in the database always matches what was actually consumed |
| Customer placing the order | An order that is either accepted in full or rejected immediately with a clear reason | `POST /order` returns `200` for a fulfillable order; returns `400` with "Insufficient ingredients for your order" otherwise, with no stock consumed |
| Customer choosing what to order | To see what a restaurant sells, at what price, and what is actually available right now | The menu endpoint lists the restaurant's products with prices, and an item shown as available is one the order endpoint will accept |
| Supplier | To be told about their own ingredients running low, and only their own | Each supplier receives one email per order listing the ingredients they supply that crossed the threshold, and never sees another supplier's ingredients or another restaurant's data |
| Restaurant owner as an account holder | Confidence that a competitor sharing the service cannot read or alter their menu, their stock, or their orders | Every owner- and supplier-scoped call is checked against the authenticated principal's own records; a valid token for the wrong restaurant is refused with `403` |

## Scope

**In scope**

- A single `POST /order` endpoint accepting a list of products and quantities.
- Ingredient stock depletion, tracked as a cumulative `consumedAmountInGrams` per ingredient.
- Rejection of any order that would push an ingredient past its total available amount.
- A one-time email alert when an ingredient crosses the 50%-consumed threshold.
- Persistence of accepted orders and their line items.

**Planned, in scope** *(roadmap phases 7–13)*

- A versioned, migration-managed schema, so data survives a restart and schema changes are reviewable.
- Restaurants that own their products and prices, suppliers that own their ingredients, and user accounts
  in three roles: customer, restaurant owner, supplier.
- Authentication by JWT, and authorization by role *and* by ownership — a role decides what a caller may
  do, the ownership link decides whose data they may do it to.
- Low-stock alerts addressed to the supplier of the depleted ingredient, grouped so each supplier is told
  only about their own.
- A menu endpoint returning a restaurant's products with prices and live availability.

**Out of scope**

- Product and ingredient CRUD endpoints. The catalogue is still seeded rather than managed through the
  API; migrations take over that seeding from `DatabaseInitializer`.
- Restocking or replenishment — stock only ever decreases. The alert tells a supplier to act outside the
  system.
- Self-service registration, password reset, refresh tokens, and token revocation. Accounts are seeded and
  tokens are short-lived.
- Payments, order status transitions, cancellation, and refunds.
- A user interface of any kind.

## Success criteria

- An order for products whose ingredients are all available is persisted, and every affected ingredient's
  consumed amount increases by `quantity × amountInGrams`.
- An order that would take any ingredient over 100% consumed is rejected with HTTP 400 and leaves all
  ingredient stock unchanged — the whole order fails, never part of it.
- The alert email names every ingredient that crossed 50% consumed on that order, and no further email is
  sent for an ingredient already past that threshold.
- Email delivery never blocks or fails the order response.

**Planned**

- A request without a valid token is refused with `401`; a valid token whose role or owner is wrong is
  refused with `403`, and the two are never conflated.
- An order depleting ingredients from two suppliers produces exactly two emails, each listing only that
  supplier's ingredients.
- The menu endpoint's availability flag agrees with the order endpoint's decision: nothing shown as
  available is rejected for insufficient stock at the same moment.
- A fresh database is built entirely by migrations, and the application refuses to start if the entities
  and the migrated schema disagree.
