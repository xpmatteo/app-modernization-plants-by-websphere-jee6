# Plants by WebSphere Customer Journey Analysis

## Overview

This document maps the complete customer user journey through the Plants by WebSphere eCommerce application, from initial landing to order completion.

## 1. Landing Page Experience (`/promo.jsf`)

**Entry Point**: Customer lands on promotional home page
- **Template**: Uses `PlantTemplate.xhtml` with navigation header/footer
- **Content**: Summer garden theme with promotional imagery
- **Featured Products**: 3 special offers with direct links:
  - Bonsai Tree ($30.00) - itemID: T0003
  - Red Delicious Strawberries ($3.50/50 seeds) - itemID: V0006
  - Tulips ($17.00/10 bulbs) - itemID: F0017
- **Navigation**: Top menu tabs for product categories
- **Shopping Cart Preview**: Shows cart status in header

**Key Actions Available**:
- Browse by category: Flowers (0), Fruits & Vegetables (1), Trees (2), Accessories (3)
- Direct product detail view via featured items
- Access to SHOPPING CART, LOGIN, HELP from global menu

## 2. Product Discovery Flow

### Category Browsing (`/shopping.jsf`)
**JSF Backing Bean**: `ShoppingBean` (session-scoped)
- **Action**: `#{shopping.performShopping}` with category parameter
- **Display**: Grid layout showing products with thumbnails
- **Image Loading**: Via `ImageServlet?action=getimage&inventoryID={id}`
- **Interaction**: Click product image/name → `#{shopping.performProductDetail}`

### Product Detail View (`/product.jsf`)
**Navigation Path**: Home > Category > Product
- **Product Information**: Name, description, price, packaging details
- **Large Product Image**: 220x250px from ImageServlet
- **Quantity Selection**: Input field with validation
- **Add to Cart**: `#{shopping.performAddToCart}` → redirects to cart

**Technical Flow**:
1. `ShoppingBean.performProductDetail()` extracts itemID from request params
2. Calls `CatalogMgr.getItemInventory(itemID)` via EJB
3. Creates `ProductBean` wrapper with pricing methods
4. Sets product quantity and displays form

## 3. Shopping Cart Experience (`/cart.jsf`)

**JSF Backing Bean**: `ShoppingBean` integrates with `ShoppingCartBean` (CDI)
- **Cart Display**: Data table showing items with quantities and subtotals
- **Item Management**: Modify quantities, remove items (set to 0)
- **AJAX Updates**: Recalculate button updates totals without page reload
- **Actions Available**:
  - Continue Shopping → back to product catalog
  - Recalculate → update cart totals
  - Checkout Now → proceed to order info (if items exist)

**Technical Integration**:
- `ShoppingBean.cartItems` wraps inventory items for display
- `ShoppingCartBean` (CDI session-scoped) manages actual cart state
- AJAX render targets: `cartTable`, `bannerform:cartPreviewGroup`, `cartsubtotal`

## 4. Authentication Flow

### Decision Point: Guest vs Registered User
- **Guest Checkout**: Supported but requires login/registration at checkout
- **Existing User**: LOGIN from global menu → `/login.jsf`
- **New User**: Registration link from login page → `/register.jsf`

### Login Process (`/login.jsf`)
**JSF Backing Bean**: `AccountBean` (session-scoped)
- **Form Fields**: Email address and password
- **Validation**: Email format and required field validation
- **Action**: `#{account.performLoginComplete}`
- **Backend**: Calls `AccountServlet` with action=login
- **Error Handling**: Displays authentication errors via `#{account.loginInfo.message}`

### Registration Process (`/register.jsf`)
**Two-Section Form**:
1. **Login Information**: Email, password, password confirmation
2. **Contact Information**: Full address, phone (all required except address line 2)

**Form Submission**: `#{account.performAccountUpdate}`
**Backend Processing**: Via `AccountServlet` with action=register
**Validation**: Client-side required field checks + server-side business rules

## 5. Checkout Process

### Order Information Collection (`/orderinfo.jsf`)
**Access Requirement**: Must be authenticated (login/register first)
**Navigation Path**: Home > Shopping Cart > Checkout

**Form Sections**:
1. **Billing Address**: Full name, address, city, state, ZIP (all required)
2. **Shipping Address**: Option to copy from billing or enter separately
3. **Credit Card Information**: Type, number, expiration, cardholder name

