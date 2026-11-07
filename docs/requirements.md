# Review Buddy Requirements

This specification derives the MVP use cases from [PRD.md](PRD.md), with the [mockups](../mockup/index.html) informing the flow descriptions and proposed details below. The PRD and explicitly confirmed UC-001 decisions below define the scope. Payments, featured listings, advanced moderation, and a mobile app are excluded. Mockup features absent from the PRD are recorded as proposals rather than mandatory acceptance criteria.

All newly derived use cases start as `NOT_IMPLEMENTED`; this is a planning status, not an assessment of repository code. The allowed statuses are `NOT_IMPLEMENTED`, `IN_PROGRESS`, and `COMPLETED`.

## Use Case Summary

| UseCase ID | Use Case Title | Status |
| --- | --- | --- |
| UC-001 | Submit a Product for Public Feedback | COMPLETED |
| UC-002 | Browse Public Products | COMPLETED |
| UC-003 | View a Product for Review | COMPLETED |
| UC-004 | Leave a Structured Product Review | COMPLETED |
| UC-005 | Read a Product's Reviews | COMPLETED |
| UC-006 | Generate a Product's AI Review Summary | COMPLETED |
| UC-007 | Read a Product's AI Review Summary | NOT_IMPLEMENTED |

## Detailed Use Cases

### UC-001 — Submit a Product for Public Feedback

- **Status:** COMPLETED
- **Depends On:** None
- **Description:** A maker submits a product so that other makers and early adopters can discover it and provide public feedback. Submission requires login, uses the mockup fields and limits, and opens a public product page after success, as confirmed for UC-001.
- **Acceptance Criteria:**
  - A maker can submit a product to Review Buddy for feedback.
  - A successfully submitted product is publicly visible.
  - Public visibility applies to visitors as well as participating makers and reviewers.
  - A maker who has submitted a product can submit another product; submission is not a one-time capability.
  - Submitting a product does not require payment or purchasing a featured listing.
  - Only a signed-in user can access the submission form or submit a product; the maker is taken from the signed-in account.
  - Submission requires a nonblank product name (maximum 60 characters), tagline (100), description (500), website URL, and an existing category.
  - An image URL is optional; when provided, it must be a valid HTTP or HTTPS URL, as must the website URL.
  - Invalid submissions show field errors and retain the entered values without creating a product.
  - A successful submission opens the new product's public page, accessible without login.

### UC-002 — Browse Public Products

- **Status:** COMPLETED
- **Depends On:** UC-001
- **Description:** Any visitor browses the products submitted for public feedback and selects a product to learn more. The home and products mockups illustrate entry points to this public discovery flow. Confirmed for UC-002: listing links open the existing public product page; review display remains in UC-004/UC-005. Browse newest first without fabricated ratings or unsupported sorting choices, preserving existing search, category filtering, and pagination.
- **Acceptance Criteria:**
  - A visitor can browse publicly available products without being a product maker or reviewer.
  - Public browsing is accessible without authentication.
  - Products successfully submitted for public feedback are available through the browsing experience.
  - A visitor can select a listed product to access that product's public information and feedback.

### UC-003 — View a Product for Review

- **Status:** COMPLETED
- **Depends On:** UC-001
- **Description:** A visitor, maker, or reviewer opens a particular public product to identify the product and access its feedback. The product mockup groups product information, individual reviews, and an AI summary into one detail experience.
- **Acceptance Criteria:**
  - A visitor can view a submitted product without authentication.
  - The product being viewed is identifiable so that feedback can be associated with that product.
  - A product remains publicly viewable before it receives reviews.
  - A maker can view their submitted product through the public product experience.

### UC-004 — Leave a Structured Product Review

- **Status:** COMPLETED
- **Depends On:** UC-003
- **Description:** A reviewer, including a maker reviewing someone else's product, submits feedback in the two areas defined by the PRD: what they liked and what could be improved. The review mockup illustrates the two separate text inputs and a return to the product after posting. Confirmed for UC-004: login is required; self-reviews and repeat reviews are prohibited using account identity. Both feedback fields must be nonblank and at most 400 characters. Validation errors retain input; success returns to the product page. Numeric ratings are excluded. Existing demo products are linked to dedicated maker accounts.
- **Acceptance Criteria:**
  - A reviewer can leave feedback for a submitted product.
  - The review captures what the reviewer liked as a distinct feedback area.
  - The review captures what could be improved as a distinct feedback area.
  - Both feedback areas remain distinguishable after the review is submitted.
  - The submitted review is associated with the product being reviewed.
  - A maker who submits products can also review another maker's product.
  - Reviews from multiple reviewers can be associated with the same product.
  - Only signed-in users can open the review form or submit; reviewer identity comes from the signed-in account.
  - A maker cannot review their own product, and an account can review a product at most once, including concurrent submissions.
  - Both feedback fields must be nonblank and at most 400 characters; invalid input is retained with field errors without storing a review.
  - Successful submission returns to the reviewed product's public page; numeric ratings are not collected.

