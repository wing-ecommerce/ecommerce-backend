carts
├── id (PK)
├── user_id (UNIQUE)
└── timestamps

cart_items
├── id (PK)
├── cart_id (FK → carts)
├── product_id (FK → products)
├── product_size_id (FK → product_sizes)
├── quantity
├── price (at time of adding)
└── UNIQUE (cart_id, product_id, product_size_id)
```

---

## 🔄 **Flow:**
```
1. User signs in
2. Click "Add to Cart"
3. Frontend → POST /api/v1/cart
4. Backend validates:
   ✅ Stock available?
   ✅ Within limits?
   ✅ Product exists?
5. Save to database
6. Return updated cart
7. Frontend updates UI
8. Cart synced across devices!
```

---

## 📁 **Backend Files:**

1. **`entity/Cart.java`** - Cart entity with helper methods
2. **`entity/CartItem.java`** - Cart item with unique constraint
3. **`repository/CartRepository.java`** - findByUserId()
4. **`repository/CartItemRepository.java`** - Cart item queries
5. **`service/impl/CartServiceImpl.java`** - All validation logic
6. **`controller/CartController.java`** - REST API endpoints
7. **`dto/request/*.java`** - Request DTOs
8. **`dto/response/CartResponse.java`** - Response DTO

---

## 📁 **Frontend Files:**

1. **`services/cart.service.ts`** - API calls to backend
2. **`contexts/CartContext.tsx`** - No more localStorage!
3. **`components/cart/CartItem.tsx`** - Updated for backend
4. **`components/cart/CartSidebar.tsx`** - Shows backend cart
5. **`app/(shop)/products/[slug]/page.tsx`** - Calls backend API

---

## 🔌 **API Endpoints:**
```
GET    /api/v1/cart              - Get cart
POST   /api/v1/cart              - Add to cart
PUT    /api/v1/cart/items/{id}   - Update quantity
DELETE /api/v1/cart/items/{id}   - Remove item
DELETE /api/v1/cart              - Clear cart