**Technical Details**:
- Uses `account.orderInfo` object for data binding
- JavaScript integration via `PlantsScripts.js`
- Form validation with required field indicators
- Action: Continue to review page

### Order Review (`/checkout_final.jsf`)
**Navigation Path**: Home > Shopping Cart > [Order Info] > Review
**Display Elements**:
- **Order Total**: Prominent display with currency formatting
- **Billing/Shipping Information**: Read-only review of entered data
- **Item Summary**: Final cart contents
- **Actions**: Continue Shopping or Submit Order

### Order Completion (`/orderdone.jsf`)
**Confirmation Elements**:
- Order number display: `#{account.lastOrderNum}`
- Success message with delivery timeframe (5-7 business days)
- Simple navigation back to home

**Technical Processing**:
- Order persistence via EJB layer
- Email notification via `MailerBean`
- Shopping cart cleanup
- Session state management

## 6. Supporting Flows

### Help System (`/help.jsf`)
**JSF Backing Bean**: `HelpBean` (CDI named bean)
- Accessible via global menu
- Static help content display

### Error Handling
- **Session Expiry**: `/viewExpired.jsf` - redirects to login
- **General Errors**: `/error.jsp` - catches unhandled exceptions
- **Validation Errors**: Inline messages on forms

### Account Management (`/account.jsf`)
**Post-Login Features**:
- View/edit customer profile
- Password changes
- Order history (implied by AccountBean structure)

## 7. Technical Architecture Summary

### JSF Backing Beans (CDI Named, Session-Scoped)
- **`shopping`**: `ShoppingBean` - Product catalog, cart interaction
- **`account`**: `AccountBean` - User authentication, checkout, orders
- **`help`**: `HelpBean` - Help system support
- **`ShoppingCartBean`**: CDI bean for cart state management

### EJB Business Layer Integration
- **`CatalogMgr`**: Product inventory management
- **`CustomerMgr`**: User authentication and customer data
- **`MailerBean`**: Order confirmation emails
- **`BackOrderMgr`**: Inventory back-order handling

### Session State Management
- Shopping cart persists across pages via session-scoped CDI bean
- User authentication state in `AccountBean`
- Product browsing state in `ShoppingBean`

### Key Navigation Patterns
- **Breadcrumb Navigation**: Consistent "Home > Category > Product" trails
- **Global Menu**: Always accessible SHOPPING CART, LOGIN, HELP
- **Category Tabs**: Persistent product category navigation
- **Template Consistency**: All pages use `PlantTemplate.xhtml`

## 8. Customer Decision Points

### Critical User Experience Moments
1. **Initial Engagement**: Featured products on home page drive discovery
2. **Category vs Search**: No search functionality - only category browsing
3. **Guest vs Member**: Authentication required for checkout creates friction
4. **Cart Abandonment Risk**: Multi-step checkout process (cart → login → info → review → done)
5. **Trust Indicators**: Simple confirmation page, email notifications

### Potential Pain Points for Modernization
- **No Search Capability**: Users must browse by category only
- **Mandatory Registration**: No guest checkout option
- **Multi-Page Checkout**: 4-step process could be streamlined
- **Image Loading**: Separate servlet calls for all product images
- **Session Management**: Long checkout flow vulnerable to timeouts

## 9. User Journey Flowchart

```
Landing (/promo.jsf)
    │
    ├─ Browse Category (/shopping.jsf)
    │   └─ Product Detail (/product.jsf)
    │       └─ Add to Cart → Shopping Cart (/cart.jsf)
    │
    └─ Featured Product Direct → Product Detail
                                    │
                                    v
                          Shopping Cart (/cart.jsf)
                                    │
                                    v
                            Checkout Now (requires auth)
                                    │
                         ┌─ Not Logged In ─┐
                         │                  │
                         v                  v
                 Login (/login.jsf)    Register (/register.jsf)
                         │                  │
                         └────────┬─────────┘
                                  │
                                  v
                         Order Info (/orderinfo.jsf)
                                  │
                                  v
                         Review Order (/checkout_final.jsf)
                                  │
                                  v
                         Order Complete (/orderdone.jsf)
```

This comprehensive customer journey analysis provides the foundation for understanding user experience patterns and identifying modernization opportunities in the Plants by WebSphere application.