### UC-005 — Read a Product's Reviews

- **Status:** COMPLETED
- **Depends On:** UC-004
- **Description:** Any visitor reads the individual reviews of a public product, allowing makers to inspect actionable feedback and reviewers or early adopters to understand other people's experiences. The product mockup illustrates separate “Liked” and “Can be improved” sections for each review. Confirmed for UC-005: display only feedback fields, with no reviewer identity or submission date; order reviews newest first, breaking timestamp ties by descending review ID.
- **Acceptance Criteria:**
  - A visitor can read a public product's submitted reviews without authentication.
  - Each review presents its own liked feedback separately from its improvement feedback.
  - Individual reviews remain available rather than being replaced by an aggregate summary.
  - The reviews shown for a product belong to that product.
  - A maker can read the reviews of their own submitted product through the public experience.
  - Each review displays only its feedback fields, without reviewer identity or submission date.
  - Reviews appear newest first, with descending review ID breaking equal-timestamp ties.

### UC-006 — Generate a Product's AI Review Summary

- **Status:** COMPLETED
- **Depends On:** UC-004
- **Description:** Review Buddy generates an AI summary of a product's reviews to help the maker understand the overall feedback. The mockup illustrates themes grouped into likes and improvements; its hard-coded sample summaries are presentation examples, not an implementation of this capability.
- **Acceptance Criteria:**
  - Review Buddy can generate a summary using AI for an individual product's reviews.
  - The summary is based on that product's reviews rather than reviews of other products.
  - All reviews for the product are included in the feedback considered for its summary.
  - The summary conveys overall feedback about what reviewers liked.
  - The summary conveys overall feedback about what reviewers thought could be improved.
  - The generated summary remains associated with the product whose reviews were summarized.
  - After each committed review, generation runs automatically in the background when the product has at least three reviews, using Spring AI with OpenAI.
  - Invalid structured output is retried twice (three total attempts) by StructuredOutputValidationAdvisor; exhaustion or provider failure preserves any previous summary.
  - A summary based on fewer reviews cannot overwrite one based on more reviews.

### UC-007 — Read a Product's AI Review Summary

- **Status:** NOT_IMPLEMENTED
- **Depends On:** UC-003, UC-006
- **Description:** A visitor or maker reads the AI summary alongside access to the underlying product reviews, gaining an overview of the feedback. The product mockup illustrates a clearly labeled AI summary with separate positive and improvement themes.
- **Acceptance Criteria:**
  - A product's generated AI summary is available in that product's public feedback experience.
  - Reading the summary does not require authentication.
  - The experience identifies the summary as AI-generated feedback.
  - The summary displayed belongs to the product being viewed.
  - A visitor reading the summary can also access the individual reviews for that product.

## Mockup Details and Product Decisions

The user confirmed login-required submission, the mockup submission fields and limits, and a public page after submission for UC-001. Those decisions are captured in UC-001 above; other proposals below remain unconfirmed. These details were considered when deriving the use cases. Except for the confirmed UC-001 details, they are proposals from the mockups rather than additional committed scope or acceptance criteria. The PRD's other open questions are not resolved solely by prototype behavior. If approved, independently implementable additions such as accounts, search, ratings, and profiles should receive their own use cases.

| Area | Mockup evidence and proposed detail | Decision still needed |
| --- | --- | --- |
| Product submission | [submit.html](../mockup/submit.html) requires product name (maximum 60 characters), tagline (100), website URL, category, and description (500). It accepts an optional image URL, assigns the maker and submission date, and opens the submitted product. | Confirmed for UC-001: these fields and limits, optional image URL, selection from existing categories, and navigation to the public product page. Website and supplied image URLs use HTTP or HTTPS. |
| Accounts | [signup.html](../mockup/signup.html) collects a required name (maximum 40 characters), email, and password (minimum eight characters), then returns to the home page. [login.html](../mockup/login.html) collects email and password. | Submission requires login for UC-001. Review form access and submission require login for UC-004. Other account-flow changes remain unconfirmed. |
| Authorization and return navigation | [app.js](../mockup/app.js) redirects signed-out users attempting submission, review, or profile access to login, preserves the requested destination, and restores it after login. Public browsing remains open. | Submission form and posting require login for UC-001. Review form access and posting require login for UC-004. Profile protection and changes to account return navigation remain unconfirmed. |
| Product cards and details | [app.js](../mockup/app.js) and [product.html](../mockup/product.html) show name, tagline, category, preview image, rating, and review count; details add description, maker, date, and an external website link. | Confirm required public metadata and whether author identity and dates are public. The PRD does not define these fields or additional privacy rules. |
| Search and categories | [products.html](../mockup/products.html) supports trimmed, case-insensitive substring search across name, tagline, and description, combined with an exact category filter and an “All categories” option. [data.js](../mockup/data.js) lists Developer Tools, Productivity, Finance, Education, Design, and Lifestyle. | Confirm whether search and categories belong in MVP scope and whether the category list is fixed. |
| Discovery ordering | [products.html](../mockup/products.html) defaults to top rated and also offers most reviewed and newest. It shows a matching-product count and a no-results message. [index.html](../mockup/index.html) highlights three reviewed products by average rating, breaking equal averages by review count, and links to category results. | Confirm ratings, home-page rankings, ordering, tie behavior, and empty discovery states. Public browsing in the PRD does not prescribe a ranking. |
| Review validation and posting | [review.html](../mockup/review.html) requires both feedback fields, each with a 400-character maximum, and returns to the product after posting. | Confirmed for UC-004: both fields are nonblank with a 400-character maximum; invalid input is retained with field errors and no review created; success returns to the product page. |
| Ratings | [review.html](../mockup/review.html) offers integer ratings from 0 through 5 with 5 initially selected. [app.js](../mockup/app.js) calculates an arithmetic average; [product.html](../mockup/product.html) shows it to one decimal place, a review count, and counts for each rating. | Numeric ratings and aggregate statistics are absent from the PRD. Confirm their inclusion, default selection, rounding, and behavior for unrated products before making them requirements. |
| Review presentation | [product.html](../mockup/product.html) and [app.js](../mockup/app.js) show author, date, rating, and both feedback areas; product reviews appear in descending date order. | Confirmed for UC-005: show only the two feedback fields, without reviewer identity or dates; newest first with descending review ID breaking timestamp ties. |
| Products without reviews | [product.html](../mockup/product.html) displays a dash instead of an average, a zero review count, and a “No reviews yet” invitation. | The PRD explicitly leaves the no-review presentation open; confirm this proposed state. |
| Summary eligibility | [product.html](../mockup/product.html), [about.html](../mockup/about.html), and [app.js](../mockup/app.js) propose an unavailable-summary message until at least two reviews exist, with separate liked and improvement lists when a summary is available. | Confirmed for UC-006: automatically generate in the background after each committed review, starting at three reviews, using Spring AI with OpenAI. Retry invalid structured output twice and preserve any previous summary after exhaustion or provider failure. Public presentation and pending states remain in UC-007. |
| Personal activity and logout | [profile.html](../mockup/profile.html) proposes a protected profile showing name, email, submission and review counts, “My products,” “My reviews,” links to the relevant products, empty-state actions, and logout returning home. | Profiles and logout are not specified in the PRD. Confirm account and personal-activity scope and ownership rules. Matching display names in demo data is not an approved identity rule. |
| Informational and missing-page flows | [about.html](../mockup/about.html) explains the feedback loop; [404.html](../mockup/404.html) links home. Missing products show a browse link in [product.html](../mockup/product.html), while [review.html](../mockup/review.html) redirects to products. | Confirm informational-page scope and missing-product behavior before treating these flows as requirements. |

## Remaining PRD Questions and Specification Limits

- **Reviewer eligibility:** Confirmed for UC-004: self-reviews are prohibited using linked maker account identity.
- **Repeat reviews:** Confirmed for UC-004: at most one review per account per product.
- **Review lifecycle:** Editing, deleting, and maker responses to reviews remain open questions. No update, deletion, or response use cases are committed here.
- **Abuse prevention:** MVP spam and abusive-review controls are undecided. Advanced moderation remains excluded.
- **Summary fidelity:** Accurate representation is a stated risk, but the PRD supplies no measurable quality threshold or evaluation method. Confirmed for UC-006: refresh after each committed review once three reviews exist; retry invalid structured output twice and retain the prior summary after exhaustion or provider failure.
- **Meaningful feedback and success measurement:** The PRD names maker return behavior and meaningful reviews as success metrics, but does not define a meaningful-review threshold or require analytics instrumentation. These metrics do not establish extra product flows.
- **Demo boundaries:** Browser-only storage, acceptance of arbitrary login credentials, seeded reviews, external sample images, fabricated names, and canned summaries demonstrate the mockups. They are not production requirements or privacy guarantees.
